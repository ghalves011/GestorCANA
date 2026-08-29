import 'package:flutter/material.dart';

import '../models/jogador.dart';

/// Sponsor/"padrinho" selector, populated from the full player list
/// (mirrors FormJogadorView's combo). Excludes [currentPlayerId] since a
/// player can't be their own padrinho (server-enforced rule too).
class PadrinhoDropdown extends StatelessWidget {
  const PadrinhoDropdown({
    super.key,
    required this.jogadores,
    required this.currentPlayerId,
    required this.selectedPadrinhoId,
    required this.onChanged,
    this.enabled = true,
  });

  final List<Jogador> jogadores;
  final int? currentPlayerId;
  final int? selectedPadrinhoId;
  final ValueChanged<int?> onChanged;
  final bool enabled;

  @override
  Widget build(BuildContext context) {
    final List<Jogador> opcoes = jogadores.where((Jogador j) => j.id != null && j.id != currentPlayerId).toList()
      ..sort((Jogador a, Jogador b) => a.nomeExibir.toLowerCase().compareTo(b.nomeExibir.toLowerCase()));

    final bool valueExists = selectedPadrinhoId != null && opcoes.any((Jogador j) => j.id == selectedPadrinhoId);

    return DropdownButtonFormField<int?>(
      initialValue: valueExists ? selectedPadrinhoId : null,
      decoration: const InputDecoration(labelText: 'Padrinho'),
      items: <DropdownMenuItem<int?>>[
        const DropdownMenuItem<int?>(value: null, child: Text('Nenhum')),
        ...opcoes.map((Jogador j) => DropdownMenuItem<int?>(value: j.id, child: Text(j.nomeExibir))),
      ],
      onChanged: enabled ? onChanged : null,
    );
  }
}
