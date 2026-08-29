import '../../../core/network/json_helpers.dart';
import '../../players/models/jogador.dart';
import 'jogador_partida.dart';

/// Mirrors backend entity Partida (table `partida`). The last five fields
/// are transient/computed on the backend (never persisted as columns) and
/// are only populated when hydrated from GET /partidas/{id} or the
/// response of POST /partidas/sortear — everywhere else they default to
/// empty, matching the backend's own behavior for plain list endpoints.
class Partida {
  Partida({
    this.id,
    required this.temporadaId,
    this.dataPartida,
    this.golsTimeAzul = 0,
    this.golsTimeVermelho = 0,
    this.nomePartida,
    this.arbitro,
    this.bandeira1,
    this.bandeira2,
    this.sumula,
    this.gridAzul,
    this.gridVermelho,
    this.formacaoAzul,
    this.formacaoVermelho,
    List<Jogador>? jogadoresAzul,
    List<Jogador>? jogadoresVermelho,
    List<JogadorPartida>? listaGeralPresenca,
  })  : jogadoresAzul = jogadoresAzul ?? <Jogador>[],
        jogadoresVermelho = jogadoresVermelho ?? <Jogador>[],
        listaGeralPresenca = listaGeralPresenca ?? <JogadorPartida>[];

  int? id;
  int temporadaId;
  DateTime? dataPartida;
  int golsTimeAzul;
  int golsTimeVermelho;
  String? nomePartida;
  String? arbitro;
  String? bandeira1;
  String? bandeira2;
  String? sumula;
  String? gridAzul;
  String? gridVermelho;

  // Transient/computed fields.
  String? formacaoAzul;
  String? formacaoVermelho;
  List<Jogador> jogadoresAzul;
  List<Jogador> jogadoresVermelho;
  List<JogadorPartida> listaGeralPresenca;

  factory Partida.novo({required int temporadaId}) => Partida(temporadaId: temporadaId);

  factory Partida.fromJson(Map<String, dynamic> json) {
    return Partida(
      id: json['id'] as int?,
      temporadaId: (json['temporadaId'] as int?) ?? 0,
      dataPartida: parseFlexibleDate(json['dataPartida']),
      golsTimeAzul: parseFlexibleInt(json['golsTimeAzul']),
      golsTimeVermelho: parseFlexibleInt(json['golsTimeVermelho']),
      nomePartida: json['nomePartida'] as String?,
      arbitro: json['arbitro'] as String?,
      bandeira1: json['bandeira1'] as String?,
      bandeira2: json['bandeira2'] as String?,
      sumula: json['sumula'] as String?,
      gridAzul: json['gridAzul'] as String?,
      gridVermelho: json['gridVermelho'] as String?,
      formacaoAzul: json['formacaoAzul'] as String?,
      formacaoVermelho: json['formacaoVermelho'] as String?,
      jogadoresAzul: _parseJogadores(json['jogadoresAzul']),
      jogadoresVermelho: _parseJogadores(json['jogadoresVermelho']),
      listaGeralPresenca: _parseListaPresenca(json['listaGeralPresenca']),
    );
  }

  static List<Jogador> _parseJogadores(dynamic raw) {
    if (raw is! List) return <Jogador>[];
    return raw.whereType<Map<String, dynamic>>().map(Jogador.fromJson).toList();
  }

  static List<JogadorPartida> _parseListaPresenca(dynamic raw) {
    if (raw is! List) return <JogadorPartida>[];
    return raw.whereType<Map<String, dynamic>>().map(JogadorPartida.fromJson).toList();
  }

  Map<String, dynamic> toJson() {
    return <String, dynamic>{
      'id': id,
      'temporadaId': temporadaId,
      'dataPartida': toDateOnlyString(dataPartida),
      'golsTimeAzul': golsTimeAzul,
      'golsTimeVermelho': golsTimeVermelho,
      'nomePartida': nomePartida,
      'arbitro': arbitro,
      'bandeira1': bandeira1,
      'bandeira2': bandeira2,
      'sumula': sumula,
      'gridAzul': gridAzul,
      'gridVermelho': gridVermelho,
      'formacaoAzul': formacaoAzul,
      'formacaoVermelho': formacaoVermelho,
      // Transient/computed on the backend (never persisted as columns), but
      // still real request fields some endpoints read directly off the body
      // — e.g. /partidas/atrasados-disponiveis compares against these to
      // exclude players already in the match. Omitting them here always
      // sent empty lists, so that filter silently excluded no one.
      'jogadoresAzul': jogadoresAzul.map((Jogador j) => j.toJson()).toList(),
      'jogadoresVermelho': jogadoresVermelho.map((Jogador j) => j.toJson()).toList(),
      'listaGeralPresenca': listaGeralPresenca.map((JogadorPartida jp) => jp.toJson()).toList(),
    };
  }

  String get placarFormatado => 'AZUL $golsTimeAzul x $golsTimeVermelho VERMELHO';

  String get dataPartidaFormatada {
    final DateTime? d = dataPartida;
    if (d == null) return '';
    return '${d.day.toString().padLeft(2, '0')}/${d.month.toString().padLeft(2, '0')}/${d.year}';
  }
}
