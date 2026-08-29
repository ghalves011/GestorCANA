/// Mirrors FormatadorUtil.formatarPlacar: "AZUL 2 x 1 VERMELHO".
class ScoreFormatUtils {
  ScoreFormatUtils._();

  static String formatarPlacar(int golsAzul, int golsVermelho) {
    return 'AZUL $golsAzul x $golsVermelho VERMELHO';
  }
}
