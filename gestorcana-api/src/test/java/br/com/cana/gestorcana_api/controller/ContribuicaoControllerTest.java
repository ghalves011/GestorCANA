package br.com.cana.gestorcana_api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 🛡️ Rollback automático
class ContribuicaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("1. Deve obter a matriz de contribuições do ano sem erros")
    void deveObterMatrizContribuicoes() throws Exception {
        mockMvc.perform(get("/contribuicoes")
                .param("ano", "2026")
                .param("busca", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. Deve testar geração de lote anual de mensalidades")
    void deveGerarLoteAnual() throws Exception {
        String payload = """
            {
                "ano": 2026,
                "valor": 50.0
            }
            """;

        mockMvc.perform(post("/contribuicoes/gerar-lote")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("3. Deve solicitar texto do recibo de pagamento")
    void deveObterTextoRecibo() throws Exception {
        mockMvc.perform(get("/contribuicoes/recibo")
                .param("jogadorId", "999999")
                .param("mes", "8")
                .param("ano", "2026"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("4. Deve testar exclusão de pagamento de mensalidade")
    void deveExcluirPagamento() throws Exception {
        mockMvc.perform(delete("/contribuicoes")
                .param("jogadorId", "999999")
                .param("mes", "8")
                .param("ano", "2026"))
                .andExpect(status().isOk());
    }
}