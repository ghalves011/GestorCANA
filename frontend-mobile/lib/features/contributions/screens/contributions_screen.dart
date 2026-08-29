import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/utils/currency_format_utils.dart';
import '../../../core/widgets/app_scaffold.dart';
import '../../../core/widgets/confirm_dialog.dart';
import '../../../core/widgets/empty_state.dart';
import '../../../core/widgets/error_view.dart';
import '../../../core/widgets/loading_view.dart';
import '../../matches/providers/season_provider.dart';
import '../models/contribuicao_row.dart';
import '../providers/contribuicao_providers.dart';
import '../widgets/month_cell.dart';
import '../widgets/receipt_share_sheet.dart';

/// Mirrors ContribuicaoView: Jan-Dec paid/unpaid matrix per player for a
/// season, with bulk-generate, pay/reverse and receipt-share flows.
class ContributionsScreen extends ConsumerStatefulWidget {
  const ContributionsScreen({super.key});

  @override
  ConsumerState<ContributionsScreen> createState() => _ContributionsScreenState();
}

class _ContributionsScreenState extends ConsumerState<ContributionsScreen> {
  final TextEditingController _searchController = TextEditingController();
  String _busca = '';
  bool _processando = false;

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _gerarLote(int ano) async {
    final bool confirmado = await showConfirmDialog(
      context,
      title: 'Gerar cobranças',
      message: 'Gerar as 12 cobranças mensais de $ano para todos os jogadores (pulando meses já existentes)?',
    );
    if (!confirmado) return;

    setState(() => _processando = true);
    try {
      final String resultado = await ref.read(contribuicaoRepositoryProvider).gerarLote(ano: ano);
      ref.invalidate(contribuicaoMatrizProvider);
      if (mounted) await showMessageDialog(context, title: 'Gerar Lote', message: resultado);
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
      }
    } finally {
      if (mounted) setState(() => _processando = false);
    }
  }

  Future<void> _tocarMes(ContribuicaoRow row, int mesUm, int ano) async {
    final bool pago = row.pagoNoMes(mesUm);

    if (!pago) {
      final bool confirmar = await showConfirmDialog(
        context,
        title: 'Registrar pagamento',
        message: 'Registrar pagamento de ${row.nomeExibir} para ${CurrencyFormatUtils.formatarReferencia(mesUm, ano)}?',
      );
      if (!confirmar) return;

      try {
        await ref.read(contribuicaoRepositoryProvider).registrarPagamento(
              jogadorId: row.jogadorId,
              mes: mesUm,
              ano: ano,
            );
        ref.invalidate(contribuicaoMatrizProvider);
        final String recibo = await ref
            .read(contribuicaoRepositoryProvider)
            .obterRecibo(jogadorId: row.jogadorId, mes: mesUm, ano: ano);
        if (mounted) await showReceiptShareSheet(context, recibo: recibo);
      } catch (e) {
        if (mounted) {
          await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
        }
      }
      return;
    }

    if (!mounted) return;
    final String? acao = await showModalBottomSheet<String>(
      context: context,
      builder: (BuildContext context) {
        return SafeArea(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              ListTile(
                leading: const Icon(Icons.receipt_long),
                title: const Text('Gerar / compartilhar recibo'),
                onTap: () => Navigator.of(context).pop('recibo'),
              ),
              ListTile(
                leading: const Icon(Icons.undo),
                title: const Text('Estornar pagamento'),
                onTap: () => Navigator.of(context).pop('estornar'),
              ),
            ],
          ),
        );
      },
    );
    if (!mounted) return;

    if (acao == 'recibo') {
      try {
        final String recibo = await ref
            .read(contribuicaoRepositoryProvider)
            .obterRecibo(jogadorId: row.jogadorId, mes: mesUm, ano: ano);
        if (mounted) await showReceiptShareSheet(context, recibo: recibo);
      } catch (e) {
        if (mounted) {
          await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
        }
      }
    } else if (acao == 'estornar') {
      final bool confirmar = await showConfirmDialog(
        context,
        title: 'Estornar pagamento',
        message: 'Estornar o pagamento de ${row.nomeExibir} em ${CurrencyFormatUtils.formatarReferencia(mesUm, ano)}?',
        destructive: true,
      );
      if (!confirmar) return;
      try {
        await ref.read(contribuicaoRepositoryProvider).estornarPagamento(jogadorId: row.jogadorId, mes: mesUm, ano: ano);
        ref.invalidate(contribuicaoMatrizProvider);
      } catch (e) {
        if (mounted) {
          await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final int ano = ref.watch(temporadaSelecionadaProvider);
    final ContribuicaoFiltro filtro = ContribuicaoFiltro(ano: ano, busca: _busca);
    final AsyncValue<List<ContribuicaoRow>> async = ref.watch(contribuicaoMatrizProvider(filtro));

    return AppScaffold(
      title: 'Contribuições $ano',
      body: Column(
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.all(12),
            child: Row(
              children: <Widget>[
                Expanded(
                  child: TextField(
                    controller: _searchController,
                    decoration: const InputDecoration(prefixIcon: Icon(Icons.search), hintText: 'Buscar jogador'),
                    onChanged: (String v) => setState(() => _busca = v),
                  ),
                ),
                const SizedBox(width: 8),
                IconButton.filled(
                  tooltip: 'Gerar cobranças do ano',
                  onPressed: _processando ? null : () => _gerarLote(ano),
                  icon: const Icon(Icons.playlist_add),
                ),
              ],
            ),
          ),
          Expanded(
            child: async.when(
              loading: () => const LoadingView(),
              error: (Object error, StackTrace stackTrace) =>
                  ErrorView(error: error, onRetry: () => ref.invalidate(contribuicaoMatrizProvider(filtro))),
              data: (List<ContribuicaoRow> rows) {
                if (rows.isEmpty) {
                  return const EmptyState(message: 'Nenhum jogador encontrado.');
                }
                return ListView.separated(
                  itemCount: rows.length,
                  separatorBuilder: (_, __) => const Divider(height: 1),
                  itemBuilder: (BuildContext context, int index) {
                    final ContribuicaoRow row = rows[index];
                    return Padding(
                      padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 12),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: <Widget>[
                          Text(row.nomeExibir, style: const TextStyle(fontWeight: FontWeight.bold)),
                          const SizedBox(height: 8),
                          Wrap(
                            spacing: 6,
                            runSpacing: 6,
                            children: List<Widget>.generate(12, (int i) {
                              return MonthCell(
                                mesIndex0: i,
                                pago: row.meses[i],
                                onTap: () => _tocarMes(row, i + 1, ano),
                              );
                            }),
                          ),
                        ],
                      ),
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
