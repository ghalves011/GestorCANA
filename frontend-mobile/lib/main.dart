import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app.dart';
import 'core/config/app_config.dart';
import 'core/network/api_client.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Load the persisted base URL override (if any) before the app starts,
  // so every repository's Dio client is built pointing at the right
  // server from the very first frame. See core/config/app_config.dart.
  final String baseUrl = await AppConfig.getBaseUrl();
  // Chave de acesso (X-API-KEY) salva nas Configurações, se houver.
  final String apiKey = await AppConfig.getApiKey();

  runApp(
    ProviderScope(
      overrides: <Override>[
        baseUrlProvider.overrideWith((Ref ref) => baseUrl),
        apiKeyProvider.overrideWith((Ref ref) => apiKey),
      ],
      child: const GestorCanaApp(),
    ),
  );
}
