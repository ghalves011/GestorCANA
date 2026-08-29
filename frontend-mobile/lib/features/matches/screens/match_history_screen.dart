import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/app_scaffold.dart';
import '../../../core/widgets/empty_state.dart';
import '../../../core/widgets/error_view.dart';
import '../../../core/widgets/loading_view.dart';
import '../models/partida.dart';
import '../providers/partida_providers.dart';
import '../providers/season_provider.dart';
import 'match_live_screen.dart';

/// Mirrors TelaConsultaPartidaView: match history list for a season. Tap a
/// row to open the live scoreboard screen in read-only historical mode.
class MatchHistoryScreen extends ConsumerWidget {
  const MatchHistoryScreen({super.key});

  Future<void> _abrir(BuildContext context, WidgetRef ref, Partida resumo) async {
    try {
      final Partida completa = await ref.read(partidaRepositoryProvider).obterPorId(resumo.id!);
      if (context.mounted) {
        context.push('/partidas/live', extra: MatchLiveArgs(partida: completa, liveMode: false));
      }
    } catch (e) {
      if (context.mounted) {
        showDialog<void>(
          context: context,
          builder: (BuildContext context) => AlertDialog(
            title: const Text('Erro'),
            content: Text(e is ApiException ? e.message : e.toString()),
            actions: <Widget>[
              TextButton(onPressed: () => Navigator.of(context).pop(), child: const Text('OK')),
            ],
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final int temporada = ref.watch(temporadaSelecionadaProvider);
    final AsyncValue<List<Partida>> async = ref.watch(partidasPorAnoProvider(temporada));

    return AppScaffold(
      title: 'Partidas $temporada',
      body: async.when(
        loading: () => const LoadingView(),
        error: (Object error, StackTrace stackTrace) =>
            ErrorView(error: error, onRetry: () => ref.invalidate(partidasPorAnoProvider(temporada))),
        data: (List<Partida> partidas) {
          if (partidas.isEmpty) {
            return const EmptyState(message: 'Nenhuma partida registrada nesta temporada.');
          }
          final List<Partida> ordenadas = List<Partida>.of(partidas)
            ..sort((Partida a, Partida b) => (b.dataPartida ?? DateTime(0)).compareTo(a.dataPartida ?? DateTime(0)));

          return ListView.separated(
            itemCount: ordenadas.length,
            separatorBuilder: (_, __) => const Divider(height: 1),
            itemBuilder: (BuildContext context, int index) {
              final Partida partida = ordenadas[index];
              return ListTile(
                title: Text(partida.nomePartida?.isNotEmpty == true ? partida.nomePartida! : 'Partida'),
                subtitle: Text(partida.dataPartidaFormatada),
                trailing: Text(partida.placarFormatado, style: const TextStyle(fontWeight: FontWeight.bold)),
                onTap: () => _abrir(context, ref, partida),
              );
            },
          );
        },
      ),
    );
  }
}
