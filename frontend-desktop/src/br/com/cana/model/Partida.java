package br.com.cana.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.cana.util.DateUtil;
import br.com.cana.util.FormatadorUtil;

public class Partida {

    private Integer id;
    private Integer temporadaId;
    private String nomePartida;

    private LocalDate dataPartida;

    private Integer golsTimeAzul;
    private Integer golsTimeVermelho;

    private String formacaoAzul;
    private String formacaoVermelho;

    private String sumula;
    private String arbitro;
    private String bandeira1;
    private String bandeira2;

    private String gridAzul;
    private String gridVermelho;

    // Listas para trânsito de dados no Swing (preenchidas pelo Service/Controller)
    private List<Jogador> jogadoresAzul = new ArrayList<>();
    private List<Jogador> jogadoresVermelho = new ArrayList<>();
    private List<JogadorPartida> listaGeralPresenca = new ArrayList<>();

    // Construtor vazio (obrigatório para desserialização REST)
    public Partida() {
    }

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

    public String getNomePartida() {
        return nomePartida;
    }

    public void setNomePartida(String nomePartida) {
        this.nomePartida = nomePartida;
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

    public String getSumula() {
        return sumula;
    }

    public void setSumula(String sumula) {
        this.sumula = sumula;
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

    public List<Jogador> getJogadoresAzul() {
        return jogadoresAzul;
    }

    public void setJogadoresAzul(List<Jogador> jogadoresAzul) {
        this.jogadoresAzul = jogadoresAzul != null ? jogadoresAzul : new ArrayList<>();
    }

    public List<Jogador> getJogadoresVermelho() {
        return jogadoresVermelho;
    }

    public void setJogadoresVermelho(List<Jogador> jogadoresVermelho) {
        this.jogadoresVermelho = jogadoresVermelho != null ? jogadoresVermelho : new ArrayList<>();
    }

    public List<JogadorPartida> getListaGeralPresenca() {
        return listaGeralPresenca;
    }

    public void setListaGeralPresenca(List<JogadorPartida> listaGeralPresenca) {
        this.listaGeralPresenca = listaGeralPresenca != null ? listaGeralPresenca : new ArrayList<>();
    }

    // --- MÉTODOS VISUAIS PARA O SWING ---

    public String getPlacarFormatado() {
        return FormatadorUtil.formatarPlacar(getGolsTimeAzul(), getGolsTimeVermelho());
    }

    public String getDataPartidaFormatada() {
        return DateUtil.paraUsuario(this.dataPartida);
    }

    public String getGridAzul() { return gridAzul; }
    public void setGridAzul(String gridAzul) { this.gridAzul = gridAzul; }

    public String getGridVermelho() { return gridVermelho; }
    public void setGridVermelho(String gridVermelho) { this.gridVermelho = gridVermelho; }
}