import 'package:flutter/material.dart';

/// Titled card wrapper used for grouping form sections (e.g. the player
/// form's "Infos Pessoais" / "Infos Esportivas" content blocks).
class SectionCard extends StatelessWidget {
  const SectionCard({super.key, this.title, required this.child, this.padding});

  final String? title;
  final Widget child;
  final EdgeInsetsGeometry? padding;

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      child: Padding(
        padding: padding ?? const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            if (title != null) ...<Widget>[
              Text(title!, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
              const SizedBox(height: 12),
            ],
            child,
          ],
        ),
      ),
    );
  }
}
