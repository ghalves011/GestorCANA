import 'package:flutter/material.dart';

import '../../players/models/jogador.dart';
import 'player_row.dart';

/// One row of a live team panel: the drawn player plus the raw
/// mini-language events cell text for their currently-active substitution
/// block (see EventoController docs — "nome (pos) TOKENS / nome2 TOKENS").
/// This mirrors the pitch table's per-slot state while a match is live;
/// in read-only historical mode the screen instead renders
/// GridHistoricoRow entries directly (no LiveSlot needed there).
class LiveSlot {
  LiveSlot({required this.jogador, this.eventos = '', String? nomesExibir, this.posicaoSlot})
      : nomesExibir = nomesExibir ?? jogador.nomeExibir;

  Jogador jogador;
  String eventos;

  /// "/"-joined substitution history of display names for this slot (e.g.
  /// "João / Pedro" after a swap) — mirrors the desktop Nome column, shown
  /// instead of jogador.nomeExibir so a mid-match substitution keeps the
  /// outgoing player's name visible in the chain.
  String nomesExibir;

  /// The tactical slot's own position sigla (e.g. "MEI"), parsed from this
  /// JogadorPartida's funcao ("Azul_MEI_3") — the formation-assigned
  /// position, which can differ from jogador.posicao when the player was
  /// improvised into the slot. Falls back to jogador.posicao for display
  /// when unset (e.g. before a listaGeralPresenca round trip).
  String? posicaoSlot;
}

/// Replaces the desktop's per-team JTable (Nome/Pos/Eventos) with a
/// scrollable list, mirroring TelaPartidaLiveView's team panels.
class TeamPanel extends StatelessWidget {
  const TeamPanel({
    super.key,
    required this.titulo,
    required this.corTime,
    required this.slots,
    required this.armedIndex,
    this.onTapSlot,
    this.onLongPressSlot,
  });

  final String titulo;
  final Color corTime;
  final List<LiveSlot> slots;

  /// Index of the slot currently armed for a swap, or null.
  final int? armedIndex;

  final void Function(int index)? onTapSlot;
  final void Function(int index)? onLongPressSlot;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 6),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Container(
            padding: const EdgeInsets.symmetric(vertical: 8),
            decoration: BoxDecoration(color: corTime, borderRadius: const BorderRadius.vertical(top: Radius.circular(12))),
            child: Text(
              titulo,
              textAlign: TextAlign.center,
              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
            ),
          ),
          ...List<Widget>.generate(slots.length, (int index) {
            final LiveSlot slot = slots[index];
            return PlayerRow(
              slot: slot,
              armado: armedIndex == index,
              onTap: onTapSlot == null ? null : () => onTapSlot!(index),
              onLongPress: onLongPressSlot == null ? null : () => onLongPressSlot!(index),
            );
          }),
        ],
      ),
    );
  }
}
