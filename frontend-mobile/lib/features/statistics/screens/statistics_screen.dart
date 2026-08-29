import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/widgets/app_scaffold.dart';
import '../../../core/widgets/empty_state.dart';
import '../../../core/widgets/error_view.dart';
import '../../../core/widgets/loading_view.dart';
import '../../matches/providers/season_provider.dart';
import '../models/estatistica_row.dart';
import '../providers/estatistica_providers.dart';

enum _SortColumn { nome, gols, amarelos, vermelhos, presenca }

/// Mirrors TelaEstatisticasView: read-only per-player season stats table.
class StatisticsScreen extends ConsumerStatefulWidget {
  const StatisticsScreen({super.key});

  @override
  ConsumerState<StatisticsScreen> createState() => _StatisticsScreenState();
}

class _StatisticsScreenState extends ConsumerState<StatisticsScreen> {
  _SortColumn _sortColumn = _SortColumn.gols;
  bool _ascending = false;

  List<EstatisticaRow> _sorted(List<EstatisticaRow> rows) {
    final List<EstatisticaRow> copy = List<EstatisticaRow>.of(rows);
    int compare(EstatisticaRow a, EstatisticaRow b) {
      switch (_sortColumn) {
        case _SortColumn.nome:
          return a.apelidoOuNome.toLowerCase().compareTo(b.apelidoOuNome.toLowerCase());
        case _SortColumn.gols:
          return a.gols.compareTo(b.gols);
        case _SortColumn.amarelos:
          return a.cartaoAmarelo.compareTo(b.cartaoAmarelo);
        case _SortColumn.vermelhos:
          return a.cartaoVermelho.compareTo(b.cartaoVermelho);
        case _SortColumn.presenca:
          final int pa = int.tryParse(a.presencaPercent.replaceAll('%', '')) ?? 0;
          final int pb = int.tryParse(b.presencaPercent.replaceAll('%', '')) ?? 0;
          return pa.compareTo(pb);
      }
    }

    copy.sort((EstatisticaRow a, EstatisticaRow b) => _ascending ? compare(a, b) : compare(b, a));
    return copy;
  }

  void _onSort(_SortColumn column) {
    setState(() {
      if (_sortColumn == column) {
        _ascending = !_ascending;
      } else {
        _sortColumn = column;
        _ascending = false;
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final int temporada = ref.watch(temporadaSelecionadaProvider);
    final AsyncValue<List<EstatisticaRow>> async = ref.watch(estatisticasPorTemporadaProvider(temporada));

    return AppScaffold(
      title: 'Estatísticas $temporada',
      body: async.when(
        loading: () => const LoadingView(),
        error: (Object error, StackTrace stackTrace) => ErrorView(
          error: error,
          onRetry: () => ref.invalidate(estatisticasPorTemporadaProvider(temporada)),
        ),
        data: (List<EstatisticaRow> rows) {
          if (rows.isEmpty) {
            return const EmptyState(message: 'Nenhuma estatística disponível para esta temporada.');
          }
          final List<EstatisticaRow> sorted = _sorted(rows);
          return SingleChildScrollView(
            child: Scrollbar(
              thumbVisibility: true,
              child: SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: DataTable(
                  columnSpacing: 20,
                  horizontalMargin: 12,
                  sortColumnIndex: _SortColumn.values.indexOf(_sortColumn),
                  sortAscending: _ascending,
                  columns: <DataColumn>[
                    DataColumn(label: const Text('Apelido'), onSort: (_, __) => _onSort(_SortColumn.nome)),
                    DataColumn(
                      label: const Text('Gols'),
                      numeric: true,
                      onSort: (_, __) => _onSort(_SortColumn.gols),
                    ),
                    DataColumn(
                      label: const Text('C.A.'),
                      numeric: true,
                      onSort: (_, __) => _onSort(_SortColumn.amarelos),
                    ),
                    DataColumn(
                      label: const Text('C.V.'),
                      numeric: true,
                      onSort: (_, __) => _onSort(_SortColumn.vermelhos),
                    ),
                    DataColumn(
                      label: const Text('Presença'),
                      numeric: true,
                      onSort: (_, __) => _onSort(_SortColumn.presenca),
                    ),
                  ],
                  rows: sorted.map((EstatisticaRow row) {
                    return DataRow(
                      cells: <DataCell>[
                        DataCell(Text(row.apelidoOuNome)),
                        DataCell(Text(row.gols.toString())),
                        DataCell(_CardBadge(count: row.cartaoAmarelo, color: const Color(0xFFFFCC00))),
                        DataCell(_CardBadge(count: row.cartaoVermelho, color: const Color(0xFF8B0000))),
                        DataCell(Text(row.presencaPercent)),
                      ],
                    );
                  }).toList(),
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}

class _CardBadge extends StatelessWidget {
  const _CardBadge({required this.count, required this.color});

  final int count;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        Container(width: 14, height: 18, color: color),
        const SizedBox(width: 6),
        Text(count.toString()),
      ],
    );
  }
}
