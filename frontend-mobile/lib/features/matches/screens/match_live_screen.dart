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
import '../models/partida.dart';
import '../providers/partida_providers.dart';
import '../utils/event_cell_utils.dart';
import '../widgets/player_event_actions_sheet.dart';
import '../widgets/score_header.dart';
import '../widgets/team_panel.dart';
import 'match_report_screen.dart';
import 'substitution_screen.dart';

class MatchLiveArgs {
  const MatchLiveArgs({required this.partida, required this.liveMode});

  final Partida partida;
  final bool liveMode;
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
  // Wraps only the ScoreHeader (not the full scrollable roster) so the
  // shared image focuses on the placar, matching the desktop print.
  final GlobalKey _scoreboardKey = GlobalKey();

  bool get _liveMode => widget.args.liveMode;

  @override
  void initState() {
    super.initState();
    _partida = widget.args.partida;

    if (_liveMode) {
      _azul = _partida.jogadoresAzul
          .map((Jogador j) => LiveSlot(jogador: j, eventos: '${j.nomeExibir} (${j.posicao ?? ''})'))
          .toList();
      _vermelho = _partida.jogadoresVermelho
          .map((Jogador j) => LiveSlot(jogador: j, eventos: '${j.nomeExibir} (${j.posicao ?? ''})'))
          .toList();
    } else {
      _carregarHistorico();
    }
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
    if (!_liveMode) return;

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

  void _trocarMesmoTime(String time, int a, int b) {
    final List<LiveSlot> lista = time == 'Azul' ? _azul : _vermelho;
    setState(() {
      final LiveSlot temp = lista[a];
      lista[a] = lista[b];
      lista[b] = temp;
    });
    // Backend stub — always returns true, no real server-side work; the
    // reorder already happened locally above. Fire-and-forget.
    ref.read(partidaRepositoryProvider).inverterMesmoTime(<String, dynamic>{'time': time}).catchError((_) => false);
  }

  void _trocarTimeAdversario(String timeA, int indexA, String timeB, int indexB) {
    final List<LiveSlot> listaA = timeA == 'Azul' ? _azul : _vermelho;
    final List<LiveSlot> listaB = timeB == 'Azul' ? _azul : _vermelho;
    setState(() {
      final LiveSlot temp = listaA[indexA];
      listaA[indexA] = listaB[indexB];
      listaB[indexB] = temp;
    });
    ref.read(partidaRepositoryProvider).permutarAdversario(<String, dynamic>{}).catchError((_) => false);
  }

  int _decrementarSemNegativo(int valor) => valor > 0 ? valor - 1 : 0;

  Future<void> _abrirAcoesEvento(String time, int index) async {
    final List<LiveSlot> lista = time == 'Azul' ? _azul : _vermelho;
    final LiveSlot slot = lista[index];

    final EventActionResult? resultado = await showPlayerEventActionsSheet(
      context,
      nomeJogador: slot.jogador.nomeExibir,
      tokensAtivos: activeTokens(slot.eventos),
    );
    if (resultado == null) return;

    if (resultado.tokenAdicionar != null) {
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
      });
    }
  }

  Future<void> _abrirSumula() async {
    final String? novaSumula = await showMatchReportSheet(
      context,
      sumulaInicial: _partida.sumula ?? '',
      somenteLeitura: !_liveMode,
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
      final List<List<dynamic>> gridAzul =
          _azul.map((LiveSlot s) => <dynamic>[s.jogador.nomeExibir, s.jogador.posicao ?? '', s.eventos]).toList();
      final List<List<dynamic>> gridVermelho =
          _vermelho.map((LiveSlot s) => <dynamic>[s.jogador.nomeExibir, s.jogador.posicao ?? '', s.eventos]).toList();

      await ref.read(partidaRepositoryProvider).finalizar(partida: _partida, gridAzul: gridAzul, gridVermelho: gridVermelho);

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
      if (mounted) context.pop();
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro ao finalizar', message: e is ApiException ? e.message : e.toString());
      }
    } finally {
      if (mounted) setState(() => _finalizando = false);
    }
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
      onLongPressSlot: _liveMode ? (int index) => _abrirAcoesEvento(time, index) : null,
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
                  // Strip the "nome (pos)" prefix the stored cell text
                  // carries (same mini-language as the live view's
                  // PlayerRow) — showing it raw put names/positions in
                  // what's meant to be just the events column.
                  Expanded(flex: 2, child: Text(activeTokensText(row.eventos), textAlign: TextAlign.right)),
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
      appBar: AppBar(title: Text(_partida.nomePartida ?? 'Partida')),
      body: _carregandoHistorico
          ? const LoadingView()
          : SingleChildScrollView(
              child: Container(
                color: Theme.of(context).scaffoldBackgroundColor,
                child: Column(
                  children: <Widget>[
                    RepaintBoundary(
                      key: _scoreboardKey,
                      child: ScoreHeader(
                        nomePartida: _partida.nomePartida ?? '',
                        golsAzul: _partida.golsTimeAzul,
                        golsVermelho: _partida.golsTimeVermelho,
                        arbitro: _partida.arbitro,
                        bandeira1: _partida.bandeira1,
                        bandeira2: _partida.bandeira2,
                      ),
                    ),
                    _liveMode ? _buildTeamPanelLive('Azul') : _buildTeamPanelHistorico('Azul'),
                    _liveMode ? _buildTeamPanelLive('Vermelho') : _buildTeamPanelHistorico('Vermelho'),
                    const SizedBox(height: 12),
                  ],
                ),
              ),
            ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: _liveMode
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
                        onPressed: _finalizando ? null : _finalizar,
                        child: _finalizando
                            ? const SizedBox(
                                width: 18,
                                height: 18,
                                child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                              )
                            : const Text('Finalizar'),
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
