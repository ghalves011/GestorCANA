package br.com.cana.service;

import br.com.cana.dao.EventoDAO;
import br.com.cana.dao.JogadorDAO;
import br.com.cana.dao.JogadorPartidaDAO;
import br.com.cana.model.Evento;
import br.com.cana.model.JogadorPartida;

public class EventoService {

    private EventoDAO eventoDAO;
    private JogadorPartidaDAO jpDAO;
    private JogadorDAO jogadorDAO;

    public EventoService() {
        this.eventoDAO = new EventoDAO();
        this.jpDAO = new JogadorPartidaDAO();
        this.jogadorDAO = new JogadorDAO();
    }

    /**
     * Esta é a "mágica" do Service:
     * Ele salva o lance e já atualiza a estatística do jogador na hora!
     */
    public String registrarLance(Evento e, JogadorPartida estatistica) {
        // 1. Validação: Não faz sentido registrar lance sem jogador ou partida
        if (e.getJogador() == null || e.getPartida() == null) {
            return "Erro: Evento sem jogador ou partida vinculada.";
        }

        // 2. Tenta salvar o lance no histórico (tabela evento_partida)
        boolean salvouEvento = eventoDAO.inserir(e);

        if (salvouEvento) {
            // 3. REGRA DE NEGÓCIO: Se o evento foi salvo, atualizamos os números do atleta
            String tipo = e.getTipo().toUpperCase();

            switch (tipo) {
                case "GOL":
                    estatistica.setGols(estatistica.getGols() + 1);
                    break;
                case "AMARELO":
                    estatistica.setCartaoAmarelo(estatistica.getCartaoAmarelo() + 1);
                    // Regra automática: 2º amarelo gera expulsão
                    if (estatistica.getCartaoAmarelo() >= 2) {
                        estatistica.setCartaoVermelho(1);
                    }

                    int totalHistorico = eventoDAO.contarAmarelosAcumulados(e.getJogador().getId());
                    if (totalHistorico >= 3) {
                        jogadorDAO.atualizarStatus(e.getJogador().getId(), "SUSPENSO");
                        System.out.println("🚨 " + e.getJogador().getNome() + " acumulou 3 amarelos e está suspenso!");
                    }
                    break;
                case "VERMELHO":
                    estatistica.setCartaoVermelho(1);
                    break;
            }

            // 4. Salva a nova estatística no banco (tabela jogador_partida)
            boolean atualizouStats = jpDAO.atualizarVinculo(estatistica);

            return atualizouStats ? "OK" : "Lance salvo, mas erro ao atualizar estatística.";
        }

        return "Erro ao gravar o lance no banco de dados.";
    }
}
