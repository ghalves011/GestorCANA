package br.com.cana.util;

public class ValidacaoUtil {

    /**
     * Valida se uma String está vazia ou nula.
     */
    public static boolean isVazio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /**
     * Valida RG (padrão básico: 7 a 9 dígitos).
     */
    public static boolean validarRG(String rg) {
        if (isVazio(rg))
            return false;
        // Remove pontos e traços se houver antes de validar
        String rgLimpo = rg.replaceAll("\\D", "");
        return rgLimpo.matches("[0-9]{7,9}");
    }

    /**
     * Valida CPF (11 dígitos numéricos).
     */
    public static boolean validarCPF(String cpf) {
        if (isVazio(cpf))
            return false;
        // Remove tudo que não for número
        String cpfLimpo = cpf.replaceAll("\\D", "");
        return cpfLimpo.matches("\\d{11}");
    }

    /**
     * Valida Telefone (10 ou 11 dígitos para fixo ou celular).
     */
    public static boolean validarTelefone(String telefone) {
        if (isVazio(telefone))
            return false;
        String telLimpo = telefone.replaceAll("\\D", "");
        return telLimpo.matches("\\d{10,11}");
    }

    /**
     * Valida se o nível técnico está no range permitido pelo CANA (1 a 100).
     */
    public static boolean validarNivel(int nivel) {
        return nivel >= 1 && nivel <= 100;
    }

    /**
     * Valida se um ID é válido para operações de banco (deve ser maior que zero).
     */
    public static boolean validarId(int id) {
        return id > 0;
    }

    /**
     * Garante que o mês está entre 1 e 12.
     */
    public static boolean validarMes(int mes) {
        return mes >= 1 && mes <= 12;
    }

    /**
     * Garante que valores de dinheiro sejam sempre positivos.
     */
    public static boolean validarValorPositivo(double valor) {
        return valor > 0;
    }
}