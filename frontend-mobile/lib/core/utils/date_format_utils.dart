/// Display-facing date helpers, mirroring the desktop's DateUtil
/// (paraUsuario/ler). Backend (de)serialization lives in
/// core/network/json_helpers.dart (parseFlexibleDate/toDateOnlyString) —
/// these helpers are purely for what the user types/sees on screen.
class DateFormatUtils {
  DateFormatUtils._();

  /// DateTime -> "dd/MM/yyyy" for display.
  static String paraUsuario(DateTime? date) {
    if (date == null) return '';
    final String d = date.day.toString().padLeft(2, '0');
    final String m = date.month.toString().padLeft(2, '0');
    final String y = date.year.toString().padLeft(4, '0');
    return '$d/$m/$y';
  }

  /// "dd/MM/yyyy" (typed by the user) -> DateTime, or null if unparseable.
  static DateTime? ler(String texto) {
    final String trimmed = texto.trim();
    if (trimmed.isEmpty) return null;
    final List<String> parts = trimmed.split('/');
    if (parts.length != 3) return null;
    try {
      final int day = int.parse(parts[0]);
      final int month = int.parse(parts[1]);
      final int year = int.parse(parts[2]);
      return DateTime(year, month, day);
    } catch (_) {
      return null;
    }
  }

  static int extrairAno(DateTime date) => date.year;
}
