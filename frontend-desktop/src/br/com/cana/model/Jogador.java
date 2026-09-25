package br.com.cana.model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import br.com.cana.util.FormatadorUtil;

public class Jogador {

    // Identificação e Dados Pessoais
    private Integer id;
    private String nome;
    private String apelido;

    @SerializedName("dataNascimento")
    private LocalDate dataNasc;
    
    private String rg;
    private String cpf;
    private String telefone;
    private String telefoneEmergencia;

    // Endereço
    private Integer enderecoId;
    private Endereco endereco;

    // Dados Esportivos
    private String posicao;
    private String peDominante;
    private Double altura;
    private Double peso;
    private String timeAnterior;
    private Integer tempoExperiencia;
    private Integer numCamisa;

    // Regras e Atributos CANA
    private Integer nivel;
    private Integer padrinhoId;
    private String grauRelacaoPadrinho;
    private LocalDate dataAdmissao;
    private String status;
    private Boolean estaSuspenso;
    private Boolean estaAutorizado;
    private Boolean mensalidadeEmDia;
    // false = excluído com histórico (inativado); só volta pelo botão Reativar
    private Boolean ativo;

    // Estatísticas Iniciais
    private Integer golsIniciais;
    private Integer cAmarelosIniciais;
    private Integer cVermelhosIniciais;
    private LocalDate dataUltimaSuspensaoCumprida;

    // Construtor vazio (obrigatório para desserialização REST)
    public Jogador() {
    }

    // --- GETTERS E SETTERS ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public LocalDate getDataNasc() {
        return dataNasc;
    }

    public void setDataNasc(LocalDate dataNasc) {
        this.dataNasc = dataNasc;
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

    public Integer getEnderecoId() {
        if (enderecoId == null && endereco != null) {
            return endereco.getId();
        }
        return enderecoId;
    }

    public void setEnderecoId(Integer enderecoId) {
        this.enderecoId = enderecoId;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
        if (endereco != null) {
            this.enderecoId = endereco.getId();
        }
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

    public Double getAltura() {
        return altura;
    }

    public void setAltura(Double altura) {
        this.altura = altura;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
    }

    public String getTimeAnterior() {
        return timeAnterior;
    }

    public void setTimeAnterior(String timeAnterior) {
        this.timeAnterior = timeAnterior;
    }

    public Integer getTempoExperiencia() {
        return tempoExperiencia;
    }

    public void setTempoExperiencia(Integer tempoExperiencia) {
        this.tempoExperiencia = tempoExperiencia;
    }

    public Integer getNumCamisa() {
        return numCamisa;
    }

    public void setNumCamisa(Integer numCamisa) {
        this.numCamisa = numCamisa;
    }

    public Integer getNivel() {
        return nivel;
    }

    public void setNivel(Integer nivel) {
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

    public LocalDate getDataAdmissao() {
        return dataAdmissao;
    }

    public void setDataAdmissao(LocalDate dataAdmissao) {
        this.dataAdmissao = dataAdmissao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getEstaSuspenso() {
        return estaSuspenso != null ? estaSuspenso : false;
    }

    public void setEstaSuspenso(Boolean estaSuspenso) {
        this.estaSuspenso = estaSuspenso;
    }

    public Boolean getEstaAutorizado() {
        return estaAutorizado != null ? estaAutorizado : true;
    }

    public void setEstaAutorizado(Boolean estaAutorizado) {
        this.estaAutorizado = estaAutorizado;
    }

    public Boolean getMensalidadeEmDia() {
        return mensalidadeEmDia != null ? mensalidadeEmDia : true;
    }

    public void setMensalidadeEmDia(Boolean mensalidadeEmDia) {
        this.mensalidadeEmDia = mensalidadeEmDia;
    }

    public Boolean getAtivo() {
        return ativo != null ? ativo : true;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Integer getGolsIniciais() {
        return golsIniciais != null ? golsIniciais : 0;
    }

    public void setGolsIniciais(Integer golsIniciais) {
        this.golsIniciais = golsIniciais;
    }

    public Integer getCAmarelosIniciais() {
        return cAmarelosIniciais != null ? cAmarelosIniciais : 0;
    }

    public void setCAmarelosIniciais(Integer cAmarelosIniciais) {
        this.cAmarelosIniciais = cAmarelosIniciais;
    }

    public Integer getCVermelhosIniciais() {
        return cVermelhosIniciais != null ? cVermelhosIniciais : 0;
    }

    public void setCVermelhosIniciais(Integer cVermelhosIniciais) {
        this.cVermelhosIniciais = cVermelhosIniciais;
    }

    public LocalDate getDataUltimaSuspensaoCumprida() {
        return dataUltimaSuspensaoCumprida;
    }

    public void setDataUltimaSuspensaoCumprida(LocalDate dataUltimaSuspensaoCumprida) {
        this.dataUltimaSuspensaoCumprida = dataUltimaSuspensaoCumprida;
    }

    // --- MÉTODOS AUXILIARES VISUAIS PARA O SWING ---

    public boolean isAptoParaJogar() {
        return !getEstaSuspenso() && getEstaAutorizado() && getMensalidadeEmDia();
    }

    public String getResumo() {
        String statusTexto = isAptoParaJogar() ? "LIBERADO" : "PENDENTE/SUSPENSO";
        return String.format("Jogador: %s | Posição: %s | Nível: %d | CPF: %s | Status: %s",
                this.nome,
                this.posicao,
                this.nivel != null ? this.nivel : 0,
                FormatadorUtil.mascaraCPF(this.cpf),
                statusTexto);
    }

    public String getResumoTime() {
        return String.format("%s - %s (%d)",
                this.posicao != null ? this.posicao : "N/A",
                this.apelido != null && !this.apelido.isEmpty() ? this.apelido : this.nome,
                this.numCamisa != null ? this.numCamisa : 0);
    }

    @Override
    public String toString() {
        return (this.nome == null) ? "" : this.nome;
    }
}