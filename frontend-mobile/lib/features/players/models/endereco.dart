/// Mirrors backend entity Endereco (table `endereco`).
class Endereco {
  Endereco({
    this.id,
    this.logradouro,
    this.numero,
    this.complemento,
    this.bairro,
    this.cidade,
    this.estado,
    this.cep,
  });

  int? id;
  String? logradouro;
  String? numero;
  String? complemento;
  String? bairro;
  String? cidade;
  String? estado;
  String? cep;

  factory Endereco.vazio() => Endereco();

  factory Endereco.fromJson(Map<String, dynamic> json) {
    return Endereco(
      id: json['id'] as int?,
      logradouro: json['logradouro'] as String?,
      numero: json['numero'] as String?,
      complemento: json['complemento'] as String?,
      bairro: json['bairro'] as String?,
      cidade: json['cidade'] as String?,
      estado: json['estado'] as String?,
      cep: json['cep'] as String?,
    );
  }

  /// Mirrors EnderecoController.criar forcing id=null when id==0, so a new
  /// nested address inserts correctly instead of trying to update id 0.
  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'id': (id == null || id == 0) ? null : id,
      'logradouro': logradouro,
      'numero': numero,
      'complemento': complemento,
      'bairro': bairro,
      'cidade': cidade,
      'estado': estado?.toUpperCase(),
      'cep': cep,
    };
  }

  Endereco copyWith({
    int? id,
    String? logradouro,
    String? numero,
    String? complemento,
    String? bairro,
    String? cidade,
    String? estado,
    String? cep,
  }) {
    return Endereco(
      id: id ?? this.id,
      logradouro: logradouro ?? this.logradouro,
      numero: numero ?? this.numero,
      complemento: complemento ?? this.complemento,
      bairro: bairro ?? this.bairro,
      cidade: cidade ?? this.cidade,
      estado: estado ?? this.estado,
      cep: cep ?? this.cep,
    );
  }
}
