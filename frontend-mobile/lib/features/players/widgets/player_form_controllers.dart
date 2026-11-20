import 'package:flutter/material.dart';

import '../../../core/utils/date_format_utils.dart';
import '../models/jogador.dart';

/// Bundles every TextEditingController the player form needs, and knows
/// how to populate itself from / write itself back into a [Jogador].
/// Kept as plain controllers (no form-state package) to mirror the
/// desktop's direct-field-access style, just organized for Flutter.
class PlayerFormControllers {
  PlayerFormControllers()
      : nome = TextEditingController(),
        apelido = TextEditingController(),
        dataNascimento = TextEditingController(),
        dataAdmissao = TextEditingController(),
        cpf = TextEditingController(),
        rg = TextEditingController(),
        telefone = TextEditingController(),
        telefoneEmergencia = TextEditingController(),
        logradouro = TextEditingController(),
        numero = TextEditingController(),
        complemento = TextEditingController(),
        bairro = TextEditingController(),
        cidade = TextEditingController(),
        estado = TextEditingController(),
        cep = TextEditingController(),
        timeAnterior = TextEditingController(),
        tempoExperiencia = TextEditingController(),
        grauRelacaoPadrinho = TextEditingController(),
        nivel = TextEditingController(text: '50'),
        numCamisa = TextEditingController(),
        altura = TextEditingController(),
        peso = TextEditingController();

  final TextEditingController nome;
  final TextEditingController apelido;
  final TextEditingController dataNascimento;
  final TextEditingController dataAdmissao;
  final TextEditingController cpf;
  final TextEditingController rg;
  final TextEditingController telefone;
  final TextEditingController telefoneEmergencia;
  final TextEditingController logradouro;
  final TextEditingController numero;
  final TextEditingController complemento;
  final TextEditingController bairro;
  final TextEditingController cidade;
  final TextEditingController estado;
  final TextEditingController cep;
  final TextEditingController timeAnterior;
  final TextEditingController tempoExperiencia;
  final TextEditingController grauRelacaoPadrinho;
  final TextEditingController nivel;
  final TextEditingController numCamisa;
  final TextEditingController altura;
  final TextEditingController peso;

  /// Strips non-digits and clamps to [maxLen]. Legacy records can have a
  /// phone/CEP value already at (or past) the length the field's
  /// [LengthLimitingTextInputFormatter] enforces; assigning that raw value
  /// straight to the controller's `.text` bypasses `inputFormatters`
  /// (they only run on user-driven edits), so the field loads "already
  /// full". From then on, every keystroke with a collapsed selection hits
  /// the formatter's "already at maxLength" branch and is silently
  /// dropped — no error, the field just looks frozen. Clamping here keeps
  /// the controller in a state the formatter would have produced itself.
  String _digitsClamped(String? raw, int maxLen) {
    final String digits = (raw ?? '').replaceAll(RegExp(r'\D'), '');
    return digits.length > maxLen ? digits.substring(0, maxLen) : digits;
  }

  void preencherDe(Jogador jogador) {
    nome.text = jogador.nome;
    apelido.text = jogador.apelido ?? '';
    dataNascimento.text = DateFormatUtils.paraUsuario(jogador.dataNascimento);
    dataAdmissao.text = DateFormatUtils.paraUsuario(jogador.dataAdmissao);
    cpf.text = jogador.cpf ?? '';
    rg.text = jogador.rg ?? '';
    telefone.text = _digitsClamped(jogador.telefone, 11);
    telefoneEmergencia.text = _digitsClamped(jogador.telefoneEmergencia, 11);
    logradouro.text = jogador.endereco.logradouro ?? '';
    numero.text = jogador.endereco.numero ?? '';
    complemento.text = jogador.endereco.complemento ?? '';
    bairro.text = jogador.endereco.bairro ?? '';
    cidade.text = jogador.endereco.cidade ?? '';
    estado.text = jogador.endereco.estado ?? '';
    cep.text = _digitsClamped(jogador.endereco.cep, 8);
    timeAnterior.text = jogador.timeAnterior ?? '';
    tempoExperiencia.text = jogador.tempoExperiencia?.toString() ?? '';
    grauRelacaoPadrinho.text = jogador.grauRelacaoPadrinho ?? '';
    nivel.text = jogador.nivel.toString();
    numCamisa.text = jogador.numCamisa?.toString() ?? '';
    altura.text = jogador.altura?.toString() ?? '';
    peso.text = jogador.peso?.toString() ?? '';
  }

  void limpar() {
    for (final TextEditingController c in <TextEditingController>[
      nome,
      apelido,
      dataNascimento,
      dataAdmissao,
      cpf,
      rg,
      telefone,
      telefoneEmergencia,
      logradouro,
      numero,
      complemento,
      bairro,
      cidade,
      estado,
      cep,
      timeAnterior,
      tempoExperiencia,
      grauRelacaoPadrinho,
      numCamisa,
      altura,
      peso,
    ]) {
      c.clear();
    }
    nivel.text = '50';
  }

  /// Writes the current field values into [jogador] (mutates and returns
  /// it, for convenience at the call site).
  Jogador escreverEm(Jogador jogador) {
    jogador
      ..nome = nome.text.trim()
      ..apelido = apelido.text.trim().isEmpty ? null : apelido.text.trim()
      ..dataNascimento = DateFormatUtils.ler(dataNascimento.text)
      ..dataAdmissao = DateFormatUtils.ler(dataAdmissao.text)
      ..cpf = cpf.text.trim().isEmpty ? null : cpf.text.trim()
      ..rg = rg.text.trim().isEmpty ? null : rg.text.trim()
      ..telefone = telefone.text.trim().isEmpty ? null : telefone.text.trim()
      ..telefoneEmergencia = telefoneEmergencia.text.trim().isEmpty ? null : telefoneEmergencia.text.trim()
      ..timeAnterior = timeAnterior.text.trim().isEmpty ? null : timeAnterior.text.trim()
      ..tempoExperiencia = int.tryParse(tempoExperiencia.text.trim())
      ..grauRelacaoPadrinho = grauRelacaoPadrinho.text.trim().isEmpty ? null : grauRelacaoPadrinho.text.trim()
      ..nivel = int.tryParse(nivel.text.trim()) ?? 50
      ..numCamisa = int.tryParse(numCamisa.text.trim())
      ..altura = double.tryParse(altura.text.trim().replaceAll(',', '.'))
      ..peso = double.tryParse(peso.text.trim().replaceAll(',', '.'));

    jogador.endereco
      ..logradouro = logradouro.text.trim()
      ..numero = numero.text.trim()
      ..complemento = complemento.text.trim().isEmpty ? null : complemento.text.trim()
      ..bairro = bairro.text.trim()
      ..cidade = cidade.text.trim()
      ..estado = estado.text.trim()
      ..cep = cep.text.trim();

    return jogador;
  }

  void dispose() {
    for (final TextEditingController c in <TextEditingController>[
      nome,
      apelido,
      dataNascimento,
      dataAdmissao,
      cpf,
      rg,
      telefone,
      telefoneEmergencia,
      logradouro,
      numero,
      complemento,
      bairro,
      cidade,
      estado,
      cep,
      timeAnterior,
      tempoExperiencia,
      grauRelacaoPadrinho,
      nivel,
      numCamisa,
      altura,
      peso,
    ]) {
      c.dispose();
    }
  }
}
