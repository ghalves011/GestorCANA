import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../../../core/utils/player_filter_utils.dart';
import '../../../core/widgets/confirm_dialog.dart';
import '../../players/models/jogador.dart';
import '../models/jogador_partida.dart';
import '../models/partida.dart';
import '../providers/partida_providers.dart';
import '../widgets/bench_list.dart';
import '../widgets/referee_row.dart';
import '../widgets/team_panel.dart';

/// Returned to match_live_screen on confirm: [partida] for the roster/
/// arbitragem/bench fields, plus [azul]/[vermelho] carrying each slot's
/// live "eventos" cell text (so already-recorded goals/cards for
/// non-substituted players survive the round trip instead of being reset).
class SubstitutionResult {
  const SubstitutionResult({required this.partida, required this.azul, required this.vermelho});

  final Partida partida;
  final List<LiveSlot> azul;
  final List<LiveSlot> vermelho;
}

/// Mirrors TelaSubstituicaoView: staged substitution manager. Changes are
/// only committed back to the caller (match_live_screen) when the user
/// taps "CONFIRMAR TROCAS"; closing without confirming discards them.
///
/// SIMPLIFICATION NOTE: the desktop's substitution bookkeeping runs
/// through several endpoints that manipulate a single-cell "/"-joined text
/// mini-language per slot (processar-substituicao-jogador,
/// atualizar-substituicao-lista-presenca, definir/remover-arbitragem with
/// history accumulation). This screen performs the actual lineup/bench
/// reordering LOCALLY (always reliable, immediately visible) and calls the
/// matching backend endpoints best-effort alongside it for suspension
/// checks and eventual server-side bookkeeping — a backend call failing
/// here does not block the local substitution, it only surfaces a
/// warning. Verify this against the real backend once it's reachable (see
/// plan §8) and tighten the coupling if the two ever disagree in practice.
class SubstitutionScreen extends ConsumerStatefulWidget {
  const SubstitutionScreen({super.key, required this.partida, required this.azul, required this.vermelho});

  final Partida partida;
  final List<LiveSlot> azul;
  final List<LiveSlot> vermelho;

  @override
  ConsumerState<SubstitutionScreen> createState() => _SubstitutionScreenState();
}

class _SubstitutionScreenState extends ConsumerState<SubstitutionScreen> {
  late List<LiveSlot> _azul;
  late List<LiveSlot> _vermelho;
  late List<JogadorPartida> _banco;
  String? _arbitro;
  String? _bandeira1;
  String? _bandeira2;

  int? _bancoSelecionadoIndex;
  bool _carregandoAtrasados = false;

  @override
  void initState() {
    super.initState();
    _azul = widget.azul
        .map((LiveSlot s) =>
            LiveSlot(jogador: s.jogador, eventos: s.eventos, nomesExibir: s.nomesExibir, posicaoSlot: s.posicaoSlot))
        .toList();
    _vermelho = widget.vermelho
        .map((LiveSlot s) =>
            LiveSlot(jogador: s.jogador, eventos: s.eventos, nomesExibir: s.nomesExibir, posicaoSlot: s.posicaoSlot))
        .toList();
    _banco = widget.partida.listaGeralPresenca
        .where((JogadorPartida jp) => (jp.status ?? '').toLowerCase() == 'reserva')
        .map((JogadorPartida jp) => jp.copyWith())
        .toList();
    _arbitro = widget.partida.arbitro;
    _bandeira1 = widget.partida.bandeira1;
    _bandeira2 = widget.partida.bandeira2;
  }

  Future<void> _selecionarBanco(int index) async {
    setState(() => _bancoSelecionadoIndex = _bancoSelecionadoIndex == index ? null : index);
  }

  Future<void> _tocarSlotTitular(String time, int index) async {
    if (_bancoSelecionadoIndex == null) return;
    final JogadorPartida entrando = _banco[_bancoSelecionadoIndex!];
    final List<LiveSlot> lista = time == 'Azul' ? _azul : _vermelho;
    final LiveSlot slotSaindo = lista[index];
    final Jogador saindo = slotSaindo.jogador;

    if (entrando.jogador == null) return;

    try {
      final String validacao = await ref.read(partidaRepositoryProvider).validaLinha(entrando.jogador!.nomeExibir);
      if (validacao.trim().isNotEmpty && validacao.trim().toUpperCase() != 'OK') {
        if (mounted) {
          await showMessageDialog(context, title: 'Não é possível substituir', message: validacao);
        }
        return;
      }
    } catch (_) {
      // Best-effort check — proceed with the local swap even if the
      // validation call itself fails (see class-level SIMPLIFICATION NOTE).
    }

    final Jogador entrandoJogador = entrando.jogador!;
    setState(() {
      // Mirrors the desktop's processarSubstituicaoJogador /
      // apiRegistrarSubstituicaoNoEvento: the Nome column accumulates the
      // full "saiu / entrou" chain, while Eventos keeps the outgoing
      // player's history but leaves the new segment blank (even when they
      // had no events at all) for the incoming player's own tokens.
      lista[index] = LiveSlot(
        jogador: entrandoJogador,
        nomesExibir: '${slotSaindo.nomesExibir} / ${entrandoJogador.nomeExibir}',
        eventos: slotSaindo.eventos.trim().isEmpty ? ' / ' : '${slotSaindo.eventos} / ',
        // The vacant slot's tactical position doesn't change just because
        // who's filling it does.
        posicaoSlot: slotSaindo.posicaoSlot,
      );
      _banco[_bancoSelecionadoIndex!] = _banco[_bancoSelecionadoIndex!].copyWith(jogador: saindo, jogadorId: saindo.id ?? 0);
      _bancoSelecionadoIndex = null;
    });
  }

  Future<void> _chegouAtrasado() async {
    setState(() => _carregandoAtrasados = true);
    try {
      // Reflect any lineup swaps already made in this session (not just
      // the state the screen was opened with) so the backend's "not
      // already in jogadoresAzul/jogadoresVermelho" filter checks current
      // data — otherwise a player just swapped in during this same visit
      // could still show up as "available".
      final Partida snapshotAtual = widget.partida
        ..jogadoresAzul = _azul.map((LiveSlot s) => s.jogador).toList()
        ..jogadoresVermelho = _vermelho.map((LiveSlot s) => s.jogador).toList();
      final List<Jogador> disponiveis = await ref.read(partidaRepositoryProvider).atrasadosDisponiveis(snapshotAtual);
      if (!mounted) return;

      final Jogador? escolhido = await showModalBottomSheet<Jogador>(
        context: context,
        isScrollControlled: true,
        builder: (BuildContext context) => _AtrasadoPicker(jogadores: disponiveis),
      );

      if (escolhido != null) {
        try {
          final String validacao = await ref.read(partidaRepositoryProvider).validaLinha(escolhido.nomeExibir);
          if (validacao.trim().isNotEmpty && validacao.trim().toUpperCase() != 'OK') {
            if (mounted) await showMessageDialog(context, title: 'Não é possível incluir', message: validacao);
            return;
          }
        } catch (_) {
          // Best-effort — see class-level note.
        }

        setState(() {
          _banco.add(
            JogadorPartida(
              jogadorId: escolhido.id ?? 0,
              jogador: escolhido,
              partidaId: widget.partida.id ?? 0,
              time: 'Nenhum',
              status: 'Reserva',
            ),
          );
        });
      }
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
      }
    } finally {
      if (mounted) setState(() => _carregandoAtrasados = false);
    }
  }

  Future<void> _atribuirCargo(String cargo) async {
    final String? atual = cargo == 'ARBITRO' ? _arbitro : (cargo == 'BANDEIRA1' ? _bandeira1 : _bandeira2);
    // Mirrors the backend's own vacancy check (PartidaService#definirArbitragemSemDuplicidade):
    // after a removal the field reads "{history} / ____", not "" or a bare
    // "____" — treating only the bare form as vacant left the role stuck
    // "occupied" forever after the first removal.
    final String atualTrim = (atual ?? '').trim();
    final bool ocupado = atualTrim.isNotEmpty && atualTrim != '____' && !atualTrim.endsWith('/ ____');

    if (ocupado) {
      final bool remover = await showConfirmDialog(
        context,
        title: 'Remover',
        message: 'Remover $atual desta função?',
      );
      if (!remover) return;
      try {
        final Partida atualizado = await ref.read(partidaRepositoryProvider).removerArbitragem(
              partida: widget.partida,
              cargo: cargo,
              gerarHistorico: true,
            );
        setState(() => _aplicarArbitragem(cargo, atualizado));
      } catch (e) {
        if (mounted) {
          await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
        }
      }
      return;
    }

    if (_bancoSelecionadoIndex == null) {
      await showMessageDialog(
        context,
        title: 'Selecione um jogador',
        message: 'Selecione um jogador no banco antes de atribuí-lo a esta função.',
      );
      return;
    }
    final Jogador? jogador = _banco[_bancoSelecionadoIndex!].jogador;
    if (jogador == null) return;

    try {
      final String aviso = await ref
          .read(partidaRepositoryProvider)
          .validarRestricaoArbitragem(partida: widget.partida, jogador: jogador);
      if (aviso.trim().isNotEmpty) {
        if (mounted) await showMessageDialog(context, title: 'Não é possível atribuir', message: aviso);
        return;
      }
      // acumular: true so a role's history reads "Jogador / Jogador" (e.g.
      // after swapping who's refereeing) instead of silently overwriting.
      final Partida atualizado = await ref.read(partidaRepositoryProvider).definirArbitragem(
            partida: widget.partida,
            jogador: jogador,
            cargo: cargo,
            acumular: true,
          );
      setState(() {
        _aplicarArbitragem(cargo, atualizado);
        _bancoSelecionadoIndex = null;
      });
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
      }
    }
  }

  void _aplicarArbitragem(String cargo, Partida atualizado) {
    if (cargo == 'ARBITRO') _arbitro = atualizado.arbitro;
    if (cargo == 'BANDEIRA1') _bandeira1 = atualizado.bandeira1;
    if (cargo == 'BANDEIRA2') _bandeira2 = atualizado.bandeira2;
  }

  void _confirmarTrocas() {
    final Partida atualizado = widget.partida
      ..jogadoresAzul = _azul.map((LiveSlot s) => s.jogador).toList()
      ..jogadoresVermelho = _vermelho.map((LiveSlot s) => s.jogador).toList()
      ..arbitro = _arbitro
      ..bandeira1 = _bandeira1
      ..bandeira2 = _bandeira2
      ..listaGeralPresenca = <JogadorPartida>[
        ...widget.partida.listaGeralPresenca.where((JogadorPartida jp) => (jp.status ?? '').toLowerCase() != 'reserva'),
        ..._banco,
      ];
    Navigator.of(context).pop(SubstitutionResult(partida: atualizado, azul: _azul, vermelho: _vermelho));
  }

  Widget _lineupSection(String time, List<LiveSlot> lista) {
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
          ...List<Widget>.generate(lista.length, (int index) {
            final LiveSlot slot = lista[index];
            return ListTile(
              dense: true,
              title: Text(slot.nomesExibir),
              subtitle: Text(slot.posicaoSlot ?? slot.jogador.posicao ?? ''),
              trailing: _bancoSelecionadoIndex != null ? const Icon(Icons.swap_horiz) : null,
              onTap: () => _tocarSlotTitular(time, index),
            );
          }),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Substituições')),
      body: ListView(
        padding: const EdgeInsets.only(bottom: 24),
        children: <Widget>[
          _lineupSection('Azul', _azul),
          _lineupSection('Vermelho', _vermelho),
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 16, 16, 4),
            child: Text('Banco de reservas', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
          BenchList(reservas: _banco, selecionadoIndex: _bancoSelecionadoIndex, onSelect: _selecionarBanco),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: OutlinedButton.icon(
              onPressed: _carregandoAtrasados ? null : _chegouAtrasado,
              icon: _carregandoAtrasados
                  ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2))
                  : const Icon(Icons.person_add_alt),
              label: const Text('+ Chegou Atrasado'),
            ),
          ),
          const Padding(
            padding: EdgeInsets.fromLTRB(16, 16, 16, 4),
            child: Text('Arbitragem', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
          RefereeRow(label: 'Árbitro', nome: _arbitro, onTap: () => _atribuirCargo('ARBITRO')),
          RefereeRow(label: 'Bandeira 1', nome: _bandeira1, onTap: () => _atribuirCargo('BANDEIRA1')),
          RefereeRow(label: 'Bandeira 2', nome: _bandeira2, onTap: () => _atribuirCargo('BANDEIRA2')),
        ],
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: ElevatedButton(onPressed: _confirmarTrocas, child: const Text('CONFIRMAR TROCAS')),
        ),
      ),
    );
  }
}

class _AtrasadoPicker extends StatefulWidget {
  const _AtrasadoPicker({required this.jogadores});

  final List<Jogador> jogadores;

  @override
  State<_AtrasadoPicker> createState() => _AtrasadoPickerState();
}

class _AtrasadoPickerState extends State<_AtrasadoPicker> {
  String _query = '';

  @override
  Widget build(BuildContext context) {
    final List<Jogador> filtrados = filterBySubstring<Jogador>(
      widget.jogadores,
      _query,
      <String Function(Jogador)>[(Jogador j) => j.nomeExibir, (Jogador j) => j.nome],
    );

    return SafeArea(
      child: SizedBox(
        height: MediaQuery.of(context).size.height * 0.7,
        child: Column(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.all(12),
              child: TextField(
                autofocus: true,
                decoration: const InputDecoration(prefixIcon: Icon(Icons.search), hintText: 'Buscar jogador'),
                onChanged: (String v) => setState(() => _query = v),
              ),
            ),
            Expanded(
              child: ListView.builder(
                itemCount: filtrados.length,
                itemBuilder: (BuildContext context, int index) {
                  final Jogador j = filtrados[index];
                  return ListTile(
                    title: Text(j.nomeExibir),
                    subtitle: Text(j.posicao ?? ''),
                    onTap: () => Navigator.of(context).pop(j),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
