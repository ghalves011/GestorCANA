import 'package:flutter/material.dart';

import '../../../core/theme/app_theme.dart';

/// Mirrors TelaPartidaLiveView's score label + ref/flags header.
class ScoreHeader extends StatelessWidget {
  const ScoreHeader({
    super.key,
    required this.nomePartida,
    required this.golsAzul,
    required this.golsVermelho,
    this.arbitro,
    this.bandeira1,
    this.bandeira2,
  });

  final String nomePartida;
  final int golsAzul;
  final int golsVermelho;
  final String? arbitro;
  final String? bandeira1;
  final String? bandeira2;

  /// Drops a trailing "/ ____" left by removerArbitragem's history
  /// placeholder for a role nobody currently holds — internal bookkeeping,
  /// not meant to be shown to the user.
  static String _semPlaceholder(String? texto) {
    return (texto ?? '').replaceAll(RegExp(r'(\s*/\s*____)+$'), '').trim();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: const BoxDecoration(
        gradient: LinearGradient(colors: <Color>[CanaColors.azulGradiente, CanaColors.vermelhoGradiente]),
      ),
      child: Column(
        children: <Widget>[
          Text(nomePartida, style: const TextStyle(color: Colors.white, fontSize: 14)),
          const SizedBox(height: 6),
          Text(
            'Azul $golsAzul x $golsVermelho Vermelho',
            style: const TextStyle(color: Colors.white, fontSize: 28, fontWeight: FontWeight.bold),
          ),
          if (_semPlaceholder(arbitro).isNotEmpty ||
              _semPlaceholder(bandeira1).isNotEmpty ||
              _semPlaceholder(bandeira2).isNotEmpty) ...<Widget>[
            const SizedBox(height: 8),
            Text(
              <String>[
                if (_semPlaceholder(arbitro).isNotEmpty) 'Árbitro: ${_semPlaceholder(arbitro)}',
                if (_semPlaceholder(bandeira1).isNotEmpty) 'Bandeira 1: ${_semPlaceholder(bandeira1)}',
                if (_semPlaceholder(bandeira2).isNotEmpty) 'Bandeira 2: ${_semPlaceholder(bandeira2)}',
              ].join(' · '),
              style: const TextStyle(color: Colors.white70, fontSize: 11),
              textAlign: TextAlign.center,
            ),
          ],
        ],
      ),
    );
  }
}
