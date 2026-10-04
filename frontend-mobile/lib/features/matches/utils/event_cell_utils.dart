/// Local read-only helpers for the live-scoreboard mini-language cell text
/// ("nome1 (pos) TOKENS / nome2 TOKENS" — see EventoController docs).
///
/// The actual add/remove/reorder logic (token normalization into
/// goals/own-goals/yellows/reds order) is server-authoritative — this app
/// always sends the full cell text to POST /eventos/adicionar or /remover
/// and stores back whatever the server returns, rather than reimplementing
/// that ordering locally. These helpers only ever READ the active
/// (current/last) substitution block, for local display and for building
/// the token-removal menu.
library;

/// Returns the raw text of the active (last "/"-separated) substitution
/// block, unparsed.
String activeBlock(String cellText) => cellText.split('/').last.trim();

/// Returns just the token portion of the active block, stripping the
/// leading "nome (pos)" prefix (everything up to and including its FIRST
/// ")"). Deliberately the first, not the last: a token like "⚽(C)" (own
/// goal) contains its own closing paren later in the string, so scanning
/// from the end would wrongly eat part of the token stream. The leading
/// "(pos)" annotation, when present, is always the first parenthesised
/// group in the block since no token appears before it.
///
/// KNOWN LIMITATION: per the mini-language, only the FIRST block ("nome1
/// (pos) TOKENS") carries the position annotation — a block after a
/// substitution ("nome2 TOKENS") does not, so there is no reliable
/// delimiter to strip the name from the tokens there; this returns the
/// block unchanged in that case (name included) rather than guessing.
String activeTokensText(String cellText) {
  final String bloco = activeBlock(cellText);
  final int fechaParenteses = bloco.indexOf(')');
  return fechaParenteses == -1 ? bloco : bloco.substring(fechaParenteses + 1).trim();
}

/// Same as [activeTokensText] but split into individual tokens (e.g. for
/// building a "remove this token" menu).
List<String> activeTokens(String cellText) {
  return activeTokensText(cellText).split(' ').where((String t) => t.trim().isNotEmpty).toList();
}

/// Score recomputed from both teams' event cells: "⚽" counts for the
/// player's own team, "⚽(C)" (own goal) for the opponent. Used after edits
/// that drop events (removed player, undone substitution) so the scoreboard
/// never drifts from what's actually recorded.
({int azul, int vermelho}) calcularPlacar(Iterable<String> eventosAzul, Iterable<String> eventosVermelho) {
  ({int gols, int contra}) contar(Iterable<String> eventos) {
    int gols = 0;
    int contra = 0;
    for (final String ev in eventos) {
      final int contraNoBloco = '⚽(C)'.allMatches(ev).length;
      contra += contraNoBloco;
      gols += '⚽'.allMatches(ev).length - contraNoBloco;
    }
    return (gols: gols, contra: contra);
  }

  final ({int gols, int contra}) azul = contar(eventosAzul);
  final ({int gols, int contra}) vermelho = contar(eventosVermelho);
  return (azul: azul.gols + vermelho.contra, vermelho: vermelho.gols + azul.contra);
}

/// Drops the last " / " block of a slot's names or events text — undoing
/// its latest substitution ("J1 / J2" -> "J1", "⚽ / 🟨" -> "⚽").
String removerUltimoBloco(String texto, {int? manterBlocos}) {
  final List<String> blocos = texto.split(' / ');
  final int manter = manterBlocos ?? blocos.length - 1;
  return blocos.take(manter.clamp(0, blocos.length)).join(' / ');
}
