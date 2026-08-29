package br.com.cana.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import br.com.cana.util.ImagemUtil;

public class TelaMenuPrincipalView extends JFrame {

    public TelaMenuPrincipalView() {
        // --- CONFIGURAÇÕES DA JANELA ---
        setTitle("Gestor CANA - Menu Principal");
        ImagemUtil.configurarIcone(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // --- DEFINIR ÍCONE DA JANELA ---
        ImagemUtil.configurarIcone(this);

        // --- PAINEL PRINCIPAL ---
        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBackground(ImagemUtil.COR_FUNDO);
        contentPane.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(contentPane);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // --- COLUNA ESQUERDA: BOTÕES ---
        JPanel painelBotoes = new JPanel();
        painelBotoes.setLayout(new BoxLayout(painelBotoes, BoxLayout.Y_AXIS));
        painelBotoes.setOpaque(false);

        // USANDO A CLASSE CENTRALIZADA (Muito mais limpo!)
        ImagemUtil.BotaoGradienteCANA btnCadastro = new ImagemUtil.BotaoGradienteCANA("CADASTRO");
        btnCadastro.addActionListener(e -> new FormJogadorView().setVisible(true));

        ImagemUtil.BotaoGradienteCANA btnContribuicao = new ImagemUtil.BotaoGradienteCANA("CONTRIBUIÇÃO");
        btnContribuicao.addActionListener(e -> new ContribuicaoView().setVisible(true));

        ImagemUtil.BotaoGradienteCANA btnPartidas = new ImagemUtil.BotaoGradienteCANA("PARTIDAS");
        btnPartidas.addActionListener(e -> new TelaMenuPartidaView().setVisible(true));

        ImagemUtil.BotaoGradienteCANA btnEstatisticas = new ImagemUtil.BotaoGradienteCANA("ESTATÍSTICAS");
        btnEstatisticas.addActionListener(e -> new TelaEstatisticasView().setVisible(true));

        // Adicionando os botões com o espaçamento
        painelBotoes.add(Box.createVerticalGlue());
        painelBotoes.add(btnCadastro);
        painelBotoes.add(Box.createVerticalStrut(25));
        painelBotoes.add(btnContribuicao);
        painelBotoes.add(Box.createVerticalStrut(25));
        painelBotoes.add(btnPartidas);
        painelBotoes.add(Box.createVerticalStrut(25));
        painelBotoes.add(btnEstatisticas);
        painelBotoes.add(Box.createVerticalGlue());

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 30);
        contentPane.add(painelBotoes, gbc);

        // --- COLUNA DIREITA: LOGO (USANDO A CLASSE CENTRALIZADA) ---
        ImagemUtil.LogoPainelCANA painelLogo = new ImagemUtil.LogoPainelCANA(280, 280);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPane.add(painelLogo, gbc);
    }
}