package br.com.cana.gestorcana_api.repository;

import br.com.cana.gestorcana_api.entity.JogadorPartida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JogadorPartidaRepository extends JpaRepository<JogadorPartida, Integer> {

    List<JogadorPartida> findByPartidaId(Integer partidaId);

    List<JogadorPartida> findByJogadorId(Integer jogadorId);

    // Atuações do jogador só nas partidas de uma temporada. A entidade guarda partidaId
    // (não a relação), por isso a subconsulta.
    @Query("SELECT jp FROM JogadorPartida jp WHERE jp.jogadorId = :jogadorId "
            + "AND jp.partidaId IN (SELECT p.id FROM Partida p WHERE p.temporadaId = :temporadaId)")
    List<JogadorPartida> buscarPorJogadorETemporada(@Param("jogadorId") Integer jogadorId,
                                                    @Param("temporadaId") Integer temporadaId);

    boolean existsByJogadorId(Integer jogadorId);

    Optional<JogadorPartida> findByJogadorIdAndPartidaId(Integer jogadorId, Integer partidaId);

    void deleteByPartidaId(Integer partidaId);
}