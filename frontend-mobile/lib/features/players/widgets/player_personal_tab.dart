import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../core/utils/cep_lookup_service.dart';
import 'cep_field.dart';
import 'player_form_controllers.dart';

/// "Infos Pessoais" tab of FormJogadorView: identity, contact and address
/// (with CEP auto-fill).
class PlayerPersonalTab extends StatelessWidget {
  const PlayerPersonalTab({super.key, required this.controllers, required this.enabled});

  final PlayerFormControllers controllers;
  final bool enabled;

  void _onCepResult(CepLookupResult? result) {
    if (result == null) return;
    controllers.logradouro.text = result.logradouro;
    controllers.bairro.text = result.bairro;
    controllers.cidade.text = result.cidade;
    controllers.estado.text = result.uf;
  }

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        TextFormField(
          controller: controllers.nome,
          enabled: enabled,
          decoration: const InputDecoration(labelText: 'Nome completo *'),
        ),
        const SizedBox(height: 12),
        TextFormField(
          controller: controllers.apelido,
          enabled: enabled,
          decoration: const InputDecoration(labelText: 'Apelido'),
        ),
        const SizedBox(height: 12),
        Row(
          children: <Widget>[
            Expanded(
              child: TextFormField(
                controller: controllers.dataNascimento,
                enabled: enabled,
                keyboardType: TextInputType.datetime,
                decoration: const InputDecoration(labelText: 'Nascimento (dd/mm/aaaa) *'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextFormField(
                controller: controllers.dataAdmissao,
                enabled: enabled,
                keyboardType: TextInputType.datetime,
                decoration: const InputDecoration(labelText: 'Admissão (dd/mm/aaaa)'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Row(
          children: <Widget>[
            Expanded(
              child: TextFormField(
                controller: controllers.rg,
                enabled: enabled,
                keyboardType: TextInputType.number,
                inputFormatters: <TextInputFormatter>[FilteringTextInputFormatter.digitsOnly],
                decoration: const InputDecoration(labelText: 'RG *'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextFormField(
                controller: controllers.cpf,
                enabled: enabled,
                keyboardType: TextInputType.number,
                inputFormatters: <TextInputFormatter>[FilteringTextInputFormatter.digitsOnly],
                decoration: const InputDecoration(labelText: 'CPF *'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Row(
          children: <Widget>[
            Expanded(
              child: TextFormField(
                controller: controllers.telefone,
                enabled: enabled,
                keyboardType: TextInputType.phone,
                inputFormatters: <TextInputFormatter>[FilteringTextInputFormatter.digitsOnly],
                decoration: const InputDecoration(labelText: 'Telefone *'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextFormField(
                controller: controllers.telefoneEmergencia,
                enabled: enabled,
                keyboardType: TextInputType.phone,
                inputFormatters: <TextInputFormatter>[FilteringTextInputFormatter.digitsOnly],
                decoration: const InputDecoration(labelText: 'Telefone de emergência'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 20),
        Text('Endereço *', style: Theme.of(context).textTheme.titleSmall),
        const SizedBox(height: 8),
        CepField(controller: controllers.cep, enabled: enabled, onResult: _onCepResult),
        const SizedBox(height: 12),
        TextFormField(
          controller: controllers.logradouro,
          enabled: enabled,
          decoration: const InputDecoration(labelText: 'Logradouro'),
        ),
        const SizedBox(height: 12),
        Row(
          children: <Widget>[
            Expanded(
              child: TextFormField(
                controller: controllers.numero,
                enabled: enabled,
                decoration: const InputDecoration(labelText: 'Número'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              flex: 2,
              child: TextFormField(
                controller: controllers.complemento,
                enabled: enabled,
                decoration: const InputDecoration(labelText: 'Complemento'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        TextFormField(
          controller: controllers.bairro,
          enabled: enabled,
          decoration: const InputDecoration(labelText: 'Bairro'),
        ),
        const SizedBox(height: 12),
        Row(
          children: <Widget>[
            Expanded(
              flex: 3,
              child: TextFormField(
                controller: controllers.cidade,
                enabled: enabled,
                decoration: const InputDecoration(labelText: 'Cidade'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextFormField(
                controller: controllers.estado,
                enabled: enabled,
                maxLength: 2,
                textCapitalization: TextCapitalization.characters,
                decoration: const InputDecoration(labelText: 'UF', counterText: ''),
              ),
            ),
          ],
        ),
      ],
    );
  }
}
