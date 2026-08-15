package br.com.cana.gestorcana_api.repository;

import br.com.cana.gestorcana_api.entity.Temporada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemporadaRepository extends JpaRepository<Temporada, Integer> {

    Optional<Temporada> findByAno(Integer ano);

    boolean existsByAno(Integer ano);
}