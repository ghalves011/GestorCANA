package br.com.cana.gestorcana_api.dto;

import java.util.List;

/**
 * Resumo mostrado antes de confirmar a virada de temporada (RF05).
 * Só leitura: montar este resumo não altera nada no banco.
 */
public record ResumoViradaDTO(
        Integer anoAtual,
        Integer anoNovo,
        long totalPartidasAnoAtual,
        List<String> jogadoresSuspensos,
        long contribuicoesPendentesAnoAtual,
        boolean loteAnoNovoJaGerado,
        List<String> avisos
) {
}
