package br.com.cana.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import br.com.cana.util.DateUtil;
import br.com.cana.util.FormatadorUtil;

public class Partida {

    private int id;
    private int temporadaId; // FK para ligar ao Evento
    private String nomePartida;
    private LocalDate dataPartida;
    private int golsTimeAzul;
    private int golsTimeVermelho;
    private String formacaoAzul;
    private String formacaoVermelho;
    private String sumula;
    private String arbitro;
    private String bandeira1;   
    private String bandeira2;

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

    // Listas para trânsito de dados (o Service vai preencher estas listas)
    private List<Jogador> jogadoresAzul = new ArrayList<>();
    private List<Jogador> jogadoresVermelho = new ArrayList<>();
    private List<JogadorPartida> listaGeralPresenca = new ArrayList<>();

    public Partida() {
    }

    // Getters e Setters - incluindo métodos auxiliares para formatação

    public String getNomePartida() {
        return nomePartida;
    }

    public void setNomePartida(String nomePartida) {
        this.nomePartida = nomePartida;
    }

    public String getPlacarFormatado() {
        return FormatadorUtil.formatarPlacar(golsTimeAzul, golsTimeVermelho);
    }

    public String getDataPartidaFormatada() {
        return DateUtil.paraUsuario(dataPartida);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTemporadaId() {
        return temporadaId;
    }

    public void setTemporadaId(int temporadaId) {
        this.temporadaId = temporadaId;
    }

    public LocalDate getDataPartida() {
        return dataPartida;
    }

    public void setDataPartida(LocalDate dataPartida) {
        this.dataPartida = dataPartida;
    }

    public int getGolsTimeAzul() {
        return golsTimeAzul;
    }

    public void setGolsTimeAzul(int golsTimeAzul) {
        this.golsTimeAzul = golsTimeAzul;
    }

    public int getGolsTimeVermelho() {
        return golsTimeVermelho;
    }

    public void setGolsTimeVermelho(int golsTimeVermelho) {
        this.golsTimeVermelho = golsTimeVermelho;
    }

    public String getSumula() {
        return sumula;
    }

    public void setSumula(String sumula) {
        this.sumula = sumula;
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

}