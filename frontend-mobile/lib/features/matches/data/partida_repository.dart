import 'dart:convert';

import 'package:dio/dio.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/network/json_helpers.dart';
import '../../players/models/jogador.dart';
import '../models/evento.dart';
import '../models/grid_historico_row.dart';
import '../models/jogador_partida.dart';
import '../models/partida.dart';

/// Wraps PartidaController (/partidas), JogadorPartidaController
/// (/jogador-partidas) and EventoController (/eventos) — kept together
/// since they're only ever used from the match-setup/live/substitution
/// flow (see plan §2's controller->repository mapping).
///
/// Endpoints whose Java desktop client sent a bare (non-object) JSON body
/// — a plain string, or a Partida/List sent directly as the whole body
/// rather than wrapped under a key — are sent here via an explicit
/// `jsonEncode(...)` + `contentType: application/json`, so the wire format
/// is deterministic regardless of Dio's default-transformer heuristics.
/// Endpoints whose body is a normal `{key: value, ...}` object use Dio's
/// standard Map auto-encoding.
///
/// NOT implemented (see plan §6 — dead/missing backend endpoints):
/// POST /partidas/obter-posicao-original, POST /partidas/improvisar-goleiro
/// (don't exist), POST /partidas/tem-goleiro-natural (exists but is a
/// stub always returning false — replaced by matches/utils/lineup_utils's
/// client-side temGoleiroNatural()).
class PartidaRepository {
  PartidaRepository(this._dio);

  final Dio _dio;

  Options get _jsonBody => Options(contentType: 'application/json');

  /// These endpoints return plain text (not JSON), so Dio's default JSON
  /// auto-decode must be disabled or it throws on the raw body.
  Options get _plainText => Options(responseType: ResponseType.plain);

  /// Combines both: a JSON-encoded request body with a plain-text response.
  Options get _jsonBodyPlainResponse =>
      Options(contentType: 'application/json', responseType: ResponseType.plain);

  ApiException _wrap(DioException e) => ApiException.fromDioException(e);

  // --- PartidaController ---------------------------------------------------

  Future<List<Partida>> listar({int? temporada}) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>(
        '/partidas',
        queryParameters: temporada == null ? null : <String, dynamic>{'temporada': temporada},
      );
      final List<dynamic> data = response.data as List<dynamic>;
      return data.whereType<Map<String, dynamic>>().map(Partida.fromJson).toList();
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<Partida> obterPorId(int id) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/partidas/$id');
      return Partida.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Runs the full team-draw + tactical-slotting algorithm server-side.
  /// Does NOT persist — returns the computed draw (jogadoresAzul/
  /// jogadoresVermelho/listaGeralPresenca populated) for the caller to
  /// review before /partidas/finalizar.
  Future<Partida> sortear({required Partida partida, required List<Jogador> jogadores}) async {
    try {
      final Map<String, dynamic> payload = <String, dynamic>{
        'partida': partida.toJson(),
        'jogadores': jogadores.map((Jogador j) => j.toJson()).toList(),
      };
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/sortear',
        data: jsonEncode(payload),
        options: _jsonBody,
      );
      return Partida.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Parses the persisted gridAzul/gridVermelho JSON-text column back into
  /// tabular rows for read-only historical display.
  Future<List<GridHistoricoRow>> gridHistorico({required Partida partida, required String time}) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/grid-historico',
        queryParameters: <String, dynamic>{'time': time},
        data: partida.toJson(),
      );
      final List<dynamic> data = response.data as List<dynamic>;
      return data.map(GridHistoricoRow.fromObjectArrayRow).toList();
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// True if any string in [eventos] is non-blank — used to lock
  /// tactical-edit UI once any goal/card has been recorded.
  Future<bool> temEventos(List<String> eventos) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/tem-eventos',
        data: jsonEncode(eventos),
        options: _jsonBody,
      );
      return parseFlexibleBool(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Active players not already in jogadoresAzul/jogadoresVermelho of
  /// [partida], sorted by tactical position order then name.
  Future<List<Jogador>> atrasados(Partida partida) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>('/partidas/atrasados', data: partida.toJson());
      final List<dynamic> data = response.data as List<dynamic>;
      return data.whereType<Map<String, dynamic>>().map(Jogador.fromJson).toList();
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Same as [atrasados] but used by the substitution screen's "chegou
  /// atrasado" flow (backend endpoint takes a raw-string Partida body).
  Future<List<Jogador>> atrasadosDisponiveis(Partida partida) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/atrasados-disponiveis',
        data: jsonEncode(partida.toJson()),
        options: _jsonBody,
      );
      final List<dynamic> data = response.data as List<dynamic>;
      return data.whereType<Map<String, dynamic>>().map(Jogador.fromJson).toList();
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Checks whether the bench player identified by [reservaDisplayText] is
  /// suspended, before allowing them onto the field. Returns "OK" or an
  /// error message.
  Future<String> validaLinha(String reservaDisplayText) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/valida-linha',
        data: <String, dynamic>{'reserva': reservaDisplayText},
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Plain upsert (new if [partida.id] is null).
  Future<bool> salvar(Partida partida) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>('/partidas', data: partida.toJson());
      return parseFlexibleBool(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Closes out a match: persists the grids as JSON text, saves [partida],
  /// wipes and re-derives per-player JogadorPartida stats, applies
  /// suspension rules. [gridAzul]/[gridVermelho] are the raw table rows as
  /// built by the live scoreboard screen (each row a List of cell values).
  Future<bool> finalizar({
    required Partida partida,
    required List<List<dynamic>> gridAzul,
    required List<List<dynamic>> gridVermelho,
  }) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/finalizar',
        data: <String, dynamic>{
          'partida': partida.toJson(),
          'gridAzul': gridAzul,
          'gridVermelho': gridVermelho,
        },
      );
      return parseFlexibleBool(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Stub on the backend — always returns true and does no real work. The
  /// actual visual reorder always happens locally in match_live_screen;
  /// this call is kept only in case the backend gains real logic later.
  Future<bool> inverterMesmoTime(Map<String, dynamic> payload) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>('/partidas/inverter-mesmo-time', data: payload);
      return parseFlexibleBool(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Stub on the backend — same caveat as [inverterMesmoTime].
  Future<bool> permutarAdversario(Map<String, dynamic> payload) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>('/partidas/permutar-adversario', data: payload);
      return parseFlexibleBool(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Suspension check for a substitution candidate by display name.
  Future<String> validarRestricaoSubstituicao(String nome) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/validar-restricao-substituicao',
        data: jsonEncode(nome),
        options: _jsonBodyPlainResponse,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Blocks assigning [jogador] to referee/flag duty if already assigned
  /// to another arbitration role in the same match. Returns empty string
  /// if OK, or a warning message.
  Future<String> validarRestricaoArbitragem({required Partida partida, required Jogador jogador}) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/validar-restricao-arbitragem',
        data: jsonEncode(<String, dynamic>{'partida': partida.toJson(), 'jogador': jogador.toJson()}),
        options: _jsonBodyPlainResponse,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Sets/accumulates the arbitration text field for [cargo]
  /// ("ARBITRO"|"BANDEIRA1"|"BANDEIRA2"). When [acumular] is true, history
  /// is preserved by " / "-joining the previous and new values.
  Future<Partida> definirArbitragem({
    required Partida partida,
    required Jogador jogador,
    required String cargo,
    required bool acumular,
  }) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/definir-arbitragem',
        data: jsonEncode(<String, dynamic>{
          'partida': partida.toJson(),
          'jogador': jogador.toJson(),
          'cargo': cargo,
          'acumular': acumular,
        }),
        options: _jsonBody,
      );
      return Partida.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Removes the current holder of [cargo]. When [gerarHistorico] is true,
  /// leaves a "____" placeholder to preserve the assignment history.
  Future<Partida> removerArbitragem({
    required Partida partida,
    required String cargo,
    required bool gerarHistorico,
  }) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/remover-arbitragem',
        data: jsonEncode(<String, dynamic>{
          'partida': partida.toJson(),
          'cargo': cargo,
          'gerarHistorico': gerarHistorico,
        }),
        options: _jsonBody,
      );
      return Partida.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Builds the new " / "-joined cell text for the pitch table plus the
  /// bench-return text. Returns [novoTextoTabela, textoBanco].
  Future<List<String>> processarSubstituicaoJogador({
    required String nomeSaindo,
    required String nomeEntrandoRaw,
    required String posicao,
  }) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/processar-substituicao-jogador',
        data: jsonEncode(<String, dynamic>{
          'nomeSaindo': nomeSaindo,
          'nomeEntrandoRaw': nomeEntrandoRaw,
          'posicao': posicao,
        }),
        options: _jsonBody,
      );
      final List<dynamic> data = response.data as List<dynamic>;
      return data.map((dynamic e) => e.toString()).toList();
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Syncs the underlying presence list after a substitution: incoming
  /// player -> time=[timeAlvo], status=Reserva (inherits outgoing's
  /// funcao); outgoing -> time=Nenhum, status=Substituido.
  Future<Partida> atualizarSubstituicaoListaPresenca({
    required Partida partida,
    required String nomeSaindo,
    required String nomeEntrandoLimpo,
    required String timeAlvo,
  }) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/partidas/atualizar-substituicao-lista-presenca',
        data: jsonEncode(<String, dynamic>{
          'partida': partida.toJson(),
          'nomeSaindo': nomeSaindo,
          'nomeEntrandoLimpo': nomeEntrandoLimpo,
          'timeAlvo': timeAlvo,
        }),
        options: _jsonBody,
      );
      return Partida.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  // --- JogadorPartidaController --------------------------------------------

  Future<List<JogadorPartida>> listarJogadorPartidas() async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/jogador-partidas');
      final List<dynamic> data = response.data as List<dynamic>;
      return data.whereType<Map<String, dynamic>>().map(JogadorPartida.fromJson).toList();
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<List<JogadorPartida>> listarPorPartida(int partidaId) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/jogador-partidas/partida/$partidaId');
      final List<dynamic> data = response.data as List<dynamic>;
      return data.whereType<Map<String, dynamic>>().map(JogadorPartida.fromJson).toList();
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<JogadorPartida> criarJogadorPartida(JogadorPartida jp) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>('/jogador-partidas', data: jp.toJson());
      return JogadorPartida.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<JogadorPartida> atualizarJogadorPartida(int id, JogadorPartida jp) async {
    try {
      final Response<dynamic> response = await _dio.put<dynamic>('/jogador-partidas/$id', data: jp.toJson());
      return JogadorPartida.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<JogadorPartida> registrarGol(int jogadorPartidaId) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>('/jogador-partidas/$jogadorPartidaId/gol');
      return JogadorPartida.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Increments cartaoAmarelo; auto-escalates to a red on the 2nd yellow
  /// in this match, and checks career total (>=3) for suspension.
  Future<String> registrarCartaoAmarelo(int jogadorPartidaId) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/jogador-partidas/$jogadorPartidaId/cartao-amarelo',
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<String> substituicao({required int saiId, required int entraId}) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/jogador-partidas/substituicao',
        queryParameters: <String, dynamic>{'saiId': saiId, 'entraId': entraId},
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<void> excluirJogadorPartida(int id) async {
    try {
      await _dio.delete<dynamic>('/jogador-partidas/$id', options: _plainText);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  // --- EventoController ------------------------------------------------------

  /// Persists a discrete goal/card event; the backend applies auto-
  /// suspension rules (3rd career yellow or any red).
  Future<String> criarEvento(Evento evento) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/eventos',
        data: evento.toJson(),
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<List<Evento>> eventosPorPartida(int partidaId) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/eventos/partida/$partidaId');
      final List<dynamic> data = response.data as List<dynamic>;
      return data.whereType<Map<String, dynamic>>().map(Evento.fromJson).toList();
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<List<Evento>> eventosPorJogador(int jogadorId) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/eventos/jogador/$jogadorId');
      final List<dynamic> data = response.data as List<dynamic>;
      return data.whereType<Map<String, dynamic>>().map(Evento.fromJson).toList();
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Appends [token] (one of EventTokens.*) to the active substitution
  /// block of the live-scoreboard cell text [eventos], re-sorting tokens
  /// into goals/own-goals/yellows/reds order. Returns the updated cell
  /// text.
  Future<String> adicionarToken({required String eventos, required String token}) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/eventos/adicionar',
        data: <String, dynamic>{'eventos': eventos, 'token': token},
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<String> removerToken({required String eventos, required String token}) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/eventos/remover',
        data: <String, dynamic>{'eventos': eventos, 'token': token},
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  Future<int> contarTokens({required String eventos, required String token}) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/eventos/contar-tokens',
        data: <String, dynamic>{'eventos': eventos, 'token': token},
      );
      return parseFlexibleInt(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }

  /// Counts yellow cards in the active block — used to detect a 2nd
  /// yellow so the client can also register a red before/along with it.
  Future<int> contarAmarelosAtivo(String eventos) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/eventos/contar-amarelos-ativo',
        data: jsonEncode(eventos),
        options: _jsonBody,
      );
      return parseFlexibleInt(response.data);
    } on DioException catch (e) {
      throw _wrap(e);
    }
  }
}
