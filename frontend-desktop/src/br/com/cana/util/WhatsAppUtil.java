package br.com.cana.util;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WhatsAppUtil {

    /**
     * Abre o WhatsApp Web apenas com o texto preenchido. 
     * Ideal para enviar em grupos ou quando não se tem o telefone do jogador na tela.
     */
    public static void enviarMensagem(String texto) {
        enviarMensagem(null, texto);
    }

    /**
     * Abre o WhatsApp Web direto na conversa do jogador com o texto preenchido.
     */
    public static void enviarMensagem(String telefone, String texto) {
        try {
            // Codifica o texto para formato URL (corrige espaços, quebras de linha e emojis)
            String textoCodificado = URLEncoder.encode(texto, StandardCharsets.UTF_8.name());

            String url;
            if (telefone != null && !telefone.trim().isEmpty()) {
                // Remove tudo que não for número (parênteses, traços, espaços)
                String apenasNumeros = telefone.replaceAll("\\D", "");
                
                // Se o usuário não digitou o código do país (55), adiciona automaticamente
                if (!apenasNumeros.startsWith("55") && apenasNumeros.length() >= 10) {
                    apenasNumeros = "55" + apenasNumeros;
                }
                
                url = "https://web.whatsapp.com/send?phone=" + apenasNumeros + "&text=" + textoCodificado;
            } else {
                // Se não tiver telefone, abre a API geral para o usuário escolher quem vai receber
                url = "https://web.whatsapp.com/send?text=" + textoCodificado;
            }

            // Executa o drible para abrir o navegador padrão do sistema operacional
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("Erro: O sistema operacional não suporta a ação de abrir o navegador.");
            }

        } catch (Exception e) {
            System.out.println("Erro ao gerar link do WhatsApp: " + e.getMessage());
        }
    }
}
