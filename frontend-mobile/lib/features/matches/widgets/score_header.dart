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
          if ((arbitro ?? '').isNotEmpty || (bandeira1 ?? '').isNotEmpty || (bandeira2 ?? '').isNotEmpty) ...<Widget>[
            const SizedBox(height: 8),
            Text(
              <String>[
                if ((arbitro ?? '').isNotEmpty) 'Árbitro: $arbitro',
                if ((bandeira1 ?? '').isNotEmpty) 'Bandeira 1: $bandeira1',
                if ((bandeira2 ?? '').isNotEmpty) 'Bandeira 2: $bandeira2',
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
