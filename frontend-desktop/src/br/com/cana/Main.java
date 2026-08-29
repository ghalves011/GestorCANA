package br.com.cana;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;

import br.com.cana.view.TelaMenuPrincipalView; 

/**
 * Projeto: Gestor de Futebol CANA V3
 * ADS 2026
 * 
 * @author Guilherme Alves
 */
public class Main {

    public static void main(String[] args) {
        
        // 1. Configurações Globais de Design do Swing
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        } catch (Exception e) {
            System.err.println("Erro ao configurar o Look and Feel: " + e.getMessage());
        }

        // 2. Boot da Interface Gráfica CANA (Swing)
        SwingUtilities.invokeLater(() -> {
            new TelaMenuPrincipalView().setVisible(true);
        });
        
    }
}