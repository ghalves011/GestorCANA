import 'package:flutter/material.dart';

import '../../../core/utils/player_filter_utils.dart';
import '../../../core/widgets/confirm_dialog.dart';
import '../../players/models/jogador.dart';
import '../../players/widgets/player_sports_tab.dart' show kPosicoes, posicaoAbreviada;

/// Rank of [jogador]'s posicao within kPosicoes's GOL/LAT/ZAG/VOL/MEI/ATA
/// order, so the attendance list groups players by position. Unset or
/// unrecognized positions sort last.
int _posicaoRank(Jogador jogador) {
  final String posicao = (jogador.posicao ?? '').trim().toUpperCase();
  final int index = kPosicoes.indexOf(posicao);
  return index == -1 ? kPosicoes.length : index;
}

class AttendanceSelectionArgs {
  const AttendanceSelectionArgs({
    required this.jogadores,
    required this.debtorIds,
    this.preSelecionados = const <Jogador>[],
  });

  final List<Jogador> jogadores;
  final Set<int> debtorIds;

  /// Previously-confirmed selection (e.g. from a prior open of this screen
  /// during the same match-setup session), so reopening doesn't lose it.
  final List<Jogador> preSelecionados;
}

class AttendanceSelectionResult {
  const AttendanceSelectionResult({required this.selecionados, required this.confirmado});

  /// Ordered by selection order (matters: this order feeds POST
  /// /partidas/sortear, mirroring DialogSelecaoJogadoresView).
  final List<Jogador> selecionados;
  final bool confirmado;
}

/// Mirrors DialogSelecaoJogadoresView: attendance checklist with a running
/// counter (highlighted green at exactly 22) and an override-with-warning
/// flow for players with late dues.
class AttendanceSelectionScreen extends StatefulWidget {
  const AttendanceSelectionScreen({super.key, required this.args});

  final AttendanceSelectionArgs args;

  @override
  State<AttendanceSelectionScreen> createState() => _AttendanceSelectionScreenState();
}

class _AttendanceSelectionScreenState extends State<AttendanceSelectionScreen> {
  final TextEditingController _searchController = TextEditingController();
  String _query = '';

  /// Selection order preserved via list, membership checked via id set.
  late final List<Jogador> _selecionadosOrdenados = List<Jogador>.of(widget.args.preSelecionados);

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  bool _isSelected(Jogador j) => _selecionadosOrdenados.any((Jogador s) => s.id == j.id);

  Future<void> _toggle(Jogador jogador) async {
    final bool jaSelecionado = _isSelected(jogador);
    if (jaSelecionado) {
      setState(() => _selecionadosOrdenados.removeWhere((Jogador s) => s.id == jogador.id));
      return;
    }

    if (widget.args.debtorIds.contains(jogador.id)) {
      final bool prosseguir = await showConfirmDialog(
        context,
        title: 'Mensalidade em atraso',
        message: '${jogador.nomeExibir} está com mensalidade em atraso. Deseja incluir mesmo assim?',
      );
      if (!prosseguir) return;
    }

    setState(() => _selecionadosOrdenados.add(jogador));
  }

  @override
  Widget build(BuildContext context) {
    final List<Jogador> filtrados = filterBySubstring<Jogador>(
      widget.args.jogadores,
      _query,
      <String Function(Jogador)>[(Jogador j) => j.nomeExibir, (Jogador j) => j.nome],
    )..sort((Jogador a, Jogador b) {
        final int porPosicao = _posicaoRank(a).compareTo(_posicaoRank(b));
        if (porPosicao != 0) return porPosicao;
        return a.nomeExibir.toLowerCase().compareTo(b.nomeExibir.toLowerCase());
      });

    final int count = _selecionadosOrdenados.length;
    final bool completo = count == 22;

    return Scaffold(
      appBar: AppBar(title: const Text('Jogadores Presentes')),
      body: Column(
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.all(12),
            child: TextField(
              controller: _searchController,
              decoration: const InputDecoration(prefixIcon: Icon(Icons.search), hintText: 'Buscar jogador'),
              onChanged: (String v) => setState(() => _query = v),
            ),
          ),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(vertical: 8),
            color: completo ? Colors.green.withValues(alpha: 0.15) : Theme.of(context).colorScheme.surfaceContainerHighest,
            child: Text(
              '$count selecionados',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: completo ? Colors.green.shade800 : null,
              ),
            ),
          ),
          Expanded(
            child: ListView.builder(
              itemCount: filtrados.length,
              itemBuilder: (BuildContext context, int index) {
                final Jogador jogador = filtrados[index];
                final bool selecionado = _isSelected(jogador);
                final bool devedor = widget.args.debtorIds.contains(jogador.id);
                final String pos = posicaoAbreviada(jogador.posicao);
                return CheckboxListTile(
                  value: selecionado,
                  onChanged: (_) => _toggle(jogador),
                  title: Text(pos.isEmpty ? jogador.nomeExibir : '${jogador.nomeExibir} - $pos'),
                  subtitle: Text(jogador.status + (devedor ? ' · mensalidade em atraso' : '')),
                  secondary: devedor ? const Icon(Icons.warning_amber, color: Colors.orange) : null,
                );
              },
            ),
          ),
        ],
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: <Widget>[
              Expanded(
                child: OutlinedButton(
                  onPressed: () => Navigator.of(context)
                      .pop(const AttendanceSelectionResult(selecionados: <Jogador>[], confirmado: false)),
                  child: const Text('Cancelar'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                flex: 2,
                child: ElevatedButton(
                  onPressed: _selecionadosOrdenados.isEmpty
                      ? null
                      : () => Navigator.of(context).pop(
                            AttendanceSelectionResult(
                              selecionados: List<Jogador>.of(_selecionadosOrdenados),
                              confirmado: true,
                            ),
                          ),
                  child: const Text('CONFIRMAR'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
