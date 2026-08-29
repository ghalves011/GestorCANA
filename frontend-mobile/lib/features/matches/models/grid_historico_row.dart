import '../../../core/network/json_helpers.dart';

/// Row shape returned by POST /partidas/grid-historico?time=Azul|Vermelho,
/// a `List<Object[]>` on the backend: [nomes, pos, eventos].
class GridHistoricoRow {
  GridHistoricoRow({required this.nomes, required this.pos, required this.eventos});

  final String nomes;
  final String pos;
  final String eventos;

  factory GridHistoricoRow.fromObjectArrayRow(dynamic row) {
    final List<dynamic> r = requireRow(row, 3, 'GridHistoricoRow');
    return GridHistoricoRow(
      nomes: (r[0] ?? '').toString(),
      pos: (r[1] ?? '').toString(),
      eventos: (r[2] ?? '').toString(),
    );
  }
}
