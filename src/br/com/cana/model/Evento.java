package br.com.cana.model;

import br.com.cana.util.FormatadorUtil;

public class Evento {

    private int id;
    private String tipo; // "GOL", "AMARELO", "VERMELHO"
    private Jogador jogador;
    private Partida partida;
    private String corTime; // "Azul" ou "Vermelho"

    // Construtor vazio
    public Evento() {
    }

    // Construtor cheio para facilitar a criação durante o jogo
    public Evento(String tipo, Jogador jogador, Partida partida, String corTime) {
        this.tipo = tipo;
        this.jogador = jogador;
        this.partida = partida;
        this.corTime = corTime;
    }

    // --- MÉTODOS DE REGRA DE NEGÓCIO ---

    /**
     * Retorna uma descrição amigável do evento para a súmula.
     * Ex: "GOL - Guilherme (Time Azul)"
     */
    public String getDescricao() {
        String nome = (jogador != null) ? jogador.getNome() : "Desconhecido";
        return FormatadorUtil.formatarDescricaoEvento(this.tipo, nome, this.corTime);
    }

    // --- GETTERS E SETTERS ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

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

    public String getCorTime() {
        return corTime;
    }

    public void setCorTime(String corTime) {
        this.corTime = corTime;
    }

}