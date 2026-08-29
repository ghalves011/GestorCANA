package br.com.cana.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

public class ApiClient {

    private static final String BASE_URL = "http://157.151.15.177:8080";
    private static final HttpClient client = HttpClient.newHttpClient();

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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Accept", "application/json; charset=UTF-8")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
        return response.body();
    }

    // Método genérico para POST (Cadastrar/Salvar)
    public static String post(String endpoint, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Método genérico para PUT (Atualizar)
    public static String put(String endpoint, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Método genérico para DELETE (Excluir)
    public static void delete(String endpoint) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .DELETE()
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}