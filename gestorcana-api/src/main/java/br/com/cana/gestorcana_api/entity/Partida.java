package br.com.cana.gestorcana_api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "partida")
public class Partida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "temporada_id", nullable = false)
    private Integer temporadaId;

    @Column(name = "data")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataPartida;

    @Column(name = "golstimeazul")
    private Integer golsTimeAzul = 0;

    @Column(name = "golstimevermelho")
    private Integer golsTimeVermelho = 0;

    @Column(name = "nomepartida")
    private String nomePartida;

    private String arbitro;
    private String bandeira1;
    private String bandeira2;

    @Column(columnDefinition = "text")
    private String sumula;

    @Column(name = "grid_azul", columnDefinition = "text")
    private String gridAzul;

    @Column(name = "grid_vermelho", columnDefinition = "text")
    private String gridVermelho;

    // --- CAMPOS EM MEMÓRIA (NÃO PERSISTIDOS NO BANCO) ---
    @Transient
    private String formacaoAzul;

    @Transient
    private String formacaoVermelho;

    @Transient
    private List<Jogador> jogadoresAzul = new ArrayList<>();

    @Transient
    private List<Jogador> jogadoresVermelho = new ArrayList<>();

    @Transient
    private List<JogadorPartida> listaGeralPresenca = new ArrayList<>();

    // --- GETTERS E SETTERS ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTemporadaId() {
        return temporadaId;
    }

    public void setTemporadaId(Integer temporadaId) {
        this.temporadaId = temporadaId;
    }

    public LocalDate getDataPartida() {
        return dataPartida;
    }

    public void setDataPartida(LocalDate dataPartida) {
        this.dataPartida = dataPartida;
    }

    public Integer getGolsTimeAzul() {
        return golsTimeAzul != null ? golsTimeAzul : 0;
    }

    public void setGolsTimeAzul(Integer golsTimeAzul) {
        this.golsTimeAzul = golsTimeAzul;
    }

    public Integer getGolsTimeVermelho() {
        return golsTimeVermelho != null ? golsTimeVermelho : 0;
    }

    public void setGolsTimeVermelho(Integer golsTimeVermelho) {
        this.golsTimeVermelho = golsTimeVermelho;
    }

    public String getNomePartida() {
        return nomePartida;
    }

    public void setNomePartida(String nomePartida) {
        this.nomePartida = nomePartida;
    }

    public String getArbitro() {
        return arbitro;
    }

    public void setArbitro(String arbitro) {
        this.arbitro = arbitro;
    }

    public String getBandeira1() {
        return bandeira1;
    }

    public void setBandeira1(String bandeira1) {
        this.bandeira1 = bandeira1;
    }

    public String getBandeira2() {
        return bandeira2;
    }

    public void setBandeira2(String bandeira2) {
        this.bandeira2 = bandeira2;
    }

    public String getSumula() {
        return sumula;
    }

    public void setSumula(String sumula) {
        this.sumula = sumula;
    }

    public String getFormacaoAzul() {
        return formacaoAzul;
    }

    public void setFormacaoAzul(String formacaoAzul) {
        this.formacaoAzul = formacaoAzul;
    }

    public String getFormacaoVermelho() {
        return formacaoVermelho;
    }

    public void setFormacaoVermelho(String formacaoVermelho) {
        this.formacaoVermelho = formacaoVermelho;
    }

    public List<Jogador> getJogadoresAzul() {
        return jogadoresAzul;
    }

    public void setJogadoresAzul(List<Jogador> jogadoresAzul) {
        this.jogadoresAzul = jogadoresAzul;
    }

    public List<Jogador> getJogadoresVermelho() {
        return jogadoresVermelho;
    }

    public void setJogadoresVermelho(List<Jogador> jogadoresVermelho) {
        this.jogadoresVermelho = jogadoresVermelho;
    }

    public List<JogadorPartida> getListaGeralPresenca() {
        return listaGeralPresenca;
    }

    public void setListaGeralPresenca(List<JogadorPartida> listaGeralPresenca) {
        this.listaGeralPresenca = listaGeralPresenca;
    }

    public String getGridAzul() {
        return gridAzul;
    }

    public void setGridAzul(String gridAzul) {
        this.gridAzul = gridAzul;
    }

    public String getGridVermelho() {
        return gridVermelho;
    }

    public void setGridVermelho(String gridVermelho) {
        this.gridVermelho = gridVermelho;
    }
}