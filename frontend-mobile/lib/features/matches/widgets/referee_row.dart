import 'package:flutter/material.dart';

/// Read-only display of a single arbitration role's current holder, plus
/// an optional assign/remove action. Used both as a static display in the
/// live scoreboard and as an interactive row in the substitution screen.
class RefereeRow extends StatelessWidget {
  const RefereeRow({
    super.key,
    required this.label,
    required this.nome,
    this.onTap,
  });

  final String label;
  final String? nome;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final bool vazio = (nome ?? '').trim().isEmpty || nome!.trim() == '____';
    return ListTile(
      dense: true,
      title: Text(label),
      subtitle: Text(vazio ? 'Não definido' : nome!),
      trailing: onTap == null ? null : const Icon(Icons.chevron_right),
      onTap: onTap,
    );
  }
}
