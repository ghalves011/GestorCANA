/// Mirrors ValidacaoUtil.validarRG on both backend and desktop: 7-9 digits.
class RgUtils {
  RgUtils._();

  static final RegExp _rgPattern = RegExp(r'^\d{7,9}$');

  static String onlyDigits(String value) => value.replaceAll(RegExp(r'\D'), '');

  static bool isValid(String value) => _rgPattern.hasMatch(onlyDigits(value));
}
