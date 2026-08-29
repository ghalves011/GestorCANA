import 'package:dio/dio.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/network/json_helpers.dart';
import '../models/temporada.dart';

/// Wraps TemporadaController (/temporadas). The desktop app never actually
/// called these (season hardcoded to 2026 in its UI) — the mobile app uses
/// GET /temporadas to populate a real season selector instead, since it's
/// cheap and more correct (see plan §5 notes on TemporadaController).
class TemporadaRepository {
  TemporadaRepository(this._dio);

  final Dio _dio;

  /// These endpoints return plain text (not JSON), so Dio's default JSON
  /// auto-decode must be disabled or it throws on the raw body.
  Options get _plainText => Options(responseType: ResponseType.plain);

  Future<List<Temporada>> listar() async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/temporadas');
      final List<dynamic> data = response.data as List<dynamic>;
      return data.whereType<Map<String, dynamic>>().map(Temporada.fromJson).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<Temporada> obterPorId(int id) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/temporadas/$id');
      return Temporada.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<Temporada?> obterPorAno(int ano) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/temporadas/ano/$ano');
      return Temporada.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      if (e.response?.statusCode == 404) return null;
      throw ApiException.fromDioException(e);
    }
  }

  Future<String> criar(Temporada temporada) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/temporadas',
        data: temporada.toJson(),
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<String> atualizar(int id, Temporada temporada) async {
    try {
      final Response<dynamic> response = await _dio.put<dynamic>(
        '/temporadas/$id',
        data: temporada.toJson(),
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  Future<void> excluir(int id) async {
    try {
      await _dio.delete<dynamic>('/temporadas/$id', options: _plainText);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }
}
