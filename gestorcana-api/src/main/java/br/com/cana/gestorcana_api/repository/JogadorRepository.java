package br.com.cana.gestorcana_api.repository;

import br.com.cana.gestorcana_api.entity.Jogador;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JogadorRepository extends JpaRepository<Jogador, Integer> {

    // Garante consulta ÚNICA via LEFT JOIN no banco de dados (elimina a lentidão)
    @Override
    @EntityGraph(attributePaths = {"endereco"})
    List<Jogador> findAll();

    List<Jogador> findByApelidoIgnoreCase(String apelido);

    List<Jogador> findByNomeIgnoreCase(String nome);

    List<Jogador> findByPadrinhoId(Integer padrinhoId);

    Optional<Jogador> findByCpf(String cpf);

    Optional<Jogador> findByRg(String rg);

    Optional<Jogador> findByNumCamisa(Integer numCamisa);

    boolean existsByNumCamisa(Integer numCamisa);

    // Só os nomes (apelido, ou nome se não houver apelido) dos jogadores ativos suspensos,
    // sem carregar a entidade inteira. "ativo" nulo conta como ativo, igual ao getter.
    @Query("SELECT CASE WHEN j.apelido IS NULL OR TRIM(j.apelido) = '' THEN j.nome ELSE j.apelido END "
            + "FROM Jogador j WHERE j.estaSuspenso = true AND (j.ativo IS NULL OR j.ativo = true)")
    List<String> listarNomesSuspensosAtivos();
}