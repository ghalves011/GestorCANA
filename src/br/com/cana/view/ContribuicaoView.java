package br.com.cana.view;
 
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
 
import br.com.cana.util.ImagemUtil;
import br.com.cana.util.WhatsAppUtil;
import br.com.cana.util.FormatadorUtil;
import br.com.cana.service.ContribuicaoService;
 
public class ContribuicaoView extends JFrame {
 
    private static final long serialVersionUID = 1L;
 
    // --- PADRÃO VISUAL GESTOR CANA ---
    private final Color COR_FUNDO = new Color(0xE9E4E4);
    private final Color COR_CARD = Color.WHITE;
    private final Color COR_BORDA = new Color(0xD1CCCC);
    private final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 22);
    private final Font FONTE_LABEL = new Font("Segoe UI", Font.BOLD, 11);
    private final Color COR_LINHA_TABELA = new Color(225, 222, 222); // Cor da grade antiga
 
    private JTextField txtBusca, txtAno, txtValor;
    private JButton btnGerar;
    private JTable table;
    private DefaultTableModel model;
 
    private final ContribuicaoService service;
    private final List<Integer> idsJogadores = new ArrayList<>();
 
    public ContribuicaoView() {
        this.service = new ContribuicaoService();
        configurarJanela();
        inicializarComponentes();
        configurarEventos();
        ImagemUtil.configurarIcone(this);
        carregarDadosContribuicao();
    }
 
    private void configurarJanela() {
        setTitle("Gestor CANA - Controle Financeiro");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
    }
 
    private void inicializarComponentes() {
        // CORREÇÃO: Gap vertical mudou de 15 para 16 (múltiplo de 4)
        setLayout(new BorderLayout(0, 16)); 
        
        // CORREÇÃO: Margem da tela mudou de 25 para 24 (múltiplo de 4)
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(24, 24, 24, 24));
 
        // --- CABEÇALHO ---
        JLabel lblTitulo = new JLabel("Controle de Contribuições");
        lblTitulo.setFont(FONTE_TITULO);
        lblTitulo.setForeground(new Color(50, 50, 50));
        add(lblTitulo, BorderLayout.NORTH);
 
        // --- CONTAINER CENTRAL ---
        JPanel pnlCentro = new JPanel(new BorderLayout(0, 20)); // 20 é múltiplo de 4, ok!
        pnlCentro.setOpaque(false);
 
        // --- PAINEL DE FILTROS ---
        JPanel pnlFiltros = new JPanel(new GridBagLayout());
        pnlFiltros.setBackground(COR_CARD);
        
        // CORREÇÃO: Margem interna do card mudou de 15 para 16 (múltiplo de 4)
        pnlFiltros.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1),
                new EmptyBorder(16, 16, 16, 16)));
 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 8, 4, 8); // 4 e 8 são múltiplos de 4, ok!
        gbc.fill = GridBagConstraints.HORIZONTAL;
 
        // Labels
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        pnlFiltros.add(criarLabel("BUSCAR JOGADOR"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        pnlFiltros.add(criarLabel("ANO"), gbc);
        gbc.gridx = 2;
        pnlFiltros.add(criarLabel("VALOR (R$)"), gbc);
 
        // Campos compactos
        gbc.gridy = 1;
        gbc.gridx = 0;
        txtBusca = new JTextField();
        txtBusca.setPreferredSize(new Dimension(150, 28)); // 28 é múltiplo de 4, ok!
        pnlFiltros.add(txtBusca, gbc);

        int anoAtual = java.time.Year.now().getValue();
 
        gbc.gridx = 1;
        txtAno = new JTextField(String.valueOf(anoAtual), 4);
        txtAno.setPreferredSize(new Dimension(50, 28));
        pnlFiltros.add(txtAno, gbc);
 
        gbc.gridx = 2;
        txtValor = new JTextField("50.00", 6);
        txtValor.setPreferredSize(new Dimension(70, 28));
        pnlFiltros.add(txtValor, gbc);
 
        // Botões alinhados à direita
        JPanel pnlAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlAcoes.setOpaque(false);
        btnGerar = new JButton("⚙️ Gerar Lote");
        btnGerar.setPreferredSize(new Dimension(130, 28));
        pnlAcoes.add(btnGerar);
 
        gbc.gridy = 1;
        gbc.gridx = 3;
        gbc.weightx = 0;
        pnlFiltros.add(pnlAcoes, gbc);
 
        pnlCentro.add(pnlFiltros, BorderLayout.NORTH);
 
        // --- TABELA ---
        configurarTabela();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COR_BORDA));
        scroll.getViewport().setBackground(Color.WHITE);
        pnlCentro.add(scroll, BorderLayout.CENTER);
 
        add(pnlCentro, BorderLayout.CENTER);
    }
 
    private JLabel criarLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(FONTE_LABEL);
        l.setForeground(new Color(120, 120, 120));
        return l;
    }
 
    private void configurarTabela() {
        String[] cols = { "Nome do Jogador", "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez" };
 
        model = new DefaultTableModel(cols, 0) {
            private static final long serialVersionUID = 1L;
 
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? String.class : Boolean.class;
            }
 
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
 
        table = new JTable(model);
        table.setFocusable(false);
        table.setRequestFocusEnabled(false);
        
        // 🛠️ CORREÇÃO DE OURO: Zeramos o espaçamento nativo para a célula manter a altura perfeita de 36px
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false); 
        table.setShowHorizontalLines(false); // Desativado o nativo (vamos desenhar via Border)
        table.setShowVerticalLines(false);
        
        table.setRowHeight(36); // 36 é múltiplo de 4, perfeito!
        table.setSelectionBackground(new Color(230, 240, 255));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getColumnModel().getColumn(0).setPreferredWidth(280);
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setMinWidth(64);
            table.getColumnModel().getColumn(i).setMaxWidth(64);
            table.getColumnModel().getColumn(i).setPreferredWidth(64);
        }
 
        // Centralizar cabeçalhos dos meses
        for (int i = 1; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(new DefaultTableCellRenderer() {
                private static final long serialVersionUID = 1L;
 
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                    super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                    setHorizontalAlignment(JLabel.CENTER);
                    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                    return this;
                }
            });
        }
 
        // Renderer do Nome do Jogador
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
 
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, v, isS, hasF, r, c);
                l.setForeground(new Color(0, 102, 204));
                l.setBackground(isS ? t.getSelectionBackground() : Color.WHITE);
 
                // 🛠️ CORREÇÃO: Adicionada a linha divisória inferior via borda controlada por pixel fixo
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, COR_LINHA_TABELA),
                        BorderFactory.createEmptyBorder(0, 8, 0, 0))); // Margem interna ajustada para 8
                return l;
            }
        });
 
        // Renderer de Checkboxes centralizados e livres de bugs visuais
        table.setDefaultRenderer(Boolean.class, new TableCellRenderer() {
            private final JCheckBox check = new JCheckBox();
 
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isS, boolean hasF, int r, int c) {
                check.setSelected(v != null && (Boolean) v);
                check.setHorizontalAlignment(SwingConstants.CENTER);
                
                // Aplicando as travas visuais anti-bug do Windows
                check.setFocusPainted(false);
                check.setOpaque(true);
                check.setMargin(new Insets(0, 0, 0, 0));
                
                // 🛠️ CORREÇÃO: Em vez de tirar a borda, usamos ela para desenhar a linha cinza inferior da tabela
                check.setBorderPainted(true);
                check.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COR_LINHA_TABELA));
 
                if (isS) {
                    check.setBackground(t.getSelectionBackground());
                } else {
                    check.setBackground(Color.WHITE);
                }
                return check;
            }
        });
    }
 
    private void configurarEventos() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col > 0) {
                    efetuarBaixa(row, col);
                }
            }
        });
 
        txtBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                carregarDadosContribuicao();
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                carregarDadosContribuicao();
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                carregarDadosContribuicao();
            }
        });
 
        btnGerar.addActionListener(e -> acionarGeracaoLote());
    }
 
    private void carregarDadosContribuicao() {
        model.setRowCount(0);
        idsJogadores.clear();
 
        try {
            int ano = Integer.parseInt(txtAno.getText().trim());
            String busca = txtBusca.getText().trim();
            List<Object[]> dados = service.obterMatrizContribuicoes(ano, busca);
 
            for (Object[] linha : dados) {
                int idJogador = (int) linha[0];
                String nome = (String) linha[1];
 
                idsJogadores.add(idJogador);
 
                Object[] linhaTabela = new Object[13];
                linhaTabela[0] = nome;
                System.arraycopy(linha, 2, linhaTabela, 1, 12);
 
                model.addRow(linhaTabela);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, digite um ano válido.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private void efetuarBaixa(int row, int col) {
        if (row >= idsJogadores.size()) return;
 
        int idJogadorReal = idsJogadores.get(row);
        String nome = (String) model.getValueAt(row, 0);
 
        try {
            int ano = Integer.parseInt(txtAno.getText().trim());
            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            String ref = FormatadorUtil.formatarReferencia(col, ano);
 
            Object valorCelula = model.getValueAt(row, col);
            boolean jaPago = (valorCelula != null && (Boolean) valorCelula);
 
            if (jaPago) {
                String[] opcoes = { "📄 Gerar Recibo", "❌ Estornar Pagamento", "↩️ Voltar" };
                int escolha = JOptionPane.showOptionDialog(this,
                        "O mês de " + ref + " já está PAGO para " + nome + ".\nO que deseja fazer?",
                        "Opções de Contribuição",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, opcoes, opcoes[0]);
 
                if (escolha == 0) {
                    String textoRecibo = service.obterTextoRecibo(idJogadorReal, col, ano);
                    JTextArea txtRecibo = new JTextArea(textoRecibo);
                    txtRecibo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    txtRecibo.setEditable(false);
                    txtRecibo.setBackground(UIManager.getColor("Panel.background"));
                    txtRecibo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
                    JOptionPane.showMessageDialog(this, new JScrollPane(txtRecibo), "📄 Recibo para WhatsApp", JOptionPane.INFORMATION_MESSAGE);
                    WhatsAppUtil.enviarMensagem(textoRecibo);
                } else if (escolha == 1) {
                    int confirmEstorno = JOptionPane.showConfirmDialog(this,
                            "Atenção! Deseja realmente ESTORNAR o pagamento de " + ref + " para " + nome + "?",
                            "Confirmar Estorno", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
 
                    if (confirmEstorno == JOptionPane.YES_OPTION) {
                        String resultado = service.excluirPagamento(idJogadorReal, col, ano);
                        if ("OK".equals(resultado)) {
                            model.setValueAt(false, row, col);
                            JOptionPane.showMessageDialog(this, "Pagamento excluído com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "Erro ao excluir: " + resultado, "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "Deseja confirmar o pagamento de " + ref + " para " + nome + "?",
                        "Confirmação de Pagamento", JOptionPane.YES_NO_OPTION);
 
                if (confirm == JOptionPane.YES_OPTION) {
                    String resultado = service.registrarPagamento(idJogadorReal, col, ano, valor);
                    if ("OK".equals(resultado)) {
                        model.setValueAt(true, row, col);
                        int querRecibo = JOptionPane.showConfirmDialog(this,
                                "Pagamento registrado com sucesso!\nDeseja abrir o WhatsApp Web para enviar o recibo?",
                                "Emitir Recibo", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 
                        if (querRecibo == JOptionPane.YES_OPTION) {
                            String textoRecibo = service.obterTextoRecibo(idJogadorReal, col, ano);
                            JTextArea txtRecibo = new JTextArea(textoRecibo);
                            txtRecibo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                            txtRecibo.setEditable(false);
                            txtRecibo.setBackground(UIManager.getColor("Panel.background"));
                            txtRecibo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
                            JOptionPane.showMessageDialog(this, new JScrollPane(txtRecibo), "📄 Recibo Gerado", JOptionPane.INFORMATION_MESSAGE);
                            WhatsAppUtil.enviarMensagem(textoRecibo);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Erro ao registrar: " + resultado, "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifique os valores de Ano e Valor digitados.", "Erro de Dados", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    private void acionarGeracaoLote() {
        try {
            int ano = Integer.parseInt(txtAno.getText().trim());
            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Deseja gerar as pendências de todas as mensalidades do ano de " + ano + " para os jogadores ativos?",
                    "Gerar Lote Anual", JOptionPane.YES_NO_OPTION);
 
            if (confirm == JOptionPane.YES_OPTION) {
                String resultado = service.gerarLoteAnual(ano, valor);
                if ("OK".equals(resultado)) {
                    JOptionPane.showMessageDialog(this, "Lote gerado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    carregarDadosContribuicao();
                } else {
                    JOptionPane.showMessageDialog(this, "Falha ao gerar lote: " + resultado, "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Preencha o Ano e o Valor Corretamente antes de gerar o lote.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}