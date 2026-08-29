import 'package:flutter/material.dart';

/// Brand colors lifted from the desktop client's ImagemUtil design tokens,
/// kept identical so the mobile app reads as the same product.
class CanaColors {
  CanaColors._();

  static const Color fundo = Color(0xFFE9E4E4);
  static const Color azulGradiente = Color(0xFF3E2BE5);
  static const Color timeAzul = Color(0xFF1A1AFF);
  static const Color vermelhoGradiente = Color(0xFFEF3333);
  static const Color timeVermelho = Color(0xFFEF3333);
  static const Color amareloCartao = Color(0xFFFFCC00);
  static const Color vermelhoCartao = Color(0xFF8B0000);
}

class AppTheme {
  AppTheme._();

  static ThemeData light() {
    final ColorScheme scheme = ColorScheme.fromSeed(
      seedColor: CanaColors.azulGradiente,
      primary: CanaColors.azulGradiente,
      secondary: CanaColors.vermelhoGradiente,
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: CanaColors.fundo,
      appBarTheme: const AppBarTheme(
        backgroundColor: CanaColors.azulGradiente,
        foregroundColor: Colors.white,
        centerTitle: true,
        elevation: 0,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: CanaColors.azulGradiente,
          foregroundColor: Colors.white,
          padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 20),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
          textStyle: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white,
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
        contentPadding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      ),
      // Card shape/elevation intentionally left at the Material 3 default
      // (ThemeData.cardTheme's exact type — CardTheme vs CardThemeData —
      // has moved across recent Flutter releases; not worth pinning here).
    );
  }
}

/// Event token glyphs used throughout the live scoreboard, matching the
/// desktop's emoji-based mini-language (see EventoController docs).
class EventTokens {
  EventTokens._();

  static const String gol = '⚽';
  static const String golContra = '⚽(C)';
  static const String amarelo = '🟨';
  static const String vermelho = '🟥';
}
