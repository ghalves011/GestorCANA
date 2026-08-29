import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../data/partida_repository.dart';
import '../data/temporada_repository.dart';
import '../models/partida.dart';
import '../models/temporada.dart';

final Provider<PartidaRepository> partidaRepositoryProvider = Provider<PartidaRepository>(
  (Ref ref) => PartidaRepository(ref.watch(dioProvider)),
);

final Provider<TemporadaRepository> temporadaRepositoryProvider = Provider<TemporadaRepository>(
  (Ref ref) => TemporadaRepository(ref.watch(dioProvider)),
);

/// GET /partidas filters by Temporada.id (an independent DB primary key),
/// not by the calendar year — so match history has to resolve the
/// selected year to its real Temporada.id first (same lookup
/// match_setup_screen does before sortear/finalizar), or every match ever
/// saved comes back missing since the year and the id rarely coincide.
final partidasPorAnoProvider = FutureProvider.family<List<Partida>, int>((Ref ref, int ano) async {
  final List<Temporada> temporadas = await ref.watch(temporadaRepositoryProvider).listar();
  Temporada? temporadaDoAno;
  for (final Temporada t in temporadas) {
    if (t.ano == ano) {
      temporadaDoAno = t;
      break;
    }
  }
  if (temporadaDoAno?.id == null) return <Partida>[];
  return ref.watch(partidaRepositoryProvider).listar(temporada: temporadaDoAno!.id);
});

final partidaPorIdProvider = FutureProvider.family<Partida, int>((Ref ref, int id) {
  return ref.watch(partidaRepositoryProvider).obterPorId(id);
});
