package br.com.cana.model;

public class Temporada {

    private Integer id;
    private Integer ano;
    private String status; // "VIGENTE" ou "ENCERRADA", vindo da API

    // Construtor vazio (obrigatório para desserialização REST)
    public Temporada() {
    }

    // Construtor sem ID (para criação de nova temporada)
    public Temporada(Integer ano) {
        this.ano = ano;
    }

    // Construtor completo
    public Temporada(Integer id, Integer ano) {
        this.id = id;
        this.ano = ano;
    }

    // --- GETTERS E SETTERS ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isVigente() {
        return "VIGENTE".equalsIgnoreCase(status);
    }

    // Texto exibido nos ComboBoxes: "2027 (vigente)" ou "2026 (encerrada)".
    // O combo guarda o próprio objeto, então nada precisa ser extraído deste texto.
    @Override
    public String toString() {
        if (this.ano == null) {
            return "";
        }
        if (status == null || status.isBlank()) {
            return String.valueOf(this.ano);
        }
        return this.ano + (isVigente() ? " (vigente)" : " (encerrada)");
    }
}