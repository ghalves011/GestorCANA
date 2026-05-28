package br.com.cana;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import br.com.cana.view.TelaMenuPrincipalView; 

/**
 * Projeto: Gestor de Futebol CANA
 * ADS 2026
 * * @author Guilherme Alves
 */

public class Main {

    public static void main(String[] args) {
        
        // 1. Configurações Globais de Design
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        } catch (Exception e) {
            System.err.println("Erro ao configurar o Look and Feel: " + e.getMessage());
        }

        // 2. Boot do Sistema CANA
        SwingUtilities.invokeLater(() -> {
            new TelaMenuPrincipalView().setVisible(true);
        });
        
    }
}