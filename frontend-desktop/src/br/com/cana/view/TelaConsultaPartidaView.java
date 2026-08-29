package br.com.cana.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import br.com.cana.util.ImagemUtil;
import br.com.cana.model.Partida;
import br.com.cana.service.ApiClient;

/**
 * Projeto: Gestor de Partidas CANA
 * Tela de Histórico e Consulta de Partidas - ADS 2026
 * 
 * @author Guilherme Alves
 */
public class TelaConsultaPartidaView extends JFrame {

    private JTable tabela;
    private DefaultTableModel model;
    private JComboBox<String> cbPeriodo;

    // ⚽ CONTROLE DE DADOS
    private List<Partida> partidasCarregadas;

    public TelaConsultaPartidaView() {

        setTitle("CANA - Histórico de Partidas");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ImagemUtil.COR_FUNDO);
        ImagemUtil.configurarIcone(this);

        setLayout(new BorderLayout(15, 15)); // 15px de respiro entre as áreas
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margem geral

        // --- TÍTULO E FILTRO (Região NORTE) ---
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        painelTopo.setBackground(ImagemUtil.COR_FUNDO);

        JLabel lblFiltro = new JLabel("Selecione o período / Temporada:");
        lblFiltro.setFont(new Font("SansSerif", Font.BOLD, 14));
        painelTopo.add(lblFiltro);

        String[] periodos = { "Todas (Temporada 2026)" };
        cbPeriodo = new JComboBox<>(periodos);
        cbPeriodo.setPreferredSize(new Dimension(250, 30));
        painelTopo.add(cbPeriodo);

        add(painelTopo, BorderLayout.NORTH);

        // --- TABELA DE RESULTADOS (Região CENTRO) ---
        String[] colunas = { "Data", "Nome da Partida", "Placar Final" };
        model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tabela = new JTable(model);
        tabela.setRowHeight(30);
        tabela.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabela.setShowHorizontalLines(true);
        tabela.setShowVerticalLines(false);
        tabela.setGridColor(new Color(0xE0E0E0));
        tabela.setIntercellSpacing(new Dimension(0, 1));

        ((DefaultTableCellRenderer) tabela.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        br.com.cana.util.ImagemUtil.TabelaLinhaLimpaRenderer centerRenderer = new br.com.cana.util.ImagemUtil.TabelaLinhaLimpaRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (c instanceof JLabel) {
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        };

        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabela.getSelectedRow() != -1) {
                    executarAberturaPartida();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tabela);
        add(scroll, BorderLayout.CENTER); // O Centro expande automaticamente!

        // --- BOTÕES DE AÇÃO (Região SUL) ---
        JPanel painelBase = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBase.setBackground(ImagemUtil.COR_FUNDO);

        ImagemUtil.BotaoGradienteCANA btnVerSumula = new ImagemUtil.BotaoGradienteCANA("VER SÚMULA DETALHADA");
        btnVerSumula.setPreferredSize(new Dimension(300, 50));
        btnVerSumula.addActionListener(e -> executarAberturaPartida());

        painelBase.add(btnVerSumula);
        add(painelBase, BorderLayout.SOUTH);

        // Carrega os dados do SQLite
        carregarPartidasDoBanco();
    }

    private void carregarPartidasDoBanco() {
        model.setRowCount(0);

        try {
            // Busca as partidas via API
            String json = ApiClient.get("/partidas?temporada=2026");

            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.List<Partida>>() {
            }.getType();
            partidasCarregadas = ApiClient.GSON.fromJson(json, type);

            if (partidasCarregadas != null && !partidasCarregadas.isEmpty()) {
                for (Partida p : partidasCarregadas) {
                    // 🌟 CORREÇÃO MVC: A View continua usando os métodos da sua classe Partida
                    String dataFormatada = p.getDataPartidaFormatada();
                    String placarFormatado = p.getPlacarFormatado();

                    model.addRow(new Object[] { dataFormatada, p.getNomePartida(), placarFormatado });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar partidas da API: " + ex.getMessage(),
                    "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void executarAberturaPartida() {
        int linhaSelecionada = tabela.getSelectedRow();

        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma partida na lista primeiro!", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (partidasCarregadas == null || linhaSelecionada >= partidasCarregadas.size())
            return;
        Partida partidaIncompleta = partidasCarregadas.get(linhaSelecionada);
        System.out.println("👉 [DEBUG 1] ID da Partida Selecionada: " + partidaIncompleta.getId());

        try {
            // Busca os detalhes completos da partida via API pelo ID
            String json = ApiClient.get("/partidas/" + partidaIncompleta.getId());

            Partida partidaHistoricaCompleta = ApiClient.GSON.fromJson(json, Partida.class);

            if (partidaHistoricaCompleta != null) {
                if (partidaHistoricaCompleta.getJogadoresAzul() != null) {
                    System.out.println(
                            "👉 [DEBUG 3] Total Azul após API: " + partidaHistoricaCompleta.getJogadoresAzul().size());
                }
                if (partidaHistoricaCompleta.getJogadoresVermelho() != null) {
                    System.out.println("👉 [DEBUG 4] Total Vermelho após API: "
                            + partidaHistoricaCompleta.getJogadoresVermelho().size());
                }

                TelaPartidaLiveView telaLive = new TelaPartidaLiveView(partidaHistoricaCompleta, true);
                telaLive.setVisible(true);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar detalhes da partida: " + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}