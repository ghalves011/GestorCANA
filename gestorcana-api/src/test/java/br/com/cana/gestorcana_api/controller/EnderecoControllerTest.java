package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.Endereco;
import br.com.cana.gestorcana_api.service.EnderecoService;
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

@WebMvcTest(EnderecoController.class)
class EnderecoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnderecoService enderecoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Endereco enderecoExemplo;

    @BeforeEach
    void setUp() {
        enderecoExemplo = new Endereco();
        enderecoExemplo.setId(1);
        enderecoExemplo.setLogradouro("Rua Dona Margarida");
        enderecoExemplo.setNumero("123");
        enderecoExemplo.setBairro("Centro");
        enderecoExemplo.setCidade("Santa Bárbara d'Oeste");
        enderecoExemplo.setEstado("SP");
        enderecoExemplo.setCep("13450-000");
    }

    @Test
    @DisplayName("Deverá listar todos os endereços com sucesso (HTTP 200)")
    void testListar() throws Exception {
        Mockito.when(enderecoService.listarTodos()).thenReturn(List.of(enderecoExemplo));

        mockMvc.perform(get("/enderecos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].logradouro", is("Rua Dona Margarida")));
    }

    @Test
    @DisplayName("Deverá buscar endereço por ID existente (HTTP 200)")
    void testBuscarPorIdSucesso() throws Exception {
        Mockito.when(enderecoService.buscarPorId(1)).thenReturn(Optional.of(enderecoExemplo));

        mockMvc.perform(get("/enderecos/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.cidade", is("Santa Bárbara d'Oeste")));
    }

    @Test
    @DisplayName("Deverá retornar HTTP 404 ao buscar por ID inexistente")
    void testBuscarPorIdNaoEncontrado() throws Exception {
        Mockito.when(enderecoService.buscarPorId(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/enderecos/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deverá buscar endereço formatado por extenso (HTTP 200)")
    void testBuscarEnderecoFormatado() throws Exception {
        String formatado = "Rua Dona Margarida, 123 - Centro, Santa Bárbara d'Oeste - SP, CEP: 13450-000";
        Mockito.when(enderecoService.buscarPorId(1)).thenReturn(Optional.of(enderecoExemplo));
        Mockito.when(enderecoService.obterEnderecoFormatado(any(Endereco.class))).thenReturn(formatado);

        mockMvc.perform(get("/enderecos/1/formatado")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(formatado));
    }

    @Test
    @DisplayName("Deverá salvar um novo endereço com sucesso (HTTP 201)")
    void testSalvarSucesso() throws Exception {
        Mockito.when(enderecoService.salvar(any(Endereco.class))).thenReturn(enderecoExemplo);

        mockMvc.perform(post("/enderecos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enderecoExemplo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.estado", is("SP")));
    }

    @Test
    @DisplayName("Deverá atualizar um endereço existente com sucesso (HTTP 200)")
    void testAtualizarSucesso() throws Exception {
        Mockito.when(enderecoService.atualizar(any(Endereco.class))).thenReturn(true);

        mockMvc.perform(put("/enderecos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(enderecoExemplo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("Deverá deletar um endereço existente com sucesso (HTTP 24 No Content)")
    void testDeletarSucesso() throws Exception {
        Mockito.when(enderecoService.deletar(eq(1))).thenReturn(true);

        mockMvc.perform(delete("/enderecos/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deverá retornar HTTP 404 ao tentar deletar ID inexistente")
    void testDeletarNaoEncontrado() throws Exception {
        Mockito.when(enderecoService.deletar(eq(99))).thenReturn(false);

        mockMvc.perform(delete("/enderecos/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}