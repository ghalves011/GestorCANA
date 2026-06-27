package br.com.cana.service;

import br.com.cana.dao.EnderecoDAO;
import br.com.cana.model.Endereco;
import br.com.cana.util.EnderecoUtil;

public class EnderecoService {
    private EnderecoDAO dao = new EnderecoDAO();

    public int salvar(Endereco e) {
        // Regra de negócio: Sigla do estado sempre em maiúsculo
        if (e.getEstado() != null) {
            e.setEstado(e.getEstado().toUpperCase());
        }
        return dao.inserir(e);
    }

    public boolean atualizar(Endereco e) {
        return dao.atualizar(e);
    }
    
    // Método para obter o endereço formatado (ex: "Rua X, 123 - Bairro Y, Cidade Z - UF")
    public String obterEnderecoFormatado(Endereco e) {
        return EnderecoUtil.formatarCompleto(e);
    }
}