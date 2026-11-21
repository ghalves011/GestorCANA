package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.dto.ResumoViradaDTO;
import br.com.cana.gestorcana_api.entity.StatusTemporada;
import br.com.cana.gestorcana_api.entity.Temporada;
import br.com.cana.gestorcana_api.exception.RegraNegocioException;
import br.com.cana.gestorcana_api.service.TemporadaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TemporadaController.class)
@TestPropertySource(properties = "gestorcana.api-key=chave-de-teste")
class TemporadaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemporadaService service;

    @Autowired
    private ObjectMapper objectMapper;

    private Temporada temporadaExemplo;

    @BeforeEach
    void setUp() {
        temporadaExemplo = new Temporada();
        temporadaExemplo.setId(1);
        temporadaExemplo.setAno(2026);
    }

    @Test
    @DisplayName("Deverá listar todas as temporadas (HTTP 200 OK)")
    void testListar() throws Exception {
        Mockito.when(service.listarTodas()).thenReturn(List.of(temporadaExemplo));

        mockMvc.perform(get("/temporadas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ano", is(2026)));
    }

    @Test
    @DisplayName("Deverá buscar temporada por ID existente (HTTP 200 OK)")
    void testBuscarPorIdSucesso() throws Exception {
        Mockito.when(service.buscarPorId(1)).thenReturn(Optional.of(temporadaExemplo));

        mockMvc.perform(get("/temporadas/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.ano", is(2026)));
    }

    @Test
    @DisplayName("Deverá retornar HTTP 404 ao buscar temporada por ID inexistente")
    void testBuscarPorIdNaoEncontrado() throws Exception {
        Mockito.when(service.buscarPorId(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/temporadas/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deverá buscar temporada por ano existente (HTTP 200 OK)")
    void testBuscarPorAnoSucesso() throws Exception {
        Mockito.when(service.buscarPorAno(2026)).thenReturn(Optional.of(temporadaExemplo));

        mockMvc.perform(get("/temporadas/ano/2026")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ano", is(2026)));
    }

    @Test
    @DisplayName("Deverá salvar temporada com sucesso (HTTP 201 Created)")
    void testSalvarSucesso() throws Exception {
        Mockito.when(service.salvar(any(Temporada.class))).thenReturn("OK");

        mockMvc.perform(post("/temporadas")
                .header("X-API-KEY", "chave-de-teste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(temporadaExemplo)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Temporada criada com sucesso!"));
    }

    @Test
    @DisplayName("Deverá recusar salvar temporada com ano inválido (HTTP 400 Bad Request)")
    void testSalvarAnoInvalido() throws Exception {
        Mockito.when(service.salvar(any(Temporada.class)))
                .thenReturn("Erro: Ano inválido (deve estar entre 2000 e 2100).");

        mockMvc.perform(post("/temporadas")
                .header("X-API-KEY", "chave-de-teste")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(temporadaExemplo)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erro: Ano inválido (deve estar entre 2000 e 2100)."));
    }

    @Test
    @DisplayName("Deverá deletar temporada existente (HTTP 204 No Content)")
    void testDeletarSucesso() throws Exception {
        Mockito.when(service.deletar(eq(1))).thenReturn(true);

        mockMvc.perform(delete("/temporadas/1")
                .header("X-API-KEY", "chave-de-teste")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deverá retornar HTTP 409 ao tentar deletar a temporada vigente")
    void testDeletarRecusado() throws Exception {
        Mockito.when(service.deletar(eq(2026)))
                .thenThrow(new RegraNegocioException("Não é possível excluir a temporada vigente."));

        mockMvc.perform(delete("/temporadas/2026")
                .header("X-API-KEY", "chave-de-teste"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Não é possível excluir a temporada vigente."));
    }

    @Test
    @DisplayName("GET /temporadas/vigente: 200 com a vigente ou 404")
    void testBuscarVigente() throws Exception {
        temporadaExemplo.setStatus(StatusTemporada.VIGENTE);
        Mockito.when(service.buscarVigente()).thenReturn(Optional.of(temporadaExemplo));

        mockMvc.perform(get("/temporadas/vigente"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ano", is(2026)))
                .andExpect(jsonPath("$.status", is("VIGENTE")));

        Mockito.when(service.buscarVigente()).thenReturn(Optional.empty());
        mockMvc.perform(get("/temporadas/vigente"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /temporadas/virada/resumo: 200 com o resumo")
    void testResumoVirada() throws Exception {
        Mockito.when(service.gerarResumoVirada()).thenReturn(new ResumoViradaDTO(
                2026, 2027, 40, List.of("Bia"), 7, false, List.of("Fazer backup antes de confirmar.")));

        mockMvc.perform(get("/temporadas/virada/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anoNovo", is(2027)))
                .andExpect(jsonPath("$.jogadoresSuspensos[0]", is("Bia")))
                .andExpect(jsonPath("$.loteAnoNovoJaGerado", is(false)));
    }

    @Test
    @DisplayName("POST /temporadas/virada: 200 com a nova temporada")
    void testVirarSucesso() throws Exception {
        Temporada nova = new Temporada();
        nova.setId(2027);
        nova.setAno(2027);
        nova.setStatus(StatusTemporada.VIGENTE);
        Mockito.when(service.virarTemporada()).thenReturn(nova);

        mockMvc.perform(post("/temporadas/virada")
                .header("X-API-KEY", "chave-de-teste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2027)))
                .andExpect(jsonPath("$.status", is("VIGENTE")));
    }

    @Test
    @DisplayName("POST /temporadas/virada: 409 com a mensagem quando a regra recusa")
    void testVirarConflito() throws Exception {
        Mockito.when(service.virarTemporada())
                .thenThrow(new RegraNegocioException("A temporada 2028 só pode ser aberta a partir de 2027."));

        mockMvc.perform(post("/temporadas/virada")
                .header("X-API-KEY", "chave-de-teste"))
                .andExpect(status().isConflict())
                .andExpect(content().string("A temporada 2028 só pode ser aberta a partir de 2027."));
    }

    @Test
    @DisplayName("Deverá retornar HTTP 404 ao tentar deletar temporada inexistente")
    void testDeletarNaoEncontrado() throws Exception {
        Mockito.when(service.deletar(eq(99))).thenReturn(false);

        mockMvc.perform(delete("/temporadas/99")
                .header("X-API-KEY", "chave-de-teste")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
