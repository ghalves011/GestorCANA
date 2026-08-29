import 'package:shared_preferences/shared_preferences.dart';

/// Single source of truth for the backend base URL.
///
/// The backend's eventual public URL isn't decided yet, so this is kept as
/// ONE easily-changeable placeholder constant, plus a runtime override
/// (persisted via [SharedPreferences], editable from the Settings screen)
/// so the URL can be changed without a rebuild during development or if it
/// changes post-release.
///
/// IMPORTANT for local development — "localhost" on a phone/emulator means
/// the device itself, NOT your development machine:
///   - Android emulator -> use 10.0.2.2 (maps to host loopback)
///   - iOS simulator -> localhost works (shares host loopback)
///   - Physical device on the same Wi-Fi -> use your machine's LAN IP
///     (e.g. 192.168.x.x) with the backend bound to 0.0.0.0, not 127.0.0.1
class AppConfig {
  AppConfig._();

  /// Change this ONE value once the backend's public URL is known.
  static const String defaultBaseUrl = 'http://157.151.15.177:8080';

  static const String _prefsKey = 'base_url_override';

  static Future<String> getBaseUrl() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final String? override = prefs.getString(_prefsKey);
    if (override == null || override.trim().isEmpty) {
      return defaultBaseUrl;
    }
    return override.trim();
  }

  static Future<void> setBaseUrl(String url) async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    final String trimmed = url.trim();
    if (trimmed.isEmpty) {
      await prefs.remove(_prefsKey);
    } else {
      await prefs.setString(_prefsKey, trimmed);
    }
  }

  static Future<void> resetBaseUrl() async {
    final SharedPreferences prefs = await SharedPreferences.getInstance();
    await prefs.remove(_prefsKey);
  }
}
