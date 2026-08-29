import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../data/estatistica_repository.dart';
import '../models/estatistica_row.dart';

final Provider<EstatisticaRepository> estatisticaRepositoryProvider = Provider<EstatisticaRepository>(
  (Ref ref) => EstatisticaRepository(ref.watch(dioProvider)),
);

final estatisticasPorTemporadaProvider = FutureProvider.family<List<EstatisticaRow>, int>((Ref ref, int temporada) {
  return ref.watch(estatisticaRepositoryProvider).listar(temporada: temporada);
});
