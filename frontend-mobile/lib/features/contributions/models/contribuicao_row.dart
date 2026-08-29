import '../../../core/network/json_helpers.dart';

/// Row shape returned by GET /contribuicoes?ano=&busca=, a `List<Object[]>`
/// on the backend: [jogadorId, nomeExibir, jan..dez (12 booleans)].
/// Indices 2..13 are the 12 month-paid flags (Jan=0 .. Dez=11 in [meses]).
class ContribuicaoRow {
  ContribuicaoRow({required this.jogadorId, required this.nomeExibir, required this.meses});

  final int jogadorId;
  final String nomeExibir;

  /// 12 entries, index 0 = Janeiro .. index 11 = Dezembro.
  final List<bool> meses;

  factory ContribuicaoRow.fromObjectArrayRow(dynamic row) {
    final List<dynamic> r = requireRow(row, 14, 'ContribuicaoRow');
    return ContribuicaoRow(
      jogadorId: parseFlexibleInt(r[0]),
      nomeExibir: (r[1] ?? '').toString(),
      meses: List<bool>.generate(12, (int i) => parseFlexibleBool(r[2 + i])),
    );
  }

  bool pagoNoMes(int mesUm) => meses[mesUm - 1];
}
