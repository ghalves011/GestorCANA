package br.com.cana.gestorcana_api.repository;

import br.com.cana.gestorcana_api.entity.Contribuicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContribuicaoRepository extends JpaRepository<Contribuicao, Integer> {

    Optional<Contribuicao> findByJogadorIdAndMesAndAno(Integer jogadorId, Integer mes, Integer ano);

    List<Contribuicao> findByAno(Integer ano);

    @Transactional
    @Modifying
    @Query("DELETE FROM Contribuicao c WHERE c.jogadorId = :jogadorId AND c.mes = :mes AND c.ano = :ano")
    void deletarPorJogadorMesAno(@Param("jogadorId") Integer jogadorId, @Param("mes") Integer mes, @Param("ano") Integer ano);
}