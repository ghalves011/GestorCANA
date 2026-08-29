import 'package:intl/intl.dart';

/// pt-BR currency/formatting helpers mirroring FormatadorUtil.
class CurrencyFormatUtils {
  CurrencyFormatUtils._();

  static final NumberFormat _currency = NumberFormat.currency(locale: 'pt_BR', symbol: 'R\$');

  static String formatarMoeda(num valor) => _currency.format(valor);

  /// "mes/ano" -> "04/2026", matching FormatadorUtil.formatarReferencia.
  static String formatarReferencia(int mes, int ano) {
    return '${mes.toString().padLeft(2, '0')}/$ano';
  }

  static String formatarAltura(double? altura) => altura == null ? '-' : '${altura.toStringAsFixed(2)} m';

  static String formatarPeso(double? peso) => peso == null ? '-' : '${peso.toStringAsFixed(1)} kg';
}
