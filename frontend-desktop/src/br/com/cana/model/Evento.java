package br.com.cana.model;

public class Evento {

    private Integer id;
    private String tipo; // "GOL", "AMARELO", "VERMELHO"
    private Integer jogadorId;
    private Integer partidaId;
    private String corTime; // "Azul" ou "Vermelho"
    private Integer minuto;

    // Relacionamentos para montagem de telas visuais (JTable, listas)
    private Jogador jogador;
    private Partida partida;

    // Construtor vazio (obrigatório para deserialização JSON)
    public Evento() {
    }

    // Construtor para novos lançamentos rápidos na tela
    public Evento(String tipo, Integer jogadorId, Integer partidaId, String corTime, Integer minuto) {
        this.tipo = tipo;
        this.jogadorId = jogadorId;
        this.partidaId = partidaId;
        this.corTime = corTime;
        this.minuto = minuto;
    }

    // --- GETTERS E SETTERS ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getJogadorId() {
        if (jogadorId == null && jogador != null) {
            return jogador.getId();
        }
        return jogadorId;
    }

    public void setJogadorId(Integer jogadorId) {
        this.jogadorId = jogadorId;
    }

    public Integer getPartidaId() {
        if (partidaId == null && partida != null) {
            return partida.getId();
        }
        return partidaId;
    }

    public void setPartidaId(Integer partidaId) {
        this.partidaId = partidaId;
    }

    public String getCorTime() {
        return corTime;
    }

    public void setCorTime(String corTime) {
        this.corTime = corTime;
    }

    public Integer getMinuto() {
        return minuto;
    }

    public void setMinuto(Integer minuto) {
        this.minuto = minuto;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public void setJogador(Jogador jogador) {
        this.jogador = jogador;
        if (jogador != null) {
            this.jogadorId = jogador.getId();
        }
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
        if (partida != null) {
            this.partidaId = partida.getId();
        }
    }
}