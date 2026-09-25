import 'dart:convert';

import 'package:dio/dio.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/network/json_helpers.dart';
import '../models/endereco.dart';
import '../models/jogador.dart';

/// Wraps JogadorController (/jogadores) and EnderecoController (/enderecos).
/// Every method returns typed models, never a raw Response/Map, per the
/// plan's explicit improvement over the desktop's raw-string ApiClient.
class JogadorRepository {
  JogadorRepository(this._dio);

  final Dio _dio;

  /// These endpoints return plain text (not JSON), so Dio's default JSON
  /// auto-decode must be disabled or it throws on the raw body.
  Options get _plainText => Options(responseType: ResponseType.plain);

  Future<List<Jogador>> listar({String? status, bool incluirInativos = false}) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>(
        '/jogadores',
        queryParameters: <String, dynamic>{
          if (status != null) 'status': status,
          if (incluirInativos) 'incluirInativos': true,
        },
      );
      final List<dynamic> data = response.data as List<dynamic>;
      return data.whereType<Map<String, dynamic>>().map(Jogador.fromJson).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<Jogador> obterPorId(int id) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/jogadores/$id');
      return Jogador.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<Jogador> criar(Jogador jogador) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>('/jogadores', data: jogador.toJson());
      return Jogador.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<Jogador> atualizar(int id, Jogador jogador) async {
    try {
      final Response<dynamic> response = await _dio.put<dynamic>('/jogadores/$id', data: jogador.toJson());
      return Jogador.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  /// Returns the backend's plain-text result message. May describe a
  /// failure (e.g. FK constraint from matches/dues) without throwing,
  /// mirroring the backend's "fails gracefully with message" behavior.
  Future<String> excluir(int id) async {
    try {
      final Response<dynamic> response = await _dio.delete<dynamic>('/jogadores/$id', options: _plainText);
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  /// Volta um jogador inativado para as listagens. Retorna a mensagem da API
  /// (pode avisar que a camisa dele ficou com outro jogador).
  Future<String> reativar(int id) async {
    try {
      final Response<dynamic> response = await _dio.put<dynamic>('/jogadores/$id/reativar', options: _plainText);
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  /// Case-insensitive exact match on apelido first, then nome. Returns
  /// null if not found.
  Future<Jogador?> buscarPorNome(String nome) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/jogadores/buscar-por-nome',
        data: jsonEncode(nome),
        options: Options(contentType: 'application/json'),
      );
      if (response.data == null) return null;
      if (response.data is! Map<String, dynamic>) return null;
      return Jogador.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  // --- EnderecoController -------------------------------------------------

  Future<List<Endereco>> listarEnderecos() async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/enderecos');
      final List<dynamic> data = response.data as List<dynamic>;
      return data.whereType<Map<String, dynamic>>().map(Endereco.fromJson).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<Endereco> obterEndereco(int id) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/enderecos/$id');
      return Endereco.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<String> obterEnderecoFormatado(int id) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/enderecos/$id/formatado', options: _plainText);
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<Endereco> criarEndereco(Endereco endereco) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>('/enderecos', data: endereco.toJson());
      return Endereco.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<Endereco> atualizarEndereco(int id, Endereco endereco) async {
    try {
      final Response<dynamic> response = await _dio.put<dynamic>('/enderecos/$id', data: endereco.toJson());
      return Endereco.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<void> excluirEndereco(int id) async {
    try {
      await _dio.delete<dynamic>('/enderecos/$id', options: _plainText);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }
}
