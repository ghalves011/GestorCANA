package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.Jogador;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TriagemService {

    /**
     * 🟢 Jogadores aptos a entrarem em campo (NÃO estão suspensos).
     * Nota: Mesmo com pendência financeira, o jogador PODE jogar (gera apenas alerta).
     */
    public List<Jogador> filtrarJogadoresAptosParaJogo(List<Jogador> todos) {
        if (todos == null) return List.of();
        
        return todos.stream()
                .filter(j -> !Boolean.TRUE.equals(j.getEstaSuspenso())) // Apenas suspensão barra do jogo
                .toList();
    }

    /**
     * ⚠️ Retorna os alertas de inadimplência para a tela avisar a diretoria.
     */
    public List<Jogador> listarJogadoresComAlertaFinanceiro(List<Jogador> todos) {
        if (todos == null) return List.of();
        
        return todos.stream()
                .filter(j -> !Boolean.TRUE.equals(j.getMensalidadeEmDia())) // Gera o alerta na tela
                .toList();
    }
}