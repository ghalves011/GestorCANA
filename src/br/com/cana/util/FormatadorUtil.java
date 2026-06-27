package br.com.cana.util;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatadorUtil {

    public static String mascaraCPF(String cpf) {
        if (cpf == null || cpf.length() != 11)
            return cpf;
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    public static String mascaraTelefone(String tel) {
        if (tel == null)
            return "";
        String limpo = tel.replaceAll("\\D", "");
        if (limpo.length() == 11)
            return limpo.replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        if (limpo.length() == 10)
            return limpo.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
        return tel;
    }

    public static String formatarPlacar(int azul, int vermelho) {
        return String.format("AZUL %d x %d VERMELHO", azul, vermelho);
    }

    public static String peso(double p) {
        return String.format("%.1f kg", p);
    }

    public static String altura(double a) {
        return String.format("%.2f m", a);
    }

    public static String formatarMoeda(double valor) {
        // A partir do Java 19, usamos o Locale.of() em vez de new Locale()
        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        return formatoMoeda.format(valor);
    }

    // Para transformar mes 4 e ano 2026 em "04/2026"
    public static String formatarReferencia(int mes, int ano) {
        return String.format("%02d/%d", mes, ano);
    }

    public static String formatarDescricaoJogador(String nome, String time, String status, String funcao) {
        return String.format("%s - Time %s [%s] (%s)", nome, time, status, funcao);
    }

    public static String formatarDescricaoEvento(String tipo, String nomeJogador, String corTime) {
        return String.format("%s - %s (Time %s)", tipo.toUpperCase(), nomeJogador, corTime);
    }
}
