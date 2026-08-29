import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/utils/cpf_utils.dart';
import '../../../core/utils/player_filter_utils.dart';
import '../../../core/widgets/empty_state.dart';
import '../../../core/widgets/error_view.dart';
import '../../../core/widgets/loading_view.dart';
import '../models/jogador.dart';
import '../providers/jogador_providers.dart';

/// Mirrors DialogBuscaView: search across all players (nome/apelido/cpf,
/// case-insensitive substring, local — see player_filter_utils.dart) and
/// pop the selected one back to the caller (typically the player form's
/// "Pesquisa" action).
class PlayerSearchScreen extends ConsumerStatefulWidget {
  const PlayerSearchScreen({super.key});

  @override
  ConsumerState<PlayerSearchScreen> createState() => _PlayerSearchScreenState();
}

class _PlayerSearchScreenState extends ConsumerState<PlayerSearchScreen> {
  final TextEditingController _searchController = TextEditingController();
  String _query = '';

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final AsyncValue<List<Jogador>> async = ref.watch(todosJogadoresProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Pesquisar Jogador')),
      body: Column(
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.all(12),
            child: TextField(
              controller: _searchController,
              autofocus: true,
              decoration: const InputDecoration(
                prefixIcon: Icon(Icons.search),
                hintText: 'Nome, apelido ou CPF',
              ),
              onChanged: (String value) => setState(() => _query = value),
            ),
          ),
          Expanded(
            child: async.when(
              loading: () => const LoadingView(),
              error: (Object error, StackTrace stackTrace) =>
                  ErrorView(error: error, onRetry: () => ref.invalidate(todosJogadoresProvider)),
              data: (List<Jogador> jogadores) {
                final List<Jogador> filtrados = filterBySubstring<Jogador>(
                  jogadores,
                  _query,
                  <String Function(Jogador)>[
                    (Jogador j) => j.nome,
                    (Jogador j) => j.apelido ?? '',
                    (Jogador j) => j.cpf ?? '',
                  ],
                )..sort((Jogador a, Jogador b) => a.nomeExibir.toLowerCase().compareTo(b.nomeExibir.toLowerCase()));

                if (filtrados.isEmpty) {
                  return const EmptyState(message: 'Nenhum jogador encontrado.');
                }

                return ListView.separated(
                  itemCount: filtrados.length,
                  separatorBuilder: (_, __) => const Divider(height: 1),
                  itemBuilder: (BuildContext context, int index) {
                    final Jogador jogador = filtrados[index];
                    return ListTile(
                      title: Text(jogador.nomeExibir),
                      subtitle: Text(
                        <String>[
                          if (jogador.nome != jogador.nomeExibir) jogador.nome,
                          if ((jogador.cpf ?? '').isNotEmpty) CpfUtils.mask(jogador.cpf!),
                        ].join(' · '),
                      ),
                      trailing: jogador.estaSuspenso
                          ? const Chip(label: Text('Suspenso'), visualDensity: VisualDensity.compact)
                          : null,
                      onTap: () => Navigator.of(context).pop(jogador),
                    );
                  },
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
