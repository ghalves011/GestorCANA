package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.Temporada;
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
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deverá retornar HTTP 404 ao tentar deletar temporada inexistente")
    void testDeletarNaoEncontrado() throws Exception {
        Mockito.when(service.deletar(eq(99))).thenReturn(false);

        mockMvc.perform(delete("/temporadas/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
