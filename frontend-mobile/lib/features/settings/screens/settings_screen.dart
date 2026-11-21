import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_config.dart';
import '../../../core/network/api_client.dart';
import '../../../core/widgets/app_scaffold.dart';
import '../../../core/widgets/confirm_dialog.dart';

/// New screen (not present on the desktop client) that lets the backend
/// base URL be changed at runtime without a rebuild — added because the
/// production URL isn't decided yet (see plan §3). Also documents the
/// common "localhost means the device, not your PC" trap.
class SettingsScreen extends ConsumerStatefulWidget {
  const SettingsScreen({super.key});

  @override
  ConsumerState<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends ConsumerState<SettingsScreen> {
  late final TextEditingController _controller;
  late final TextEditingController _chaveController;
  bool _mostrarChave = false;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: ref.read(baseUrlProvider));
    _chaveController = TextEditingController(text: ref.read(apiKeyProvider));
  }

  @override
  void dispose() {
    _controller.dispose();
    _chaveController.dispose();
    super.dispose();
  }

  Future<void> _salvarChave() async {
    final String chave = _chaveController.text.trim();
    await AppConfig.setApiKey(chave);
    ref.read(apiKeyProvider.notifier).state = chave;
    if (mounted) {
      await showMessageDialog(
        context,
        title: 'Configurações',
        message: chave.isEmpty ? 'Chave de acesso removida.' : 'Chave de acesso salva.',
      );
    }
  }

  Future<void> _salvar() async {
    final String url = _controller.text.trim();
    if (url.isEmpty) return;
    await AppConfig.setBaseUrl(url);
    ref.read(baseUrlProvider.notifier).state = url;
    if (mounted) {
      await showMessageDialog(context, title: 'Configurações', message: 'Endereço do servidor atualizado.');
    }
  }

  Future<void> _restaurarPadrao() async {
    await AppConfig.resetBaseUrl();
    _controller.text = AppConfig.defaultBaseUrl;
    ref.read(baseUrlProvider.notifier).state = AppConfig.defaultBaseUrl;
  }

  @override
  Widget build(BuildContext context) {
    return AppScaffold(
      title: 'Configurações',
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: <Widget>[
          Text('Endereço do servidor (API)', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          TextField(
            controller: _controller,
            keyboardType: TextInputType.url,
            decoration: const InputDecoration(hintText: 'http://10.0.2.2:8080'),
          ),
          const SizedBox(height: 12),
          Row(
            children: <Widget>[
              Expanded(
                child: ElevatedButton(onPressed: _salvar, child: const Text('Salvar')),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: OutlinedButton(onPressed: _restaurarPadrao, child: const Text('Padrão')),
              ),
            ],
          ),
          const SizedBox(height: 24),
          Text('Chave de acesso', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 4),
          Text(
            'Exigida para excluir registros e para virar a temporada. Deixe vazio e salve para remover.',
            style: Theme.of(context).textTheme.bodySmall,
          ),
          const SizedBox(height: 8),
          TextField(
            controller: _chaveController,
            obscureText: !_mostrarChave,
            autocorrect: false,
            enableSuggestions: false,
            decoration: InputDecoration(
              hintText: 'Cole a chave aqui',
              suffixIcon: IconButton(
                tooltip: _mostrarChave ? 'Ocultar' : 'Mostrar',
                icon: Icon(_mostrarChave ? Icons.visibility_off : Icons.visibility),
                onPressed: () => setState(() => _mostrarChave = !_mostrarChave),
              ),
            ),
          ),
          const SizedBox(height: 12),
          ElevatedButton(onPressed: _salvarChave, child: const Text('Salvar chave')),
          const SizedBox(height: 24),
          const Divider(),
          const SizedBox(height: 12),
          Text('Dicas', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          const _Dica(
            title: 'Emulador Android',
            text: 'Use 10.0.2.2 no lugar de "localhost" — é o endereço que o emulador usa para '
                'chegar até o seu computador.',
          ),
          const _Dica(
            title: 'Celular físico (mesma rede Wi-Fi)',
            text: 'Use o IP local do seu computador na rede (ex: 192.168.0.10:8080), com o backend '
                'escutando em 0.0.0.0, não em 127.0.0.1.',
          ),
          const _Dica(
            title: 'Simulador iOS',
            text: '"localhost:8080" funciona normalmente, pois o simulador compartilha a rede do Mac.',
          ),
          const _Dica(
            title: 'Servidor publicado',
            text: 'Assim que a API tiver um endereço público definitivo, cole-o aqui. Se ele não '
                'for HTTPS, pode ser necessário liberar tráfego HTTP nas configurações do app nativo.',
          ),
        ],
      ),
    );
  }
}

class _Dica extends StatelessWidget {
  const _Dica({required this.title, required this.text});

  final String title;
  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: <Widget>[
          Text(title, style: const TextStyle(fontWeight: FontWeight.bold)),
          Text(text, style: Theme.of(context).textTheme.bodySmall),
        ],
      ),
    );
  }
}
