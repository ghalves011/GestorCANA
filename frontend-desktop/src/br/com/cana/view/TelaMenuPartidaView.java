package br.com.cana.view;

import javax.swing.*;
import br.com.cana.util.ImagemUtil;

/**
 * Projeto: Gestor de Partidas CANA
 * Menu de Opções de Partida - ADS 2026
 * * @author Gui
 */
public class TelaMenuPartidaView extends JFrame {

    public TelaMenuPartidaView() {
        // --- CONFIGURAÇÕES DA JANELA ---
        setTitle("CANA - Menu de Partida");
        ImagemUtil.configurarIcone(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(null); // Posicionamento absoluto conforme o Figma

        // 1. ÍCONE E FUNDO (Padronizados)
        ImagemUtil.configurarIcone(this); 
        getContentPane().setBackground(ImagemUtil.COR_FUNDO);

        // --- BOTÕES (USANDO A CLASSE CENTRALIZADA) ---
        
        // Criar o botão usando a fábrica da ImagemUtil
        ImagemUtil.BotaoGradienteCANA btnConsultar = new ImagemUtil.BotaoGradienteCANA("CONSULTAR PARTIDA");
        btnConsultar.setBounds(60, 160, 300, 80); 
        btnConsultar.addActionListener(e -> new TelaConsultaPartidaView().setVisible(true));

        ImagemUtil.BotaoGradienteCANA btnCriar = new ImagemUtil.BotaoGradienteCANA("CRIAR PARTIDA");
        btnCriar.setBounds(60, 340, 300, 80); 
        btnCriar.addActionListener(e -> new TelaGeracaoPartidaView().setVisible(true));

        // --- LOGO DO CANA (ALTA DEFINIÇÃO) ---
        // Usando o LogoPainelCANA que centralizamos na ImagemUtil
        ImagemUtil.LogoPainelCANA painelLogo = new ImagemUtil.LogoPainelCANA(350, 350);
        painelLogo.setBounds(400, 125, 350, 350);

        // Adicionando componentes à tela
        add(btnConsultar);
        add(btnCriar);
        add(painelLogo);
    }
}