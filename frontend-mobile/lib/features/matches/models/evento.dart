/// Mirrors backend entity Evento (table `evento`). tipoEvento values:
/// "GOL", "AMARELO", "VERMELHO" (plain strings, case-insensitive on server).
class Evento {
  Evento({
    this.id,
    required this.tipo,
    required this.jogadorId,
    required this.partidaId,
    this.corTime,
    this.minuto,
    this.tipoEvento,
  });

  int? id;
  String tipo;
  int jogadorId;
  int partidaId;
  String? corTime;
  int? minuto;
  String? tipoEvento;

  factory Evento.fromJson(Map<String, dynamic> json) {
    return Evento(
      id: json['id'] as int?,
      tipo: (json['tipo'] as String?) ?? '',
      jogadorId: json['jogadorId'] as int,
      partidaId: json['partidaId'] as int,
      corTime: json['corTime'] as String?,
      minuto: json['minuto'] as int?,
      tipoEvento: json['tipoEvento'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'id': id,
      'tipo': tipo,
      'jogadorId': jogadorId,
      'partidaId': partidaId,
      'corTime': corTime,
      'minuto': minuto,
      'tipoEvento': tipoEvento,
    };
  }
}
