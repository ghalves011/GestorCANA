package br.com.cana.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CepUtil {

    // Método que vai na internet, consulta o ViaCEP e devolve um Map com os dados
    public static Map<String, String> buscar(String cep) {
        Map<String, String> endereco = new HashMap<>();
        try {
            // Remove a máscara, deixando só os números
            cep = cep.replaceAll("[^0-9]", "");
            if (cep.length() != 8) return endereco;

            // Faz a conexão HTTP com a API
            URL url = java.net.URI.create("https://viacep.com.br/ws/" + cep + "/json/").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                return endereco; // Erro de conexão ou CEP inválido
            }

            // Lê a resposta do ViaCEP
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder jsonSb = new StringBuilder();
            String linha;
            while ((linha = br.readLine()) != null) {
                jsonSb.append(linha);
            }
            conn.disconnect();

            String json = jsonSb.toString();
            if (json.contains("\"erro\": true")) return endereco; // CEP não existe

            // Extrai os dados do JSON manualmente para não precisar de bibliotecas extras
            endereco.put("logradouro", extrairValorJson(json, "logradouro"));
            endereco.put("bairro", extrairValorJson(json, "bairro"));
            endereco.put("cidade", extrairValorJson(json, "localidade")); 
            endereco.put("uf", extrairValorJson(json, "uf"));

        } catch (Exception e) {
            System.err.println("❌ Erro ao buscar CEP na internet: " + e.getMessage());
        }
        return endereco;
    }

    // Método auxiliar para pescar o texto dentro do JSON do ViaCEP
    private static String extrairValorJson(String json, String chave) {
        String chaveBuscada = "\"" + chave + "\": \"";
        int inicio = json.indexOf(chaveBuscada);
        if (inicio == -1) return "";
        inicio += chaveBuscada.length();
        int fim = json.indexOf("\"", inicio);
        return json.substring(inicio, fim);
    }
}
