import 'package:flutter_test/flutter_test.dart';
import 'package:gestorcana_mobile/features/matches/utils/event_cell_utils.dart';

void main() {
  group('activeBlock', () {
    test('returns the whole text when there is no substitution history', () {
      expect(activeBlock('João (ATA) ⚽ 🟨'), 'João (ATA) ⚽ 🟨');
    });

    test('returns only the last "/"-separated block', () {
      expect(activeBlock('João (ATA) ⚽ / Pedro 🟨'), 'Pedro 🟨');
    });
  });

  group('activeTokensText', () {
    test('strips the "nome (pos)" prefix from the active block', () {
      expect(activeTokensText('João (ATA) ⚽ 🟨'), '⚽ 🟨');
    });

    test('cannot separate the name from tokens when the block has no "(pos)" '
        '— returns the block as-is (known mini-language limitation for '
        'post-substitution blocks, which omit the position annotation)', () {
      expect(activeTokensText('João (ATA) ⚽ / Pedro 🟨 🟥'), 'Pedro 🟨 🟥');
    });

    test('returns empty string when no events yet', () {
      expect(activeTokensText('João (ATA) '), '');
      expect(activeTokensText('João (ATA)'), '');
    });
  });

  group('activeTokens', () {
    test('splits the active block into individual tokens', () {
      expect(activeTokens('João (ATA) ⚽ ⚽(C) 🟨'), <String>['⚽', '⚽(C)', '🟨']);
    });

    test('returns an empty list when there are no tokens yet', () {
      expect(activeTokens('João (ATA)'), isEmpty);
    });
  });
}
