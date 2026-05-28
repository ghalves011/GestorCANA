package br.com.cana.model;

import br.com.cana.util.FormatadorUtil;

public class JogadorPartida {
    private int id; // ID do registro na tabela jogador_partida
    private Jogador jogador;
    private Partida partida;
    private String time; // "Azul" ou "Vermelho"
    private String status; // "Titular" ou "Reserva"
    private String funcao; // "Goleiro", "Zagueiro", etc.
    private boolean substituido = false; // Campo novo para controle de substituição
    private int gols =0;
    private int cartaoAmarelo = 0;
    private int cartaoVermelho = 0;

    public JogadorPartida() {
    }

    // Construtor cheio (ajuda muito na hora de instanciar)
    public JogadorPartida(Jogador jogador, Partida partida, String time, String status, String funcao, int gols, int cartaoAmarelo, int cartaoVermelho) {
        this.jogador = jogador;
        this.partida = partida;
        this.time = time;
        this.status = status;
        this.funcao = funcao;
        this.gols = gols;
        this.cartaoAmarelo = cartaoAmarelo;
        this.cartaoVermelho = cartaoVermelho;
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Alterna entre titular e reserva (ex: para simular uma substituição)
    public void alternarStatus() {
        if (this.status != null && this.status.equalsIgnoreCase("Titular")) {
            this.status = "Reserva";
        } else {
            this.status = "Titular";
        }
    }

    
     // Define a função do jogador (Ex: Mudar de Zagueiro para Goleiro)
     
    public void definirFuncao(String novaFuncao) {
        this.funcao = novaFuncao;
    }

    /**
     * Verifica se o jogador está trabalhando na organização (Juiz, Mesário, etc.)
     * em vez de estar jogando.
     */
    public boolean isAtuandoComoStaff() {
        if (this.funcao == null)
            return false;
        return this.funcao.equalsIgnoreCase("Staff") ||
                this.funcao.equalsIgnoreCase("Arbitro") ||
                this.funcao.equalsIgnoreCase("Mesario");
    }

    /**
     * Retorna um resumo: "Gui - Time Azul [Titular] (Meia)"
     */
    public String getDescricaoCompleta() {
        return FormatadorUtil.formatarDescricaoJogador(
            this.jogador.getNome(),
            this.time,
            this.status,
            this.funcao
        );
    }

    /**
     * Move o jogador para um time e já coloca como titular.
     */
    public void entrarNoTime(String novoTime) {
        this.time = novoTime;
        this.status = "Titular";
    }

    /**
     * Foi para a reserva.
     */
    public void voltarParaReserva() {
        this.status = "Reserva";
    }

    // Getters e Setters
    public Jogador getJogador() {
        return jogador;
    }

    public void setJogador(Jogador jogador) {
        this.jogador = jogador;
    }

    public Partida getPartida() {
        return partida;
    }

    public void setPartida(Partida partida) {
        this.partida = partida;
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

    public boolean isSubstituido() {
        return substituido;
    }

    public void setSubstituido(boolean substituido) {
        this.substituido = substituido;
    }

    public int getGols() {
        return gols;
    }

    public void setGols(int gols) {
        this.gols = gols;
    }

    public int getCartaoAmarelo() {
        return cartaoAmarelo;
    }

    public void setCartaoAmarelo(int cartaoAmarelo) {
        this.cartaoAmarelo = cartaoAmarelo;
    }

    public int getCartaoVermelho() {
        return cartaoVermelho;
    }

    public void setCartaoVermelho(int cartaoVermelho) {
        this.cartaoVermelho = cartaoVermelho;
    }

}