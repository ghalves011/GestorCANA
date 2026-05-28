package br.com.cana.service;

import br.com.cana.model.Jogador;
import java.util.List;
import java.util.stream.Collectors;

public class TriagemService {

    /**
     * O "Filtro de Segurança": Mantém quem pagou E quem não está suspenso.
     */
    public List<Jogador> filtrarJogadoresAptos(List<Jogador> todosOsJogadores) {
        return todosOsJogadores.stream()
                .filter(j -> j.isMensalidadeEmDia())      // Mantém se for TRUE (Pagou)
                .filter(j -> !j.isEstaSuspenso())         // Mantém se NÃO for TRUE (Não está suspenso)
                .collect(Collectors.toList());
    }

    /**
     * Retorna a "Lista Negra": Quem deve OU quem está suspenso.
     */
    public List<Jogador> listarBarrados(List<Jogador> todos) {
        return todos.stream()
                .filter(j -> !j.isMensalidadeEmDia() || j.isEstaSuspenso())
                .collect(Collectors.toList());
    }
}