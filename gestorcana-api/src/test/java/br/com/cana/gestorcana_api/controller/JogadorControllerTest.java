package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.Jogador;
import br.com.cana.gestorcana_api.service.JogadorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 🛡️ Rollback automático: Limpa tudo após o teste e NÃO altera o Supabase
class JogadorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JogadorService jogadorService; // 👈 MockBean adicionado para simular o Service no SpringBootTest

    @Test
    @DisplayName("1. Deve listar todos os jogadores (HTTP 200 OK)")
    void deveListarTodosJogadores() throws Exception {
        mockMvc.perform(get("/jogadores")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("2. Deve listar apenas jogadores ativos com ?status=Ativo sem ambiguidade de rota")
    void deveListarJogadoresAtivos() throws Exception {
        mockMvc.perform(get("/jogadores")
                .param("status", "Ativo")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("3. Deverá buscar jogador por ID existente (HTTP 200)")
    void deveBuscarJogadorPorId() throws Exception {
        Jogador jogadorMock = new Jogador();
        jogadorMock.setId(1);
        jogadorMock.setNome("Jogador Teste");

        Mockito.when(jogadorService.listarTodos()).thenReturn(List.of(jogadorMock));

        mockMvc.perform(get("/jogadores/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Jogador Teste")));
    }
}