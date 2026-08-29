import 'package:flutter/material.dart';

/// Mirrors TelaSumulaView: a simple match-report text editor. Editable
/// pre-finalization (saved only into the local Partida.sumula field —
/// persisted for real later as part of POST /partidas/finalizar); read-only
/// after finalization ("FECHAR" instead of "SALVAR SÚMULA").
Future<String?> showMatchReportSheet(
  BuildContext context, {
  required String sumulaInicial,
  required bool somenteLeitura,
}) {
  final TextEditingController controller = TextEditingController(text: sumulaInicial);

  return showModalBottomSheet<String>(
    context: context,
    isScrollControlled: true,
    builder: (BuildContext context) {
      return Padding(
        padding: EdgeInsets.only(
          left: 16,
          right: 16,
          top: 16,
          bottom: MediaQuery.of(context).viewInsets.bottom + 16,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            Text('Súmula', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 12),
            TextField(
              controller: controller,
              enabled: !somenteLeitura,
              maxLines: 8,
              minLines: 4,
              decoration: const InputDecoration(hintText: 'Observações da partida...'),
            ),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => Navigator.of(context).pop(somenteLeitura ? null : controller.text),
              child: Text(somenteLeitura ? 'FECHAR' : 'SALVAR SÚMULA'),
            ),
          ],
        ),
      );
    },
  );
}
