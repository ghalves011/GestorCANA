package br.com.cana.gestorcana_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "jogador")
public class Jogador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    private String apelido;
    private String cpf;
    private String rg;

    @Column(name = "datanasc")
    private LocalDate dataNascimento;

    @Column(name = "dataadmissao")
    private LocalDate dataAdmissao;

    private String telefone;

    @Column(name = "telefoneemergencia")
    private String telefoneEmergencia;

    private String posicao;
    private Integer nivel;

    @Column(name = "pedominante")
    private String peDominante;

    private Double altura;
    private Double peso;

    @Column(name = "timeanterior")
    private String timeAnterior;

    @Column(name = "tempoexperiencia")
    private Integer tempoExperiencia;

    @Column(name = "padrinho_id")
    private Integer padrinhoId;

    @Column(name = "graurelacaopadrinho")
    private String grauRelacaoPadrinho;

    @Column(name = "estasuspenso")
    private Boolean estaSuspenso = false;

    @Column(name = "estaautorizado")
    private Boolean estaAutorizado = true;

    @Column(name = "mensalidadeemdia")
    private Boolean mensalidadeEmDia = true;

    @Column(name = "numcamisa")
    private Integer numCamisa;

    @Column(name = "golsiniciais")
    private Integer golsIniciais = 0;

    @Column(name = "camarelosiniciais")
    private Integer cAmarelosIniciais = 0;

    @Column(name = "cvermelhosiniciais")
    private Integer cVermelhosIniciais = 0;

    // Exclusão lógica: jogador com partidas no histórico não pode sair do banco
    // (FK de jogadorpartida), então "excluir" só o esconde das listagens.
    @Column(name = "ativo", columnDefinition = "boolean default true")
    private Boolean ativo = true;

    // Relacionamento otimizado para evitar estouro de proxy e consultas extras
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_endereco", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Endereco endereco;

    @Transient
    private String status;

    // --- GETTERS E SETTERS ---

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getApelido() { return apelido; }
    public void setApelido(String apelido) { this.apelido = apelido; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public LocalDate getDataAdmissao() { return dataAdmissao; }
    public void setDataAdmissao(LocalDate dataAdmissao) { this.dataAdmissao = dataAdmissao; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getTelefoneEmergencia() { return telefoneEmergencia; }
    public void setTelefoneEmergencia(String telefoneEmergencia) { this.telefoneEmergencia = telefoneEmergencia; }

    public String getPosicao() { return posicao; }
    public void setPosicao(String posicao) { this.posicao = posicao; }

    public Integer getNivel() { return nivel != null ? nivel : 50; }
    public void setNivel(Integer nivel) { this.nivel = nivel; }

    public String getPeDominante() { return peDominante; }
    public void setPeDominante(String peDominante) { this.peDominante = peDominante; }

    public Double getAltura() { return altura; }
    public void setAltura(Double altura) { this.altura = altura; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public String getTimeAnterior() { return timeAnterior; }
    public void setTimeAnterior(String timeAnterior) { this.timeAnterior = timeAnterior; }

    public Integer getTempoExperiencia() { return tempoExperiencia; }
    public void setTempoExperiencia(Integer tempoExperiencia) { this.tempoExperiencia = tempoExperiencia; }

    public Integer getPadrinhoId() { return padrinhoId; }
    public void setPadrinhoId(Integer padrinhoId) { this.padrinhoId = padrinhoId; }

    public String getGrauRelacaoPadrinho() { return grauRelacaoPadrinho; }
    public void setGrauRelacaoPadrinho(String grauRelacaoPadrinho) { this.grauRelacaoPadrinho = grauRelacaoPadrinho; }

    public Boolean getEstaSuspenso() { return estaSuspenso != null ? estaSuspenso : false; }
    public Boolean isEstaSuspenso() { return getEstaSuspenso(); }
    public void setEstaSuspenso(Boolean estaSuspenso) { this.estaSuspenso = estaSuspenso; }

    public Boolean getEstaAutorizado() { return estaAutorizado != null ? estaAutorizado : true; }
    public void setEstaAutorizado(Boolean estaAutorizado) { this.estaAutorizado = estaAutorizado; }

    public Boolean getMensalidadeEmDia() { return mensalidadeEmDia != null ? mensalidadeEmDia : true; }
    public void setMensalidadeEmDia(Boolean mensalidadeEmDia) { this.mensalidadeEmDia = mensalidadeEmDia; }

    public Integer getNumCamisa() { return numCamisa; }
    public void setNumCamisa(Integer numCamisa) { this.numCamisa = numCamisa; }

    public Integer getGolsIniciais() { return golsIniciais != null ? golsIniciais : 0; }
    public void setGolsIniciais(Integer golsIniciais) { this.golsIniciais = golsIniciais; }

    public Integer getcAmarelosIniciais() { return cAmarelosIniciais != null ? cAmarelosIniciais : 0; }
    public void setcAmarelosIniciais(Integer cAmarelosIniciais) { this.cAmarelosIniciais = cAmarelosIniciais; }

    public Integer getcVermelhosIniciais() { return cVermelhosIniciais != null ? cVermelhosIniciais : 0; }
    public void setcVermelhosIniciais(Integer cVermelhosIniciais) { this.cVermelhosIniciais = cVermelhosIniciais; }

    public Boolean getAtivo() { return ativo != null ? ativo : true; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public String getStatus() {
        if (status != null) return status;
        return Boolean.TRUE.equals(estaSuspenso) ? "Suspenso" : "Ativo";
    }
    public void setStatus(String status) {
        this.status = status;
        this.estaSuspenso = "Suspenso".equalsIgnoreCase(status);
    }
}