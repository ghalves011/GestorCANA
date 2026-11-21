package br.com.cana.gestorcana_api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyFilterTest {

    // Valor falso, só para teste. Nunca use a chave real aqui.
    private static final String CHAVE_TESTE = "chave-de-teste";

    private final ApiKeyFilter filtro = new ApiKeyFilter(CHAVE_TESTE);

    /** Executa o filtro e devolve a resposta; a cadeia guarda a requisição se ela passou. */
    private MockHttpServletResponse executar(ApiKeyFilter f, MockHttpServletRequest req, MockFilterChain chain)
            throws Exception {
        MockHttpServletResponse resp = new MockHttpServletResponse();
        f.doFilter(req, resp, chain);
        return resp;
    }

    private void assertBarrado(MockHttpServletResponse resp, MockFilterChain chain) throws Exception {
        assertEquals(401, resp.getStatus());
        assertEquals(ApiKeyFilter.MENSAGEM_401, resp.getContentAsString());
        assertNull(chain.getRequest(), "a requisição não deveria chegar ao controller");
    }

    @Test
    @DisplayName("DELETE sem chave → 401")
    void deleteSemChave() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        assertBarrado(executar(filtro, new MockHttpServletRequest("DELETE", "/jogadores/1"), chain), chain);
    }

    @Test
    @DisplayName("DELETE com chave errada → 401")
    void deleteChaveErrada() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("DELETE", "/jogadores/1");
        req.addHeader(ApiKeyFilter.CABECALHO, "chave-errada");
        MockFilterChain chain = new MockFilterChain();
        assertBarrado(executar(filtro, req, chain), chain);
    }

    @Test
    @DisplayName("DELETE com a chave certa → passa")
    void deleteChaveCerta() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("DELETE", "/jogadores/1");
        req.addHeader(ApiKeyFilter.CABECALHO, CHAVE_TESTE);
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletResponse resp = executar(filtro, req, chain);
        assertEquals(200, resp.getStatus());
        assertNotNull(chain.getRequest());
    }

    @Test
    @DisplayName("GET sem chave → passa")
    void getSemChave() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        executar(filtro, new MockHttpServletRequest("GET", "/temporadas/virada/resumo"), chain);
        assertNotNull(chain.getRequest());
    }

    @Test
    @DisplayName("POST/PUT de partida, jogador e contribuição continuam sem chave")
    void rotasDoJogoContinuamAbertas() throws Exception {
        String[][] abertas = {
                {"POST", "/partidas"}, {"PUT", "/partidas/5"},
                {"POST", "/jogadores"}, {"PUT", "/jogadores/3"},
                {"POST", "/contribuicoes/gerar-lote"},
        };
        for (String[] r : abertas) {
            MockFilterChain chain = new MockFilterChain();
            executar(filtro, new MockHttpServletRequest(r[0], r[1]), chain);
            assertNotNull(chain.getRequest(), r[0] + " " + r[1] + " deveria passar sem chave");
        }
    }

    @Test
    @DisplayName("Virada e alterações de temporada exigem chave (inclusive com barra extra)")
    void rotasDeTemporadaProtegidas() throws Exception {
        String[][] protegidas = {
                {"POST", "/temporadas/virada"}, {"POST", "/temporadas"}, {"PUT", "/temporadas/2026"},
                {"POST", "/temporadas/virada/"}, {"POST", "//temporadas//virada"},
        };
        for (String[] r : protegidas) {
            MockFilterChain chain = new MockFilterChain();
            assertBarrado(executar(filtro, new MockHttpServletRequest(r[0], r[1]), chain), chain);
        }
    }

    @Test
    @DisplayName("Chave não configurada → 401 nas rotas protegidas (falha fechada)")
    void semChaveConfigurada() throws Exception {
        ApiKeyFilter semChave = new ApiKeyFilter("");

        MockHttpServletRequest delete = new MockHttpServletRequest("DELETE", "/temporadas/2026");
        delete.addHeader(ApiKeyFilter.CABECALHO, "");
        MockFilterChain chain1 = new MockFilterChain();
        assertBarrado(executar(semChave, delete, chain1), chain1);

        MockFilterChain chain2 = new MockFilterChain();
        assertBarrado(executar(semChave, new MockHttpServletRequest("POST", "/temporadas/virada"), chain2), chain2);

        // As rotas abertas seguem funcionando mesmo sem chave configurada.
        MockFilterChain chain3 = new MockFilterChain();
        executar(semChave, new MockHttpServletRequest("GET", "/temporadas/vigente"), chain3);
        assertNotNull(chain3.getRequest());
    }
}
