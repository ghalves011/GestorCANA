import 'package:flutter/material.dart';
import 'package:share_plus/share_plus.dart';

import '../../../core/utils/whatsapp_utils.dart';

/// Shows the WhatsApp-formatted receipt text (from GET /contribuicoes/
/// recibo) and offers to share it, mirroring the desktop's
/// WhatsAppUtil.enviarMensagem flow but via the native share sheet /
/// wa.me deep link instead of Desktop.browse().
Future<void> showReceiptShareSheet(BuildContext context, {required String recibo, String? telefone}) {
  return showModalBottomSheet<void>(
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
            Text('Recibo', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 12),
            Container(
              constraints: const BoxConstraints(maxHeight: 300),
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surfaceContainerHighest,
                borderRadius: BorderRadius.circular(10),
              ),
              child: SingleChildScrollView(child: Text(recibo)),
            ),
            const SizedBox(height: 16),
            Row(
              children: <Widget>[
                Expanded(
                  child: OutlinedButton.icon(
                    icon: const Icon(Icons.share),
                    label: const Text('Compartilhar'),
                    onPressed: () => Share.share(recibo),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton.icon(
                    icon: const Icon(Icons.chat),
                    label: const Text('WhatsApp'),
                    onPressed: () => WhatsAppUtils.enviarMensagem(recibo, telefone: telefone),
                  ),
                ),
              ],
            ),
          ],
        ),
      );
    },
  );
}
