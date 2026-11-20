package br.com.cana.gestorcana_api.repository;

import br.com.cana.gestorcana_api.entity.StatusTemporada;
import br.com.cana.gestorcana_api.entity.Temporada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemporadaRepository extends JpaRepository<Temporada, Integer> {

    Optional<Temporada> findByAno(Integer ano);

    boolean existsByAno(Integer ano);

    Optional<Temporada> findByStatus(StatusTemporada status);

    // O id da temporada é igual ao ano (partida.temporada_id e os apps usam o ano),
    // por isso a nova temporada é inserida por SQL nativo, sem passar pela sequence.
    @Modifying
    @Query(value = "INSERT INTO temporada (id, ano, status) VALUES (:ano, :ano, 'VIGENTE')", nativeQuery = true)
    void inserirVigenteComIdIgualAoAno(@Param("ano") Integer ano);
}
