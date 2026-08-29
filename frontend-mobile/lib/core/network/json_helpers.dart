/// Parsing helpers that bridge the backend's loose/legacy JSON shapes
/// (dual date formats, quoted-string bodies, 1/0-as-bool, Object[] rows)
/// to well-typed Dart values. Mirrors the tolerance the desktop client's
/// custom Gson adapters had (see ApiClient.GSON on the Java side).
library;

/// Accepts null, "yyyy-MM-dd", or "yyyy-MM-ddTHH:mm:ss[...]" and returns a
/// date-only [DateTime] (time components stripped), or null if [value] is
/// null/blank/unparseable.
DateTime? parseFlexibleDate(dynamic value) {
  if (value == null) return null;
  final String raw = value.toString().trim();
  if (raw.isEmpty) return null;

  final String datePart = raw.contains('T') ? raw.split('T').first : raw;

  try {
    if (datePart.contains('-')) {
      final DateTime parsed = DateTime.parse(datePart);
      return DateTime(parsed.year, parsed.month, parsed.day);
    }
    if (datePart.contains('/')) {
      final List<String> parts = datePart.split('/');
      if (parts.length == 3) {
        final int day = int.parse(parts[0]);
        final int month = int.parse(parts[1]);
        final int year = int.parse(parts[2]);
        return DateTime(year, month, day);
      }
    }
  } catch (_) {
    return null;
  }
  return null;
}

/// Serializes a date-only value back to "yyyy-MM-dd" for request bodies,
/// matching what the backend expects to persist.
String? toDateOnlyString(DateTime? date) {
  if (date == null) return null;
  final String y = date.year.toString().padLeft(4, '0');
  final String m = date.month.toString().padLeft(2, '0');
  final String d = date.day.toString().padLeft(2, '0');
  return '$y-$m-$d';
}

/// Some endpoints respond with a JSON-encoded quoted string (e.g. `"OK"`)
/// as their entire body. Depending on how Dio decoded the response this
/// may already be a clean Dart String, or it may still carry the literal
/// wrapping quotes (and escaped inner quotes) if the server sent it with
/// a content-type Dio didn't auto-decode. Handle both.
String unwrapQuotedString(dynamic raw) {
  if (raw == null) return '';
  if (raw is! String) return raw.toString();
  final String trimmed = raw.trim();
  if (trimmed.length >= 2 && trimmed.startsWith('"') && trimmed.endsWith('"')) {
    return trimmed
        .substring(1, trimmed.length - 1)
        .replaceAll('\\"', '"')
        .replaceAll('\\\\', '\\');
  }
  return raw;
}

/// Tolerant boolean parsing: accepts real booleans, 1/0 ints, and "1"/"0"/
/// "true"/"false" strings, matching the backend's inconsistency between
/// entity JSON (int 1/0 for Contribuicao.pago) and the Object[]-based
/// matrix endpoints (which may hand back real booleans).
bool parseFlexibleBool(dynamic value) {
  if (value == null) return false;
  if (value is bool) return value;
  if (value is num) return value != 0;
  final String raw = value.toString().trim().toLowerCase();
  return raw == '1' || raw == 'true';
}

/// Tolerant int parsing for fields the backend sometimes serializes as
/// numeric strings inside Object[] rows (e.g. EstatisticaRow's gols/cartao
/// counts arrive as String-wrapped ints).
int parseFlexibleInt(dynamic value, {int fallback = 0}) {
  if (value == null) return fallback;
  if (value is int) return value;
  if (value is num) return value.toInt();
  return int.tryParse(value.toString().trim()) ?? fallback;
}

double? parseFlexibleDouble(dynamic value) {
  if (value == null) return null;
  if (value is double) return value;
  if (value is num) return value.toDouble();
  return double.tryParse(value.toString().trim().replaceAll(',', '.'));
}

/// Validates an Object[]-row's length before positional access, throwing a
/// clear [FormatException] (with the offending row content) instead of a
/// raw RangeError if the backend's shape ever drifts.
List<dynamic> requireRow(dynamic row, int expectedMinLength, String context) {
  if (row is! List) {
    throw FormatException('Expected a row array for $context, got: $row');
  }
  if (row.length < expectedMinLength) {
    throw FormatException(
      'Row for $context has ${row.length} fields, expected at least '
      '$expectedMinLength. Row content: $row',
    );
  }
  return row;
}
