import '../../../core/network/json_helpers.dart';

/// Row shape returned by GET /estatisticas?temporada=, a `List<Object[]>` on
/// the backend: [apelidoOuNome, gols(String-int), cartaoAmarelo(String-int),
/// cartaoVermelho(String-int), presencaPercent(String, e.g. "42%")].
class EstatisticaRow {
  EstatisticaRow({
    required this.apelidoOuNome,
    required this.gols,
    required this.cartaoAmarelo,
    required this.cartaoVermelho,
    required this.presencaPercent,
  });

  final String apelidoOuNome;
  final int gols;
  final int cartaoAmarelo;
  final int cartaoVermelho;

  /// Kept as a display string (e.g. "42%") since it's presentational only.
  final String presencaPercent;

  factory EstatisticaRow.fromObjectArrayRow(dynamic row) {
    final List<dynamic> r = requireRow(row, 5, 'EstatisticaRow');
    return EstatisticaRow(
      apelidoOuNome: (r[0] ?? '').toString(),
      gols: parseFlexibleInt(r[1]),
      cartaoAmarelo: parseFlexibleInt(r[2]),
      cartaoVermelho: parseFlexibleInt(r[3]),
      presencaPercent: (r[4] ?? '0%').toString(),
    );
  }
}
