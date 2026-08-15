package br.com.cana.gestorcana_api.repository;

import br.com.cana.gestorcana_api.entity.JogadorPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JogadorPartidaRepository extends JpaRepository<JogadorPartida, Integer> {

    List<JogadorPartida> findByPartidaId(Integer partidaId);

    List<JogadorPartida> findByJogadorId(Integer jogadorId);

    Optional<JogadorPartida> findByJogadorIdAndPartidaId(Integer jogadorId, Integer partidaId);

    void deleteByPartidaId(Integer partidaId);
}