package br.com.cana.gestorcana_api.util;

import br.com.cana.gestorcana_api.entity.Endereco;

public class EnderecoUtil {

    /**
     * Retorna o endereço formatado em uma única linha.
     */
    public static String formatarCompleto(Endereco e) {
        if (e == null) return "Endereço não informado";

        StringBuilder sb = new StringBuilder();

        String logradouro = (e.getLogradouro() != null) ? e.getLogradouro().trim() : "";
        String numero = (e.getNumero() != null) ? e.getNumero().trim() : "";

        sb.append(logradouro).append(", ").append(numero);

        if (e.getComplemento() != null && !e.getComplemento().trim().isEmpty()) {
            sb.append(" (").append(e.getComplemento().trim()).append(")");
        }

        String bairro = (e.getBairro() != null) ? e.getBairro().trim() : "";
        String cidade = (e.getCidade() != null) ? e.getCidade().trim() : "";
        String estado = (e.getEstado() != null) ? e.getEstado().trim().toUpperCase() : "";

        sb.append(" - ").append(bairro)
          .append(", ").append(cidade)
          .append("/").append(estado);

        return sb.toString();
    }
}