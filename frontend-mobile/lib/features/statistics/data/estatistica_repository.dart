import 'package:dio/dio.dart';

import '../../../core/network/api_exception.dart';
import '../models/estatistica_row.dart';

/// Wraps EstatisticaController (/estatisticas).
class EstatisticaRepository {
  EstatisticaRepository(this._dio);

  final Dio _dio;

  Future<List<EstatisticaRow>> listar({required int temporada}) async {
    try {
      final Response<dynamic> response = await _dio.get<dynamic>(
        '/estatisticas',
        queryParameters: <String, dynamic>{'temporada': temporada},
      );
      final List<dynamic> data = response.data as List<dynamic>;
      return data.map(EstatisticaRow.fromObjectArrayRow).toList();
    } on DioException catch (e) {
      throw ApiException.fromDioException(e);
    }
  }
}
