import 'package:dio/dio.dart';

import 'json_helpers.dart';

/// Typed wrapper around network/backend failures, surfacing the backend's
/// plain-text 400 error bodies (e.g. player validation errors from
/// JogadorController) as a readable [message] instead of a raw stack trace.
class ApiException implements Exception {
  ApiException(this.message, {this.statusCode});

  final String message;
  final int? statusCode;

  factory ApiException.fromDioException(DioException error) {
    final int? status = error.response?.statusCode;
    final dynamic data = error.response?.data;

    // 401 = a API recusou a chave (X-API-KEY) numa ação protegida.
    if (status == 401) {
      return ApiException(
        'Esta ação exige a chave de acesso. Salve a chave em Configurações > Chave de acesso.',
        statusCode: status,
      );
    }

    if (data != null) {
      final String text = data is String ? unwrapQuotedString(data) : data.toString();
      if (text.trim().isNotEmpty) {
        return ApiException(text.trim(), statusCode: status);
      }
    }

    switch (error.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return ApiException('Tempo de conexão esgotado. Verifique o endereço do servidor nas Configurações.');
      case DioExceptionType.connectionError:
        return ApiException('Não foi possível conectar ao servidor. Verifique o endereço nas Configurações e sua conexão de rede.');
      default:
        return ApiException(error.message ?? 'Erro de comunicação com o servidor.', statusCode: status);
    }
  }

  @override
  String toString() => message;
}
