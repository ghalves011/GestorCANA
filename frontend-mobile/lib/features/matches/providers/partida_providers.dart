import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../data/partida_repository.dart';
import '../data/temporada_repository.dart';
import '../models/partida.dart';

final Provider<PartidaRepository> partidaRepositoryProvider = Provider<PartidaRepository>(
  (Ref ref) => PartidaRepository(ref.watch(dioProvider)),
);

final Provider<TemporadaRepository> temporadaRepositoryProvider = Provider<TemporadaRepository>(
  (Ref ref) => TemporadaRepository(ref.watch(dioProvider)),
);

final partidasPorTemporadaProvider = FutureProvider.family<List<Partida>, int?>((Ref ref, int? temporada) {
  return ref.watch(partidaRepositoryProvider).listar(temporada: temporada);
});

final partidaPorIdProvider = FutureProvider.family<Partida, int>((Ref ref, int id) {
  return ref.watch(partidaRepositoryProvider).obterPorId(id);
});
