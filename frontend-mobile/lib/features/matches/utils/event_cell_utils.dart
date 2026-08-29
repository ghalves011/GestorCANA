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
