package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.Endereco;
import br.com.cana.gestorcana_api.repository.EnderecoRepository;
import br.com.cana.gestorcana_api.util.EnderecoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnderecoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    public List<Endereco> listarTodos() {
        return enderecoRepository.findAll();
    }

    public Optional<Endereco> buscarPorId(Integer id) {
        if (id == null) return Optional.empty();
        return enderecoRepository.findById(id);
    }

    public Endereco salvar(Endereco e) {
        if (e == null) {
            return null;
        }
        if (e.getId() != null && e.getId() == 0) {
            e.setId(null);
        }
        if (e.getEstado() != null) {
            e.setEstado(e.getEstado().toUpperCase());
        }
        return enderecoRepository.save(e);
    }

    public boolean atualizar(Endereco e) {
        if (e == null || e.getId() == null) {
            return false;
        }
        if (e.getEstado() != null) {
            e.setEstado(e.getEstado().toUpperCase());
        }
        enderecoRepository.save(e);
        return true;
    }

    public boolean deletar(Integer id) {
        if (id == null || !enderecoRepository.existsById(id)) {
            return false;
        }
        enderecoRepository.deleteById(id);
        return true;
    }

    public String obterEnderecoFormatado(Endereco e) {
        return EnderecoUtil.formatarCompleto(e);
    }
}