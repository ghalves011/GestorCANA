package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.Evento;
import br.com.cana.gestorcana_api.entity.Jogador;
import br.com.cana.gestorcana_api.repository.EventoRepository;
import br.com.cana.gestorcana_api.repository.JogadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final JogadorRepository jogadorRepository;

    @Autowired
    public EventoService(EventoRepository eventoRepository, JogadorRepository jogadorRepository) {
        this.eventoRepository = eventoRepository;
        this.jogadorRepository = jogadorRepository;
    }

    /**
     * Registra o evento no banco e aplica as regras de suspensão por cartões.
     */
    public String registrarLance(Evento e) {
        if (e == null || e.getJogadorId() == null || e.getPartidaId() == null) {
            return "Erro: Evento sem jogador ou partida vinculada.";
        }

        // 1. Salva o evento no Supabase
        Evento eventoSalvo = eventoRepository.save(e);

        if (eventoSalvo != null && eventoSalvo.getId() != null) {
            String tipo = e.getTipo() != null ? e.getTipo().toUpperCase() : "";

            // 2. Regra de Suspensão Automática por 3 Amarelos
            if ("AMARELO".equals(tipo) || "CARTÃO AMARELO".equals(tipo)) {
                int totalAmarelos = eventoRepository.countByJogadorIdAndTipo(e.getJogadorId(), "AMARELO")
                        + eventoRepository.countByJogadorIdAndTipoEvento(e.getJogadorId(), "AMARELO");

                if (totalAmarelos >= 3) {
                    Optional<Jogador> optJ = jogadorRepository.findById(e.getJogadorId());
                    if (optJ.isPresent()) {
                        Jogador j = optJ.get();
                        j.setEstaSuspenso(true);
                        j.setStatus("Suspenso");
                        jogadorRepository.save(j);
                        System.out.println("🚨 " + j.getNome() + " acumulou 3 amarelos e está suspenso!");
                    }
                }
            } else if ("VERMELHO".equals(tipo) || "CARTÃO VERMELHO".equals(tipo)) {
                // 3. Regra de Suspensão Automática por Cartão Vermelho Direto
                Optional<Jogador> optJ = jogadorRepository.findById(e.getJogadorId());
                if (optJ.isPresent()) {
                    Jogador j = optJ.get();
                    j.setEstaSuspenso(true);
                    j.setStatus("Suspenso");
                    jogadorRepository.save(j);
                }
            }

            return "OK";
        }

        return "Erro ao gravar o lance no banco de dados.";
    }

    public List<Evento> listarPorPartida(Integer partidaId) {
        if (partidaId == null) return List.of();
        return eventoRepository.findByPartidaId(partidaId);
    }

    public List<Evento> listarPorJogador(Integer jogadorId) {
        if (jogadorId == null) return List.of();
        return eventoRepository.findByJogadorId(jogadorId);
    }
}