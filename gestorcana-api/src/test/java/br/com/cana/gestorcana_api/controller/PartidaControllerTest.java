package br.com.cana.gestorcana_api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 🛡️ Rollback automático
class PartidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. Deve listar todas as partidas da temporada com sucesso")
    void deveListarPartidasPorTemporada() throws Exception {
        mockMvc.perform(get("/partidas")
                .param("temporada", "2026")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("2. Deve retornar 404 para ID inexistente")
    void deveBuscarPartidaPorIdInexistente() throws Exception {
        mockMvc.perform(get("/partidas/999999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("3. Deve salvar nova partida enviando temporadaId correto")
    void deveSalvarNovaPartida() throws Exception {
        // Usa a propriedade 'temporadaId' exata da Entity Partida.java
        String partidaJson = """
            {
                "temporadaId": 2026,
                "dataPartida": "2026-08-08",
                "nomePartida": "Partida de Teste Automático",
                "golsTimeAzul": 2,
                "golsTimeVermelho": 1
            }
            """;

        mockMvc.perform(post("/partidas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(partidaJson))
                .andExpect(status().isOk());
    }
}