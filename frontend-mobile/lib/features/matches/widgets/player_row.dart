import 'package:flutter/material.dart';

import '../utils/event_cell_utils.dart';
import 'team_panel.dart';

/// Single pitch-slot row: name, position, and the active substitution
/// block's event tokens (⚽ 🟨 🟥). Tap arms/executes a swap; long-press
/// opens the goal/card/remove actions sheet.
class PlayerRow extends StatelessWidget {
  const PlayerRow({super.key, required this.slot, required this.armado, this.onTap, this.onLongPress});

  final LiveSlot slot;
  final bool armado;
  final VoidCallback? onTap;
  final VoidCallback? onLongPress;

  /// Only the active (last) substitution block's tokens are shown, per the
  /// live-scoreboard mini-language ("nome (pos) TOKENS / nome2 TOKENS").
  String get _tokensAtivos => activeTokensText(slot.eventos);

  @override
  Widget build(BuildContext context) {
    final bool incompleto = slot.jogador.id == null || slot.jogador.id == 0;

    return InkWell(
      onTap: onTap,
      onLongPress: incompleto ? null : onLongPress,
      child: Container(
        color: armado ? Theme.of(context).colorScheme.primary.withValues(alpha: 0.15) : null,
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        child: Row(
          children: <Widget>[
            Expanded(
              flex: 3,
              child: Text(
                slot.jogador.nomeExibir,
                style: TextStyle(
                  fontStyle: incompleto ? FontStyle.italic : FontStyle.normal,
                  color: incompleto ? Theme.of(context).disabledColor : null,
                ),
              ),
            ),
            Expanded(
              flex: 2,
              child: Text(slot.jogador.posicao ?? '', style: Theme.of(context).textTheme.bodySmall),
            ),
            Expanded(
              flex: 2,
              child: Text(_tokensAtivos, textAlign: TextAlign.right),
            ),
          ],
        ),
      ),
    );
  }
}
