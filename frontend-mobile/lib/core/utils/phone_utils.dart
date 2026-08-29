/// Mirrors ValidacaoUtil.validarTelefone (10-11 digits, DDD+number) and
/// FormatadorUtil.mascaraTelefone (10 vs 11 digit aware).
class PhoneUtils {
  PhoneUtils._();

  static final RegExp _phonePattern = RegExp(r'^\d{10,11}$');

  static String onlyDigits(String value) => value.replaceAll(RegExp(r'\D'), '');

  static bool isValid(String value) => _phonePattern.hasMatch(onlyDigits(value));

  static String mask(String value) {
    final String digits = onlyDigits(value);
    if (digits.length == 11) {
      return '(${digits.substring(0, 2)}) ${digits.substring(2, 7)}-${digits.substring(7)}';
    }
    if (digits.length == 10) {
      return '(${digits.substring(0, 2)}) ${digits.substring(2, 6)}-${digits.substring(6)}';
    }
    return value;
  }
}
