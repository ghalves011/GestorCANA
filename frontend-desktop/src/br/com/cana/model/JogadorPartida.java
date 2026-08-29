package br.com.cana.model;

import br.com.cana.util.FormatadorUtil;

public class JogadorPartida {

    private Integer id;
    private Integer jogadorId;
    private Integer partidaId;

    private String time; // "Azul" ou "Vermelho"
    private String status; // "Titular" ou "Reserva"
    private String funcao; // "Goleiro", "Zagueiro", etc.
    private String presente; // "SIM" ou "NAO"
    private Boolean substituido;

    private Integer gols;
    private Integer cartaoAmarelo;
    private Integer cartaoVermelho;

    // Relacionamentos para renderização em componentes Swing (JTable, JList)
    private Jogador jogador;
    private Partida partida;

    // Construtor vazio (obrigatório para desserialização REST)
    public JogadorPartida() {
    }

    // Construtor completo
    public JogadorPartida(Jogador jogador, Partida partida, String time, String status, String funcao, Integer gols, Integer cartaoAmarelo, Integer cartaoVermelho) {
        this.jogador = jogador;
        this.partida = partida;
        if (jogador != null) this.jogadorId = jogador.getId();
        if (partida != null) this.partidaId = partida.getId();
        this.time = time;
        this.status = status;
        this.funcao = funcao;
        this.gols = gols != null ? gols : 0;
        this.cartaoAmarelo = cartaoAmarelo != null ? cartaoAmarelo : 0;
        this.cartaoVermelho = cartaoVermelho != null ? cartaoVermelho : 0;
    }

    // --- GETTERS E SETTERS ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFuncao() {
        return funcao;
    }

    public void setFuncao(String funcao) {
        this.funcao = funcao;
    }

    public String getPresente() {
        return presente;
    }

    public void setPresente(String presente) {
        this.presente = presente;
    }

    public Boolean getSubstituido() {
        return substituido != null ? substituido : false;
    }

    public void setSubstituido(Boolean substituido) {
        this.substituido = substituido;
    }

    public Integer getGols() {
        return gols != null ? gols : 0;
    }

    public void setGols(Integer gols) {
        this.gols = gols;
    }

    public Integer getCartaoAmarelo() {
        return cartaoAmarelo != null ? cartaoAmarelo : 0;
    }

    public void setCartaoAmarelo(Integer cartaoAmarelo) {
        this.cartaoAmarelo = cartaoAmarelo;
    }

    public Integer getCartaoVermelho() {
        return cartaoVermelho != null ? cartaoVermelho : 0;
    }

    public void setCartaoVermelho(Integer cartaoVermelho) {
        this.cartaoVermelho = cartaoVermelho;
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

    // --- MÉTODOS VISUAIS PARA O SWING ---

    /**
     * Retorna a descrição formatada usando o apelido (se existir) ou nome.
     * Ex: "Gui - Time Azul [Titular] (Meia)"
     */
    public String getDescricaoCompleta() {
        return FormatadorUtil.formatarDescricaoJogador(this.jogador, this.time, this.status, this.funcao);
    }
}