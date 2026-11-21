package br.com.cana.service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import br.com.cana.model.ResumoViradaTemporada;
import br.com.cana.model.Temporada;

/**
 * Ponto central para as temporadas vindas da API.
 * Substitui o 2026 fixo que existia nas telas.
 * Lembrete: na API o id da temporada é igual ao ano (temporada 2026 → id 2026).
 */
public class TemporadaService {

    private TemporadaService() {
    }

    /** GET /temporadas/vigente. Devolve null se não houver temporada vigente (404). */
    public static Temporada obterVigente() throws Exception {
        String json = ApiClient.getOuNulo("/temporadas/vigente");
        if (json == null || json.isBlank()) {
            return null;
        }
        return ApiClient.GSON.fromJson(json, Temporada.class);
    }

    /** GET /temporadas, ordenado do ano mais novo para o mais antigo. */
    public static List<Temporada> listar() throws Exception {
        String json = ApiClient.getOuFalha("/temporadas");
        Type tipo = new TypeToken<List<Temporada>>() {
        }.getType();
        List<Temporada> lista = ApiClient.GSON.fromJson(json, tipo);
        List<Temporada> ordenada = lista != null ? new ArrayList<>(lista) : new ArrayList<>();
        ordenada.removeIf(t -> t == null || t.getId() == null);
        ordenada.sort(Comparator.comparing(Temporada::getAno,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return ordenada;
    }

    /**
     * Preenche o combo com as temporadas da API (mais nova primeiro) e deixa a vigente
     * selecionada. Se nenhuma estiver vigente, seleciona a mais nova.
     * Chame antes de registrar o ActionListener do combo.
     */
    public static void preencherCombo(javax.swing.JComboBox<Temporada> combo) throws Exception {
        combo.removeAllItems();
        Temporada selecionar = null;
        for (Temporada t : listar()) {
            combo.addItem(t);
            if (selecionar == null && t.isVigente()) {
                selecionar = t;
            }
        }
        if (selecionar != null) {
            combo.setSelectedItem(selecionar);
        } else if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(0);
        }
    }

    /** GET /temporadas/virada/resumo. Só leitura; não altera nada no banco. */
    public static ResumoViradaTemporada obterResumoVirada() throws Exception {
        String json = ApiClient.getOuFalha("/temporadas/virada/resumo");
        return ApiClient.GSON.fromJson(json, ResumoViradaTemporada.class);
    }

    /**
     * POST /temporadas/virada. Exige a chave de acesso.
     * Em 409 lança a mensagem da API; em 401, a mensagem da chave (ver ApiClient).
     */
    public static Temporada virar() throws Exception {
        String json = ApiClient.postOuFalha("/temporadas/virada", "");
        return ApiClient.GSON.fromJson(json, Temporada.class);
    }
}
