package br.com.cana.gestorcana_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "evento")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "jogador_id", nullable = false)
    private Integer jogadorId;

    @Column(name = "partida_id", nullable = false)
    private Integer partidaId;

    @Column(name = "cortime")
    private String corTime;

    private Integer minuto;

    @Column(name = "tipoevento")
    private String tipoEvento; // "GOL", "AMARELO", "VERMELHO"

    // --- GETTERS E SETTERS ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Integer getJogadorId() { return jogadorId; }
    public void setJogadorId(Integer jogadorId) { this.jogadorId = jogadorId; }

    public Integer getPartidaId() { return partidaId; }
    public void setPartidaId(Integer partidaId) { this.partidaId = partidaId; }

    public String getCorTime() { return corTime; }
    public void setCorTime(String corTime) { this.corTime = corTime; }

    public Integer getMinuto() { return minuto; }
    public void setMinuto(Integer minuto) { this.minuto = minuto; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
}