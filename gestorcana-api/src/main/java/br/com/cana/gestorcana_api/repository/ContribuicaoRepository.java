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

    boolean existsByJogadorIdAndPago(Integer jogadorId, Integer pago);

    // Pendências já vencidas (meses até ultimoMesVencido) de jogadores ativos.
    // Goleiros são isentos: mesma regra de ContribuicaoService.podeJogar (posição contém "GOLEIRO").
    // "ativo" nulo conta como ativo e "pago" nulo conta como não pago, igual ao resto do sistema.
    @Query("SELECT COUNT(c) FROM Contribuicao c WHERE c.ano = :ano AND c.mes <= :ultimoMesVencido "
            + "AND (c.pago IS NULL OR c.pago <> 1) "
            + "AND c.jogadorId IN (SELECT j.id FROM Jogador j WHERE (j.ativo IS NULL OR j.ativo = true) "
            + "AND (j.posicao IS NULL OR UPPER(j.posicao) NOT LIKE '%GOLEIRO%'))")
    long contarPendentesVencidas(@Param("ano") Integer ano, @Param("ultimoMesVencido") Integer ultimoMesVencido);

    boolean existsByAno(Integer ano);

    @Transactional
    @Modifying
    @Query("DELETE FROM Contribuicao c WHERE c.jogadorId = :jogadorId AND c.mes = :mes AND c.ano = :ano")
    void deletarPorJogadorMesAno(@Param("jogadorId") Integer jogadorId, @Param("mes") Integer mes, @Param("ano") Integer ano);

    @Transactional
    @Modifying
    @Query("DELETE FROM Contribuicao c WHERE c.jogadorId = :jogadorId AND (c.pago IS NULL OR c.pago <> 1)")
    void deletarPendentesPorJogador(@Param("jogadorId") Integer jogadorId);
}
