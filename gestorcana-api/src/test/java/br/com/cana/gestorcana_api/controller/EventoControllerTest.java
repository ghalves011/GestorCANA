package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.Evento;
import br.com.cana.gestorcana_api.service.EventoService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventoController.class)
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventoService eventoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Evento eventoExemplo;

    @BeforeEach
    void setUp() {
        eventoExemplo = new Evento();
        eventoExemplo.setId(1);
        eventoExemplo.setPartidaId(10);
        eventoExemplo.setJogadorId(5);
        eventoExemplo.setTipo("GOL");
    }

    @Test
    @DisplayName("Deverá registrar um lance/evento com sucesso (HTTP 201 Created)")
    void testRegistrarLanceSucesso() throws Exception {
        Mockito.when(eventoService.registrarLance(any(Evento.class))).thenReturn("OK");

        mockMvc.perform(post("/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventoExemplo)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Evento registrado com sucesso!"));
    }

    @Test
    @DisplayName("Deverá retornar HTTP 400 ao tentar registrar lance inválido")
    void testRegistrarLanceInvalido() throws Exception {
        Mockito.when(eventoService.registrarLance(any(Evento.class)))
                .thenReturn("Erro: Evento sem jogador ou partida vinculada.");

        mockMvc.perform(post("/eventos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(eventoExemplo)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Erro: Evento sem jogador ou partida vinculada."));
    }

    @Test
    @DisplayName("Deverá listar eventos por partida (HTTP 200 OK)")
    void testListarPorPartida() throws Exception {
        Mockito.when(eventoService.listarPorPartida(eq(10))).thenReturn(List.of(eventoExemplo));

        mockMvc.perform(get("/eventos/partida/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].partidaId", is(10)))
                .andExpect(jsonPath("$[0].tipo", is("GOL")));
    }

    @Test
    @DisplayName("Deverá listar eventos por jogador (HTTP 200 OK)")
    void testListarPorJogador() throws Exception {
        Mockito.when(eventoService.listarPorJogador(eq(5))).thenReturn(List.of(eventoExemplo));

        mockMvc.perform(get("/eventos/jogador/5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].jogadorId", is(5)));
    }
}