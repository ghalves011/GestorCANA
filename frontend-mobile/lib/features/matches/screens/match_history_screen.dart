import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/widgets/app_scaffold.dart';
import '../../../core/widgets/confirm_dialog.dart';
import '../../../core/widgets/empty_state.dart';
import '../../../core/widgets/error_view.dart';
import '../../../core/widgets/loading_view.dart';
import '../models/partida.dart';
import '../providers/partida_providers.dart';
import '../providers/season_provider.dart';
import 'match_live_screen.dart';

enum _AcaoPartida { ver, editar, excluir }

/// Mirrors TelaConsultaPartidaView: match history list for a season. Tap a
/// row to open the live scoreboard screen in read-only historical mode; the
/// row menu also offers editing (to fix mistakes) and deleting the match.
class MatchHistoryScreen extends ConsumerWidget {
  const MatchHistoryScreen({super.key});

  void _mostrarErro(BuildContext context, Object e) {
    showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
  }

  Future<void> _abrir(BuildContext context, WidgetRef ref, Partida resumo) async {
    try {
      final Partida completa = await ref.read(partidaRepositoryProvider).obterPorId(resumo.id!);
      if (context.mounted) {
        context.push('/partidas/live', extra: MatchLiveArgs(partida: completa, liveMode: false));
      }
    } catch (e) {
      if (context.mounted) _mostrarErro(context, e);
    }
  }

  Future<void> _editar(BuildContext context, WidgetRef ref, Partida resumo, int temporada) async {
    try {
      final Partida completa = await ref.read(partidaRepositoryProvider).obterPorId(resumo.id!);
      bool semGrid(String? grid) => grid == null || grid.trim().isEmpty || grid.trim() == '[]';
      if (semGrid(completa.gridAzul) && semGrid(completa.gridVermelho)) {
        if (context.mounted) {
          await showMessageDialog(
            context,
            title: 'Edição indisponível',
            message: 'Esta partida não tem a escalação salva (registro antigo) e não pode ser editada.',
          );
        }
        return;
      }
      if (!context.mounted) return;
      final bool? salvou = await context.push<bool>(
        '/partidas/live',
        extra: MatchLiveArgs(partida: completa, liveMode: false, editMode: true),
      );
      if (salvou == true) ref.invalidate(partidasPorAnoProvider(temporada));
    } catch (e) {
      if (context.mounted) _mostrarErro(context, e);
    }
  }

  Future<void> _excluir(BuildContext context, WidgetRef ref, Partida partida, int temporada) async {
    final bool confirmar = await showConfirmDialog(
      context,
      title: 'Excluir partida',
      message: 'Excluir "${partida.nomePartida ?? 'Partida'}" (${partida.dataPartidaFormatada})? Gols, cartões e '
          'presença dela saem das estatísticas. Essa ação não pode ser desfeita.',
      confirmLabel: 'Excluir',
      destructive: true,
    );
    if (!confirmar) return;

    try {
      await ref.read(partidaRepositoryProvider).excluir(partida.id!);
      ref.invalidate(partidasPorAnoProvider(temporada));
    } catch (e) {
      if (context.mounted) _mostrarErro(context, e);
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
                trailing: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: <Widget>[
                    Text(partida.placarFormatado, style: const TextStyle(fontWeight: FontWeight.bold)),
                    PopupMenuButton<_AcaoPartida>(
                      onSelected: (_AcaoPartida acao) {
                        switch (acao) {
                          case _AcaoPartida.ver:
                            _abrir(context, ref, partida);
                          case _AcaoPartida.editar:
                            _editar(context, ref, partida, temporada);
                          case _AcaoPartida.excluir:
                            _excluir(context, ref, partida, temporada);
                        }
                      },
                      itemBuilder: (BuildContext context) => const <PopupMenuEntry<_AcaoPartida>>[
                        PopupMenuItem<_AcaoPartida>(
                          value: _AcaoPartida.ver,
                          child: ListTile(leading: Icon(Icons.visibility_outlined), title: Text('Ver')),
                        ),
                        PopupMenuItem<_AcaoPartida>(
                          value: _AcaoPartida.editar,
                          child: ListTile(leading: Icon(Icons.edit_outlined), title: Text('Editar')),
                        ),
                        PopupMenuItem<_AcaoPartida>(
                          value: _AcaoPartida.excluir,
                          child: ListTile(leading: Icon(Icons.delete_outline), title: Text('Excluir')),
                        ),
                      ],
                    ),
                  ],
                ),
                onTap: () => _abrir(context, ref, partida),
              );
            },
          );
        },
      ),
    );
  }
}
