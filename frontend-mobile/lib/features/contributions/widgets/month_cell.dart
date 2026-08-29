import 'package:flutter/material.dart';

const List<String> kMesesAbrev = <String>[
  'Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez',
];

/// A single Jan-Dec paid/unpaid cell in the dues matrix, mirroring
/// ContribuicaoView's checkbox grid (green when paid, tappable either way).
class MonthCell extends StatelessWidget {
  const MonthCell({super.key, required this.mesIndex0, required this.pago, required this.onTap});

  final int mesIndex0;
  final bool pago;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(8),
      child: Container(
        width: 44,
        padding: const EdgeInsets.symmetric(vertical: 6),
        decoration: BoxDecoration(
          color: pago ? Colors.green.withValues(alpha: 0.15) : Colors.grey.withValues(alpha: 0.12),
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: pago ? Colors.green : Colors.grey.shade400),
        ),
        child: Column(
          children: <Widget>[
            Text(kMesesAbrev[mesIndex0], style: const TextStyle(fontSize: 11)),
            Icon(
              pago ? Icons.check_circle : Icons.radio_button_unchecked,
              size: 16,
              color: pago ? Colors.green : Colors.grey,
            ),
          ],
        ),
      ),
    );
  }
}
