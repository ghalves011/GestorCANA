package br.com.cana.gestorcana_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "jogadorpartida")
public class JogadorPartida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "jogador_id", nullable = false)
    private Integer jogadorId;

    @Transient
    private Jogador jogador;

    @Column(name = "partida_id", nullable = false)
    private Integer partidaId;

    private String time; // "AZUL" ou "VERMELHO"
    private String status; // "TITULAR", "RESERVA", "SUBSTITUIDO"
    private String funcao;
    private String presente; // "Sim" ou "Não"

    private Integer gols = 0;

    @Column(name = "cartao_amarelo")
    private Integer cartaoAmarelo = 0;

    @Column(name = "cartao_vermelho")
    private Integer cartaoVermelho = 0;

    // --- GETTERS E SETTERS ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getJogadorId() { return jogadorId; }
    public void setJogadorId(Integer jogadorId) { this.jogadorId = jogadorId; }

    public Integer getPartidaId() { return partidaId; }
    public void setPartidaId(Integer partidaId) { this.partidaId = partidaId; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFuncao() { return funcao; }
    public void setFuncao(String funcao) { this.funcao = funcao; }

    public String getPresente() { return presente; }
    public void setPresente(String presente) { this.presente = presente; }

    public Integer getGols() { return gols != null ? gols : 0; }
    public void setGols(Integer gols) { this.gols = gols; }

    public Integer getCartaoAmarelo() { return cartaoAmarelo != null ? cartaoAmarelo : 0; }
    public void setCartaoAmarelo(Integer cartaoAmarelo) { this.cartaoAmarelo = cartaoAmarelo; }

    public Integer getCartaoVermelho() { return cartaoVermelho != null ? cartaoVermelho : 0; }
    public void setCartaoVermelho(Integer cartaoVermelho) { this.cartaoVermelho = cartaoVermelho; }

    public Jogador getJogador() { return jogador; }
    public void setJogador(Jogador jogador) { this.jogador = jogador; }
}