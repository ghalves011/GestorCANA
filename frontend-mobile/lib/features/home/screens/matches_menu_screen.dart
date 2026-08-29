import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/widgets/app_scaffold.dart';

/// Mirrors TelaMenuPartidaView: match-related sub-menu. No API calls.
class MatchesMenuScreen extends StatelessWidget {
  const MatchesMenuScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return AppScaffold(
      title: 'Partidas',
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 480),
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                SizedBox(
                  width: double.infinity,
                  height: 64,
                  child: ElevatedButton.icon(
                    onPressed: () => context.push('/partidas/historico'),
                    icon: const Icon(Icons.history),
                    label: const Text('CONSULTAR PARTIDA'),
                  ),
                ),
                const SizedBox(height: 16),
                SizedBox(
                  width: double.infinity,
                  height: 64,
                  child: ElevatedButton.icon(
                    onPressed: () => context.push('/partidas/nova'),
                    icon: const Icon(Icons.add_circle_outline),
                    label: const Text('CRIAR PARTIDA'),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
