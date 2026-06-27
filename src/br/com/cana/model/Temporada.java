package br.com.cana.model;

public class Temporada {
    private int id;
    private int ano;

    // Construtores
    public Temporada() {}
    public Temporada(int id, int ano) {
        this.id = id;
        this.ano = ano;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    // O toString ajuda a exibir o ano diretamente em ComboBoxes na interface
    @Override
    public String toString() {
        return String.valueOf(this.ano);
    }
}
