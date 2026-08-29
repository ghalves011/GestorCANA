import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_client.dart';
import '../data/contribuicao_repository.dart';
import '../models/contribuicao_row.dart';

final Provider<ContribuicaoRepository> contribuicaoRepositoryProvider = Provider<ContribuicaoRepository>(
  (Ref ref) => ContribuicaoRepository(ref.watch(dioProvider)),
);

class ContribuicaoFiltro {
  const ContribuicaoFiltro({required this.ano, this.busca = ''});

  final int ano;
  final String busca;

  @override
  bool operator ==(Object other) => other is ContribuicaoFiltro && other.ano == ano && other.busca == busca;

  @override
  int get hashCode => Object.hash(ano, busca);
}

final contribuicaoMatrizProvider =
    FutureProvider.family<List<ContribuicaoRow>, ContribuicaoFiltro>((Ref ref, ContribuicaoFiltro filtro) {
  return ref.watch(contribuicaoRepositoryProvider).listarMatriz(ano: filtro.ano, busca: filtro.busca);
});
