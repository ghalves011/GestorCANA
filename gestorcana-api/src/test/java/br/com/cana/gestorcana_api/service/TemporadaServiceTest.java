package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.dto.ResumoViradaDTO;
import br.com.cana.gestorcana_api.entity.StatusTemporada;
import br.com.cana.gestorcana_api.entity.Temporada;
import br.com.cana.gestorcana_api.exception.RegraNegocioException;
import br.com.cana.gestorcana_api.repository.ContribuicaoRepository;
import br.com.cana.gestorcana_api.repository.JogadorRepository;
import br.com.cana.gestorcana_api.repository.PartidaRepository;
import br.com.cana.gestorcana_api.repository.TemporadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TemporadaServiceTest {

    @Mock
    private TemporadaRepository repository;
    @Mock
    private PartidaRepository partidaRepository;
    @Mock
    private JogadorRepository jogadorRepository;
    @Mock
    private ContribuicaoRepository contribuicaoRepository;

    private TemporadaService service;

    // "Hoje" fixo em 20/11/2026: a temporada 2027 pode ser aberta, a 2028 ainda não.
    private final Clock relogio = Clock.fixed(Instant.parse("2026-11-20T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    @BeforeEach
    void setUp() {
        service = new TemporadaService(repository, partidaRepository, jogadorRepository, contribuicaoRepository, relogio);
    }

    private Temporada temporada(int ano, StatusTemporada status) {
        Temporada t = new Temporada();
        t.setId(ano);
        t.setAno(ano);
        t.setStatus(status);
        return t;
    }

    private void verificaQueNadaFoiGravado() {
        verify(repository, never()).save(any());
        verify(repository, never()).saveAndFlush(any());
        verify(repository, never()).inserirVigenteComIdIgualAoAno(anyInt());
        verify(repository, never()).deleteById(any());
        verifyNoInteractions(jogadorRepository, contribuicaoRepository);
    }

    @Test
    @DisplayName("Virada com sucesso: a vigente fica ENCERRADA (com flush) antes do insert da nova")
    void viradaComSucesso() {
        Temporada t2026 = temporada(2026, StatusTemporada.VIGENTE);
        Temporada t2027 = temporada(2027, StatusTemporada.VIGENTE);
        when(repository.findByStatus(StatusTemporada.VIGENTE)).thenReturn(Optional.of(t2026));
        when(repository.existsById(2027)).thenReturn(false);
        when(repository.existsByAno(2027)).thenReturn(false);
        when(repository.findById(2027)).thenReturn(Optional.of(t2027));

        Temporada nova = service.virarTemporada();

        assertEquals(2027, nova.getId());
        assertEquals(StatusTemporada.VIGENTE, nova.getStatus());
        assertEquals(StatusTemporada.ENCERRADA, t2026.getStatus());

        InOrder ordem = inOrder(repository);
        ordem.verify(repository).saveAndFlush(t2026);
        ordem.verify(repository).inserirVigenteComIdIgualAoAno(2027);

        // RN12 / RF02: nada de jogadores, partidas ou contribuições é alterado
        verifyNoInteractions(jogadorRepository, partidaRepository, contribuicaoRepository);
    }

    @Test
    @DisplayName("Virada sem temporada vigente: lança exceção e não grava nada")
    void viradaSemVigente() {
        when(repository.findByStatus(StatusTemporada.VIGENTE)).thenReturn(Optional.empty());

        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> service.virarTemporada());

        assertEquals("Não existe temporada vigente.", e.getMessage());
        verificaQueNadaFoiGravado();
    }

    @Test
    @DisplayName("Virada quando a temporada do ano novo já existe: lança exceção e não grava nada")
    void viradaAnoNovoJaExiste() {
        when(repository.findByStatus(StatusTemporada.VIGENTE))
                .thenReturn(Optional.of(temporada(2026, StatusTemporada.VIGENTE)));
        when(repository.existsById(2027)).thenReturn(false);
        when(repository.existsByAno(2027)).thenReturn(true);

        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> service.virarTemporada());

        assertEquals("A temporada 2027 já existe.", e.getMessage());
        verificaQueNadaFoiGravado();
    }

    @Test
    @DisplayName("Virada quando o id do ano novo já existe: lança exceção e não grava nada")
    void viradaIdAnoNovoJaExiste() {
        when(repository.findByStatus(StatusTemporada.VIGENTE))
                .thenReturn(Optional.of(temporada(2026, StatusTemporada.VIGENTE)));
        when(repository.existsById(2027)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> service.virarTemporada());
        verificaQueNadaFoiGravado();
    }

    @Test
    @DisplayName("Virada para além do ano atual + 1: lança exceção e não grava nada")
    void viradaCedoDemais() {
        // Em 2026 a vigente já é 2027: abrir 2028 só a partir de 2027
        when(repository.findByStatus(StatusTemporada.VIGENTE))
                .thenReturn(Optional.of(temporada(2027, StatusTemporada.VIGENTE)));
        when(repository.existsById(2028)).thenReturn(false);
        when(repository.existsByAno(2028)).thenReturn(false);

        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> service.virarTemporada());

        assertEquals("A temporada 2028 só pode ser aberta a partir de 2027.", e.getMessage());
        verificaQueNadaFoiGravado();
    }

    @Test
    @DisplayName("Resumo da virada: monta os números e não chama nenhum método de escrita")
    void resumoNaoGrava() {
        when(repository.findByStatus(StatusTemporada.VIGENTE))
                .thenReturn(Optional.of(temporada(2026, StatusTemporada.VIGENTE)));
        when(partidaRepository.countByTemporadaId(2026)).thenReturn(40L);
        when(jogadorRepository.listarNomesSuspensosAtivos()).thenReturn(List.of("Zé", "Bia"));
        when(contribuicaoRepository.countByAnoAndPago(2026, 0)).thenReturn(7L);
        when(contribuicaoRepository.existsByAno(2027)).thenReturn(false);

        ResumoViradaDTO r = service.gerarResumoVirada();

        assertEquals(2026, r.anoAtual());
        assertEquals(2027, r.anoNovo());
        assertEquals(40L, r.totalPartidasAnoAtual());
        assertEquals(List.of("Bia", "Zé"), r.jogadoresSuspensos());
        assertEquals(7L, r.contribuicoesPendentesAnoAtual());
        assertFalse(r.loteAnoNovoJaGerado());
        assertTrue(r.avisos().contains("Lote de contribuições de 2027 ainda não foi gerado."));
        assertTrue(r.avisos().contains("Fazer backup antes de confirmar."));

        verify(repository, never()).save(any());
        verify(repository, never()).saveAndFlush(any());
        verify(repository, never()).inserirVigenteComIdIgualAoAno(anyInt());
        verify(repository, never()).deleteById(any());
        verify(partidaRepository, never()).save(any());
        verify(jogadorRepository, never()).save(any());
        verify(jogadorRepository, never()).saveAll(any());
        verify(contribuicaoRepository, never()).save(any());
        verify(contribuicaoRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Excluir: recusa a temporada vigente")
    void deletarRecusaVigente() {
        when(repository.findById(2026)).thenReturn(Optional.of(temporada(2026, StatusTemporada.VIGENTE)));

        RegraNegocioException e = assertThrows(RegraNegocioException.class, () -> service.deletar(2026));

        assertEquals("Não é possível excluir a temporada vigente.", e.getMessage());
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Excluir: recusa a temporada com partidas vinculadas")
    void deletarRecusaComPartidas() {
        when(repository.findById(2025)).thenReturn(Optional.of(temporada(2025, StatusTemporada.ENCERRADA)));
        when(partidaRepository.existsByTemporadaId(2025)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> service.deletar(2025));
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Excluir: apaga temporada encerrada sem partidas e retorna false se não existir")
    void deletarPermitido() {
        when(repository.findById(2025)).thenReturn(Optional.of(temporada(2025, StatusTemporada.ENCERRADA)));
        when(partidaRepository.existsByTemporadaId(2025)).thenReturn(false);
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertTrue(service.deletar(2025));
        verify(repository).deleteById(2025);
        assertFalse(service.deletar(99));
    }

    @Test
    @DisplayName("Salvar continua criando a temporada como ENCERRADA")
    void salvarNasceEncerrada() {
        Temporada t = new Temporada();
        t.setAno(2030);
        when(repository.existsByAno(2030)).thenReturn(false);

        assertEquals("OK", service.salvar(t));
        assertEquals(StatusTemporada.ENCERRADA, t.getStatus());
        verify(repository).save(t);
    }
}
