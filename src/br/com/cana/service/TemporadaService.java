package br.com.cana.service;
import br.com.cana.dao.TemporadaDAO;
import br.com.cana.model.Temporada;

public class TemporadaService {
    private TemporadaDAO dao = new TemporadaDAO();

    public boolean salvar(Temporada t) {
        // Regra de Negócio: O ano deve ser válido
        if (t.getAno() < 2000 || t.getAno() > 2100) {
            System.err.println("Ano inválido!");
            return false;
        }

        // Regra de Negócio: Verificar se o ano já existe (Evita duplicidade)
        if (dao.buscarPorAno(t.getAno()) != null) {
            System.err.println("Temporada já cadastrada!");
            return false;
        }

        return dao.inserir(t);
    }
}