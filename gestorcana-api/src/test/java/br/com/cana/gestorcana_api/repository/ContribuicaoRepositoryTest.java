package br.com.cana.gestorcana_api.repository;

import br.com.cana.gestorcana_api.entity.Contribuicao;
import br.com.cana.gestorcana_api.entity.Jogador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Testa a consulta no PostgreSQL de verdade. Perfil local fixo: nunca usa o banco de produção. */
@SpringBootTest
@ActiveProfiles("local")
@Transactional // Rollback automático
class ContribuicaoRepositoryTest {

    // Ano sem dados reais, para a contagem depender só do que o teste grava
    private static final int ANO = 2099;

    @Autowired
    private ContribuicaoRepository contribuicaoRepository;
    @Autowired
    private JogadorRepository jogadorRepository;

    private Jogador jogador(String posicao, boolean ativo) {
        Jogador j = new Jogador();
        j.setNome("Teste");
        j.setPosicao(posicao);
        j.setAtivo(ativo);
        return jogadorRepository.saveAndFlush(j);
    }

    private void cobranca(Jogador j, int mes, int pago) {
        Contribuicao c = new Contribuicao();
        c.setJogadorId(j.getId());
        c.setAno(ANO);
        c.setMes(mes);
        c.setPago(pago);
        contribuicaoRepository.saveAndFlush(c);
    }

    @Test
    @DisplayName("Pendências: goleiros são isentos e não entram na contagem")
    void goleirosNaoContam() {
        cobranca(jogador("Goleiro", true), 1, 0);
        cobranca(jogador(" GOLEIRO ", true), 1, 0);
        cobranca(jogador("goleiro reserva", true), 1, 0);
        assertEquals(0, contribuicaoRepository.contarPendentesVencidas(ANO, 11));

        cobranca(jogador("Zagueiro", true), 1, 0);
        cobranca(jogador(null, true), 1, 0); // sem posição não é isento
        assertEquals(2, contribuicaoRepository.contarPendentesVencidas(ANO, 11));
    }

    @Test
    @DisplayName("Pendências: só não pagas, de ativos e até o último mês vencido")
    void soVencidasDeAtivos() {
        Jogador ativo = jogador("Meia", true);
        cobranca(ativo, 10, 1);
        cobranca(ativo, 11, 0);
        cobranca(ativo, 12, 0);
        cobranca(jogador("Meia", false), 11, 0);

        assertEquals(1, contribuicaoRepository.contarPendentesVencidas(ANO, 11));
    }
}
