package br.com.cana.util;

import br.com.cana.model.Endereco;

public class EnderecoUtil {

    /**
     * Retorna o endereço formatado em uma única linha.
     */
    public static String formatarCompleto(Endereco e) {
        if (e == null) return "Endereço não informado";
        
        StringBuilder sb = new StringBuilder();
        sb.append(e.getLogradouro()).append(", ").append(e.getNumero());
        
        if (e.getComplemento() != null && !e.getComplemento().isEmpty()) {
            sb.append(" (").append(e.getComplemento()).append(")");
        }
        
        sb.append(" - ").append(e.getBairro())
          .append(", ").append(e.getCidade())
          .append("/").append(e.getEstado());
          
        return sb.toString();
    }

}