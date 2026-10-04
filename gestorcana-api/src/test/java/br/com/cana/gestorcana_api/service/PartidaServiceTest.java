package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.Jogador;
import br.com.cana.gestorcana_api.entity.JogadorPartida;
import br.com.cana.gestorcana_api.entity.Partida;
import br.com.cana.gestorcana_api.repository.EventoRepository;
import br.com.cana.gestorcana_api.repository.JogadorPartidaRepository;
import br.com.cana.gestorcana_api.repository.JogadorRepository;
import br.com.cana.gestorcana_api.repository.PartidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartidaServiceTest {

    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private JogadorRepository jogadorRepository;
    @Mock
    private JogadorPartidaRepository jogadorPartidaRepository;
    @Mock
    private EventoRepository eventoRepository;

    @InjectMocks
    private PartidaService service;

    private final List<Jogador> cadastro = new ArrayList<>();

    @BeforeEach
    void setUp() {
        when(jogadorRepository.findById(anyInt())).thenAnswer(inv -> cadastro.stream()
                .filter(j -> j.getId().equals(inv.getArgument(0))).findFirst());
        when(jogadorRepository.findByApelidoIgnoreCase(anyString())).thenAnswer(inv -> cadastro.stream()
                .filter(j -> j.getApelido() != null && j.getApelido().equalsIgnoreCase(inv.getArgument(0))).toList());
        when(jogadorRepository.findByNomeIgnoreCase(anyString())).thenReturn(new ArrayList<>());
    }

    private Jogador jogador(int id, String apelido, String posicao) {
        Jogador j = new Jogador();
        j.setId(id);
        j.setNome(apelido);
        j.setApelido(apelido);
        j.setPosicao(posicao);
        cadastro.add(j);
        return j;
    }

    private JogadorPartida jp(Jogador j, String time, String status, String funcao) {
        JogadorPartida jp = new JogadorPartida();
        jp.setJogadorId(j.getId());
        jp.setJogador(j);
        jp.setTime(time);
        jp.setStatus(status);
        jp.setFuncao(funcao);
        return jp;
    }

    @Test
    @DisplayName("Remover escalado de linha: entra o próximo de linha do banco, pulando goleiro e suspenso")
    void removerEscaladoLinhaPegaProximoDeLinha() {
        Jogador ana = jogador(1, "Ana", "Meia");
        Jogador goleiroReserva = jogador(2, "Gol2", "Goleiro");
        Jogador suspenso = jogador(3, "Susp", "Atacante");
        suspenso.setEstaSuspenso(true);
        Jogador bia = jogador(4, "Bia", "Zagueiro");
        Jogador caio = jogador(5, "Caio", "Lateral");

        Partida p = new Partida();
        p.getListaGeralPresenca().add(jp(ana, "Azul", "Titular", "Azul_MEI_7"));
        p.getListaGeralPresenca().add(jp(goleiroReserva, "Nenhum", "Reserva", "GOL"));
        p.getListaGeralPresenca().add(jp(suspenso, "Nenhum", "Reserva", "LIN"));
        p.getListaGeralPresenca().add(jp(bia, "Nenhum", "Reserva", "LIN"));
        p.getListaGeralPresenca().add(jp(caio, "Nenhum", "Reserva", "LIN"));

        Jogador entrou = service.removerJogadorEscalado(p, "Ana", "Azul", "MEI");

        assertEquals("Bia", entrou.getApelido());
        assertTrue(p.getListaGeralPresenca().stream().noneMatch(x -> x.getJogadorId() == 1));
        JogadorPartida jpBia = p.getListaGeralPresenca().stream().filter(x -> x.getJogadorId() == 4).findFirst().get();
        assertEquals("Azul", jpBia.getTime());
        assertEquals("Titular", jpBia.getStatus());
        assertEquals("Azul_MEI_7", jpBia.getFuncao());
    }

    @Test
    @DisplayName("Remover goleiro sem goleiro no banco deixa a vaga vazia")
    void removerGoleiroSemReservaDeixaVazio() {
        Jogador gol = jogador(1, "Gol1", "Goleiro");
        Jogador linha = jogador(2, "Linha", "Meia");

        Partida p = new Partida();
        p.getListaGeralPresenca().add(jp(gol, "Vermelho", "Titular", "Vermelho_GOL_1"));
        p.getListaGeralPresenca().add(jp(linha, "Nenhum", "Reserva", "LIN"));

        assertNull(service.removerJogadorEscalado(p, "Gol1", "Vermelho", "GOL"));
        assertEquals(1, p.getListaGeralPresenca().size());
        assertEquals("Nenhum", p.getListaGeralPresenca().get(0).getTime());
    }

    @Test
    @DisplayName("Remover escalado pula quem está apitando")
    void removerEscaladoPulaArbitragem() {
        Jogador ana = jogador(1, "Ana", "Meia");
        Jogador juiz = jogador(2, "Juiz", "Meia");
        Jogador bia = jogador(3, "Bia", "Meia");

        Partida p = new Partida();
        p.setArbitro("Juiz");
        p.getListaGeralPresenca().add(jp(ana, "Azul", "Titular", "Azul_MEI_7"));
        p.getListaGeralPresenca().add(jp(juiz, "Nenhum", "Reserva", "LIN"));
        p.getListaGeralPresenca().add(jp(bia, "Nenhum", "Reserva", "LIN"));

        assertEquals("Bia", service.removerJogadorEscalado(p, "Ana", "Azul", "MEI").getApelido());
    }

    @Test
    @DisplayName("Desfazer substituição devolve quem entrou ao banco e quem saiu à vaga")
    void desfazerSubstituicao() {
        Jogador j1 = jogador(1, "J1", "Meia");
        Jogador j2 = jogador(2, "J2", "Meia");

        Partida p = new Partida();
        JogadorPartida jp1 = jp(j1, "Nenhum", "Substituido", "Azul_MEI_7");
        JogadorPartida jp2 = jp(j2, "Azul", "Reserva", "Azul_MEI_7");
        p.getListaGeralPresenca().add(jp1);
        p.getListaGeralPresenca().add(jp2);

        assertTrue(service.desfazerSubstituicao(p, "J1 / J2", "Azul"));
        assertEquals("Azul", jp1.getTime());
        assertEquals("Titular", jp1.getStatus());
        assertEquals("Azul_MEI_7", jp1.getFuncao());
        assertEquals("Nenhum", jp2.getTime());
        assertEquals("Reserva", jp2.getStatus());
    }

    @Test
    @DisplayName("Desfazer recria quem tinha saído e foi excluído do banco")
    void desfazerRecriaJogadorExcluido() {
        jogador(1, "J1", "Meia");
        Jogador j2 = jogador(2, "J2", "Meia");

        Partida p = new Partida();
        p.getListaGeralPresenca().add(jp(j2, "Azul", "Reserva", "Azul_MEI_7"));

        service.desfazerSubstituicao(p, "J1 / J2", "Azul");
        JogadorPartida volta = p.getListaGeralPresenca().stream().filter(x -> x.getJogadorId() == 1).findFirst().get();
        assertEquals("Azul", volta.getTime());
        assertEquals("Azul_MEI_7", volta.getFuncao());
    }

    @Test
    @DisplayName("Editar partida: cartão já cumprido (zerado) não volta, e remover cartão tira a suspensão")
    void editarAplicaSoDiferencaDeCartoes() {
        Jogador ana = jogador(1, "Ana", "Meia");
        Jogador bia = jogador(2, "Bia", "Meia");
        bia.setEstaSuspenso(true);
        bia.setStatus("Suspenso");

        Partida salva = new Partida();
        salva.setId(10);
        salva.setGridAzul("[[\"Ana\",\"MEI\",\"🟨\"],[\"Bia\",\"MEI\",\"🟥\"]]");
        salva.setGridVermelho("[]");
        when(partidaRepository.findById(10)).thenReturn(Optional.of(salva));

        // Ana já cumpriu suspensão: o amarelo dela foi zerado no histórico
        JogadorPartida gravadaAna = jp(ana, "Azul", "TITULAR", "Azul_MEI");
        gravadaAna.setPartidaId(10);
        gravadaAna.setCartaoAmarelo(0);
        JogadorPartida gravadaBia = jp(bia, "Azul", "TITULAR", "Azul_MEI");
        gravadaBia.setPartidaId(10);
        gravadaBia.setCartaoVermelho(1);
        when(jogadorPartidaRepository.findByPartidaId(10)).thenReturn(List.of(gravadaAna, gravadaBia));

        List<JogadorPartida> salvas = new ArrayList<>();
        when(jogadorPartidaRepository.saveAll(any())).thenAnswer(inv -> {
            inv.<Iterable<JogadorPartida>>getArgument(0).forEach(salvas::add);
            return salvas;
        });
        when(jogadorPartidaRepository.findByJogadorId(anyInt())).thenAnswer(inv -> salvas.stream()
                .filter(x -> x.getJogadorId().equals(inv.getArgument(0))).toList());

        Partida dados = new Partida();
        dados.setNomePartida("Corrigida");
        dados.setGolsTimeAzul(1);
        // Ana mantém o amarelo e ganha um gol; o vermelho da Bia foi lançado por engano
        boolean ok = service.editarPartida(10, dados,
                "[[\"Ana\",\"MEI\",\"⚽ 🟨\"],[\"Bia\",\"MEI\",\"\"]]", "[]");

        assertTrue(ok);
        assertEquals("Corrigida", salva.getNomePartida());
        JogadorPartida novaAna = salvas.stream().filter(x -> x.getJogadorId() == 1).findFirst().get();
        assertEquals(0, novaAna.getCartaoAmarelo());
        assertEquals(1, novaAna.getGols());
        JogadorPartida novaBia = salvas.stream().filter(x -> x.getJogadorId() == 2).findFirst().get();
        assertEquals(0, novaBia.getCartaoVermelho());
        assertFalse(bia.getEstaSuspenso());
        verify(jogadorPartidaRepository).deleteByPartidaId(10);
    }

    @Test
    @DisplayName("Excluir partida apaga atuações, eventos e a partida")
    void excluirPartida() {
        Partida salva = new Partida();
        salva.setId(20);
        when(partidaRepository.findById(20)).thenReturn(Optional.of(salva));
        when(jogadorPartidaRepository.findByPartidaId(20)).thenReturn(new ArrayList<>());
        when(eventoRepository.findByPartidaId(20)).thenReturn(new ArrayList<>());

        assertTrue(service.excluirPartida(20));
        verify(jogadorPartidaRepository).deleteByPartidaId(20);
        verify(partidaRepository).delete(salva);
        assertFalse(service.excluirPartida(99));
    }
}
