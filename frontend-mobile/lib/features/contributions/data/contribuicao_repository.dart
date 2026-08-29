import 'package:dio/dio.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/network/json_helpers.dart';
import '../models/contribuicao_row.dart';

/// Wraps ContribuicaoController (/contribuicoes).
class ContribuicaoRepository {
  ContribuicaoRepository(this._dio);

  final Dio _dio;

  /// These endpoints return plain text (not JSON) — e.g. "OK" or a
  /// human-readable message — so Dio's default JSON auto-decode must be
  /// disabled or it throws a FormatException on the raw body.
  Options get _plainText => Options(responseType: ResponseType.plain);

  Future<List<ContribuicaoRow>> listarMatriz({required int ano, String busca = ''}) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>(
        '/contribuicoes',
        queryParameters: <String, dynamic>{'ano': ano, 'busca': busca},
      );
      final List<dynamic> data = response.data as List<dynamic>;
      return data.map(ContribuicaoRow.fromObjectArrayRow).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  /// Registers a payment. Returns the backend's plain-text result ("OK").
  Future<String> registrarPagamento({
    required int jogadorId,
    required int mes,
    required int ano,
    double valor = 50.0,
  }) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/contribuicoes',
        data: <String, dynamic>{'jogadorId': jogadorId, 'mes': mes, 'ano': ano, 'valor': valor},
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  /// Bulk-generates 12 pending monthly charges for every player for [ano],
  /// skipping months that already have a record.
  Future<String> gerarLote({required int ano, double valor = 50.0}) async {
    try {
      final Response<dynamic> response = await _dio.post<dynamic>(
        '/contribuicoes/gerar-lote',
        data: <String, dynamic>{'ano': ano, 'valor': valor},
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  /// WhatsApp-formatted receipt text.
  Future<String> obterRecibo({required int jogadorId, required int mes, required int ano}) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>(
        '/contribuicoes/recibo',
        queryParameters: <String, dynamic>{'jogadorId': jogadorId, 'mes': mes, 'ano': ano},
        options: _plainText,
      );
      return unwrapQuotedString(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  /// Reverses/deletes a payment record.
  Future<void> estornarPagamento({required int jogadorId, required int mes, required int ano}) async {
    try {
      await _dio.delete<dynamic>(
        '/contribuicoes',
        queryParameters: <String, dynamic>{'jogadorId': jogadorId, 'mes': mes, 'ano': ano},
        options: _plainText,
      );
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }

  /// Business rule: player can't play if suspended OR has any unpaid month
  /// between admission month (or Jan if admitted a previous year) and the
  /// current month of the current year.
  Future<bool> podeJogar(int jogadorId) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>('/contribuicoes/pode-jogar/$jogadorId');
      return parseFlexibleBool(response.data);
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }
}
