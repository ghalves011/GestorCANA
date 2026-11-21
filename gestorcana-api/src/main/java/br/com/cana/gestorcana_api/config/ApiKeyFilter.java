package br.com.cana.gestorcana_api.config;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Exige o cabeçalho X-API-KEY nas operações perigosas, enquanto o login (v5) não existe.
 *
 * Rotas protegidas:
 * - qualquer DELETE;
 * - POST /temporadas e POST /temporadas/virada;
 * - PUT /temporadas/{id}.
 *
 * Todas as outras rotas (GETs, partidas, jogadores, contribuições) continuam abertas.
 * Se a chave não estiver configurada, as rotas protegidas respondem 401 (falha fechada),
 * mas a aplicação sobe normalmente.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String CABECALHO = "X-API-KEY";
    public static final String MENSAGEM_401 = "Chave de acesso inválida ou ausente.";

    private static final Logger log = LoggerFactory.getLogger(ApiKeyFilter.class);

    private final byte[] chaveConfigurada;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    public ApiKeyFilter(@Value("${gestorcana.api-key:}") String chave) {
        String limpa = chave == null ? "" : chave.trim();
        this.chaveConfigurada = limpa.getBytes(StandardCharsets.UTF_8);
    }

    @PostConstruct
    void avisarSeSemChave() {
        if (chaveConfigurada.length == 0) {
            log.warn("gestorcana.api-key não configurada (GESTORCANA_API_KEY vazia): "
                    + "DELETE e as alterações de temporada vão responder 401.");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (exigeChave(request) && !chaveValida(request.getHeader(CABECALHO))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(MENSAGEM_401);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean exigeChave(HttpServletRequest request) {
        String metodo = request.getMethod();
        if ("DELETE".equalsIgnoreCase(metodo)) {
            return true;
        }

        String caminho = normalizar(urlPathHelper.getPathWithinApplication(request));
        if ("POST".equalsIgnoreCase(metodo)) {
            return caminho.equals("/temporadas") || caminho.equals("/temporadas/virada");
        }
        if ("PUT".equalsIgnoreCase(metodo)) {
            return caminho.startsWith("/temporadas/");
        }
        return false;
    }

    /** Junta barras repetidas e tira a barra final, para "//temporadas/" não escapar do filtro. */
    private static String normalizar(String caminho) {
        String c = caminho == null ? "" : caminho.replaceAll("/{2,}", "/");
        while (c.length() > 1 && c.endsWith("/")) {
            c = c.substring(0, c.length() - 1);
        }
        return c;
    }

    private boolean chaveValida(String recebida) {
        if (chaveConfigurada.length == 0 || recebida == null) {
            return false;
        }
        // Comparação em tempo constante; nunca usar equals aqui.
        return MessageDigest.isEqual(chaveConfigurada, recebida.trim().getBytes(StandardCharsets.UTF_8));
    }
}
