import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/theme/app_theme.dart';

/// Mirrors TelaMenuPrincipalView: the app's home/root screen. Pure
/// navigation hub, no API calls of its own.
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('GestorCANA'),
        actions: <Widget>[
          IconButton(
            icon: const Icon(Icons.settings),
            tooltip: 'Configurações',
            onPressed: () => context.push('/settings'),
          ),
        ],
      ),
      body: SafeArea(
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 480),
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  Image.asset('assets/images/logo_cana.png', height: 140),
                  const SizedBox(height: 32),
                  _MenuButton(
                    label: 'CADASTRO',
                    icon: Icons.person_add_alt,
                    onTap: () => context.push('/jogadores/novo'),
                  ),
                  const SizedBox(height: 16),
                  _MenuButton(
                    label: 'CONTRIBUIÇÃO',
                    icon: Icons.payments_outlined,
                    onTap: () => context.push('/contribuicoes'),
                  ),
                  const SizedBox(height: 16),
                  _MenuButton(
                    label: 'PARTIDAS',
                    icon: Icons.sports_soccer,
                    onTap: () => context.push('/partidas'),
                  ),
                  const SizedBox(height: 16),
                  _MenuButton(
                    label: 'ESTATÍSTICAS',
                    icon: Icons.bar_chart,
                    onTap: () => context.push('/estatisticas'),
                    color: CanaColors.vermelhoGradiente,
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _MenuButton extends StatelessWidget {
  const _MenuButton({required this.label, required this.icon, required this.onTap, this.color});

  final String label;
  final IconData icon;
  final VoidCallback onTap;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      height: 64,
      child: ElevatedButton.icon(
        style: color == null
            ? null
            : ElevatedButton.styleFrom(backgroundColor: color, foregroundColor: Colors.white),
        onPressed: onTap,
        icon: Icon(icon),
        label: Text(label),
      ),
    );
  }
}
