import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../models/jogador.dart';
import 'padrinho_dropdown.dart';
import 'player_form_controllers.dart';

const List<String> kPosicoes = <String>['GOLEIRO', 'LATERAL', 'ZAGUEIRO', 'VOLANTE', 'MEIA', 'ATACANTE'];
const List<String> kPesDominantes = <String>['DIREITO', 'ESQUERDO', 'AMBIDESTRO'];

/// Legacy desktop client (FormJogadorView) saves peDominante using a
/// different vocabulary ("Destro"/"Canhoto") than kPesDominantes — maps one
/// onto the other so records created there don't crash the dropdown here.
const Map<String, String> kPeDominanteSinonimos = <String, String>{
  'DESTRO': 'DIREITO',
  'CANHOTO': 'ESQUERDO',
};

/// Normalizes a value loaded from the backend (trim + uppercase, then
/// [sinonimos] lookup) so it safely matches one of [validos]. Returns null
/// — rather than an unmatched value — when nothing matches, since a
/// DropdownButtonFormField crashes if its value isn't exactly one of its
/// items; null just shows the field unset instead of throwing.
String? normalizarValorDropdown(String? bruto, List<String> validos, [Map<String, String> sinonimos = const <String, String>{}]) {
  if (bruto == null) return null;
  final String upper = bruto.trim().toUpperCase();
  if (upper.isEmpty) return null;
  if (validos.contains(upper)) return upper;
  final String? mapeado = sinonimos[upper];
  if (mapeado != null && validos.contains(mapeado)) return mapeado;
  return null;
}

/// Short form (GOL/LAT/ZAG/VOL/MEI/ATA) of each entry in [kPosicoes], for
/// compact display (e.g. attendance lists).
const Map<String, String> kPosicaoAbreviada = <String, String>{
  'GOLEIRO': 'Gol',
  'LATERAL': 'Lat',
  'ZAGUEIRO': 'Zag',
  'VOLANTE': 'Vol',
  'MEIA': 'Mei',
  'ATACANTE': 'Ata',
};

/// [posicao] abbreviated via [kPosicaoAbreviada], or '' if unset/unrecognized.
String posicaoAbreviada(String? posicao) => kPosicaoAbreviada[(posicao ?? '').trim().toUpperCase()] ?? '';

/// "Infos Esportivas" tab of FormJogadorView: position, dominant foot,
/// physical attributes, previous club, sponsor/"padrinho", skill level,
/// shirt number, experience.
class PlayerSportsTab extends StatelessWidget {
  const PlayerSportsTab({
    super.key,
    required this.controllers,
    required this.enabled,
    required this.posicao,
    required this.onPosicaoChanged,
    required this.peDominante,
    required this.onPeDominanteChanged,
    required this.jogadores,
    required this.currentPlayerId,
    required this.padrinhoId,
    required this.onPadrinhoChanged,
  });

  final PlayerFormControllers controllers;
  final bool enabled;
  final String? posicao;
  final ValueChanged<String?> onPosicaoChanged;
  final String? peDominante;
  final ValueChanged<String?> onPeDominanteChanged;
  final List<Jogador> jogadores;
  final int? currentPlayerId;
  final int? padrinhoId;
  final ValueChanged<int?> onPadrinhoChanged;

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        DropdownButtonFormField<String?>(
          initialValue: posicao,
          decoration: const InputDecoration(labelText: 'Posição'),
          items: kPosicoes
              .map((String p) => DropdownMenuItem<String?>(value: p, child: Text(p)))
              .toList(),
          onChanged: enabled ? onPosicaoChanged : null,
        ),
        const SizedBox(height: 12),
        DropdownButtonFormField<String?>(
          initialValue: peDominante,
          decoration: const InputDecoration(labelText: 'Pé dominante'),
          items: kPesDominantes
              .map((String p) => DropdownMenuItem<String?>(value: p, child: Text(p)))
              .toList(),
          onChanged: enabled ? onPeDominanteChanged : null,
        ),
        const SizedBox(height: 12),
        Row(
          children: <Widget>[
            Expanded(
              child: TextFormField(
                controller: controllers.altura,
                enabled: enabled,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: const InputDecoration(labelText: 'Altura (m)'),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: TextFormField(
                controller: controllers.peso,
                enabled: enabled,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: const InputDecoration(labelText: 'Peso (kg)'),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        TextFormField(
          controller: controllers.timeAnterior,
          enabled: enabled,
          decoration: const InputDecoration(labelText: 'Time anterior'),
        ),
        const SizedBox(height: 12),
        TextFormField(
          controller: controllers.tempoExperiencia,
          enabled: enabled,
          keyboardType: TextInputType.number,
          inputFormatters: <TextInputFormatter>[FilteringTextInputFormatter.digitsOnly],
          decoration: const InputDecoration(labelText: 'Tempo de experiência (anos)'),
        ),
        const SizedBox(height: 20),
        PadrinhoDropdown(
          jogadores: jogadores,
          currentPlayerId: currentPlayerId,
          selectedPadrinhoId: padrinhoId,
          onChanged: onPadrinhoChanged,
          enabled: enabled,
        ),
        const SizedBox(height: 12),
        TextFormField(
          controller: controllers.grauRelacaoPadrinho,
          enabled: enabled,
          decoration: const InputDecoration(labelText: 'Grau de relação com o padrinho'),
        ),
        const SizedBox(height: 20),
        TextFormField(
          controller: controllers.nivel,
          enabled: enabled,
          keyboardType: TextInputType.number,
          inputFormatters: <TextInputFormatter>[FilteringTextInputFormatter.digitsOnly],
          decoration: const InputDecoration(labelText: 'Nível técnico (1-100)'),
        ),
        const SizedBox(height: 12),
        TextFormField(
          controller: controllers.numCamisa,
          enabled: enabled,
          keyboardType: TextInputType.number,
          inputFormatters: <TextInputFormatter>[FilteringTextInputFormatter.digitsOnly],
          decoration: const InputDecoration(labelText: 'Número da camisa'),
        ),
      ],
    );
  }
}
