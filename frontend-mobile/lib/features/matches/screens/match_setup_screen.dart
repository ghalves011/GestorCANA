import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/utils/date_format_utils.dart';
import '../../../core/widgets/app_scaffold.dart';
import '../../../core/widgets/confirm_dialog.dart';
import '../../players/models/jogador.dart';
import '../../players/providers/jogador_providers.dart';
import '../../contributions/providers/contribuicao_providers.dart';
import '../models/partida.dart';
import '../models/temporada.dart';
import '../providers/partida_providers.dart';
import '../providers/season_provider.dart';
import 'attendance_selection_screen.dart';
import 'match_live_screen.dart';

const List<String> kFormacoes = <String>['4-4-2', '4-3-3', '3-5-2', '4-5-1', '3-4-3', '5-3-2'];

/// Mirrors TelaGeracaoPartidaView: new-match setup (name, date, formation
/// per team, attendance selection) then triggers the server-side draw.
class MatchSetupScreen extends ConsumerStatefulWidget {
  const MatchSetupScreen({super.key});

  @override
  ConsumerState<MatchSetupScreen> createState() => _MatchSetupScreenState();
}

class _MatchSetupScreenState extends ConsumerState<MatchSetupScreen> {
  final TextEditingController _nomeController = TextEditingController();
  final TextEditingController _dataController = TextEditingController();
  String _formacaoAzul = kFormacoes.first;
  String _formacaoVermelho = kFormacoes.first;
  List<Jogador> _selecionados = <Jogador>[];
  bool _carregandoJogadores = false;
  bool _sorteando = false;

  @override
  void dispose() {
    _nomeController.dispose();
    _dataController.dispose();
    super.dispose();
  }

  Future<void> _selecionarJogadores() async {
    setState(() => _carregandoJogadores = true);
    try {
      final List<Jogador> todos = await ref.read(jogadorRepositoryProvider).listar();
      final List<Jogador> elegiveis = todos.where((Jogador j) => j.status == 'Ativo' || j.status == 'Suspenso').toList()
        ..sort((Jogador a, Jogador b) => a.nomeExibir.toLowerCase().compareTo(b.nomeExibir.toLowerCase()));

      final List<bool> podemJogar = await Future.wait(
        elegiveis.map((Jogador j) => j.id == null
            ? Future<bool>.value(true)
            : ref.read(contribuicaoRepositoryProvider).podeJogar(j.id!).catchError((_) => true)),
      );

      final Set<int> devedores = <int>{
        for (int i = 0; i < elegiveis.length; i++)
          if (!podemJogar[i] && elegiveis[i].id != null) elegiveis[i].id!,
      };

      if (!mounted) return;
      final AttendanceSelectionResult? resultado = await Navigator.of(context).push<AttendanceSelectionResult>(
        MaterialPageRoute<AttendanceSelectionResult>(
          builder: (_) => AttendanceSelectionScreen(
            args: AttendanceSelectionArgs(
              jogadores: elegiveis,
              debtorIds: devedores,
              preSelecionados: _selecionados,
            ),
          ),
        ),
      );

      if (resultado != null && resultado.confirmado) {
        setState(() => _selecionados = resultado.selecionados);
      }
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
      }
    } finally {
      if (mounted) setState(() => _carregandoJogadores = false);
    }
  }

  Future<void> _sortearEGerar() async {
    if (_nomeController.text.trim().isEmpty) {
      await showMessageDialog(context, title: 'Verifique os campos', message: 'Informe o nome da partida.');
      return;
    }
    final DateTime? data = DateFormatUtils.ler(_dataController.text);
    if (data == null) {
      await showMessageDialog(context, title: 'Verifique os campos', message: 'Informe a data (dd/mm/aaaa).');
      return;
    }
    if (_selecionados.isEmpty) {
      await showMessageDialog(
        context,
        title: 'Verifique os campos',
        message: 'Selecione ao menos um jogador presente.',
      );
      return;
    }

    setState(() => _sorteando = true);
    try {
      final int ano = ref.read(temporadaSelecionadaProvider);
      final List<Temporada> temporadas = await ref.read(temporadaRepositoryProvider).listar();
      Temporada? temporadaAtual;
      for (final Temporada t in temporadas) {
        if (t.ano == ano) {
          temporadaAtual = t;
          break;
        }
      }
      if (temporadaAtual == null || temporadaAtual.id == null) {
        if (mounted) {
          await showMessageDialog(
            context,
            title: 'Temporada não encontrada',
            message: 'Não existe uma temporada cadastrada para o ano $ano. Cadastre-a antes de criar a partida.',
          );
        }
        return;
      }

      final Partida partida = Partida.novo(temporadaId: temporadaAtual.id!)
        ..nomePartida = _nomeController.text.trim()
        ..dataPartida = data
        ..formacaoAzul = _formacaoAzul
        ..formacaoVermelho = _formacaoVermelho;

      final Partida sorteada =
          await ref.read(partidaRepositoryProvider).sortear(partida: partida, jogadores: _selecionados);

      if (mounted) {
        context.push('/partidas/live', extra: MatchLiveArgs(partida: sorteada, liveMode: true));
      }
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro ao sortear', message: e is ApiException ? e.message : e.toString());
      }
    } finally {
      if (mounted) setState(() => _sorteando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return AppScaffold(
      title: 'Criar Partida',
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: <Widget>[
          TextField(
            controller: _nomeController,
            decoration: const InputDecoration(labelText: 'Nome da partida'),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _dataController,
            keyboardType: TextInputType.datetime,
            decoration: const InputDecoration(labelText: 'Data (dd/mm/aaaa)'),
          ),
          const SizedBox(height: 20),
          Row(
            children: <Widget>[
              Expanded(
                child: DropdownButtonFormField<String>(
                  initialValue: _formacaoAzul,
                  decoration: const InputDecoration(labelText: 'Formação Azul'),
                  items: kFormacoes.map((String f) => DropdownMenuItem<String>(value: f, child: Text(f))).toList(),
                  onChanged: (String? v) => setState(() => _formacaoAzul = v ?? _formacaoAzul),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: DropdownButtonFormField<String>(
                  initialValue: _formacaoVermelho,
                  decoration: const InputDecoration(labelText: 'Formação Vermelho'),
                  items: kFormacoes.map((String f) => DropdownMenuItem<String>(value: f, child: Text(f))).toList(),
                  onChanged: (String? v) => setState(() => _formacaoVermelho = v ?? _formacaoVermelho),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          OutlinedButton.icon(
            onPressed: _carregandoJogadores ? null : _selecionarJogadores,
            icon: _carregandoJogadores
                ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2))
                : const Icon(Icons.people_outline),
            label: Text('SELECIONAR JOGADORES PRESENTES (${_selecionados.length})'),
          ),
          const SizedBox(height: 32),
          ElevatedButton(
            onPressed: _sorteando ? null : _sortearEGerar,
            child: _sorteando
                ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                : const Text('SORTEAR E GERAR'),
          ),
        ],
      ),
    );
  }
}
