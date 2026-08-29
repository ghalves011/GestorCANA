import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Holds the currently-active base URL. Seeded at app boot in main.dart
/// from the persisted value (see AppConfig.getBaseUrl), and updated live
/// by the Settings screen — every repository provider that watches
/// [dioProvider] automatically rebuilds against the new server.
final StateProvider<String> baseUrlProvider = StateProvider<String>(
  (Ref ref) => 'http://157.151.15.177:8080',
);

Dio buildDio(String baseUrl) {
  final Dio dio = Dio(
    BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(seconds: 20),
      sendTimeout: const Duration(seconds: 20),
      headers: <String, String>{
        'Accept': 'application/json; charset=UTF-8',
      },
      // Some endpoints return a bare quoted string ("OK") or plain text
      // instead of a JSON object; validateStatus keeps Dio from throwing
      // on 4xx so ApiException can read the backend's error text body.
      responseType: ResponseType.json,
    ),
  );

  dio.interceptors.add(
    LogInterceptor(
      requestBody: true,
      responseBody: true,
      logPrint: (Object obj) {
        // Only meaningful during development; harmless in release builds.
        // ignore: avoid_print
        print(obj);
      },
    ),
  );

  return dio;
}

final Provider<Dio> dioProvider = Provider<Dio>((Ref ref) {
  final String baseUrl = ref.watch(baseUrlProvider);
  return buildDio(baseUrl);
});
