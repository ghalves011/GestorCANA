import 'package:flutter_test/flutter_test.dart';
import 'package:gestorcana_mobile/features/contributions/models/contribuicao_row.dart';
import 'package:gestorcana_mobile/features/matches/models/grid_historico_row.dart';
import 'package:gestorcana_mobile/features/matches/models/partida.dart';
import 'package:gestorcana_mobile/features/statistics/models/estatistica_row.dart';

void main() {
  group('Partida.fromJson', () {
    test('parses persisted fields and leaves transient lists empty when absent', () {
      final Partida partida = Partida.fromJson(<String, dynamic>{
        'id': 7,
        'temporadaId': 1,
        'dataPartida': '2026-08-15',
        'golsTimeAzul': 3,
        'golsTimeVermelho': '2',
        'nomePartida': 'Racha de sábado',
      });

      expect(partida.id, 7);
      expect(partida.dataPartida, DateTime(2026, 8, 15));
      expect(partida.golsTimeAzul, 3);
      // golsTimeVermelho arrives as a numeric string here — must tolerate.
      expect(partida.golsTimeVermelho, 2);
      expect(partida.placarFormatado, 'AZUL 3 x 2 VERMELHO');
      expect(partida.jogadoresAzul, isEmpty);
      expect(partida.listaGeralPresenca, isEmpty);
    });

    test('hydrates transient jogadoresAzul/jogadoresVermelho when present', () {
      final Partida partida = Partida.fromJson(<String, dynamic>{
        'temporadaId': 1,
        'jogadoresAzul': <Map<String, dynamic>>[
          <String, dynamic>{'id': 1, 'nome': 'A'},
          <String, dynamic>{'id': 2, 'nome': 'B'},
        ],
      });
      expect(partida.jogadoresAzul, hasLength(2));
      expect(partida.jogadoresAzul.first.nome, 'A');
    });
  });

  group('ContribuicaoRow.fromObjectArrayRow', () {
    test('parses a 14-field Object[] row with 12 month booleans', () {
      final List<dynamic> row = <dynamic>[
        10, 'Zico', true, false, 1, 0, true, true, false, false, true, false, true, false,
      ];
      final ContribuicaoRow parsed = ContribuicaoRow.fromObjectArrayRow(row);
      expect(parsed.jogadorId, 10);
      expect(parsed.nomeExibir, 'Zico');
      expect(parsed.meses, hasLength(12));
      expect(parsed.pagoNoMes(1), isTrue);
      expect(parsed.pagoNoMes(2), isFalse);
      expect(parsed.pagoNoMes(3), isTrue); // arrives as int 1
    });

    test('throws a clear FormatException when the row is too short', () {
      expect(() => ContribuicaoRow.fromObjectArrayRow(<dynamic>[1, 'nome']), throwsFormatException);
    });
  });

  group('EstatisticaRow.fromObjectArrayRow', () {
    test('parses string-wrapped ints and keeps presencaPercent as display text', () {
      final EstatisticaRow row = EstatisticaRow.fromObjectArrayRow(<dynamic>['Pelé', '15', '3', '0', '42%']);
      expect(row.apelidoOuNome, 'Pelé');
      expect(row.gols, 15);
      expect(row.cartaoAmarelo, 3);
      expect(row.cartaoVermelho, 0);
      expect(row.presencaPercent, '42%');
    });
  });

  group('GridHistoricoRow.fromObjectArrayRow', () {
    test('parses the 3-field [nomes, pos, eventos] row', () {
      final GridHistoricoRow row = GridHistoricoRow.fromObjectArrayRow(<dynamic>['João', 'ATA', '⚽ 🟨']);
      expect(row.nomes, 'João');
      expect(row.pos, 'ATA');
      expect(row.eventos, '⚽ 🟨');
    });
  });
}
