package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.Jogador;
import br.com.cana.gestorcana_api.entity.JogadorPartida;
import br.com.cana.gestorcana_api.entity.Partida;
import br.com.cana.gestorcana_api.entity.StatusTemporada;
import br.com.cana.gestorcana_api.entity.Temporada;
import br.com.cana.gestorcana_api.repository.JogadorPartidaRepository;
import br.com.cana.gestorcana_api.repository.JogadorRepository;
import br.com.cana.gestorcana_api.repository.PartidaRepository;
import br.com.cana.gestorcana_api.repository.TemporadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** RN12: só os cartões da temporada vigente contam para a suspensão. Vigente = 2027. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SuspensaoTemporadaTest {

    private static final int ENCERRADA = 2026;
    private static final int VIGENTE = 2027;
    private static final String GRID_AMARELO = "[[\"Ana\",\"MEI\",\"🟨\"]]";
    private static final String GRID_VERMELHO = "[[\"Ana\",\"MEI\",\"🟥\"]]";
    private static final String GRID_SEM_CARTAO = "[[\"Ana\",\"MEI\",\"\"]]";

    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private JogadorRepository jogadorRepository;
    @Mock
    private JogadorPartidaRepository jogadorPartidaRepository;
    @Mock
    private TemporadaRepository temporadaRepository;

    private PartidaService partidaService;
    private JogadorService jogadorService;
    private JogadorPartidaService jogadorPartidaService;

    private final Jogador ana = new Jogador();
    private final List<Partida> partidas = new ArrayList<>();
    private final List<JogadorPartida> gravadas = new ArrayList<>();

    @BeforeEach
    void setUp() {
        CartoesVigentesService cartoes = new CartoesVigentesService(jogadorPartidaRepository, temporadaRepository);
        partidaService = new PartidaService(partidaRepository, jogadorRepository, null, jogadorPartidaRepository, cartoes);
        jogadorService = new JogadorService(jogadorRepository, null, jogadorPartidaRepository, cartoes);
        jogadorPartidaService = new JogadorPartidaService(jogadorPartidaRepository, jogadorRepository, cartoes);

        ana.setId(1);
        ana.setNome("Ana");
        ana.setApelido("Ana");
        when(jogadorRepository.findById(1)).thenReturn(Optional.of(ana));
        when(jogadorRepository.findByApelidoIgnoreCase(anyString())).thenAnswer(inv ->
                "Ana".equalsIgnoreCase(inv.getArgument(0)) ? List.of(ana) : List.of());

        Temporada vigente = new Temporada();
        vigente.setId(VIGENTE);
        vigente.setAno(VIGENTE);
        vigente.setStatus(StatusTemporada.VIGENTE);
        when(temporadaRepository.findByStatus(StatusTemporada.VIGENTE)).thenReturn(Optional.of(vigente));

        // "Banco" em memória, com o mesmo filtro da consulta por temporada
        when(partidaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(partidaRepository.findById(anyInt())).thenAnswer(inv -> partidas.stream()
                .filter(p -> p.getId().equals(inv.getArgument(0))).findFirst());
        when(jogadorPartidaRepository.findByJogadorId(1)).thenAnswer(inv -> new ArrayList<>(gravadas));
        when(jogadorPartidaRepository.findByPartidaId(anyInt())).thenAnswer(inv -> gravadas.stream()
                .filter(jp -> jp.getPartidaId().equals(inv.getArgument(0))).toList());
        when(jogadorPartidaRepository.buscarPorJogadorETemporada(anyInt(), anyInt())).thenAnswer(inv -> gravadas.stream()
                .filter(jp -> partidas.stream().anyMatch(p -> p.getId().equals(jp.getPartidaId())
                        && p.getTemporadaId().equals(inv.getArgument(1)))).toList());
        when(jogadorPartidaRepository.save(any())).thenAnswer(inv -> {
            JogadorPartida jp = inv.getArgument(0);
            if (!gravadas.contains(jp)) gravadas.add(jp);
            return jp;
        });
        when(jogadorPartidaRepository.saveAll(any())).thenAnswer(inv -> {
            inv.<Iterable<JogadorPartida>>getArgument(0).forEach(gravadas::add);
            return gravadas;
        });
        doAnswer(inv -> {
            gravadas.removeIf(jp -> jp.getPartidaId().equals(inv.getArgument(0)));
            return null;
        }).when(jogadorPartidaRepository).deleteByPartidaId(anyInt());
    }

    private Partida partida(int id, int temporadaId) {
        Partida p = new Partida();
        p.setId(id);
        p.setTemporadaId(temporadaId);
        p.setGridAzul("[]");
        p.setGridVermelho("[]");
        partidas.add(p);
        return p;
    }

    private JogadorPartida cartoes(int partidaId, int temporadaId, int amarelos, int vermelhos) {
        partida(partidaId, temporadaId);
        JogadorPartida jp = new JogadorPartida();
        jp.setJogadorId(1);
        jp.setPartidaId(partidaId);
        jp.setCartaoAmarelo(amarelos);
        jp.setCartaoVermelho(vermelhos);
        gravadas.add(jp);
        return jp;
    }

    private void semVigente() {
        when(temporadaRepository.findByStatus(StatusTemporada.VIGENTE)).thenReturn(Optional.empty());
    }

    // --- FINALIZAR ---

    @Test
    @DisplayName("Finalizar: 2 amarelos da encerrada + 1 da vigente não suspendem")
    void finalizarIgnoraEncerrada() {
        cartoes(1, ENCERRADA, 2, 0);
        partidaService.finalizarEFecharPartida(partida(2, VIGENTE), GRID_AMARELO, "[]");
        assertFalse(ana.getEstaSuspenso());
    }

    @Test
    @DisplayName("Finalizar: 3º amarelo na vigente suspende")
    void finalizarTerceiroAmarelo() {
        cartoes(1, VIGENTE, 2, 0);
        partidaService.finalizarEFecharPartida(partida(2, VIGENTE), GRID_AMARELO, "[]");
        assertTrue(ana.getEstaSuspenso());
    }

    @Test
    @DisplayName("Finalizar: 2º amarelo na vigente ainda não suspende")
    void finalizarSegundoAmarelo() {
        cartoes(1, VIGENTE, 1, 0);
        partidaService.finalizarEFecharPartida(partida(2, VIGENTE), GRID_AMARELO, "[]");
        assertFalse(ana.getEstaSuspenso());
    }

    @Test
    @DisplayName("Finalizar: vermelho suspende na vigente, mas não em partida de temporada encerrada")
    void finalizarVermelho() {
        partidaService.finalizarEFecharPartida(partida(1, ENCERRADA), GRID_VERMELHO, "[]");
        assertFalse(ana.getEstaSuspenso());
        assertEquals(1, gravadas.get(0).getCartaoVermelho()); // o cartão entra no histórico

        partidaService.finalizarEFecharPartida(partida(2, VIGENTE), GRID_VERMELHO, "[]");
        assertTrue(ana.getEstaSuspenso());
    }

    @Test
    @DisplayName("Sem temporada vigente: conta os cartões de todas as temporadas")
    void semVigenteContaTudo() {
        semVigente();
        cartoes(1, ENCERRADA, 2, 0);
        partidaService.finalizarEFecharPartida(partida(2, VIGENTE), GRID_AMARELO, "[]");
        assertTrue(ana.getEstaSuspenso());
    }

    // --- EDITAR E EXCLUIR ---

    @Test
    @DisplayName("Editar partida da vigente: cartões da encerrada não somam; os da vigente sim")
    void editarContaSoVigente() {
        cartoes(1, ENCERRADA, 2, 1);
        partida(2, VIGENTE);
        partidaService.editarPartida(2, new Partida(), GRID_AMARELO, "[]");
        assertFalse(ana.getEstaSuspenso());

        cartoes(3, VIGENTE, 1, 0);
        partida(4, VIGENTE);
        partidaService.editarPartida(4, new Partida(), GRID_AMARELO, "[]");
        assertTrue(ana.getEstaSuspenso());
    }

    @Test
    @DisplayName("Editar partida da encerrada: não gera nem retira suspensão")
    void editarEncerradaNaoMexe() {
        partida(1, ENCERRADA);
        partidaService.editarPartida(1, new Partida(), GRID_VERMELHO, "[]");
        assertFalse(ana.getEstaSuspenso());

        ana.setEstaSuspenso(true);
        partidaService.editarPartida(1, new Partida(), GRID_SEM_CARTAO, "[]");
        assertTrue(ana.getEstaSuspenso());
    }

    @Test
    @DisplayName("Excluir partida da encerrada não retira a suspensão; da vigente retira")
    void excluirPartida() {
        cartoes(1, ENCERRADA, 0, 1);
        cartoes(2, VIGENTE, 0, 1);
        ana.setEstaSuspenso(true);

        assertTrue(partidaService.excluirPartida(1));
        assertTrue(ana.getEstaSuspenso());
        assertTrue(partidaService.excluirPartida(2));
        assertFalse(ana.getEstaSuspenso());
    }

    // --- CUMPRIMENTO DA SUSPENSÃO ---

    @Test
    @DisplayName("Cumprir suspensão: zera só os cartões da vigente e preserva os da encerrada")
    void cumprirPreservaEncerrada() {
        JogadorPartida antiga = cartoes(1, ENCERRADA, 2, 1);
        JogadorPartida atual = cartoes(2, VIGENTE, 3, 1);
        atual.setGols(2);
        ana.setEstaSuspenso(true);

        Partida p = partida(3, VIGENTE);
        p.setArbitro("Ana");
        partidaService.finalizarEFecharPartida(p, "[]", "[]");

        assertFalse(ana.getEstaSuspenso());
        assertEquals(0, atual.getCartaoAmarelo());
        assertEquals(0, atual.getCartaoVermelho());
        assertEquals(2, atual.getGols());
        assertEquals(2, antiga.getCartaoAmarelo());
        assertEquals(1, antiga.getCartaoVermelho());
        verify(jogadorPartidaRepository, never()).save(antiga);
    }

    @Test
    @DisplayName("Depois de cumprir, a contagem recomeça: 1 amarelo não suspende de novo")
    void contagemRecomecaDepoisDeCumprir() {
        cartoes(1, ENCERRADA, 2, 1);
        cartoes(2, VIGENTE, 3, 0);
        ana.setEstaSuspenso(true);
        Partida p = partida(3, VIGENTE);
        p.setBandeira1("Ana");
        partidaService.finalizarEFecharPartida(p, "[]", "[]");

        partidaService.finalizarEFecharPartida(partida(4, VIGENTE), GRID_AMARELO, "[]");
        assertFalse(ana.getEstaSuspenso());
    }

    @Test
    @DisplayName("Cumprir suspensão pela edição da arbitragem também preserva a encerrada")
    void cumprirPelaEdicao() {
        JogadorPartida antiga = cartoes(1, ENCERRADA, 2, 0);
        JogadorPartida atual = cartoes(2, VIGENTE, 3, 0);
        ana.setEstaSuspenso(true);
        partida(3, VIGENTE);
        Partida dados = new Partida();
        dados.setBandeira2("Ana");

        partidaService.editarPartida(3, dados, "[]", "[]");

        assertFalse(ana.getEstaSuspenso());
        assertEquals(0, atual.getCartaoAmarelo());
        assertEquals(2, antiga.getCartaoAmarelo());
    }

    @Test
    @DisplayName("Cumprir suspensão sem temporada vigente: zera todos os cartões (comportamento antigo)")
    void cumprirSemVigente() {
        semVigente();
        JogadorPartida antiga = cartoes(1, ENCERRADA, 3, 0);
        ana.setEstaSuspenso(true);

        partidaService.processarLimpezaDeSuspensaoPosJogo(List.of("Ana"), partida(2, VIGENTE));

        assertEquals(0, antiga.getCartaoAmarelo());
    }

    // --- JOGADORSERVICE E JOGADORPARTIDASERVICE ---

    @Test
    @DisplayName("sincronizarStatusSuspensao: ignora a encerrada e usa >= 3 na vigente")
    void sincronizar() {
        cartoes(1, ENCERRADA, 3, 1);
        jogadorService.sincronizarStatusSuspensao(ana);
        assertFalse(ana.getEstaSuspenso());

        cartoes(2, VIGENTE, 4, 0);
        jogadorService.sincronizarStatusSuspensao(ana);
        assertTrue(ana.getEstaSuspenso());
    }

    @Test
    @DisplayName("aplicarCartaoAmarelo: conta só a vigente e partida encerrada não suspende")
    void aplicarCartaoAmarelo() {
        JogadorPartida antiga = cartoes(1, ENCERRADA, 2, 0);
        JogadorPartida atual = cartoes(2, VIGENTE, 0, 0);
        jogadorPartidaService.aplicarCartaoAmarelo(atual);
        assertFalse(ana.getEstaSuspenso());

        cartoes(3, VIGENTE, 2, 0);
        jogadorPartidaService.aplicarCartaoAmarelo(antiga);
        assertFalse(ana.getEstaSuspenso());

        jogadorPartidaService.aplicarCartaoAmarelo(atual);
        assertTrue(ana.getEstaSuspenso());
    }
}
