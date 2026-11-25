package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.JogadorPartida;
import br.com.cana.gestorcana_api.entity.StatusTemporada;
import br.com.cana.gestorcana_api.entity.Temporada;
import br.com.cana.gestorcana_api.repository.JogadorPartidaRepository;
import br.com.cana.gestorcana_api.repository.TemporadaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Define quais cartões contam para a suspensão (RN12): só os das partidas da
 * temporada vigente. Depende apenas de repositories, para os outros services
 * poderem usar sem dependência circular.
 */
@Service
public class CartoesVigentesService {

    private static final Logger log = LoggerFactory.getLogger(CartoesVigentesService.class);

    private final JogadorPartidaRepository jogadorPartidaRepository;
    private final TemporadaRepository temporadaRepository;

    public CartoesVigentesService(JogadorPartidaRepository jogadorPartidaRepository,
                                  TemporadaRepository temporadaRepository) {
        this.jogadorPartidaRepository = jogadorPartidaRepository;
        this.temporadaRepository = temporadaRepository;
    }

    /**
     * Atuações do jogador na temporada vigente. Sem temporada vigente, devolve
     * todas (comportamento antigo) e avisa no log.
     */
    public List<JogadorPartida> buscarAtuacoes(Integer jogadorId) {
        Optional<Temporada> vigente = buscarVigente();
        return vigente.isPresent()
                ? jogadorPartidaRepository.buscarPorJogadorETemporada(jogadorId, vigente.get().getId())
                : jogadorPartidaRepository.findByJogadorId(jogadorId);
    }

    /**
     * Partida de temporada encerrada não gera nem retira suspensão. Sem
     * temporada vigente, toda partida conta (comportamento antigo).
     */
    public boolean partidaConta(Integer temporadaId) {
        return buscarVigente().map(t -> t.getId().equals(temporadaId)).orElse(true);
    }

    private Optional<Temporada> buscarVigente() {
        Optional<Temporada> vigente = temporadaRepository.findByStatus(StatusTemporada.VIGENTE);
        if (vigente.isEmpty()) {
            log.warn("Não existe temporada vigente: a suspensão considera os cartões de todas as temporadas.");
        }
        return vigente;
    }
}
