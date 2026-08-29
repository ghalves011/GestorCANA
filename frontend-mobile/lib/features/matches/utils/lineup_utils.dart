import '../models/jogador_partida.dart';

/// Replaces POST /partidas/tem-goleiro-natural, which exists on the
/// backend but is a stub that always returns false (see plan §6). Inspects
/// the current lineup locally for a player whose posicao is GOLEIRO,
/// currently placed on the given team.
bool temGoleiroNatural(List<JogadorPartida> lineup, {required String time}) {
  for (final JogadorPartida jp in lineup) {
    final String? posicao = jp.jogador?.posicao;
    final bool mesmoTime = (jp.time ?? '').toLowerCase() == time.toLowerCase();
    if (mesmoTime && (posicao ?? '').trim().toUpperCase() == 'GOLEIRO') {
      return true;
    }
  }
  return false;
}
