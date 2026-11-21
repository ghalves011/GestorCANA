package br.com.cana.model;

import java.util.List;

/**
 * Resumo mostrado antes de confirmar a virada de temporada.
 * Espelha o ResumoViradaDTO da API (GET /temporadas/virada/resumo).
 */
public class ResumoViradaTemporada {

    private Integer anoAtual;
    private Integer anoNovo;
    private long totalPartidasAnoAtual;
    private List<String> jogadoresSuspensos;
    private long contribuicoesPendentesAnoAtual;
    private boolean loteAnoNovoJaGerado;
    private List<String> avisos;

    public ResumoViradaTemporada() {
    }

    public Integer getAnoAtual() {
        return anoAtual;
    }

    public Integer getAnoNovo() {
        return anoNovo;
    }

    public long getTotalPartidasAnoAtual() {
        return totalPartidasAnoAtual;
    }

    public List<String> getJogadoresSuspensos() {
        return jogadoresSuspensos != null ? jogadoresSuspensos : List.of();
    }

    public long getContribuicoesPendentesAnoAtual() {
        return contribuicoesPendentesAnoAtual;
    }

    public boolean isLoteAnoNovoJaGerado() {
        return loteAnoNovoJaGerado;
    }

    public List<String> getAvisos() {
        return avisos != null ? avisos : List.of();
    }
}
