import '../../../core/network/json_helpers.dart';
import 'endereco.dart';

/// Mirrors backend entity Jogador (table `jogador`). Position/status/foot
/// values are plain strings on the backend (no formal enums) — kept as
/// case-insensitive-tolerant strings here too rather than Dart enums, to
/// avoid silently dropping values the backend accepts but this app doesn't
/// know about yet.
class Jogador {
  Jogador({
    this.id,
    required this.nome,
    this.apelido,
    this.cpf,
    this.rg,
    this.dataNascimento,
    this.dataAdmissao,
    this.telefone,
    this.telefoneEmergencia,
    this.posicao,
    this.nivel = 50,
    this.peDominante,
    this.altura,
    this.peso,
    this.timeAnterior,
    this.tempoExperiencia,
    this.padrinhoId,
    this.grauRelacaoPadrinho,
    this.estaSuspenso = false,
    this.estaAutorizado = true,
    this.mensalidadeEmDia = true,
    this.numCamisa,
    this.golsIniciais = 0,
    this.cAmarelosIniciais = 0,
    this.cVermelhosIniciais = 0,
    Endereco? endereco,
  }) : endereco = endereco ?? Endereco.vazio();

  int? id;
  String nome;
  String? apelido;
  String? cpf;
  String? rg;
  DateTime? dataNascimento;
  DateTime? dataAdmissao;
  String? telefone;
  String? telefoneEmergencia;
  String? posicao;
  int nivel;
  String? peDominante;
  double? altura;
  double? peso;
  String? timeAnterior;
  int? tempoExperiencia;
  int? padrinhoId;
  String? grauRelacaoPadrinho;
  bool estaSuspenso;
  bool estaAutorizado;
  bool mensalidadeEmDia;
  int? numCamisa;
  int golsIniciais;
  int cAmarelosIniciais;
  int cVermelhosIniciais;
  Endereco endereco;

  /// Derived client-side too, matching the backend's transient `status`
  /// getter (Suspenso/Ativo from estaSuspenso).
  String get status => estaSuspenso ? 'Suspenso' : 'Ativo';

  /// Preferred display name: apelido if set, else nome (matches
  /// FormatadorUtil.formatarDescricaoJogador's nickname-first rule).
  String get nomeExibir => (apelido != null && apelido!.trim().isNotEmpty) ? apelido!.trim() : nome;

  factory Jogador.novo() => Jogador(nome: '', endereco: Endereco.vazio());

  factory Jogador.fromJson(Map<String, dynamic> json) {
    final dynamic enderecoJson = json['endereco'];
    return Jogador(
      id: json['id'] as int?,
      nome: (json['nome'] as String?) ?? '',
      apelido: json['apelido'] as String?,
      cpf: json['cpf'] as String?,
      rg: json['rg'] as String?,
      dataNascimento: parseFlexibleDate(json['dataNascimento']),
      dataAdmissao: parseFlexibleDate(json['dataAdmissao']),
      telefone: json['telefone'] as String?,
      telefoneEmergencia: json['telefoneEmergencia'] as String?,
      posicao: json['posicao'] as String?,
      nivel: parseFlexibleInt(json['nivel'], fallback: 50),
      peDominante: json['peDominante'] as String?,
      altura: parseFlexibleDouble(json['altura']),
      peso: parseFlexibleDouble(json['peso']),
      timeAnterior: json['timeAnterior'] as String?,
      tempoExperiencia: json['tempoExperiencia'] as int?,
      padrinhoId: json['padrinhoId'] as int?,
      grauRelacaoPadrinho: json['grauRelacaoPadrinho'] as String?,
      estaSuspenso: parseFlexibleBool(json['estaSuspenso']),
      estaAutorizado: json['estaAutorizado'] == null ? true : parseFlexibleBool(json['estaAutorizado']),
      mensalidadeEmDia: json['mensalidadeEmDia'] == null ? true : parseFlexibleBool(json['mensalidadeEmDia']),
      numCamisa: json['numCamisa'] as int?,
      golsIniciais: parseFlexibleInt(json['golsIniciais']),
      cAmarelosIniciais: parseFlexibleInt(json['cAmarelosIniciais']),
      cVermelhosIniciais: parseFlexibleInt(json['cVermelhosIniciais']),
      endereco: enderecoJson is Map<String, dynamic> ? Endereco.fromJson(enderecoJson) : Endereco.vazio(),
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'id': id,
      'nome': nome,
      'apelido': apelido,
      'cpf': cpf,
      'rg': rg,
      'dataNascimento': toDateOnlyString(dataNascimento),
      'dataAdmissao': toDateOnlyString(dataAdmissao),
      'telefone': telefone,
      'telefoneEmergencia': telefoneEmergencia,
      'posicao': posicao,
      'nivel': nivel,
      'peDominante': peDominante,
      'altura': altura,
      'peso': peso,
      'timeAnterior': timeAnterior,
      'tempoExperiencia': tempoExperiencia,
      'padrinhoId': padrinhoId,
      'grauRelacaoPadrinho': grauRelacaoPadrinho,
      'estaSuspenso': estaSuspenso,
      'estaAutorizado': estaAutorizado,
      'mensalidadeEmDia': mensalidadeEmDia,
      'numCamisa': numCamisa,
      'golsIniciais': golsIniciais,
      'cAmarelosIniciais': cAmarelosIniciais,
      'cVermelhosIniciais': cVermelhosIniciais,
      'endereco': endereco.toJson(),
    };
  }
}
