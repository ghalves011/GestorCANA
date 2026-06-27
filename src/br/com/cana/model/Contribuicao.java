package br.com.cana.model;

import java.time.LocalDate;

import br.com.cana.util.DateUtil;
import br.com.cana.util.FormatadorUtil;

public class Contribuicao {

    private int id;
    private int mes;
    private int ano;
    private boolean pago;
    private LocalDate dataPagamento;
    private double valor;
    private Jogador jogador; // A associação "Jogador tem Contribuições"

    // Construtor vazio
    public Contribuicao() {
    }

    // Métodos de Regra de Negócio

    /**
     * Marca a contribuição como paga e registra a data atual.
     */
    public void registrarPagamento() {
        this.pago = true;
        this.dataPagamento = LocalDate.now();
    }

    /**
     * Retorna o mês e ano de referência (Ex: 04/2026)
     */
    public String getReferencia() {
        return String.format("%02d/%d", this.mes, this.ano);
    }

    /**
     * Gera um texto pronto para enviar ao jogador
     */
    public String getReciboFormatado() {
        if (!pago) return "Esta contribuição ainda não foi paga.";

        return String.format(
                "⚽ *RECIBO CANA*\n" +
                "Recebemos de: %s\n" +
                "Referente a: %s\n" +
                "Valor: %s\n" +
                "Pagamento realizado em: %s",
                this.jogador.getNome(),
                FormatadorUtil.formatarReferencia(this.mes, this.ano),
                FormatadorUtil.formatarMoeda(this.valor),
                DateUtil.paraUsuario(this.dataPagamento) // Usa o Util!
        );
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public int getAno() {
        return ano;
    }

    public void setAno(int ano) {
        this.ano = ano;
    }

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public Jogador getJogador() {
        return jogador;
    }

    public void setJogador(Jogador jogador) {
        this.jogador = jogador;
    }
}