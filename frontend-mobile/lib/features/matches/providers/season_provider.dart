import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../models/temporada.dart';
import 'partida_providers.dart';

/// All seasons, via GET /temporadas — used to populate a real season
/// selector (the desktop app hardcoded 2026 in its UI instead; the mobile
/// app does this properly since it's cheap and more correct, per plan §5).
final FutureProvider<List<Temporada>> temporadasProvider = FutureProvider<List<Temporada>>((Ref ref) {
  return ref.watch(temporadaRepositoryProvider).listar();
});

/// Currently-selected season year across the Contribuições/Estatísticas/
/// Partidas screens. Defaults to the current calendar year; screens that
/// load /temporadas can offer a picker that updates this.
final StateProvider<int> temporadaSelecionadaProvider = StateProvider<int>(
  (Ref ref) => DateTime.now().year,
);
