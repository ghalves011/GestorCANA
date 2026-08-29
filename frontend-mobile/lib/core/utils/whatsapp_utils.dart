import 'package:url_launcher/url_launcher.dart';

/// Mirrors WhatsAppUtil: builds a wa.me deep link (auto-prefixing the
/// Brazilian country code 55 when missing) and opens it. On mobile this
/// opens the WhatsApp app directly when installed, falling back to
/// web.whatsapp.com in the browser otherwise (url_launcher handles that
/// automatically since wa.me redirects appropriately).
class WhatsAppUtils {
  WhatsAppUtils._();

  static String _onlyDigits(String value) => value.replaceAll(RegExp(r'\D'), '');

  static Uri buildUri(String texto, {String? telefone}) {
    final String encoded = Uri.encodeComponent(texto);
    if (telefone == null || telefone.trim().isEmpty) {
      return Uri.parse('https://wa.me/?text=$encoded');
    }
    String digits = _onlyDigits(telefone);
    if (!digits.startsWith('55')) {
      digits = '55$digits';
    }
    return Uri.parse('https://wa.me/$digits?text=$encoded');
  }

  /// Returns false if no app could handle the link (caller should show a
  /// user-facing error in that case).
  static Future<bool> enviarMensagem(String texto, {String? telefone}) async {
    final Uri uri = buildUri(texto, telefone: telefone);
    return launchUrl(uri, mode: LaunchMode.externalApplication);
  }
}
