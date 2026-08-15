package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.Temporada;
import br.com.cana.gestorcana_api.repository.TemporadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TemporadaService {

    private final TemporadaRepository repository;

    @Autowired
    public TemporadaService(TemporadaRepository repository) {
        this.repository = repository;
    }

    public String salvar(Temporada t) {
        if (t == null || t.getAno() == null) {
            return "Erro: Dados da temporada inválidos.";
        }

        // Regra de Negócio: O ano deve ser válido
        if (t.getAno() < 2000 || t.getAno() > 2100) {
            System.err.println("Ano inválido!");
            return "Erro: Ano inválido (deve estar entre 2000 e 2100).";
        }

        // Regra de Negócio: Verificar se o ano já existe em cadastros novos
        if (t.getId() == null && repository.existsByAno(t.getAno())) {
            System.err.println("Temporada já cadastrada!");
            return "Erro: Temporada com o ano " + t.getAno() + " já está cadastrada!";
        }

        repository.save(t);
        return "OK";
    }

    public List<Temporada> listarTodas() {
        return repository.findAll();
    }

    public Optional<Temporada> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        return repository.findById(id);
    }

    public Optional<Temporada> buscarPorAno(Integer ano) {
        if (ano == null) return Optional.empty();
        return repository.findByAno(ano);
    }

    public boolean deletar(Integer id) {
        if (id == null || !repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}