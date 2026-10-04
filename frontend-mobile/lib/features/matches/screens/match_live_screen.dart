import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/utils/screenshot_utils.dart';
import '../../../core/widgets/confirm_dialog.dart';
import '../../../core/widgets/loading_view.dart';
import '../../players/models/jogador.dart';
import '../models/grid_historico_row.dart';
import '../models/jogador_partida.dart';
import '../models/partida.dart';
import '../providers/partida_providers.dart';
import '../utils/event_cell_utils.dart';
import '../widgets/player_event_actions_sheet.dart';
import '../widgets/score_header.dart';
import '../widgets/team_panel.dart';
import 'match_report_screen.dart';
import 'substitution_screen.dart';

class MatchLiveArgs {
  const MatchLiveArgs({required this.partida, required this.liveMode, this.editMode = false});

  final Partida partida;
  final bool liveMode;

  /// Editing an already-finalized match (from match history): the saved
  /// grid is loaded into editable slots and "Salvar" rewrites the match
  /// (PUT /partidas/{id}) instead of finalizing it. Pops `true` once saved.
  final bool editMode;
}

/// Mirrors TelaPartidaLiveView: the core live-scoreboard screen, dual-
/// purpose (live edit mode for a fresh match, or read-only historical mode
/// when opened from match history). See plan §5/§6 for the mobile-specific
/// adaptations (long-press bottom sheet replaces the right-click context
/// menu; goalkeeper-improvise omitted since its backend endpoints don't
/// exist).
class MatchLiveScreen extends ConsumerStatefulWidget {
  const MatchLiveScreen({super.key, required this.args});

  final MatchLiveArgs args;

  @override
  ConsumerState<MatchLiveScreen> createState() => _MatchLiveScreenState();
}

class _ArmedSlot {
  const _ArmedSlot(this.time, this.index);
  final String time; // "Azul" | "Vermelho"
  final int index;
}

class _MatchLiveScreenState extends ConsumerState<MatchLiveScreen> {
  late Partida _partida;
  List<LiveSlot> _azul = <LiveSlot>[];
  List<LiveSlot> _vermelho = <LiveSlot>[];
  List<GridHistoricoRow> _azulHistorico = <GridHistoricoRow>[];
  List<GridHistoricoRow> _vermelhoHistorico = <GridHistoricoRow>[];
  _ArmedSlot? _armado;
  bool _carregandoHistorico = false;
  bool _finalizando = false;
  // Wraps the whole printable match (score + both rosters) for sharing —
  // opening the shared photo shows everything, matching the desktop print.
  final GlobalKey _scoreboardKey = GlobalKey();

  bool get _liveMode => widget.args.liveMode;
  bool get _editMode => widget.args.editMode;

  /// Slots accept events/swaps/substitutions (live match or editing a
  /// finalized one); false only for the read-only history view.
  bool get _interativo => _liveMode || _editMode;

  @override
  void initState() {
    super.initState();
    _partida = widget.args.partida;

    if (_liveMode) {
      _azul = _slotsParaTime('Azul');
      _vermelho = _slotsParaTime('Vermelho');
    } else {
      _carregarHistorico();
    }
  }

  /// Edit mode: rebuilds editable slots from the saved grid rows, resolving
  /// each slot's active (last) player against listaGeralPresenca so events
  /// can be recorded for them (unknown names get an id-less Jogador).
  List<LiveSlot> _slotsDoHistorico(List<GridHistoricoRow> rows) {
    return rows.map((GridHistoricoRow row) {
      final String nomeAtivo = _nomeAtivo(row.nomes);
      Jogador? jogador;
      for (final JogadorPartida jp in _partida.listaGeralPresenca) {
        if (jp.jogador != null && jp.jogador!.nomeExibir.toLowerCase() == nomeAtivo.toLowerCase()) {
          jogador = jp.jogador;
          break;
        }
      }
      return LiveSlot(
        jogador: jogador ?? Jogador(nome: nomeAtivo),
        eventos: row.eventos,
        nomesExibir: row.nomes,
        posicaoSlot: row.pos,
      );
    }).toList();
  }

  /// "J1 / J2 (MEI)" -> "J2".
  String _nomeAtivo(String nomes) {
    final String ultimo = nomes.split(' / ').last.trim();
    final int parenteses = ultimo.indexOf(' (');
    return parenteses == -1 ? ultimo : ultimo.substring(0, parenteses).trim();
  }

  void _recalcularPlacar() {
    final ({int azul, int vermelho}) placar = calcularPlacar(
      _azul.map((LiveSlot s) => s.eventos),
      _vermelho.map((LiveSlot s) => s.eventos),
    );
    _partida.golsTimeAzul = placar.azul;
    _partida.golsTimeVermelho = placar.vermelho;
  }

  /// Builds this team's live slots from listaGeralPresenca (status
  /// "Titular"), ordered by the tactical slot index in each entry's funcao
  /// ("Azul_MEI_3") — this is the backend's actual formation-following
  /// allocation (obterTemplateFormacao + improvisation), including
  /// placeholder "Incompleto" slots for positions no one could fill.
  /// jogadoresAzul/jogadoresVermelho, by contrast, are just grouped by
  /// registered position with no slot/formation info, so they're only used
  /// as a fallback if listaGeralPresenca wasn't populated for some reason.
  List<LiveSlot> _slotsParaTime(String time) {
    final List<JogadorPartida> titulares = _partida.listaGeralPresenca
        .where((JogadorPartida jp) => (jp.time ?? '') == time && (jp.status ?? '') == 'Titular')
        .toList()
      ..sort((JogadorPartida a, JogadorPartida b) => _indiceFuncao(a.funcao).compareTo(_indiceFuncao(b.funcao)));

    if (titulares.isEmpty) {
      final List<Jogador> fallback = time == 'Azul' ? _partida.jogadoresAzul : _partida.jogadoresVermelho;
      return fallback.map((Jogador j) => LiveSlot(jogador: j)).toList();
    }

    return titulares.map((JogadorPartida jp) {
      return LiveSlot(
        jogador: jp.jogador ?? Jogador(nome: '?'),
        posicaoSlot: _siglaFuncao(jp.funcao),
      );
    }).toList();
  }

  /// "Azul_MEI_3" -> "MEI"; null if funcao doesn't match that shape.
  String? _siglaFuncao(String? funcao) {
    final List<String>? partes = funcao?.split('_');
    return (partes != null && partes.length >= 3) ? partes[1] : null;
  }

  /// "Azul_MEI_3" -> 3; a very large fallback so malformed entries sort last
  /// instead of crashing the comparator.
  int _indiceFuncao(String? funcao) {
    final List<String>? partes = funcao?.split('_');
    if (partes == null || partes.length < 3) return 1 << 20;
    return int.tryParse(partes.last) ?? (1 << 20);
  }

  Future<void> _carregarHistorico() async {
    setState(() => _carregandoHistorico = true);
    try {
      final List<GridHistoricoRow> azul =
          await ref.read(partidaRepositoryProvider).gridHistorico(partida: _partida, time: 'Azul');
      final List<GridHistoricoRow> vermelho =
          await ref.read(partidaRepositoryProvider).gridHistorico(partida: _partida, time: 'Vermelho');
      if (mounted) {
        setState(() {
          _azulHistorico = azul;
          _vermelhoHistorico = vermelho;
          if (_editMode) {
            _azul = _slotsDoHistorico(azul);
            _vermelho = _slotsDoHistorico(vermelho);
          }
        });
      }
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
      }
    } finally {
      if (mounted) setState(() => _carregandoHistorico = false);
    }
  }

  void _onTapSlot(String time, int index) {
    if (!_interativo) return;

    if (_armado == null) {
      setState(() => _armado = _ArmedSlot(time, index));
      return;
    }

    if (_armado!.time == time && _armado!.index == index) {
      setState(() => _armado = null);
      return;
    }

    final _ArmedSlot origem = _armado!;
    setState(() => _armado = null);
    _confirmarTroca(origem.time, origem.index, time, index);
  }

  Future<void> _confirmarTroca(String timeA, int indexA, String timeB, int indexB) async {
    final Jogador jogadorA = (timeA == 'Azul' ? _azul : _vermelho)[indexA].jogador;
    final Jogador jogadorB = (timeB == 'Azul' ? _azul : _vermelho)[indexB].jogador;

    final bool confirmar = await showConfirmDialog(
      context,
      title: 'Trocar jogadores',
      message: 'Trocar ${jogadorA.nomeExibir} com ${jogadorB.nomeExibir}?',
    );
    if (!confirmar) return;

    if (timeA == timeB) {
      _trocarMesmoTime(timeA, indexA, indexB);
    } else {
      _trocarTimeAdversario(timeA, indexA, timeB, indexB);
    }
  }

  // The slot's posicaoSlot belongs to the tactical grid column, not to
  // whichever player fills it — only the player (and their event history)
  // moves between slots, mirroring substitution_screen.dart's
  // _tocarSlotTitular pattern.
  void _trocarJogadoresNoSlot(LiveSlot destino, LiveSlot origem) {
    final Jogador jogadorTemp = destino.jogador;
    final String eventosTemp = destino.eventos;
    final String nomesExibirTemp = destino.nomesExibir;

    destino.jogador = origem.jogador;
    destino.eventos = origem.eventos;
    destino.nomesExibir = origem.nomesExibir;

    origem.jogador = jogadorTemp;
    origem.eventos = eventosTemp;
    origem.nomesExibir = nomesExibirTemp;
  }

  void _trocarMesmoTime(String time, int a, int b) {
    final List<LiveSlot> lista = time == 'Azul' ? _azul : _vermelho;
    setState(() {
      _trocarJogadoresNoSlot(lista[a], lista[b]);
    });
    // Backend stub — always returns true, no real server-side work; the
    // reorder already happened locally above. Fire-and-forget.
    ref.read(partidaRepositoryProvider).inverterMesmoTime(<String, dynamic>{'time': time}).catchError((_) => false);
  }

  void _trocarTimeAdversario(String timeA, int indexA, String timeB, int indexB) {
    final List<LiveSlot> listaA = timeA == 'Azul' ? _azul : _vermelho;
    final List<LiveSlot> listaB = timeB == 'Azul' ? _azul : _vermelho;
    setState(() {
      _trocarJogadoresNoSlot(listaA[indexA], listaB[indexB]);
    });
    ref.read(partidaRepositoryProvider).permutarAdversario(<String, dynamic>{}).catchError((_) => false);
  }

  int _decrementarSemNegativo(int valor) => valor > 0 ? valor - 1 : 0;

  Future<void> _abrirAcoesEvento(String time, int index) async {
    final List<LiveSlot> lista = time == 'Azul' ? _azul : _vermelho;
    final LiveSlot slot = lista[index];

    final bool vagaVazia = slot.jogador.id == null || slot.jogador.id == 0;
    final EventActionResult? resultado = await showPlayerEventActionsSheet(
      context,
      nomeJogador: slot.jogador.nomeExibir,
      tokensAtivos: activeTokens(slot.eventos),
      // A slot with substitution history is undone via "Remover
      // substituição" on the substitutions screen instead.
      podeRemoverJogador: !vagaVazia && !slot.nomesExibir.contains(' / '),
    );
    if (resultado == null) return;

    if (resultado.removerJogador) {
      await _removerJogador(time, index);
    } else if (resultado.tokenAdicionar != null) {
      await _registrarEvento(time, index, resultado.tokenAdicionar!);
    } else if (resultado.tokenRemover != null) {
      await _removerEvento(time, index, resultado.tokenRemover!);
    }
  }

  Future<void> _registrarEvento(String time, int index, String token) async {
    final List<LiveSlot> lista = time == 'Azul' ? _azul : _vermelho;
    final LiveSlot slot = lista[index];
    if (slot.jogador.id == null || slot.jogador.id == 0) return;

    try {
      // 2nd yellow this match auto-escalates to a red, matching desktop
      // (checked via /eventos/contar-amarelos-ativo before adding).
      bool escalarParaVermelho = false;
      if (token == EventTokens.amarelo) {
        final int amarelosAtivos = await ref.read(partidaRepositoryProvider).contarAmarelosAtivo(slot.eventos);
        escalarParaVermelho = amarelosAtivos >= 1;
      }

      // Only the "eventos" cell-text token is updated here, matching
      // desktop (TelaPartidaLiveView): goals/cards are derived into real
      // JogadorPartida/Evento stats server-side by /partidas/finalizar once
      // the match is saved, not persisted individually mid-match — this
      // match has no id yet (sortear doesn't persist), so there'd be no
      // valid partidaId to attach a standalone Evento to.
      String novoTexto = await ref
          .read(partidaRepositoryProvider)
          .adicionarToken(eventos: slot.eventos, token: token);

      if (escalarParaVermelho) {
        novoTexto =
            await ref.read(partidaRepositoryProvider).adicionarToken(eventos: novoTexto, token: EventTokens.vermelho);
      }

      setState(() {
        slot.eventos = novoTexto;
        if (token == EventTokens.gol) {
          if (time == 'Azul') {
            _partida.golsTimeAzul++;
          } else {
            _partida.golsTimeVermelho++;
          }
        } else if (token == EventTokens.golContra) {
          if (time == 'Azul') {
            _partida.golsTimeVermelho++;
          } else {
            _partida.golsTimeAzul++;
          }
        }
      });
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
      }
    }
  }

  Future<void> _removerEvento(String time, int index, String token) async {
    final List<LiveSlot> lista = time == 'Azul' ? _azul : _vermelho;
    final LiveSlot slot = lista[index];

    try {
      final String novoTexto = await ref.read(partidaRepositoryProvider).removerToken(eventos: slot.eventos, token: token);
      setState(() {
        slot.eventos = novoTexto;
        if (token == EventTokens.gol) {
          if (time == 'Azul') {
            _partida.golsTimeAzul = _decrementarSemNegativo(_partida.golsTimeAzul);
          } else {
            _partida.golsTimeVermelho = _decrementarSemNegativo(_partida.golsTimeVermelho);
          }
        } else if (token == EventTokens.golContra) {
          if (time == 'Azul') {
            _partida.golsTimeVermelho = _decrementarSemNegativo(_partida.golsTimeVermelho);
          } else {
            _partida.golsTimeAzul = _decrementarSemNegativo(_partida.golsTimeAzul);
          }
        }
      });
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
      }
    }
  }

  Future<void> _removerJogador(String time, int index) async {
    final List<LiveSlot> lista = time == 'Azul' ? _azul : _vermelho;
    final LiveSlot slot = lista[index];
    final String posicao = slot.posicaoSlot ?? '';
    final bool vagaDeGol = posicao.toUpperCase() == 'GOL';
    final String tipo = vagaDeGol ? 'goleiro' : 'jogador de linha';

    final bool confirmar = await showConfirmDialog(
      context,
      title: 'Remover jogador',
      message: 'Remover ${slot.jogador.nomeExibir} da partida? O próximo $tipo do banco entra no lugar.'
          '${slot.eventos.trim().isNotEmpty ? '\n\nOs eventos registrados para ele serão descartados.' : ''}',
      confirmLabel: 'Remover',
      destructive: true,
    );
    if (!confirmar) return;

    try {
      final ({Partida partida, Jogador? substituto}) resultado =
          await ref.read(partidaRepositoryProvider).removerEscalado(
                partida: _partida,
                nomeJogador: slot.jogador.nomeExibir,
                time: time,
                posicao: posicao,
              );
      final String nomeRemovido = slot.jogador.nomeExibir;
      setState(() {
        _partida.listaGeralPresenca = resultado.partida.listaGeralPresenca;
        lista[index] = LiveSlot(
          // Empty slot (id 0): the substitutions screen fills it directly.
          jogador: resultado.substituto ?? Jogador(id: 0, nome: 'Vaga ${posicao.isEmpty ? '' : posicao}'.trim()),
          posicaoSlot: slot.posicaoSlot,
        );
        _recalcularPlacar();
      });
      if (mounted) {
        await showMessageDialog(
          context,
          title: 'Jogador removido',
          message: resultado.substituto != null
              ? '$nomeRemovido saiu. ${resultado.substituto!.nomeExibir} entrou no lugar.'
              : '$nomeRemovido saiu. Não há $tipo no banco: a vaga ficou vazia.',
        );
      }
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
      }
    }
  }

  Future<void> _abrirSubstituicoes() async {
    final SubstitutionResult? resultado = await Navigator.of(context).push<SubstitutionResult>(
      MaterialPageRoute<SubstitutionResult>(
        builder: (_) => SubstitutionScreen(partida: _partida, azul: _azul, vermelho: _vermelho),
      ),
    );
    if (resultado != null) {
      setState(() {
        _partida = resultado.partida;
        // Slots come back as-is (not rebuilt from scratch) so already-
        // recorded event history for non-substituted players survives.
        _azul = resultado.azul;
        _vermelho = resultado.vermelho;
        // An undone substitution drops the incoming player's events.
        _recalcularPlacar();
      });
    }
  }

  Future<void> _abrirSumula() async {
    final String? novaSumula = await showMatchReportSheet(
      context,
      sumulaInicial: _partida.sumula ?? '',
      somenteLeitura: !_interativo,
    );
    if (novaSumula != null) {
      setState(() => _partida.sumula = novaSumula);
    }
  }

  Future<void> _finalizar() async {
    final bool confirmar = await showConfirmDialog(
      context,
      title: 'Finalizar partida',
      message: 'Finalizar a partida e consolidar as estatísticas de todos os jogadores?',
    );
    if (!confirmar) return;

    setState(() => _finalizando = true);
    try {
      final bool salvou = await ref
          .read(partidaRepositoryProvider)
          .finalizar(partida: _partida, gridAzul: _montarGrid(_azul), gridVermelho: _montarGrid(_vermelho));
      if (!salvou) {
        if (mounted) {
          await showMessageDialog(
            context,
            title: 'Erro ao finalizar',
            message: 'O servidor não confirmou o salvamento da partida. Tente novamente.',
          );
        }
        return;
      }

      if (mounted) {
        final bool compartilhar = await showConfirmDialog(
          context,
          title: 'Partida finalizada',
          message: 'Deseja compartilhar uma imagem do placar final?',
          confirmLabel: 'Compartilhar',
          cancelLabel: 'Não',
        );
        if (compartilhar) {
          await ScreenshotUtils.shareBoundaryAsImage(_scoreboardKey, text: _partida.placarFormatado);
        }
      }
      // Straight back to the home screen either way (share or decline),
      // instead of just popping one route back to match setup.
      if (mounted) context.go('/');
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro ao finalizar', message: e is ApiException ? e.message : e.toString());
      }
    } finally {
      if (mounted) setState(() => _finalizando = false);
    }
  }

  List<List<dynamic>> _montarGrid(List<LiveSlot> slots) {
    return slots
        .map((LiveSlot s) => <dynamic>[s.nomesExibir, s.posicaoSlot ?? s.jogador.posicao ?? '', s.eventos])
        .toList();
  }

  Future<void> _salvarEdicao() async {
    final bool confirmar = await showConfirmDialog(
      context,
      title: 'Salvar alterações',
      message: 'Gravar as alterações desta partida? As estatísticas dos jogadores serão recalculadas.',
      confirmLabel: 'Salvar',
    );
    if (!confirmar) return;

    setState(() => _finalizando = true);
    try {
      _recalcularPlacar();
      final bool salvou = await ref
          .read(partidaRepositoryProvider)
          .editar(partida: _partida, gridAzul: _montarGrid(_azul), gridVermelho: _montarGrid(_vermelho));
      if (!mounted) return;
      if (!salvou) {
        await showMessageDialog(
          context,
          title: 'Erro ao salvar',
          message: 'O servidor não confirmou a alteração da partida. Tente novamente.',
        );
        return;
      }
      await showMessageDialog(context, title: 'Partida atualizada', message: 'Alterações salvas com sucesso.');
      if (mounted) Navigator.of(context).pop(true);
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro ao salvar', message: e is ApiException ? e.message : e.toString());
      }
    } finally {
      if (mounted) setState(() => _finalizando = false);
    }
  }

  Future<void> _editarDados() async {
    final TextEditingController nomeController = TextEditingController(text: _partida.nomePartida ?? '');
    DateTime data = _partida.dataPartida ?? DateTime.now();

    final bool? salvar = await showDialog<bool>(
      context: context,
      builder: (BuildContext context) => StatefulBuilder(
        builder: (BuildContext context, StateSetter setDialogState) => AlertDialog(
          title: const Text('Editar dados da partida'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              TextField(
                controller: nomeController,
                decoration: const InputDecoration(labelText: 'Nome da partida'),
              ),
              const SizedBox(height: 12),
              ListTile(
                contentPadding: EdgeInsets.zero,
                leading: const Icon(Icons.calendar_today),
                title: Text(
                  '${data.day.toString().padLeft(2, '0')}/${data.month.toString().padLeft(2, '0')}/${data.year}',
                ),
                onTap: () async {
                  final DateTime? escolhida = await showDatePicker(
                    context: context,
                    initialDate: data,
                    firstDate: DateTime(2000),
                    lastDate: DateTime(2100),
                  );
                  if (escolhida != null) setDialogState(() => data = escolhida);
                },
              ),
            ],
          ),
          actions: <Widget>[
            TextButton(onPressed: () => Navigator.of(context).pop(false), child: const Text('Cancelar')),
            FilledButton(onPressed: () => Navigator.of(context).pop(true), child: const Text('OK')),
          ],
        ),
      ),
    );

    if (salvar == true) {
      setState(() {
        _partida.nomePartida = nomeController.text.trim();
        _partida.dataPartida = data;
      });
    }
    nomeController.dispose();
  }

  Future<void> _compartilharPlacar() async {
    await ScreenshotUtils.shareBoundaryAsImage(_scoreboardKey, text: _partida.placarFormatado);
  }

  Widget _buildTeamPanelLive(String time) {
    final List<LiveSlot> slots = time == 'Azul' ? _azul : _vermelho;
    return TeamPanel(
      titulo: time == 'Azul' ? 'TIME AZUL' : 'TIME VERMELHO',
      corTime: time == 'Azul' ? CanaColors.timeAzul : CanaColors.timeVermelho,
      slots: slots,
      armedIndex: _armado?.time == time ? _armado!.index : null,
      onTapSlot: (int index) => _onTapSlot(time, index),
      onLongPressSlot: _interativo ? (int index) => _abrirAcoesEvento(time, index) : null,
    );
  }

  Widget _buildTeamPanelHistorico(String time) {
    final List<GridHistoricoRow> rows = time == 'Azul' ? _azulHistorico : _vermelhoHistorico;
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Container(
            padding: const EdgeInsets.symmetric(vertical: 8),
            decoration: BoxDecoration(
              color: time == 'Azul' ? CanaColors.timeAzul : CanaColors.timeVermelho,
              borderRadius: const BorderRadius.vertical(top: Radius.circular(12)),
            ),
            child: Text(
              time == 'Azul' ? 'TIME AZUL' : 'TIME VERMELHO',
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
            ),
          ),
          ...rows.map(
            (GridHistoricoRow row) => Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
              child: Row(
                children: <Widget>[
                  Expanded(flex: 3, child: Text(row.nomes)),
                  Expanded(flex: 2, child: Text(row.pos, style: Theme.of(context).textTheme.bodySmall)),
                  Expanded(flex: 2, child: Text(row.eventos, textAlign: TextAlign.right)),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(_partida.nomePartida ?? 'Partida'),
        actions: <Widget>[
          if (_editMode)
            IconButton(
              tooltip: 'Editar nome e data',
              icon: const Icon(Icons.edit_calendar),
              onPressed: _editarDados,
            ),
        ],
      ),
      body: _carregandoHistorico
          ? const LoadingView()
          : SingleChildScrollView(
              child: RepaintBoundary(
                // Captures the whole printable match (score + both
                // rosters) as one image, so opening the shared photo shows
                // everything — the scoreboard sits prominently at the top
                // so most apps' auto-generated thumbnail still leads with it.
                key: _scoreboardKey,
                child: Container(
                  color: Theme.of(context).scaffoldBackgroundColor,
                  child: Column(
                    children: <Widget>[
                      ScoreHeader(
                        nomePartida: _partida.nomePartida ?? '',
                        golsAzul: _partida.golsTimeAzul,
                        golsVermelho: _partida.golsTimeVermelho,
                        arbitro: _partida.arbitro,
                        bandeira1: _partida.bandeira1,
                        bandeira2: _partida.bandeira2,
                      ),
                      _interativo ? _buildTeamPanelLive('Azul') : _buildTeamPanelHistorico('Azul'),
                      _interativo ? _buildTeamPanelLive('Vermelho') : _buildTeamPanelHistorico('Vermelho'),
                      const SizedBox(height: 12),
                    ],
                  ),
                ),
              ),
            ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: _interativo
              ? Row(
                  children: <Widget>[
                    Expanded(
                      child: OutlinedButton(
                        style: OutlinedButton.styleFrom(padding: const EdgeInsets.symmetric(horizontal: 4)),
                        onPressed: _abrirSubstituicoes,
                        child: const FittedBox(
                          fit: BoxFit.scaleDown,
                          child: Text('Substituições', maxLines: 1, softWrap: false),
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: OutlinedButton(onPressed: _abrirSumula, child: const Text('Súmula')),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: ElevatedButton(
                        onPressed: _finalizando ? null : (_editMode ? _salvarEdicao : _finalizar),
                        child: _finalizando
                            ? const SizedBox(
                                width: 18,
                                height: 18,
                                child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                              )
                            : Text(_editMode ? 'Salvar' : 'Finalizar'),
                      ),
                    ),
                  ],
                )
              : Row(
                  children: <Widget>[
                    Expanded(
                      child: OutlinedButton(onPressed: _abrirSumula, child: const Text('Súmula')),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      flex: 2,
                      child: ElevatedButton.icon(
                        onPressed: _compartilharPlacar,
                        icon: const Icon(Icons.share),
                        label: const Text('COMPARTILHAR PLACAR'),
                      ),
                    ),
                  ],
                ),
        ),
      ),
    );
  }
}
