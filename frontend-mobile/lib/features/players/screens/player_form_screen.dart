import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/utils/cpf_utils.dart';
import '../../../core/utils/date_format_utils.dart';
import '../../../core/utils/phone_utils.dart';
import '../../../core/utils/rg_utils.dart';
import '../../../core/widgets/confirm_dialog.dart';
import '../../../core/widgets/loading_view.dart';
import '../../contributions/providers/contribuicao_providers.dart';
import '../models/jogador.dart';
import '../providers/jogador_providers.dart';
import '../widgets/player_form_controllers.dart';
import '../widgets/player_personal_tab.dart';
import '../widgets/player_sports_tab.dart';
import 'player_search_screen.dart';

/// Mirrors FormJogadorView: full player CRUD form with two tabs. Fields
/// start locked when editing an existing player (matches desktop's
/// bloquearCampos(true) on load) and unlocked immediately when creating a
/// new one or after a search selection (also matching desktop behavior).
class PlayerFormScreen extends ConsumerStatefulWidget {
  const PlayerFormScreen({super.key, this.jogadorId});

  final int? jogadorId;

  @override
  ConsumerState<PlayerFormScreen> createState() => _PlayerFormScreenState();
}

class _PlayerFormScreenState extends ConsumerState<PlayerFormScreen> with SingleTickerProviderStateMixin {
  late final TabController _tabController;
  final PlayerFormControllers _controllers = PlayerFormControllers();

  Jogador _jogadorAtual = Jogador.novo();
  Jogador? _snapshotParaCancelar;
  bool _locked = false;
  bool _loadingInicial = false;
  bool _salvando = false;
  String? _posicao;
  String? _peDominante;
  int? _padrinhoId;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);

    if (widget.jogadorId != null) {
      _locked = true;
      _carregarJogador(widget.jogadorId!);
    } else {
      _preencherFormComJogador(Jogador.novo());
    }
  }

  @override
  void dispose() {
    _tabController.dispose();
    _controllers.dispose();
    super.dispose();
  }

  void _preencherFormComJogador(Jogador jogador) {
    _jogadorAtual = jogador;
    _controllers.preencherDe(jogador);
    // Normaliza posicao/peDominante antes de alimentar os dropdowns: registros
    // antigos (criados pelo desktop) usam grafia/vocabulário diferente
    // ("Meia" vs "MEIA", "Destro" vs "DIREITO") e um valor sem correspondência
    // exata em kPosicoes/kPesDominantes derruba o DropdownButtonFormField.
    _posicao = normalizarValorDropdown(jogador.posicao, kPosicoes);
    _peDominante = normalizarValorDropdown(jogador.peDominante, kPesDominantes, kPeDominanteSinonimos);
    _padrinhoId = jogador.padrinhoId;
  }

  Future<void> _carregarJogador(int id) async {
    setState(() => _loadingInicial = true);
    try {
      final Jogador jogador = await ref.read(jogadorRepositoryProvider).obterPorId(id);
      _preencherFormComJogador(jogador);
      _snapshotParaCancelar = jogador;
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
      }
    } finally {
      if (mounted) setState(() => _loadingInicial = false);
    }
  }

  void _novo() {
    setState(() {
      _preencherFormComJogador(Jogador.novo());
      _snapshotParaCancelar = null;
      _locked = false;
    });
  }

  void _alterar() {
    setState(() => _locked = false);
  }

  void _cancelar() {
    setState(() {
      if (_snapshotParaCancelar != null) {
        _preencherFormComJogador(_snapshotParaCancelar!);
      } else {
        _preencherFormComJogador(Jogador.novo());
      }
      _locked = true;
    });
  }

  Future<void> _pesquisar() async {
    final Jogador? selecionado = await Navigator.of(context).push<Jogador>(
      MaterialPageRoute<Jogador>(builder: (_) => const PlayerSearchScreen()),
    );
    if (selecionado != null) {
      setState(() {
        _preencherFormComJogador(selecionado);
        _snapshotParaCancelar = selecionado;
        _locked = false;
      });
    }
  }

  Future<void> _excluir() async {
    final int? id = _jogadorAtual.id;
    if (id == null) return;
    final bool confirmado = await showConfirmDialog(
      context,
      title: 'Excluir jogador',
      message: 'Tem certeza que deseja excluir ${_jogadorAtual.nomeExibir}?',
      destructive: true,
    );
    if (!confirmado) return;

    try {
      final String resultado = await ref.read(jogadorRepositoryProvider).excluir(id);
      ref.invalidate(todosJogadoresProvider);
      ref.invalidate(contribuicaoMatrizProvider);
      if (!mounted) return;
      await showMessageDialog(context, title: 'Exclusão', message: resultado);
      if (!mounted) return;
      Navigator.of(context).maybePop();
    } catch (e) {
      if (mounted) {
        await showMessageDialog(context, title: 'Erro', message: e is ApiException ? e.message : e.toString());
      }
    }
  }

  List<String> _validarLocalmente() {
    final List<String> erros = <String>[];
    if (_controllers.nome.text.trim().isEmpty) erros.add('Nome é obrigatório.');
    if (_controllers.apelido.text.trim().isEmpty) erros.add('Apelido é obrigatório.');
    if (!RgUtils.isValid(_controllers.rg.text)) erros.add('RG deve ter entre 7 e 9 dígitos.');
    if (!CpfUtils.isValid(_controllers.cpf.text)) erros.add('CPF deve ter 11 dígitos.');
    if (!PhoneUtils.isValid(_controllers.telefone.text)) erros.add('Telefone deve ter 10 ou 11 dígitos.');
    if (DateFormatUtils.ler(_controllers.dataNascimento.text) == null) {
      erros.add('Data de nascimento é obrigatória (dd/mm/aaaa).');
    }
    final int nivel = int.tryParse(_controllers.nivel.text.trim()) ?? -1;
    if (nivel < 1 || nivel > 100) erros.add('Nível técnico deve estar entre 1 e 100.');
    if (_controllers.logradouro.text.trim().isEmpty) erros.add('Logradouro é obrigatório.');
    if (_controllers.bairro.text.trim().isEmpty) erros.add('Bairro é obrigatório.');
    if (_controllers.cidade.text.trim().isEmpty) erros.add('Cidade é obrigatória.');
    if (_controllers.estado.text.trim().isEmpty) erros.add('Estado (UF) é obrigatório.');
    if (_controllers.cep.text.trim().isEmpty) erros.add('CEP é obrigatório.');
    if (_padrinhoId != null && _padrinhoId == _jogadorAtual.id) {
      erros.add('Um jogador não pode ser padrinho de si mesmo.');
    }
    return erros;
  }

  Future<void> _gravar() async {
    final List<String> erros = _validarLocalmente();
    if (erros.isNotEmpty) {
      await showMessageDialog(context, title: 'Verifique os campos', message: erros.join('\n'));
      return;
    }

    final Jogador jogador = _controllers.escreverEm(_jogadorAtual)
      ..posicao = _posicao
      ..peDominante = _peDominante
      ..padrinhoId = _padrinhoId;

    setState(() => _salvando = true);
    try {
      final Jogador salvo = jogador.id == null
          ? await ref.read(jogadorRepositoryProvider).criar(jogador)
          : await ref.read(jogadorRepositoryProvider).atualizar(jogador.id!, jogador);

      ref.invalidate(todosJogadoresProvider);
      ref.invalidate(contribuicaoMatrizProvider);

      setState(() {
        _preencherFormComJogador(salvo);
        _snapshotParaCancelar = salvo;
        _locked = true;
      });

      if (mounted) {
        await showMessageDialog(context, title: 'Sucesso', message: 'Jogador salvo com sucesso.');
      }
    } catch (e) {
      if (mounted) {
        await showMessageDialog(
          context,
          title: 'Não foi possível salvar',
          message: e is ApiException ? e.message : e.toString(),
        );
      }
    } finally {
      if (mounted) setState(() => _salvando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final AsyncValue<List<Jogador>> todosAsync = ref.watch(todosJogadoresProvider);

    return Scaffold(
      appBar: AppBar(
        title: Text(_jogadorAtual.id == null ? 'Novo Jogador' : _jogadorAtual.nomeExibir),
        actions: <Widget>[
          IconButton(icon: const Icon(Icons.search), tooltip: 'Pesquisa', onPressed: _pesquisar),
          if (!_locked)
            IconButton(icon: const Icon(Icons.add), tooltip: 'Novo', onPressed: _novo),
          if (_locked)
            IconButton(icon: const Icon(Icons.edit), tooltip: 'Alterar', onPressed: _alterar),
          if (_jogadorAtual.id != null)
            IconButton(icon: const Icon(Icons.delete_outline), tooltip: 'Excluir', onPressed: _excluir),
        ],
        bottom: TabBar(
          controller: _tabController,
          tabs: const <Tab>[
            Tab(text: 'Infos Pessoais'),
            Tab(text: 'Infos Esportivas'),
          ],
        ),
      ),
      body: _loadingInicial
          ? const LoadingView()
          : TabBarView(
              controller: _tabController,
              children: <Widget>[
                PlayerPersonalTab(controllers: _controllers, enabled: !_locked),
                todosAsync.when(
                  loading: () => const LoadingView(),
                  error: (Object error, StackTrace stackTrace) => PlayerSportsTab(
                    controllers: _controllers,
                    enabled: !_locked,
                    posicao: _posicao,
                    onPosicaoChanged: (String? v) => setState(() => _posicao = v),
                    peDominante: _peDominante,
                    onPeDominanteChanged: (String? v) => setState(() => _peDominante = v),
                    jogadores: const <Jogador>[],
                    currentPlayerId: _jogadorAtual.id,
                    padrinhoId: _padrinhoId,
                    onPadrinhoChanged: (int? v) => setState(() => _padrinhoId = v),
                  ),
                  data: (List<Jogador> todos) => PlayerSportsTab(
                    controllers: _controllers,
                    enabled: !_locked,
                    posicao: _posicao,
                    onPosicaoChanged: (String? v) => setState(() => _posicao = v),
                    peDominante: _peDominante,
                    onPeDominanteChanged: (String? v) => setState(() => _peDominante = v),
                    jogadores: todos,
                    currentPlayerId: _jogadorAtual.id,
                    padrinhoId: _padrinhoId,
                    onPadrinhoChanged: (int? v) => setState(() => _padrinhoId = v),
                  ),
                ),
              ],
            ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: <Widget>[
              if (!_locked) ...<Widget>[
                Expanded(
                  child: OutlinedButton(onPressed: _salvando ? null : _cancelar, child: const Text('Cancelar')),
                ),
                const SizedBox(width: 12),
                Expanded(
                  flex: 2,
                  child: ElevatedButton(
                    onPressed: _salvando ? null : _gravar,
                    child: _salvando
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : const Text('GRAVAR'),
                  ),
                ),
              ] else
                const Expanded(
                  child: Text(
                    'Toque em Alterar para editar, ou Pesquisa para carregar outro jogador.',
                    textAlign: TextAlign.center,
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
