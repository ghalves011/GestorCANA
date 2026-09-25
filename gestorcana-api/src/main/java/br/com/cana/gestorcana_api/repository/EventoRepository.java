package br.com.cana.gestorcana_api.repository;

import br.com.cana.gestorcana_api.entity.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Integer> {

    List<Evento> findByPartidaId(Integer partidaId);

    List<Evento> findByJogadorId(Integer jogadorId);

    boolean existsByJogadorId(Integer jogadorId);

    int countByJogadorIdAndTipo(Integer jogadorId, String tipo);

    int countByJogadorIdAndTipoEvento(Integer jogadorId, String tipoEvento);
}