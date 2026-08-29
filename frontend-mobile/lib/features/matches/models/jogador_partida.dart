import '../../../core/network/json_helpers.dart';
import '../../players/models/jogador.dart';

/// Mirrors backend entity JogadorPartida (table `jogadorpartida`).
/// `time`: "Azul" | "Vermelho" | "Nenhum". `status`: "Titular" | "Reserva" |
/// "Substituido"/"SUBSTITUIDO" | "Incompleto". `funcao` encodes a tactical
/// slot like "Azul_MEI_3" — kept as a raw string, not parsed structurally,
/// matching how the backend treats it.
class JogadorPartida {
  JogadorPartida({
    this.id,
    required this.jogadorId,
    this.jogador,
    required this.partidaId,
    this.time,
    this.status,
    this.funcao,
    this.presente,
    this.gols = 0,
    this.cartaoAmarelo = 0,
    this.cartaoVermelho = 0,
  });

  int? id;
  int jogadorId;
  Jogador? jogador;
  int partidaId;
  String? time;
  String? status;
  String? funcao;
  String? presente;
  int gols;
  int cartaoAmarelo;
  int cartaoVermelho;

  factory JogadorPartida.fromJson(Map<String, dynamic> json) {
    final dynamic jogadorJson = json['jogador'];
    return JogadorPartida(
      id: json['id'] as int?,
      // partidaId (and, defensively, jogadorId) may arrive null here: the
      // sortear response builds this list for a Partida that hasn't been
      // persisted yet (no id), so its FK back to the match is still unset.
      jogadorId: parseFlexibleInt(json['jogadorId']),
      jogador: jogadorJson is Map<String, dynamic> ? Jogador.fromJson(jogadorJson) : null,
      partidaId: parseFlexibleInt(json['partidaId']),
      time: json['time'] as String?,
      status: json['status'] as String?,
      funcao: json['funcao'] as String?,
      presente: json['presente'] as String?,
      gols: (json['gols'] as int?) ?? 0,
      cartaoAmarelo: (json['cartaoAmarelo'] as int?) ?? 0,
      cartaoVermelho: (json['cartaoVermelho'] as int?) ?? 0,
    );
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'id': id,
      'jogadorId': jogadorId,
      // The nested player is what name-matching filters on the backend
      // (e.g. /partidas/atrasados-disponiveis) actually compare against —
      // without it, every entry here is invisible to those checks.
      'jogador': jogador?.toJson(),
      'partidaId': partidaId,
      'time': time,
      'status': status,
      'funcao': funcao,
      'presente': presente,
      'gols': gols,
      'cartaoAmarelo': cartaoAmarelo,
      'cartaoVermelho': cartaoVermelho,
    };
  }

  JogadorPartida copyWith({
    int? id,
    int? jogadorId,
    Jogador? jogador,
    int? partidaId,
    String? time,
    String? status,
    String? funcao,
    String? presente,
    int? gols,
    int? cartaoAmarelo,
    int? cartaoVermelho,
  }) {
    return JogadorPartida(
      id: id ?? this.id,
      jogadorId: jogadorId ?? this.jogadorId,
      jogador: jogador ?? this.jogador,
      partidaId: partidaId ?? this.partidaId,
      time: time ?? this.time,
      status: status ?? this.status,
      funcao: funcao ?? this.funcao,
      presente: presente ?? this.presente,
      gols: gols ?? this.gols,
      cartaoAmarelo: cartaoAmarelo ?? this.cartaoAmarelo,
      cartaoVermelho: cartaoVermelho ?? this.cartaoVermelho,
    );
  }
}
