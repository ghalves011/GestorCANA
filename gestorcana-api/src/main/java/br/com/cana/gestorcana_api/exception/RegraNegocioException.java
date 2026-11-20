package br.com.cana.gestorcana_api.exception;

/**
 * Erro de regra de negócio: a mensagem é pensada para ser mostrada ao usuário.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
