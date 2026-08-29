import 'package:flutter_test/flutter_test.dart';
import 'package:gestorcana_mobile/core/network/json_helpers.dart';

void main() {
  group('parseFlexibleDate', () {
    test('parses plain yyyy-MM-dd', () {
      final DateTime? d = parseFlexibleDate('2026-08-17');
      expect(d, DateTime(2026, 8, 17));
    });

    test('parses ISO with T-suffix', () {
      final DateTime? d = parseFlexibleDate('2026-08-17T00:00:00');
      expect(d, DateTime(2026, 8, 17));
    });

    test('returns null for null/blank/garbage', () {
      expect(parseFlexibleDate(null), isNull);
      expect(parseFlexibleDate(''), isNull);
      expect(parseFlexibleDate('not-a-date'), isNull);
    });
  });

  group('toDateOnlyString', () {
    test('formats as yyyy-MM-dd with zero padding', () {
      expect(toDateOnlyString(DateTime(2026, 1, 5)), '2026-01-05');
    });

    test('returns null for null input', () {
      expect(toDateOnlyString(null), isNull);
    });
  });

  group('unwrapQuotedString', () {
    test('strips wrapping quotes', () {
      expect(unwrapQuotedString('"OK"'), 'OK');
    });

    test('passes through an already-clean string', () {
      expect(unwrapQuotedString('OK'), 'OK');
    });

    test('handles null', () {
      expect(unwrapQuotedString(null), '');
    });

    test('unescapes inner quotes and backslashes', () {
      expect(unwrapQuotedString(r'"linha \"citada\""'), 'linha "citada"');
    });
  });

  group('parseFlexibleBool', () {
    test('accepts real booleans', () {
      expect(parseFlexibleBool(true), isTrue);
      expect(parseFlexibleBool(false), isFalse);
    });

    test('accepts 1/0 ints', () {
      expect(parseFlexibleBool(1), isTrue);
      expect(parseFlexibleBool(0), isFalse);
    });

    test('accepts "1"/"0" strings', () {
      expect(parseFlexibleBool('1'), isTrue);
      expect(parseFlexibleBool('0'), isFalse);
    });

    test('null is false', () {
      expect(parseFlexibleBool(null), isFalse);
    });
  });

  group('requireRow', () {
    test('accepts a row with enough fields', () {
      expect(requireRow(<dynamic>[1, 2, 3], 3, 'test'), <dynamic>[1, 2, 3]);
    });

    test('throws FormatException with context when too short', () {
      expect(
        () => requireRow(<dynamic>[1], 3, 'MinhaLinha'),
        throwsA(isA<FormatException>().having((FormatException e) => e.message, 'message', contains('MinhaLinha'))),
      );
    });

    test('throws FormatException when not a list', () {
      expect(() => requireRow('not a list', 1, 'x'), throwsA(isA<FormatException>()));
    });
  });
}
