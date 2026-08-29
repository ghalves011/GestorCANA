package br.com.cana.model;

public class Temporada {

    private Integer id;
    private Integer ano;

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

    // Facilita a exibição direta do ano em ComboBoxes (JComboBox) do Swing
    @Override
    public String toString() {
        return this.ano != null ? String.valueOf(this.ano) : "";
    }
}