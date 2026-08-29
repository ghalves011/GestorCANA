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

  runApp(
    ProviderScope(
      overrides: <Override>[
        baseUrlProvider.overrideWith((Ref ref) => baseUrl),
      ],
      child: const GestorCanaApp(),
    ),
  );
}
