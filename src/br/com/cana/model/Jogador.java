package br.com.cana.model;

import java.time.LocalDate;

import br.com.cana.util.FormatadorUtil;

public class Jogador {

    // Identificação e Dados Pessoais
    private int id;
    private String nome;
    private String apelido; // Opcional, pode ser usado para exibir um nome mais curto ou conhecido
    private LocalDate dataNascimento;
    private String rg;
    private String cpf;
    private String telefone;
    private String telefoneEmergencia;

    // Endereço
    private Endereco endereco; // Composição: Jogador tem um Endereço

    // Dados Esportivos
    private String posicao;
    private String peDominante;
    private double altura;
    private double peso;
    private String timeAnterior;
    private int tempoExperiencia; // em anos
    private int numCamisa; // Número da camisa do jogador

    // Regras de Negócio do CANA
    private int nivel; // 1 a 100
    private Integer padrinhoId; // Integer permite ser 'null' caso não tenha padrinho
    private String grauRelacaoPadrinho; // "FAMILIAR", "AMIGO", "EX_JOGADOR" ou "OUTRO"
    private LocalDate dataAdmissao;
    public LocalDate getDataUltimaSuspensaoCumprida() {
        return dataUltimaSuspensaoCumprida;
    }

    public void setDataUltimaSuspensaoCumprida(LocalDate dataUltimaSuspensaoCumprida) {
        this.dataUltimaSuspensaoCumprida = dataUltimaSuspensaoCumprida;
    }

    private String status;
    private boolean estaSuspenso; // Indica se o jogador está suspenso
    private boolean estaAutorizado; // Indica se o jogador está autorizado a jogar
    private boolean mensalidadeEmDia; // Indica se a mensalidade do jogador está em dia
    private int golsIniciais; // Estatística de gols (pode ser atualizada manualmente)
    private int cAmarelosIniciais; // Estatística de cartões amarelos (pode ser atualizada manualmente)
    private int cVermelhosIniciais; // Estatística de cartões vermelhos (pode ser atualizada manualmente)
    private LocalDate dataUltimaSuspensaoCumprida; // Para controlar quando o jogador cumpriu a última suspensão

    // Construtor vazio 
    public Jogador() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getTelefoneEmergencia() {
        return telefoneEmergencia;
    }

    public void setTelefoneEmergencia(String telefoneEmergencia) {
        this.telefoneEmergencia = telefoneEmergencia;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public void setEnderecoId(int id) {
        if (this.endereco == null) {
            this.endereco = new Endereco();
        }
        this.endereco.setId(id);
    }

    public String getPosicao() {
        return posicao;
    }

    public void setPosicao(String posicao) {
        this.posicao = posicao;
    }

    public String getPeDominante() {
        return peDominante;
    }

    public void setPeDominante(String peDominante) {
        this.peDominante = peDominante;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getTimeAnterior() {
        return timeAnterior;
    }

    public void setTimeAnterior(String timeAnterior) {
        this.timeAnterior = timeAnterior;
    }

    public int getTempoExperiencia() {
        return tempoExperiencia;
    }

    public void setTempoExperiencia(int tempoExperiencia) {
        this.tempoExperiencia = tempoExperiencia;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    public Integer getPadrinhoId() {
        return padrinhoId;
    }

    public void setPadrinhoId(Integer padrinhoId) {
        this.padrinhoId = padrinhoId;
    }

    public String getGrauRelacaoPadrinho() {
        return grauRelacaoPadrinho;
    }

    public void setGrauRelacaoPadrinho(String grauRelacaoPadrinho) {
        this.grauRelacaoPadrinho = grauRelacaoPadrinho;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getNumCamisa() {
        return numCamisa;
    }

    public void setNumCamisa(int numCamisa) {
        this.numCamisa = numCamisa;
    }

    public boolean isEstaSuspenso() {
        return estaSuspenso;
    }

    public void setEstaSuspenso(boolean estaSuspenso) {
        this.estaSuspenso = estaSuspenso;
    }

    public boolean isEstaAutorizado() {
        return estaAutorizado;
    }

    public void setEstaAutorizado(boolean estaAutorizado) {
        this.estaAutorizado = estaAutorizado;
    }

    public boolean isMensalidadeEmDia() {
        return mensalidadeEmDia;
    }

    public void setMensalidadeEmDia(boolean mensalidadeEmDia) {
        this.mensalidadeEmDia = mensalidadeEmDia;
    }

    public int getGolsIniciais() {
        return golsIniciais;
    }

    public void setGolsIniciais(int golsIniciais) {
        this.golsIniciais = golsIniciais;
    }

    public int getcAmarelosIniciais() {
        return cAmarelosIniciais;
    }

    public void setcAmarelosIniciais(int cAmarelosIniciais) {
        this.cAmarelosIniciais = cAmarelosIniciais;
    }

    public int getcVermelhosIniciais() {
        return cVermelhosIniciais;
    }

    public void setcVermelhosIniciais(int cVermelhosIniciais) {
        this.cVermelhosIniciais = cVermelhosIniciais;
    }

    // Método para verificar se o jogador está apto para jogar

    public boolean isAptoParaJogar() {

        // O jogador só pode jogar se:
        // 1. NÃO estiver suspenso
        // 2. ESTIVER autorizado
        // 3. A mensalidade ESTIVER em dia

        return !this.estaSuspenso && this.estaAutorizado && this.mensalidadeEmDia;
    }

    // Método para obter um resumo do jogador (para exibição em listas ou detalhes)

    public String getResumo() {
        // 1. Lógica de status (interna do model)
        String statusTexto = this.isAptoParaJogar() ? "LIBERADO" : "PENDENTE/SUSPENSO";

        // 2. Montagem do texto usando os formatadores do utilitário (ex:
        // FormatadorUtil.mascaraCPF) e os dados do jogador
        return String.format("Jogador: %s | Posição: %s | Nível: %d | CPF: %s | Status: %s",
                this.nome,
                this.posicao,
                this.nivel,
                FormatadorUtil.mascaraCPF(this.cpf),
                statusTexto);
    }

    // Método para adicionar estatísticas manuais

    public void adicionarEstatisticasManuais(int gols, int amarelos, int vermelhos) {
        this.golsIniciais += gols;
        this.cAmarelosIniciais += amarelos;
        this.cVermelhosIniciais += vermelhos;

        System.out.println("Estatísticas atualizadas para " + this.nome);
    }

    @Override
    public String toString() {
        return (this.nome == null) ? "" : this.nome;
    }

    // Resumo simplificado para aparecer na escalação dos times
    public String getResumoTime() {
        return String.format("%s - %s (%d)",
                this.posicao,
                this.apelido != null && !this.apelido.isEmpty() ? this.apelido : this.nome,
                this.numCamisa);
    }

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

}
