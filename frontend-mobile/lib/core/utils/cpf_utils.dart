/// Mirrors the desktop's CPF handling: 11-digit length check only (the
/// backend never validates the CPF check-digit either), plus a display
/// mask matching FormatadorUtil.mascaraCPF.
class CpfUtils {
  CpfUtils._();

  static final RegExp _cpfPattern = RegExp(r'^\d{11}$');

  static String onlyDigits(String value) => value.replaceAll(RegExp(r'\D'), '');

  static bool isValid(String value) => _cpfPattern.hasMatch(onlyDigits(value));

  static String mask(String value) {
    final String digits = onlyDigits(value);
    if (digits.length != 11) return value;
    return '${digits.substring(0, 3)}.${digits.substring(3, 6)}.'
        '${digits.substring(6, 9)}-${digits.substring(9, 11)}';
  }
}
