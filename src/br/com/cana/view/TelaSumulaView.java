package br.com.cana.view;
 
import javax.swing.*;
import java.awt.*;
import br.com.cana.util.ImagemUtil;
import br.com.cana.model.Partida;
import br.com.cana.service.PartidaService; // Agora este import será usado corretamente!
 
public class TelaSumulaView extends JDialog {
 
    private JTextArea txtSumula;
    private JButton btnSalvar;
    private PartidaService partidaService; // Limpo: sem o prefixo br.com.cana...
    private Partida partidaObjeto; // Mantido apenas um para evitar redundância de "não usado"
 
    public TelaSumulaView(JFrame pai, Partida partida, boolean partidaFinalizada) {
        super(pai, "CANA - Súmula da Partida", true);
        this.partidaObjeto = partida;
        this.partidaService = new PartidaService(); // Limpo
 
        setSize(600, 500);
        setLocationRelativeTo(pai);
        setLayout(null);
        getContentPane().setBackground(ImagemUtil.COR_FUNDO);
        ImagemUtil.configurarIcone(this);
 
        // --- TÍTULO ---
        JLabel lblTitulo = new JLabel("SÚMULA DA PARTIDA", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setBounds(0, 15, 600, 30);
        add(lblTitulo);
 
        // --- CAIXA DE TEXTO DA SÚMULA ---
        txtSumula = new JTextArea();
        if (partidaObjeto != null && partidaObjeto.getSumula() != null) {
            txtSumula.setText(partidaObjeto.getSumula());
        }
        txtSumula.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtSumula.setLineWrap(true);
        txtSumula.setWrapStyleWord(true);
 
        // 🧠 REGRA: Se a partida já acabou, desabilita a edição e muda a cor para cinza
        if (partidaFinalizada) {
            txtSumula.setEditable(false);
            txtSumula.setBackground(new Color(0xE0E0E0));
        } else {
            txtSumula.setEditable(true);
            txtSumula.setBackground(Color.WHITE);
        }
 
        JScrollPane scroll = new JScrollPane(txtSumula);
        scroll.setBounds(40, 60, 520, 300);
        scroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        add(scroll);
 
        // --- BOTÃO SALVAR / FECHAR ---
        String textoBotao = partidaFinalizada ? "FECHAR" : "SALVAR SÚMULA";
        btnSalvar = new JButton(textoBotao);
        btnSalvar.setBounds(200, 380, 200, 45);
        btnSalvar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnSalvar.setFocusPainted(false);
 
        if (partidaFinalizada) {
            btnSalvar.setBackground(Color.LIGHT_GRAY);
        } else {
            btnSalvar.setBackground(new Color(0xCCFFCC));
        }
 
        btnSalvar.addActionListener(e -> {
            if (!partidaFinalizada) {
                // Modifica a instância em memória temporariamente usando o Service oficial
                partidaService.salvarSumulaProvisoria(partidaObjeto, txtSumula.getText().trim());
                JOptionPane.showMessageDialog(this,
                    "Súmula salva temporariamente! Lembre-se de finalizar o jogo para gravar no SQLite.");
            }
            dispose();
        });
 
        add(btnSalvar);
    }
}