package br.com.cana.gestorcana_api.repository;

import br.com.cana.gestorcana_api.entity.Jogador;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
}