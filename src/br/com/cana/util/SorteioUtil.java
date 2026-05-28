package br.com.cana.util;
 
import br.com.cana.model.Jogador;
import br.com.cana.model.Partida;
import java.util.*;
import java.util.stream.Collectors;
 
public class SorteioUtil {
 
    /**
     * Lógica principal de sorteio, equilíbrio e ordenação tática baseada no cadastro.
     */
    public static void realizarSorteio(Partida partida, List<Jogador> presentes, String formacao) {
        if (presentes == null || presentes.size() < 7) {
            return; 
        }
 
        // Assegura que as listas internas da entidade estejam limpas antes do sorteio
        partida.setJogadoresAzul(new ArrayList<>());
        partida.setJogadoresVermelho(new ArrayList<>());
 
        String formacaoUpper = (formacao != null) ? formacao.toUpperCase().trim() : "4-4-2";
        String formacaoEfetiva = (presentes.size() < 22) ? "LIVRE" : formacaoUpper;
        
        Map<String, Integer> vagas = definirVagasPorFormacao(formacaoEfetiva);
 
        // Agrupa os jogadores usando as posições por extenso vindas do banco/cadastro
        Map<String, List<Jogador>> potes = presentes.stream()
                .collect(Collectors.groupingBy(j -> j.getPosicao() != null ? j.getPosicao().toUpperCase().trim() : ""));
 
        // 🛠️ SEPARAÇÃO ESTRITA DE GOLEIROS: Garante 1 para cada lado usando o termo "GOLEIRO"
        List<Jogador> goleiros = potes.getOrDefault("GOLEIRO", new ArrayList<>());
        Collections.shuffle(goleiros);
        if (!goleiros.isEmpty()) {
            partida.getJogadoresAzul().add(goleiros.remove(0));
        }
        if (!goleiros.isEmpty()) {
            partida.getJogadoresVermelho().add(goleiros.remove(0));
        }
 
        // Distribui os jogadores de linha usando os termos exatos do cadastro por extenso
        String[] posicoesLinha = { "LATERAL", "ZAGUEIRO", "MEIA", "ATACANTE" };
        for (String pos : posicoesLinha) {
            List<Jogador> candidatos = potes.getOrDefault(pos, new ArrayList<>());
            Collections.shuffle(candidatos);
            candidatos.sort(Comparator.comparingInt(Jogador::getNivel).reversed()); // Equilibra pelo nível técnico
 
            int qtdNecessariaPorTime = vagas.getOrDefault(pos, 0);
            for (int i = 0; i < qtdNecessariaPorTime; i++) {
                alocarEquilibrado(partida, candidatos);
            }
        }
 
        // Pote de Sobras (quem sobrou vai para o banco de reservas)
        List<Jogador> sobras = new ArrayList<>();
        String[] posicoesLinhaSobras = { "LATERAL", "ZAGUEIRO", "MEIA", "ATACANTE" };
        for (String pos : posicoesLinhaSobras) {
            List<Jogador> sobrouNaPosicao = potes.getOrDefault(pos, new ArrayList<>());
            // Como usamos candidatos.remove(0) lá em cima, o que ficou na lista é sobra real de quem foi selecionado
            sobras.addAll(sobrouNaPosicao);
        }
        
        // Se sobrou algum goleiro reserva (ex: tinha 3 goleiros selecionados), manda pra sobra também
        List<Jogador> sobrouGoleiro = potes.getOrDefault("GOLEIRO", new ArrayList<>());
        sobras.addAll(sobrouGoleiro);
 
        // Ordena as sobras por nível técnico para o banco ficar equilibrado
        sobras.sort(Comparator.comparingInt(Jogador::getNivel).reversed());
 
        // Distribui o banco de reservas entre os dois times
        while (!sobras.isEmpty()) {
            alocarEquilibrado(partida, sobras);
        }
 
        // Ordena internamente os times de cima para baixo antes de enviar para a View
        partida.setJogadoresAzul(ordenarTimePorPosicao(partida.getJogadoresAzul()));
        partida.setJogadoresVermelho(ordenarTimePorPosicao(partida.getJogadoresVermelho()));
    }
 
    /**
     * Mapeamento de vagas disponíveis por esquema tático por extenso.
     */
    private static Map<String, Integer> definirVagasPorFormacao(String formacao) {
        Map<String, Integer> v = new HashMap<>();
        v.put("GOLEIRO", 1);
        
        switch (formacao.toUpperCase().trim()) {
            case "4-4-2": v.put("ZAGUEIRO", 2); v.put("LATERAL", 2); v.put("MEIA", 4); v.put("ATACANTE", 2); break;
            case "4-3-3": v.put("ZAGUEIRO", 2); v.put("LATERAL", 2); v.put("MEIA", 3); v.put("ATACANTE", 3); break;
            case "3-5-2": v.put("ZAGUEIRO", 3); v.put("LATERAL", 0); v.put("MEIA", 5); v.put("ATACANTE", 2); break;
            case "4-5-1": v.put("ZAGUEIRO", 2); v.put("LATERAL", 2); v.put("MEIA", 5); v.put("ATACANTE", 1); break;
            case "LIVRE":
                v.put("ZAGUEIRO", 0); v.put("LATERAL", 0); v.put("MEIA", 0); v.put("ATACANTE", 0);
                break;
            default:
                v.put("ZAGUEIRO", 2); v.put("LATERAL", 2); v.put("MEIA", 4); v.put("ATACANTE", 2);
        }
        return v;
    }
 
    /**
     * Algoritmo de balanço técnico para distribuição dos lados Azul e Vermelho.
     */
    private static void alocarEquilibrado(Partida p, List<Jogador> candidatos) {
        if (candidatos.isEmpty()) return;
 
        if (candidatos.size() >= 2) {
            Jogador j1 = candidatos.remove(0);
            Jogador j2 = candidatos.remove(0);
 
            if (calcularSomaNivel(p.getJogadoresAzul()) <= calcularSomaNivel(p.getJogadoresVermelho())) {
                p.getJogadoresAzul().add(j1);
                p.getJogadoresVermelho().add(j2);
            } else {
                p.getJogadoresAzul().add(j2);
                p.getJogadoresVermelho().add(j1);
            }
        } else {
            Jogador j = candidatos.remove(0);
            if (calcularSomaNivel(p.getJogadoresAzul()) <= calcularSomaNivel(p.getJogadoresVermelho())) {
                p.getJogadoresAzul().add(j);
            } else {
                p.getJogadoresVermelho().add(j);
            }
        }
    }
 
    /**
     * Organiza a lista na ordem tática exata do futebol de cima para baixo.
     */
    private static List<Jogador> ordenarTimePorPosicao(List<Jogador> time) {
        List<Jogador> timeOrdenado = new ArrayList<>();
        String[] ordemPosicoes = { "GOLEIRO", "LATERAL", "ZAGUEIRO", "MEIA", "ATACANTE" };
        
        for (String pos : ordemPosicoes) {
            for (Jogador j : time) {
                if (j.getPosicao() != null && j.getPosicao().toUpperCase().trim().equals(pos)) {
                    timeOrdenado.add(j);
                }
            }
        }
        return timeOrdenado;
    }
 
    public static int calcularSomaNivel(List<Jogador> time) {
        return time.stream().mapToInt(Jogador::getNivel).sum();
    }
}