package br.com.cana.view;

import br.com.cana.model.Jogador;
import br.com.cana.service.JogadorService;
import br.com.cana.util.FormatadorUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class DialogBuscaView extends JDialog {

    private JTextField txtBusca;
    private JTable tabela;
    private DefaultTableModel modelo;
    private JogadorService service = new JogadorService();
    private List<Jogador> todosJogadores;
    private List<Jogador> listaFiltrada;
    private Jogador jogadorSelecionado;

    public DialogBuscaView(Frame parent) {
        super(parent, "Busca de Jogadores", true);
        setSize(800, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("Table.focusCellHighlightBorderColor", new Color(0, 0, 0, 0));
        UIManager.put("Table.focusSelectedCellHighlightBorderColor", new Color(0, 0, 0, 0));

        // Painel Superior
        JPanel painelBusca = new JPanel(new BorderLayout());
        painelBusca.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtBusca = new JTextField();
        painelBusca.add(new JLabel("Busca (Nome ou CPF): "), BorderLayout.WEST);
        painelBusca.add(txtBusca, BorderLayout.CENTER);

        add(painelBusca, BorderLayout.NORTH);

        // Tabela
        modelo = new DefaultTableModel(new Object[] { "Nome", "Apelido", "CPF" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        tabela.setFocusable(false);

        // Tabela
        modelo = new DefaultTableModel(new Object[] { "Nome", "Apelido", "CPF" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabela = new JTable(modelo);
        tabela.setFocusable(false);

        // Coluna 0 (Nome): Tamanho grande
        tabela.getColumnModel().getColumn(0).setPreferredWidth(400); 
        
        // Coluna 1 (Apelido): Tamanho médio
        tabela.getColumnModel().getColumn(1).setPreferredWidth(200); 
        
        // Coluna 2 (CPF): Cravamos o tamanho exato!
        tabela.getColumnModel().getColumn(2).setPreferredWidth(120); 
        tabela.getColumnModel().getColumn(2).setMinWidth(120); // Impede que o usuário esprema demais
        tabela.getColumnModel().getColumn(2).setMaxWidth(120); // Impede que a coluna estique ao maximizar a tela
        
        // Evento de clique duplo para selecionar jogador
        tabela.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    acaoSelecionar();
                }
            }
        });
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Rodapé
        JButton btnSelecionar = new JButton("Selecionar Jogador");
        btnSelecionar.setFocusable(false);
        btnSelecionar.addActionListener(e -> acaoSelecionar());
        add(btnSelecionar, BorderLayout.SOUTH);

        // Carregar dados iniciais
        todosJogadores = service.listarTodos();
        atualizarTabela(todosJogadores);

        // Evento de filtro em tempo real
        txtBusca.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filtrar();
            }
        });
    }

    private void atualizarTabela(List<Jogador> lista) {
        this.listaFiltrada = lista;
        modelo.setRowCount(0);
        for (Jogador j : lista) {
            modelo.addRow(new Object[] {
                    j.getNome(),
                    j.getApelido(),
                    FormatadorUtil.mascaraCPF(j.getCpf())
            });
        }
    }

    private void filtrar() {
        String termo = txtBusca.getText().toUpperCase();
        List<Jogador> filtrados = todosJogadores.stream()
                .filter(j -> j.getNome().toUpperCase().contains(termo) || j.getCpf().contains(termo))
                .collect(Collectors.toList());
        atualizarTabela(filtrados);
    }

    private void acaoSelecionar() {
        int linha = tabela.getSelectedRow();
        if (linha != -1) {
            // Converte o índice caso a tabela esteja filtrada
            int indiceModel = tabela.convertRowIndexToModel(linha);
            this.jogadorSelecionado = listaFiltrada.get(indiceModel);
            this.dispose(); // Fecha e volta para a tela de cadastro
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um jogador na tabela.");
        }
    }

    public Jogador getJogadorSelecionado() {
        return jogadorSelecionado;
    }

}