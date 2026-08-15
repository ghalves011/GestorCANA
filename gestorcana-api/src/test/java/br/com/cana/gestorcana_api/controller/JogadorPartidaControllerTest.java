package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.JogadorPartida;
import br.com.cana.gestorcana_api.service.JogadorPartidaService;
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

@WebMvcTest(JogadorPartidaController.class)
class JogadorPartidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JogadorPartidaService service;

    @Autowired
    private ObjectMapper objectMapper;

    private JogadorPartida jpExemplo;

    @BeforeEach
    void setUp() {
        jpExemplo = new JogadorPartida();
        jpExemplo.setId(1);
        jpExemplo.setPartidaId(10);
        jpExemplo.setJogadorId(5);
        jpExemplo.setGols(0);
        jpExemplo.setCartaoAmarelo(0);
        jpExemplo.setCartaoVermelho(0);
        jpExemplo.setStatus("TITULAR");
    }

    @Test
    @DisplayName("Deverá listar todas as estatísticas dos jogadores na partida (HTTP 200 OK)")
    void testListar() throws Exception {
        Mockito.when(service.listarTodos()).thenReturn(List.of(jpExemplo));

        mockMvc.perform(get("/jogador-partidas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @DisplayName("Deverá buscar estatística por ID existente (HTTP 200 OK)")
    void testBuscarPorIdSucesso() throws Exception {
        Mockito.when(service.buscarPorId(1)).thenReturn(Optional.of(jpExemplo));

        mockMvc.perform(get("/jogador-partidas/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("Deverá adicionar gol a um jogador existente (HTTP 200 OK)")
    void testAdicionarGol() throws Exception {
        JogadorPartida jpComGol = new JogadorPartida();
        jpComGol.setId(1);
        jpComGol.setGols(1);

        Mockito.when(service.buscarPorId(1)).thenReturn(Optional.of(jpExemplo));
        Mockito.when(service.adicionarGol(any(JogadorPartida.class))).thenReturn(jpComGol);

        mockMvc.perform(post("/jogador-partidas/1/gol")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gols", is(1)));
    }

    @Test
    @DisplayName("Deverá aplicar cartão amarelo a um jogador (HTTP 200 OK)")
    void testAplicarCartaoAmarelo() throws Exception {
        Mockito.when(service.buscarPorId(1)).thenReturn(Optional.of(jpExemplo));
        Mockito.when(service.aplicarCartaoAmarelo(any(JogadorPartida.class))).thenReturn("Cartão amarelo aplicado.");

        mockMvc.perform(post("/jogador-partidas/1/cartao-amarelo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Cartão amarelo aplicado."));
    }

    @Test
    @DisplayName("Deverá registrar substituição de jogadores com sucesso (HTTP 200 OK)")
    void testRegistrarSubstituicaoSucesso() throws Exception {
        Mockito.when(service.registrarSubstituicao(1, 2)).thenReturn(true);

        mockMvc.perform(post("/jogador-partidas/substituicao")
                .param("saiId", "1")
                .param("entraId", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Substituição realizada com sucesso!"));
    }

    @Test
    @DisplayName("Deverá deletar registro com sucesso (HTTP 204 No Content)")
    void testDeletarSucesso() throws Exception {
        Mockito.when(service.deletar(eq(1))).thenReturn(true);

        mockMvc.perform(delete("/jogador-partidas/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}