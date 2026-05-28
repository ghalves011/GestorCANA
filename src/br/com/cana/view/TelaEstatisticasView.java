package br.com.cana.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.List;

import java.awt.*;
import br.com.cana.util.ImagemUtil;
import br.com.cana.service.PartidaService;

/**
 * Projeto: Gestor de Partidas CANA
 * Tela de Estatísticas Gerais - ADS 2026
 * * @author Guilherme Alves
 */
public class TelaEstatisticasView extends JFrame {

    private JTable tabela;
    private DefaultTableModel model;
    private JComboBox<String> cbPeriodo;

    private PartidaService partidaService;

    public TelaEstatisticasView() {

        this.partidaService = new PartidaService();

        setTitle("CANA - Estatísticas de Jogadores");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ImagemUtil.COR_FUNDO);
        ImagemUtil.configurarIcone(this);

        // Layout dinâmico principal com margens
        setLayout(new BorderLayout(15, 15));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // --- TÍTULO E FILTRO (Região NORTE) ---
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        painelTopo.setBackground(ImagemUtil.COR_FUNDO);

        JLabel lblFiltro = new JLabel("Selecione o período / Temporada:");
        lblFiltro.setFont(new Font("SansSerif", Font.BOLD, 14));
        painelTopo.add(lblFiltro);

        // Pega o ano atual do sistema (Ex: vai retornar 2026 agora, e 2027 no ano que vem)
        int anoAtual = java.time.Year.now().getValue();

        String[] periodos = { "Todas (Temporada 2026)" };
        cbPeriodo = new JComboBox<>(periodos);
        cbPeriodo.setPreferredSize(new Dimension(250, 30));
        painelTopo.add(cbPeriodo);

        add(painelTopo, BorderLayout.NORTH);

        // --- EVENTO DO JCOMBOBOX ---
        cbPeriodo.addActionListener(e -> {
            String selecao = (String) cbPeriodo.getSelectedItem();
            
            if (selecao != null) {
                // Remove tudo que não for número da String (Ex: "2026" fica "2026", e "Todas (2026)" também fica "2026")
                String apenasNumeros = selecao.replaceAll("[^0-9]", "");
                
                if (!apenasNumeros.isEmpty()) {
                    // Converte a String "2026" para o número inteiro 2026
                    int anoSelecionado = Integer.parseInt(apenasNumeros);
                    
                    // Manda pro banco buscar os dados dinamicamente!
                    carregarEstatisticas(anoSelecionado);
                }
            }
        });

        // --- TABELA DE ESTATÍSTICAS (Região CENTRO) ---
        // Utilizando os ícones universais para dar a cara do rascunho
        String[] colunas = { "Apelido", "⚽ Gols", "C.A.", "C.V.", "Presença %" };
        model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false; // Tabela 100% bloqueada para edição
            }
        };

        tabela = new JTable(model);
        tabela.setRowHeight(30);
        tabela.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 🌟 AJUSTE VISUAL: Linhas horizontais e VERITCAIS ativadas
        tabela.setShowHorizontalLines(true);
        tabela.setShowVerticalLines(true); // Exibe divisórias das colunas
        tabela.setGridColor(new Color(0xE0E0E0));
        tabela.setIntercellSpacing(new Dimension(1, 1));

        // --- ÍCONES CUSTOMIZADOS PARA OS CARTÕES ---
        // Desenhamos os cartões na mão para garantir que fiquem 100% preenchidos em
        // qualquer PC
        Icon iconeCartaoAmarelo = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(new Color(255, 204, 0)); // Amarelo vivo (cor de cartão mesmo)
                g.fillRect(x, y, 12, 16); // Desenha o retângulo preenchido (largura 12, altura 16)
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, 12, 16); // Bordinha escura para destacar do fundo
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };

        Icon iconeCartaoVermelho = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(Color.RED); // Vermelho puro
                g.fillRect(x, y, 12, 16);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, 12, 16);
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }
        };

        // --- RENDERER CUSTOMIZADO PARA O CABEÇALHO ---
        DefaultTableCellRenderer customHeaderRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                JLabel label = (JLabel) c;

                // Limpa qualquer ícone que já exista (para não duplicar nas outras colunas)
                label.setIcon(null);

                // 1. Alinhamento Dinâmico
                if (column == 0) {
                    label.setHorizontalAlignment(SwingConstants.LEFT);
                    ((JComponent) c).setBorder(BorderFactory.createCompoundBorder(
                            UIManager.getBorder("TableHeader.cellBorder"),
                            BorderFactory.createEmptyBorder(0, 10, 0, 0)));
                } else {
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    ((JComponent) c).setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                }

                // 2. Garante que a fonte continue em negrito e preta
                c.setFont(new Font("SansSerif", Font.BOLD, 14));
                c.setForeground(Color.BLACK);

                // 3. Aplica os ícones desenhados nas colunas corretas
                if (column == 2) { // Coluna Amarelos
                    label.setIcon(iconeCartaoAmarelo);
                } else if (column == 3) { // Coluna Vermelhos
                    label.setIcon(iconeCartaoVermelho);
                }

                return c;
            }
        };

        // Aplica esse visualizador em todas as colunas do cabeçalho
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setHeaderRenderer(customHeaderRenderer);
        }

        // --- ORDENAÇÃO DAS COLUNAS (TableRowSorter) ---
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        tabela.setRowSorter(sorter);

        // Comparador customizado para as colunas de Gols e Cartões (String para Integer)
        java.util.Comparator<String> comparadorNumerico = (s1, s2) -> {
            Integer v1 = Integer.valueOf(s1.trim());
            Integer v2 = Integer.valueOf(s2.trim());
            return v1.compareTo(v2);
        };

        // Comparador customizado para a coluna de Presença (Tira o "%" antes de comparar)
        java.util.Comparator<String> comparadorPorcentagem = (s1, s2) -> {
            Integer v1 = Integer.valueOf(s1.replace("%", "").trim());
            Integer v2 = Integer.valueOf(s2.replace("%", "").trim());
            return v1.compareTo(v2);
        };

        // Aplicando os comparadores nas colunas corretas
        sorter.setComparator(1, comparadorNumerico); // ⚽ Gols
        sorter.setComparator(2, comparadorNumerico); // 🟨 C.A.
        sorter.setComparator(3, comparadorNumerico); // 🟥 C.V.
        sorter.setComparator(4, comparadorPorcentagem); // Presença %

        // --- CONFIGURAÇÃO DE ALINHAMENTO DAS CÉLULAS ---

        // 1. Renderer para alinhar à Esquerda (Usado no Apelido)
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        // Adiciona um pequeno padding (margem) à esquerda para não colar na linha
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // 2. Renderer para Centralizar (Usado nos Gols, Cartões e Presença)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Aplicando as regras de alinhamento nas colunas
        tabela.getColumnModel().getColumn(0).setCellRenderer(leftRenderer); // Apelido
        for (int i = 1; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(centerRenderer); // Demais colunas
        }

        // Ajuste de largura das colunas (Opcional, para deixar a coluna de Nome maior)
        tabela.getColumnModel().getColumn(0).setPreferredWidth(250);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(tabela);
        add(scroll, BorderLayout.CENTER); // Preenche todo o resto da tela

        carregarEstatisticas(anoAtual);
    }

    private void carregarEstatisticas(int temporadaId) {
        model.setRowCount(0); // Limpa a tabela
        
        List<Object[]> dados = partidaService.carregarDadosTelaEstatisticas(temporadaId);
        
        for (Object[] linha : dados) {
            model.addRow(linha);
        }
    }
}