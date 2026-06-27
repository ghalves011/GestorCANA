package br.com.cana.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
    private static final DateTimeFormatter BR_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // 1. Converte para o padrão brasileiro (USO NA TELA/GUI)
    public static String paraUsuario(LocalDate data) {
        return (data != null) ? data.format(BR_FORMATTER) : "";
    }

    // 2. Converte para o padrão ISO (USO NO BANCO/DAO)
    public static String paraBanco(LocalDate data) {
        return (data != null) ? data.toString() : null; // Retorna "2026-04-27"
    }

    // 3. EXTRA: Pega o ano de uma data (PARA A CLASSE TEMPORADA)
    public static int extrairAno(LocalDate data) {
        return (data != null) ? data.getYear() : 0;
    }

    // 4. O seu método 'ler' inteligente (MANTIDO E MELHORADO)
    public static LocalDate ler(String dataTexto) {
        if (dataTexto == null || dataTexto.trim().isEmpty())
            return null;

        try {
            // Se veio do SQLite (ex: 2026-04-27)
            if (dataTexto.contains("-")) {
                return LocalDate.parse(dataTexto);
            }
            // Se veio da tela do usuário (ex: 27/04/2026)
            return LocalDate.parse(dataTexto, BR_FORMATTER);
        } catch (DateTimeParseException e) {
            return null; 
        }
    }
}