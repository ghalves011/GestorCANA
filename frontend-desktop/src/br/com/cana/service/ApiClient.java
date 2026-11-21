package br.com.cana.service;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public class ApiClient {

    // Produção por padrão. Para testar contra outra API sem alterar o código:
    // java -Dgestorcana.api.url=http://localhost:8080 -jar frontend-desktop.jar
    private static final String BASE_URL = carregarBaseUrl();

    private static String carregarBaseUrl() {
        String url = System.getProperty("gestorcana.api.url");
        if (url == null || url.isBlank()) {
            return "http://157.151.15.177:8080";
        }
        url = url.trim().replaceAll("/+$", "");
        System.err.println("⚠️ API alternativa em uso: " + url);
        return url;
    }
    private static final HttpClient client = HttpClient.newHttpClient();

    // Chave exigida pela API em DELETE e nas alterações de temporada (cabeçalho X-API-KEY).
    // Lida uma única vez de %USERPROFILE%\.gestorcana\gestorcana.properties (api.key), fora do repositório.
    // Sem o arquivo o desktop funciona normalmente; só as ações protegidas recebem 401.
    private static final String API_KEY = carregarChave();

    public static final String MENSAGEM_SEM_CHAVE =
            "Esta ação exige a chave de acesso. Configure o arquivo .gestorcana\\gestorcana.properties na sua pasta de usuário.";

    private static String carregarChave() {
        Path arquivo = Paths.get(System.getProperty("user.home"), ".gestorcana", "gestorcana.properties");
        if (!Files.isRegularFile(arquivo)) {
            return null;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(arquivo)) {
            props.load(in);
            String chave = props.getProperty("api.key");
            return chave == null || chave.isBlank() ? null : chave.trim();
        } catch (Exception e) {
            System.err.println("Não foi possível ler " + arquivo + ": " + e.getMessage());
            return null;
        }
    }

    // Toda requisição sai daqui, já com a chave (quando houver). Nas rotas abertas ela é ignorada.
    private static HttpRequest.Builder requisicao(String endpoint) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(BASE_URL + endpoint));
        if (API_KEY != null) {
            builder.header("X-API-KEY", API_KEY);
        }
        return builder;
    }

    // 401 = a API recusou a chave; troca o texto cru por uma orientação para a pessoa.
    private static void verificarChave(HttpResponse<String> response) throws Exception {
        if (response.statusCode() == 401) {
            throw new Exception(MENSAGEM_SEM_CHAVE);
        }
    }

    // 🌟 GSON CENTRALIZADO TURBINADO (LocalDate + Booleans vindos como 1/0)
    // ❌ MUDAR O BLOCO DO GSON E DO MÉTODOGET PARA:

    public static final Gson GSON = new GsonBuilder()
            // 1. Suporte a LocalDate flexível (ISO YYYY-MM-DD e BR DD/MM/YYYY)
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> {
                if (json == null || json.getAsString().trim().isEmpty()) {
                    return null;
                }
                String str = json.getAsString().trim().split("T")[0];
                try {
                    if (str.contains("-")) {
                        return LocalDate.parse(str, java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);
                    } else if (str.contains("/")) {
                        return LocalDate.parse(str, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao converter data JSON: " + str);
                }
                return null;
            })
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> src == null ? null
                            : context.serialize(src.toString()))

            // 2. Suporte a Booleanos (1/0 ou true/false)
            .registerTypeAdapter(boolean.class, (JsonDeserializer<Boolean>) (json, typeOfT, context) -> {
                if (json.isJsonPrimitive()) {
                    if (json.getAsJsonPrimitive().isNumber()) {
                        return json.getAsInt() == 1;
                    }
                    if (json.getAsJsonPrimitive().isBoolean()) {
                        return json.getAsBoolean();
                    }
                }
                return false;
            })
            .registerTypeAdapter(Boolean.class, (JsonDeserializer<Boolean>) (json, typeOfT, context) -> {
                if (json.isJsonPrimitive()) {
                    if (json.getAsJsonPrimitive().isNumber()) {
                        return json.getAsInt() == 1;
                    }
                    if (json.getAsJsonPrimitive().isBoolean()) {
                        return json.getAsBoolean();
                    }
                }
                return null;
            })
            .create();

    // Método GET com codificação UTF-8 forçada
    public static String get(String endpoint) throws Exception {
        HttpRequest request = requisicao(endpoint)
                .header("Accept", "application/json; charset=UTF-8")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        verificarChave(response);
        return response.body();
    }

    // GET que falha com a mensagem da API quando ela recusa (status >= 300).
    public static String getOuFalha(String endpoint) throws Exception {
        return enviarOuFalha(requisicao(endpoint)
                .header("Accept", "application/json; charset=UTF-8")
                .GET()
                .build());
    }

    // GET que devolve null no 404 (ex.: nenhuma temporada vigente) e falha nos demais erros.
    public static String getOuNulo(String endpoint) throws Exception {
        HttpRequest request = requisicao(endpoint)
                .header("Accept", "application/json; charset=UTF-8")
                .GET()
                .build();
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        if (response.statusCode() == 404) {
            return null;
        }
        verificarChave(response);
        if (response.statusCode() >= 300) {
            String corpo = response.body();
            throw new Exception(corpo != null && !corpo.isBlank() ? corpo : "HTTP " + response.statusCode());
        }
        return response.body();
    }

    // Método genérico para POST (Cadastrar/Salvar)
    public static String post(String endpoint, String jsonBody) throws Exception {
        HttpRequest request = requisicao(endpoint)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        verificarChave(response);
        return response.body();
    }

    // Método genérico para PUT (Atualizar)
    public static String put(String endpoint, String jsonBody) throws Exception {
        HttpRequest request = requisicao(endpoint)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        verificarChave(response);
        return response.body();
    }

    // POST/PUT que falham com a mensagem da API quando ela recusa (status >= 300).
    // post()/put() devolvem o corpo de qualquer resposta, então um 400 com
    // "A camisa número 10 já pertence a outro jogador!" parecia sucesso.
    public static String postOuFalha(String endpoint, String jsonBody) throws Exception {
        return enviarOuFalha(requisicao(endpoint)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build());
    }

    public static String putOuFalha(String endpoint, String jsonBody) throws Exception {
        return enviarOuFalha(requisicao(endpoint)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build());
    }

    private static String enviarOuFalha(HttpRequest request) throws Exception {
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        verificarChave(response);
        String corpo = response.body();
        if (response.statusCode() >= 300) {
            throw new Exception(corpo != null && !corpo.isBlank() ? corpo : "HTTP " + response.statusCode());
        }
        return corpo;
    }

    // Método genérico para DELETE (Excluir)
    public static void delete(String endpoint) throws Exception {
        HttpRequest request = requisicao(endpoint)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        verificarChave(response);
        // Sem isso, uma recusa da API (ex.: 400 "jogador possui partidas") passava
        // como sucesso e a tela dizia "Excluído" sem nada ter sido excluído.
        if (response.statusCode() >= 300) {
            String corpo = response.body();
            throw new Exception(corpo != null && !corpo.isBlank() ? corpo : "HTTP " + response.statusCode());
        }
    }
}