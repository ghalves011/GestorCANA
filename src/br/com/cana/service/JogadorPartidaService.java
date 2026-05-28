package br.com.cana.service;

import br.com.cana.dao.JogadorPartidaDAO;
import br.com.cana.model.JogadorPartida;

public class JogadorPartidaService {

    private JogadorPartidaDAO jpDAO;

    public JogadorPartidaService() {
        this.jpDAO = new JogadorPartidaDAO();
    }

    // Regra: Adicionar um gol
    public void adicionarGol(JogadorPartida jp) {
        if (jp.getCartaoVermelho() > 0) {
            System.out.println("⚠️ Jogador expulso não pode marcar gol!");
            return;
        }
        jp.setGols(jp.getGols() + 1);
        jpDAO.atualizarVinculo(jp);
    }

    // Regra: Controle de cartões
    public String aplicarCartaoAmarelo(JogadorPartida jp) {
        jp.setCartaoAmarelo(jp.getCartaoAmarelo() + 1);

        String mensagem = "Cartão amarelo aplicado.";

        if (jp.getCartaoAmarelo() >= 2) {
            jp.setCartaoVermelho(1);
            mensagem = "Jogador expulso pelo segundo amarelo!";
        }

        jpDAO.atualizarVinculo(jp);

        return mensagem;
    }

    public void registrarSubstituicao(JogadorPartida sai, JogadorPartida entra) {
        sai.setSubstituido(true);
        jpDAO.atualizarVinculo(sai);
        // Lógica para ativar o 'entra' como titular...
    }
}
