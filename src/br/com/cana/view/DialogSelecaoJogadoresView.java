package br.com.cana.view;

import javax.swing.*;
import java.awt.*;
import br.com.cana.util.ImagemUtil;
import br.com.cana.model.Jogador;
import java.util.ArrayList;
import java.util.List;

public class DialogSelecaoJogadoresView extends JDialog {
    private JPanel painelLista;
    private List<JCheckBox> checkBoxes = new ArrayList<>();
    private List<String> jogadoresNaOrdemDeChegada = new ArrayList<>();
    private boolean confirmado = false;

    // 🌟 NOVO: Label para mostrar o contador de jogadores selecionados
    private JLabel lblContador;

    public DialogSelecaoJogadoresView(Frame parent, List<Jogador> jogadoresAtivos, List<Integer> idsDevedores) {
        super(parent, "Selecionar Jogadores Presentes", true);
        setSize(400, 550); // Aumentei um pouquinho a altura (de 500 para 550) para acomodar o topo
                           // perfeitamente
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // 🌟 2. PAINEL DO TOPO: Cria uma barra fixa superior para abrigar o contador
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelTopo.setBackground(Color.WHITE);
        painelTopo.setBorder(BorderFactory.createEmptyBorder(12, 16, 6, 16));

        lblContador = new JLabel("Jogadores selecionados: 0");
        lblContador.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblContador.setForeground(new Color(0x333333));

        painelTopo.add(lblContador);
        add(painelTopo, BorderLayout.NORTH); // Fixa no norte da tela

        painelLista = new JPanel();
        painelLista.setLayout(new BoxLayout(painelLista, BoxLayout.Y_AXIS));
        painelLista.setBackground(Color.WHITE);

       if (jogadoresAtivos != null) {
            for (Jogador j : jogadoresAtivos) {
                
                // Verifica se o ID do cara está na lista de inadimplentes
                boolean isDevedor = idsDevedores != null && idsDevedores.contains(j.getId());

                String nomeExibir = (j.getApelido() != null && !j.getApelido().trim().isEmpty())
                        ? j.getApelido()
                        : j.getNome();
                        
                if (j.getPosicao() != null && !j.getPosicao().trim().isEmpty()) {
                    nomeExibir += " (" + j.getPosicao() + ")";
                }

                JCheckBox cb = new JCheckBox(nomeExibir);
                cb.setFont(new Font("SansSerif", Font.PLAIN, 14));
                cb.setBackground(Color.WHITE);
                cb.setOpaque(true);
                cb.setBorderPainted(false);
                cb.setFocusPainted(false);
                cb.setMargin(new Insets(0, 0, 0, 0));
                cb.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

                Dimension tamanhoFixo = new Dimension(Integer.MAX_VALUE, 32);
                cb.setPreferredSize(new Dimension(280, 32));
                cb.setMinimumSize(new Dimension(280, 32));
                cb.setMaximumSize(tamanhoFixo);
                cb.setAlignmentX(Component.LEFT_ALIGNMENT);
                
                // Se for devedor, pinta a fonte de laranja pra chamar atenção, mas NÃO bloqueia
                if (isDevedor) {
                    cb.setForeground(new Color(200, 100, 0)); 
                }

                final String apelidoPuro = (j.getApelido() != null && !j.getApelido().trim().isEmpty())
                        ? j.getApelido()
                        : j.getNome();

                // 🛑 A INTERCEPTAÇÃO DA REGRA DE NEGÓCIO: Pop-up de Sim ou Não
                cb.addActionListener(e -> {
                    if (cb.isSelected()) {
                        if (isDevedor) {
                            int opcao = JOptionPane.showConfirmDialog(DialogSelecaoJogadoresView.this,
                                    "O jogador " + apelidoPuro + " possui mensalidades atrasadas.\nDeseja escalá-lo mesmo assim?",
                                    "Aviso de Inadimplência",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.WARNING_MESSAGE);
                            
                            // Se o usuário clicou em NÃO (ou fechou a janela)
                            if (opcao != JOptionPane.YES_OPTION) {
                                cb.setSelected(false); // Desmarca a caixinha automaticamente
                                return; // Aborta e não adiciona na lista de selecionados
                            }
                        }
                        
                        // Se não devia nada OU o mesário autorizou no "Sim"
                        jogadoresNaOrdemDeChegada.add(apelidoPuro);
                        
                    } else {
                        // Desmarcou normalmente
                        jogadoresNaOrdemDeChegada.remove(apelidoPuro);
                    }
                    atualizarContadorVisual();
                });

                checkBoxes.add(cb);
                painelLista.add(cb);
            }
        }

        JScrollPane scroll = new JScrollPane(painelLista);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        // --- BOTÃO CONFIRMAR ---
        JPanel painelBotao = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelBotao.setBackground(Color.WHITE);
        painelBotao.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        ImagemUtil.BotaoGradienteCANA btnOk = new ImagemUtil.BotaoGradienteCANA("CONFIRMAR");
        btnOk.setPreferredSize(new Dimension(220, 48));
        btnOk.setFocusPainted(false);

        btnOk.addActionListener(e -> {
            confirmado = true;
            setVisible(false);
        });

        painelBotao.add(btnOk);
        add(painelBotao, BorderLayout.SOUTH);
    }

    // 🌟 4. MÉTODO UTILITÁRIO: Recalcula a lista e pinta o texto de verde escuro
    // quando cravar os 22 ideais do racha
    private void atualizarContadorVisual() {
        int total = jogadoresNaOrdemDeChegada.size();
        lblContador.setText("Jogadores selecionados: " + total);

        if (total == 22) {
            lblContador.setForeground(new Color(0x006400)); // Verde CANA de meta batida!
        } else {
            lblContador.setForeground(new Color(0x333333)); // Grafite padrão
        }
    }

    public List<String> getJogadoresSelecionados() {
        return jogadoresNaOrdemDeChegada;
    }

    public boolean isConfirmado() {
        return confirmado;
    }
}