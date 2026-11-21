package br.com.cana.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import br.com.cana.model.ResumoViradaTemporada;
import br.com.cana.model.Temporada;
import br.com.cana.service.TemporadaService;
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
        gbc.gridy = 0;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 30);
        contentPane.add(painelBotoes, gbc);

        // --- COLUNA DIREITA: LOGO (USANDO A CLASSE CENTRALIZADA) ---
        ImagemUtil.LogoPainelCANA painelLogo = new ImagemUtil.LogoPainelCANA(280, 280);
        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPane.add(painelLogo, gbc);

        // --- RODAPÉ: VIRADA DE TEMPORADA ---
        // Botão pequeno e discreto no canto, longe dos botões principais,
        // para não ser clicado por engano. A confirmação ainda exige digitar o ano novo.
        JButton btnVirarTemporada = new JButton("Virar temporada...");
        btnVirarTemporada.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btnVirarTemporada.setFocusable(false);
        btnVirarTemporada.setToolTipText("Encerra a temporada vigente e abre a do ano seguinte");
        btnVirarTemporada.addActionListener(e -> acaoVirarTemporada());

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.insets = new Insets(10, 0, 0, 0);
        contentPane.add(btnVirarTemporada, gbc);
    }

    /**
     * Fluxo da virada: mostra o resumo da API, exige que a pessoa digite o ano novo
     * e só então chama POST /temporadas/virada (que também exige a chave de acesso).
     */
    private void acaoVirarTemporada() {
        // 1. Resumo (só leitura)
        ResumoViradaTemporada resumo;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            resumo = TemporadaService.obterResumoVirada();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Virada de temporada", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
        if (resumo == null || resumo.getAnoNovo() == null) {
            JOptionPane.showMessageDialog(this, "A API não devolveu o resumo da virada.", "Virada de temporada",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. Diálogo com o resumo + campo para digitar o ano novo
        String anoNovo = String.valueOf(resumo.getAnoNovo());

        JTextArea txtResumo = new JTextArea(montarTextoResumo(resumo));
        txtResumo.setEditable(false);
        txtResumo.setLineWrap(true);
        txtResumo.setWrapStyleWord(true);
        txtResumo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        txtResumo.setBackground(UIManager.getColor("Panel.background"));
        txtResumo.setCaretPosition(0); // mostra o resumo a partir do topo
        JScrollPane scrollResumo = new JScrollPane(txtResumo);
        scrollResumo.setPreferredSize(new Dimension(460, 220));

        JTextField txtConfirmacao = new JTextField(8);
        JPanel painelConfirmacao = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        painelConfirmacao.add(new JLabel("Para confirmar, digite o ano novo (" + anoNovo + "):  "));
        painelConfirmacao.add(txtConfirmacao);

        JPanel painel = new JPanel(new BorderLayout(0, 12));
        painel.add(scrollResumo, BorderLayout.CENTER);
        painel.add(painelConfirmacao, BorderLayout.SOUTH);

        int opcao = JOptionPane.showConfirmDialog(this, painel,
                "Virar temporada: " + resumo.getAnoAtual() + " → " + anoNovo,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) {
            return;
        }
        if (!anoNovo.equals(txtConfirmacao.getText().trim())) {
            JOptionPane.showMessageDialog(this, "O ano digitado não confere. Nada foi alterado.",
                    "Virada de temporada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Virada de fato. 409 traz a mensagem da API; 401 traz a mensagem da chave.
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Temporada nova = TemporadaService.virar();
            setCursor(Cursor.getDefaultCursor());
            Integer anoAberto = (nova != null && nova.getAno() != null) ? nova.getAno() : resumo.getAnoNovo();
            JOptionPane.showMessageDialog(this,
                    "Temporada " + anoAberto + " aberta. A " + resumo.getAnoAtual() + " foi encerrada.",
                    "Virada de temporada", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Virada de temporada", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String montarTextoResumo(ResumoViradaTemporada r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Temporada atual: ").append(r.getAnoAtual()).append(" (será encerrada)\n");
        sb.append("Temporada nova: ").append(r.getAnoNovo()).append(" (será aberta)\n\n");
        sb.append("Partidas em ").append(r.getAnoAtual()).append(": ").append(r.getTotalPartidasAnoAtual()).append('\n');
        sb.append("Contribuições pendentes em ").append(r.getAnoAtual()).append(": ")
                .append(r.getContribuicoesPendentesAnoAtual()).append('\n');
        sb.append("Lote de ").append(r.getAnoNovo()).append(" já gerado: ")
                .append(r.isLoteAnoNovoJaGerado() ? "sim" : "não").append("\n\n");

        if (r.getJogadoresSuspensos().isEmpty()) {
            sb.append("Jogadores suspensos: nenhum\n");
        } else {
            sb.append("Jogadores suspensos (").append(r.getJogadoresSuspensos().size()).append("): ")
                    .append(String.join(", ", r.getJogadoresSuspensos())).append('\n');
        }

        if (!r.getAvisos().isEmpty()) {
            sb.append("\nAvisos:\n");
            for (String aviso : r.getAvisos()) {
                sb.append("• ").append(aviso).append('\n');
            }
        }
        return sb.toString();
    }
}