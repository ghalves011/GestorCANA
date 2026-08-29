package br.com.cana.util;

import java.text.NumberFormat;
import java.util.Locale;
import br.com.cana.model.Jogador;

public class FormatadorUtil {

    public static String mascaraCPF(String cpf) {
        if (cpf == null) return "";
        String limpo = cpf.replaceAll("\\D", "");
        if (limpo.length() != 11) return cpf;
        return limpo.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    public static String mascaraTelefone(String tel) {
        if (tel == null) return "";
        String limpo = tel.replaceAll("\\D", "");
        if (limpo.length() == 11)
            return limpo.replaceAll("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        if (limpo.length() == 10)
            return limpo.replaceAll("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
        return tel;
    }

    public static String formatarPlacar(Integer azul, Integer vermelho) {
        int a = azul != null ? azul : 0;
        int v = vermelho != null ? vermelho : 0;
        return String.format("AZUL %d x %d VERMELHO", a, v);
    }

    public static String peso(Double p) {
        if (p == null) return "N/A";
        return String.format("%.1f kg", p);
    }

    public static String altura(Double a) {
        if (a == null) return "N/A";
        return String.format("%.2f m", a);
    }

    public static String formatarMoeda(Double valor) {
        double v = valor != null ? valor : 0.0;
        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        return formatoMoeda.format(v);
    }

    public static String formatarReferencia(Integer mes, Integer ano) {
        int m = mes != null ? mes : 0;
        int a = ano != null ? ano : 0;
        return String.format("%02d/%d", m, a);
    }

    public static String formatarDescricaoJogador(String nomeOuApelido, String time, String status, String funcao) {
        String n = nomeOuApelido != null ? nomeOuApelido : "Jogador";
        String t = time != null ? time : "N/A";
        String s = status != null ? status : "N/A";
        String f = funcao != null ? funcao : "N/A";
        return String.format("%s - Time %s [%s] (%s)", n, t, s, f);
    }

    // Sobrecarga inteligente que já pega o apelido se existir, senão usa o nome
    public static String formatarDescricaoJogador(Jogador jogador, String time, String status, String funcao) {
        String nomeExibicao = "Jogador";
        if (jogador != null) {
            if (jogador.getApelido() != null && !jogador.getApelido().trim().isEmpty()) {
                nomeExibicao = jogador.getApelido();
            } else if (jogador.getNome() != null) {
                nomeExibicao = jogador.getNome();
            }
        }
        return formatarDescricaoJogador(nomeExibicao, time, status, funcao);
    }

    public static String formatarDescricaoEvento(String tipo, String nomeJogador, String corTime) {
        String tp = tipo != null ? tipo.toUpperCase() : "EVENTO";
        String nj = nomeJogador != null ? nomeJogador : "Jogador";
        String ct = corTime != null ? corTime : "N/A";
        return String.format("%s - %s (Time %s)", tp, nj, ct);
    }
}