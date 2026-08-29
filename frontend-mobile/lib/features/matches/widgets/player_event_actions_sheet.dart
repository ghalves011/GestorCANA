import 'package:flutter/material.dart';

import '../../../core/theme/app_theme.dart';

class EventActionResult {
  const EventActionResult.adicionar(String token)
      : tokenAdicionar = token,
        tokenRemover = null;

  const EventActionResult.remover(String token)
      : tokenAdicionar = null,
        tokenRemover = token;

  final String? tokenAdicionar;
  final String? tokenRemover;
}

/// Replaces the desktop's right-click context menu on a pitch-slot row
/// (Registrar Gol / Gol Contra / Cartão Amarelo / Cartão Vermelho Direto,
/// plus conditional "Remover" options per token type currently present).
///
/// The goalkeeper improvise/un-improvise menu items from the desktop are
/// intentionally NOT offered here — their backend endpoints
/// (/partidas/obter-posicao-original, /partidas/improvisar-goleiro) don't
/// exist (see plan §6); a client-only stub would silently lose its state
/// on the next match reload, which is worse than omitting the feature.
Future<EventActionResult?> showPlayerEventActionsSheet(
  BuildContext context, {
  required String nomeJogador,
  required List<String> tokensAtivos,
}) {
  return showModalBottomSheet<EventActionResult>(
    context: context,
    builder: (BuildContext context) {
      return SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.all(16),
              child: Text(nomeJogador, style: Theme.of(context).textTheme.titleMedium),
            ),
            ListTile(
              leading: const Text(EventTokens.gol, style: TextStyle(fontSize: 20)),
              title: const Text('Registrar Gol'),
              onTap: () => Navigator.of(context).pop(const EventActionResult.adicionar(EventTokens.gol)),
            ),
            ListTile(
              leading: const Text(EventTokens.golContra, style: TextStyle(fontSize: 20)),
              title: const Text('Registrar Gol Contra'),
              onTap: () => Navigator.of(context).pop(const EventActionResult.adicionar(EventTokens.golContra)),
            ),
            ListTile(
              leading: const Text(EventTokens.amarelo, style: TextStyle(fontSize: 20)),
              title: const Text('Cartão Amarelo'),
              onTap: () => Navigator.of(context).pop(const EventActionResult.adicionar(EventTokens.amarelo)),
            ),
            ListTile(
              leading: const Text(EventTokens.vermelho, style: TextStyle(fontSize: 20)),
              title: const Text('Cartão Vermelho Direto'),
              onTap: () => Navigator.of(context).pop(const EventActionResult.adicionar(EventTokens.vermelho)),
            ),
            if (tokensAtivos.isNotEmpty) ...<Widget>[
              const Divider(),
              ...tokensAtivos.map(
                (String token) => ListTile(
                  leading: const Icon(Icons.remove_circle_outline),
                  title: Text('Remover $token'),
                  onTap: () => Navigator.of(context).pop(EventActionResult.remover(token)),
                ),
              ),
            ],
          ],
        ),
      );
    },
  );
}
