import 'package:dio/dio.dart';

/// Result of a ViaCEP lookup, mirroring CepUtil's `Map<String,String>` return
/// (logradouro, bairro, cidade, uf) used to auto-fill the address form.
class CepLookupResult {
  const CepLookupResult({
    required this.logradouro,
    required this.bairro,
    required this.cidade,
    required this.uf,
  });

  final String logradouro;
  final String bairro;
  final String cidade;
  final String uf;
}

/// Looks up a Brazilian postal code (CEP) via the public ViaCEP API, same
/// service the desktop's CepUtil uses. This talks to viacep.com.br directly
/// (not the GestorCANA backend), so it uses its own Dio instance rather than
/// the app's configured backend client.
class CepLookupService {
  CepLookupService() : _dio = Dio(BaseOptions(connectTimeout: const Duration(seconds: 10)));

  final Dio _dio;

  static String onlyDigits(String value) => value.replaceAll(RegExp(r'\D'), '');

  /// Returns null if the CEP is malformed or not found.
  Future<CepLookupResult?> buscar(String cep) async {
    final String digits = onlyDigits(cep);
    if (digits.length != 8) return null;

    try {
      final Response<dynamic> response = await _dio.get<dynamic>('https://viacep.com.br/ws/$digits/json/');
      final dynamic data = response.data;
      if (data is! Map) return null;
      if (data['erro'] == true) return null;

      return CepLookupResult(
        logradouro: (data['logradouro'] ?? '').toString(),
        bairro: (data['bairro'] ?? '').toString(),
        cidade: (data['localidade'] ?? '').toString(),
        uf: (data['uf'] ?? '').toString(),
      );
    } catch (_) {
      return null;
    }
  }
}
