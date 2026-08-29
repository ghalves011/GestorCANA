import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../data/jogador_repository.dart';
import '../models/jogador.dart';

final Provider<JogadorRepository> jogadorRepositoryProvider = Provider<JogadorRepository>((Ref ref) {
  return JogadorRepository(ref.watch(dioProvider));
});

/// All players (used by: padrinho dropdown, player search, match setup's
/// attendance flow, substitution screen's late-arrival flow).
final FutureProvider<List<Jogador>> todosJogadoresProvider = FutureProvider<List<Jogador>>((Ref ref) {
  return ref.watch(jogadorRepositoryProvider).listar();
});

final jogadoresPorStatusProvider = FutureProvider.family<List<Jogador>, String>((Ref ref, String status) {
  return ref.watch(jogadorRepositoryProvider).listar(status: status);
});

final jogadorPorIdProvider = FutureProvider.family<Jogador, int>((Ref ref, int id) {
  return ref.watch(jogadorRepositoryProvider).obterPorId(id);
});
