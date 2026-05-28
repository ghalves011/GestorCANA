package br.com.cana.util;

public class TextoUtil {
    
    // Normaliza o texto: remove espaços extras e converte para maiúsculas
    public static String normalizar(String t) {
        return (t != null) ? t.trim().toUpperCase() : "";
    }

    // Remove tudo que não for número de uma string
    public static String apenasNumeros(String texto) {
        if (texto == null)
            return "";
        return texto.replaceAll("\\D", "");
    }

    // Formata um CEP no formato 11111-111
    public static String formatarCep(String cep) {
        String limpo = apenasNumeros(cep);
        if (limpo.length() == 8) {
            return limpo.substring(0, 5) + "-" + limpo.substring(5);
        }
        return cep; // Retorna o original caso não tenha 8 dígitos
    }
}