package br.com.cana.gestorcana_api.repository;

import br.com.cana.gestorcana_api.entity.Partida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartidaRepository extends JpaRepository<Partida, Integer> {
    List<Partida> findByTemporadaId(Integer temporadaId);
}