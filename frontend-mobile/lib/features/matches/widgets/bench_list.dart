import 'package:flutter/material.dart';

import '../models/jogador_partida.dart';

/// Scrollable bench/reserves list, mirroring TelaSubstituicaoView's
/// reserve button grid (padded to a minimum of 12 visual slots there;
/// here it's simply a list, since a phone has no room for a button grid).
class BenchList extends StatelessWidget {
  const BenchList({
    super.key,
    required this.reservas,
    required this.selecionadoIndex,
    required this.onSelect,
  });

  final List<JogadorPartida> reservas;
  final int? selecionadoIndex;
  final ValueChanged<int> onSelect;

  @override
  Widget build(BuildContext context) {
    if (reservas.isEmpty) {
      return const Padding(
        padding: EdgeInsets.all(16),
        child: Text('Nenhum jogador no banco.'),
      );
    }

    return Column(
      children: List<Widget>.generate(reservas.length, (int index) {
        final JogadorPartida jp = reservas[index];
        final bool selecionado = selecionadoIndex == index;
        return ListTile(
          dense: true,
          selected: selecionado,
          selectedTileColor: Theme.of(context).colorScheme.primary.withValues(alpha: 0.12),
          leading: const Icon(Icons.person_outline),
          title: Text(jp.jogador?.nomeExibir ?? 'Jogador #${jp.jogadorId}'),
          subtitle: Text(jp.jogador?.posicao ?? ''),
          onTap: () => onSelect(index),
        );
      }),
    );
  }
}
