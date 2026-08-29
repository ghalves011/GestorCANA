package br.com.cana.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import br.com.cana.util.ImagemUtil;
import br.com.cana.service.ApiClient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class TelaSubstituicaoView extends JFrame {

    private JLabel lblNomeArb, lblNomeB1, lblNomeB2;
    private DefaultTableModel modelAzul, modelVermelho;
    private JTable tabelaAzul, tabelaVermelho;

    private DefaultTableModel mainModelAzul, mainModelVermelho;

    private String reservaSelecionado = "";
    private JButton btnReservaAtivo = null;

    private br.com.cana.model.Partida partidaObjeto;
    private br.com.cana.model.Partida partidaClone;

    private String arbOriginal = "";
    private String b1Original = "";
    private String b2Original = "";

    private final Gson gson = ApiClient.GSON;

    public TelaSubstituicaoView() {
        this(null, null, null, null, null, null);
    }

    public TelaSubstituicaoView(br.com.cana.model.Partida partida, DefaultTableModel mainModelAzul,
            DefaultTableModel mainModelVermelho,
            JLabel mainLblArb, JLabel mainLblB1, JLabel mainLblB2) {

        this.partidaObjeto = partida;
        this.mainModelAzul = mainModelAzul;
        this.mainModelVermelho = mainModelVermelho;
        String jsonClone = ApiClient.GSON.toJson(partida);
        this.partidaClone = ApiClient.GSON.fromJson(jsonClone, br.com.cana.model.Partida.class);

        setTitle("CANA - Gerenciar Substituções");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(ImagemUtil.COR_FUNDO);
        ImagemUtil.configurarIcone(this);

        // --- TÍTULO ---
        JLabel lblTitulo = new JLabel("SUBSTITUIÇÕES", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setBounds(0, 10, 1000, 35);
        add(lblTitulo);

        // 🛠️ ARQUITETURA DE BANCO DINÂMICO
        JPanel painelReservas = new JPanel(new java.awt.GridLayout(0, 4, 5, 5));
        painelReservas.setBackground(ImagemUtil.COR_FUNDO);

        // --- GRID DE RESERVAS ---
        JLabel lblReservas = new JLabel("JOGADORES DISPONÍVEIS (BANCO)", SwingConstants.CENTER);
        lblReservas.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblReservas.setBounds(0, 480, 1000, 20);
        add(lblReservas);

        // 🌟 EMBALAGEM DE SCROLL
        JScrollPane scrollReservas = new JScrollPane(painelReservas);
        scrollReservas.setBounds(150, 510, 700, 130);
        scrollReservas.setBorder(null);
        scrollReservas.getViewport().setBackground(ImagemUtil.COR_FUNDO);
        scrollReservas.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollReservas.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollReservas);

        // --- ARBITRAGEM OPERACIONAL ---
        lblNomeArb = criarCampoArbitragem("Árbitro:", 60);
        lblNomeB1 = criarCampoArbitragem("Bandeira 1:", 90);
        lblNomeB2 = criarCampoArbitragem("Bandeira 2:", 120);

        if (mainLblArb != null) {
            String txt = mainLblArb.getText().replace("Árbitro: ", "").trim();
            lblNomeArb.setText(txt.contains("____") ? "Selecione..." : txt);
        }
        if (mainLblB1 != null) {
            String txt = mainLblB1.getText().replace("Bandeira 1: ", "").trim();
            lblNomeB1.setText(txt.contains("____") ? "Selecione..." : txt);
        }
        if (mainLblB2 != null) {
            String txt = mainLblB2.getText().replace("Bandeira 2: ", "").trim();
            lblNomeB2.setText(txt.contains("____") ? "Selecione..." : txt);
        }

        // 🌟 LOCK DE SEGURANÇA
        if (partidaClone != null) {
            this.arbOriginal = partidaClone.getArbitro() != null ? partidaClone.getArbitro() : "";
            this.b1Original = partidaClone.getBandeira1() != null ? partidaClone.getBandeira1() : "";
            this.b2Original = partidaClone.getBandeira2() != null ? partidaClone.getBandeira2() : "";
        }

        add(criarBotaoTroca(60, lblNomeArb, mainLblArb, "Árbitro: ", painelReservas));
        add(criarBotaoTroca(90, lblNomeB1, mainLblB1, "Bandeira 1: ", painelReservas));
        add(criarBotaoTroca(120, lblNomeB2, mainLblB2, "Bandeira 2: ", painelReservas));

        // --- TIMES ---
        add(criarPainelTime(true, 80, 160, 330, 300));
        add(criarPainelTime(false, 590, 160, 330, 300));

        // BOTÃO DE ATRASADOS
        JButton btnAdicionarAtrasado = new JButton("＋ Chegou Atrasado");
        btnAdicionarAtrasado.setBounds(700, 478, 150, 24);
        btnAdicionarAtrasado.setFont(new Font("SansSerif", Font.BOLD, 10));
        btnAdicionarAtrasado.setBackground(new Color(0xCCFFCC));
        btnAdicionarAtrasado.setFocusPainted(false);
        add(btnAdicionarAtrasado);

        atualizarGridBotoesReservas(painelReservas);

        // --- BOTÃO CONFIRMAR ---
        br.com.cana.util.ImagemUtil.BotaoGradienteCANA btnConfirmar = new br.com.cana.util.ImagemUtil.BotaoGradienteCANA(
                "CONFIRMAR TROCAS");
        btnConfirmar.setBounds(415, 648, 240, 36);
        btnConfirmar.addActionListener(e -> {
            if (mainModelAzul != null) {
                for (int i = 0; i < modelAzul.getRowCount(); i++) {
                    String nomeNovo = modelAzul.getValueAt(i, 0).toString();
                    if (i >= mainModelAzul.getRowCount()) {
                        String posReal = "LIN";
                        if (partidaClone != null && partidaClone.getListaGeralPresenca() != null) {
                            for (br.com.cana.model.JogadorPartida jp : partidaClone.getListaGeralPresenca()) {
                                if (jp == null || jp.getJogador() == null)
                                    continue;
                                String apelido = jp.getJogador().getApelido();
                                String nomeJ = (apelido != null && !apelido.trim().isEmpty())
                                        ? apelido.trim()
                                        : (jp.getJogador().getNome() != null ? jp.getJogador().getNome().trim() : "");
                                if (nomeJ.equalsIgnoreCase(nomeNovo)) {
                                    posReal = apiEncurtarPosicaoInterna(jp.getJogador().getPosicao());
                                    break;
                                }
                            }
                        }
                        mainModelAzul.addRow(new Object[] { nomeNovo, posReal, "" });
                    } else {
                        String nomeAntigo = mainModelAzul.getValueAt(i, 0).toString();
                        if (!nomeAntigo.equals(nomeNovo)) {
                            mainModelAzul.setValueAt(nomeNovo, i, 0);
                            if (!nomeAntigo.trim().startsWith("Azul ")) {
                                String evAtual = mainModelAzul.getValueAt(i, 2).toString();
                                mainModelAzul.setValueAt(apiRegistrarSubstituicaoNoEvento(evAtual), i, 2);
                            }
                        }
                    }
                }
                for (int i = 0; i < modelVermelho.getRowCount(); i++) {
                    String nomeNovo = modelVermelho.getValueAt(i, 0).toString();
                    if (i >= mainModelVermelho.getRowCount()) {
                        String posReal = "LIN";
                        if (partidaClone != null && partidaClone.getListaGeralPresenca() != null) {
                            for (br.com.cana.model.JogadorPartida jp : partidaClone.getListaGeralPresenca()) {
                                if (jp == null || jp.getJogador() == null)
                                    continue;
                                String apelido = jp.getJogador().getApelido();
                                String nomeJ = (apelido != null && !apelido.trim().isEmpty())
                                        ? apelido.trim()
                                        : (jp.getJogador().getNome() != null ? jp.getJogador().getNome().trim() : "");
                                if (nomeJ.equalsIgnoreCase(nomeNovo)) {
                                    posReal = apiEncurtarPosicaoInterna(jp.getJogador().getPosicao());
                                    break;
                                }
                            }
                        }
                        mainModelVermelho.addRow(new Object[] { nomeNovo, posReal, "" });
                    } else {
                        String nomeAntigo = mainModelVermelho.getValueAt(i, 0).toString();
                        if (!nomeAntigo.equals(nomeNovo)) {
                            mainModelVermelho.setValueAt(nomeNovo, i, 0);
                            if (!nomeAntigo.trim().startsWith("Vermelho ")) {
                                String evAtual = mainModelVermelho.getValueAt(i, 2).toString();
                                mainModelVermelho.setValueAt(apiRegistrarSubstituicaoNoEvento(evAtual), i, 2);
                            }
                        }
                    }
                }
            }

            if (partidaClone != null) {
                partidaObjeto.setListaGeralPresenca(partidaClone.getListaGeralPresenca());
                partidaObjeto.setArbitro(partidaClone.getArbitro());
                partidaObjeto.setBandeira1(partidaClone.getBandeira1());
                partidaObjeto.setBandeira2(partidaClone.getBandeira2());
            }
            
            this.dispose();
        });
        add(btnConfirmar);

        btnAdicionarAtrasado.addActionListener(al -> {
            
            // 1. Declaramos a lista como final logo de cara
            final List<br.com.cana.model.Jogador> disponiveis = new ArrayList<>();
            
            try {
                // Requisita a lista de atrasados no Back-end
                String jsonRes = ApiClient.post("/partidas/atrasados-disponiveis", ApiClient.GSON.toJson(partidaClone));
                
                java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<br.com.cana.model.Jogador>>() {}.getType();
                List<br.com.cana.model.Jogador> responseList = ApiClient.GSON.fromJson(jsonRes, type);
                
                if (responseList != null) {
                    // 2. Usamos addAll() em vez de "=" para manter a variável efetivamente final!
                    disponiveis.addAll(responseList); 
                }
            } catch (Exception ex) {
                System.err.println("Erro ao buscar atrasados na API: " + ex.getMessage());
            }

            if (disponiveis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos os jogadores ativos já estão relacionados nesta partida!");
                return;
            }

            JPanel panelModal = new JPanel(new BorderLayout(5, 5));
            JTextField txtBusca = new JTextField();
            txtBusca.putClientProperty("JTextField.placeholderText", "🔍 Filtrar...");

            DefaultListModel<String> listModel = new DefaultListModel<>();
            for (br.com.cana.model.Jogador j : disponiveis) {
                String nome = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                        : j.getNome();
                listModel.addElement(
                        "[" + (j.getPosicao() != null ? j.getPosicao().toUpperCase().trim() : "-") + "] " + nome);
            }

            JList<String> listAtrasados = new JList<>(listModel);
            listAtrasados.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane scrollList = new JScrollPane(listAtrasados);
            scrollList.setPreferredSize(new Dimension(250, 150));
            scrollList.getVerticalScrollBar().setUnitIncrement(16);

            txtBusca.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void filtrar() {
                    listModel.clear();
                    String termo = txtBusca.getText();
                    for (br.com.cana.model.Jogador j : disponiveis) {
                        String nome = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                                : j.getNome();
                        String item = "[" + (j.getPosicao() != null ? j.getPosicao().toUpperCase().trim() : "-") + "] "
                                + nome;
                        if (apiFiltrarJogadoresPorTexto(termo, item)) {
                            listModel.addElement(item);
                        }
                    }
                }

                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    filtrar();
                }

                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    filtrar();
                }

                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    filtrar();
                }
            });

            panelModal.add(txtBusca, BorderLayout.NORTH);
            panelModal.add(scrollList, BorderLayout.CENTER);

            int result = JOptionPane.showConfirmDialog(this, panelModal, "Jogador Atrasado",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION && listAtrasados.getSelectedValue() != null) {
                String selecionadoRaw = listAtrasados.getSelectedValue();
                String nomeAtrasado = apiObterNomeAtivo(
                        selecionadoRaw.substring(selecionadoRaw.indexOf("]") + 1).trim());

                br.com.cana.model.Jogador jModel = apiBuscarJogadorPorNomeOuApelido(nomeAtrasado);

                if (jModel != null) {
                    String msgRestricao = apiValidarRestricaoParaSubstituicao(nomeAtrasado);
                    if (msgRestricao != null) {
                        JOptionPane.showMessageDialog(this, msgRestricao, "Restrição", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    br.com.cana.model.JogadorPartida jpNew = new br.com.cana.model.JogadorPartida();
                    jpNew.setJogador(jModel);
                    jpNew.setTime("Nenhum");
                    jpNew.setStatus("Reserva");
                    jpNew.setFuncao(
                            jModel.getPosicao() != null && jModel.getPosicao().toUpperCase().contains("GOL") ? "GOL"
                                    : "LIN");

                    partidaClone.getListaGeralPresenca().add(jpNew);
                    atualizarGridBotoesReservas(painelReservas);

                    JOptionPane.showMessageDialog(this, "O jogador '" + nomeAtrasado + "' foi adicionado ao banco!");
                }
            }
        });
    }

    private JLabel criarCampoArbitragem(String cargo, int y) {
        JLabel lblCargo = new JLabel(cargo);
        lblCargo.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblCargo.setBounds(250, y, 100, 25);
        add(lblCargo);

        JLabel lblNome = new JLabel(" Selecione...", SwingConstants.LEFT);
        lblNome.setOpaque(true);
        lblNome.setBackground(Color.WHITE);
        lblNome.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lblNome.setBounds(350, y, 300, 25);
        add(lblNome);
        return lblNome;
    }

    private JButton criarBotaoTroca(int y, JLabel localLabel, JLabel mainLabel, String prefixo, JPanel painelReservas) {
        JButton btn = new JButton("⬅ OK");
        btn.setBounds(660, y, 80, 24);
        btn.setFont(new Font("SansSerif", Font.BOLD, 10));

        btn.addActionListener(e -> {
            String cargo = prefixo.contains("Bandeira 1") ? "BANDEIRA1"
                    : prefixo.contains("Bandeira 2") ? "BANDEIRA2" : "ARBITRO";

            if (!reservaSelecionado.isEmpty()) {
                String nomeReservaLimpo = apiObterNomeAtivo(reservaSelecionado);
                br.com.cana.model.Jogador jogadorModel = apiBuscarJogadorPorNomeOuApelido(nomeReservaLimpo);

                String msgErro = apiValidarRestricaoParaArbitragem(partidaClone, jogadorModel);
                if (msgErro != null) {
                    JOptionPane.showMessageDialog(this, msgErro, "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String textoAtualEntidade = cargo.equalsIgnoreCase("BANDEIRA1") ? partidaClone.getBandeira1()
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? partidaClone.getBandeira2()
                                : partidaClone.getArbitro();
                String nomeAtivoAtual = apiObterNomeAtivo(textoAtualEntidade);

                String originalSessao = cargo.equalsIgnoreCase("BANDEIRA1") ? b1Original
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? b2Original : arbOriginal;

                boolean acumularHistorico = false;
                if (originalSessao != null && !originalSessao.trim().isEmpty() && !nomeAtivoAtual.isEmpty()) {
                    acumularHistorico = originalSessao.contains(nomeAtivoAtual);
                }

                apiDefinirArbitragemSemDuplicidade(partidaClone, jogadorModel, cargo, acumularHistorico);

                String textoAcumulado = cargo.equalsIgnoreCase("BANDEIRA1") ? partidaClone.getBandeira1()
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? partidaClone.getBandeira2()
                                : partidaClone.getArbitro();

                localLabel.setText(textoAcumulado);
                if (mainLabel != null) {
                    mainLabel.setText(prefixo + textoAcumulado);
                }

                reservaSelecionado = "";
                btnReservaAtivo = null;
            } else {
                String originalSessao = cargo.equalsIgnoreCase("BANDEIRA1") ? b1Original
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? b2Original : arbOriginal;

                String textoAtualEntidade = cargo.equalsIgnoreCase("BANDEIRA1") ? partidaClone.getBandeira1()
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? partidaClone.getBandeira2()
                                : partidaClone.getArbitro();

                String nomeSaindoLimpo = apiObterNomeAtivo(textoAtualEntidade);

                boolean gerarHistorico = false;
                if (originalSessao != null && !originalSessao.trim().isEmpty() && !nomeSaindoLimpo.isEmpty()) {
                    gerarHistorico = originalSessao.contains(nomeSaindoLimpo);
                }

                apiRemoverDaArbitragem(partidaClone, cargo, gerarHistorico);

                String textoResultado = cargo.equalsIgnoreCase("BANDEIRA1") ? partidaClone.getBandeira1()
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? partidaClone.getBandeira2()
                                : partidaClone.getArbitro();

                if (textoResultado == null || textoResultado.trim().isEmpty()) {
                    localLabel.setText("Selecione...");
                    if (mainLabel != null) {
                        mainLabel.setText(prefixo + "___________________________");
                    }
                } else {
                    localLabel.setText(textoResultado);
                    if (mainLabel != null) {
                        mainLabel.setText(prefixo + textoResultado);
                    }
                }
            }

            atualizarGridBotoesReservas(painelReservas);
        });
        return btn;
    }

    private JPanel criarPainelTime(boolean isAzul, int x, int y, int w, int h) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBounds(x, y, w, h);
        Color corTime = isAzul ? new Color(0x1A1AFF) : new Color(0xEF3333);
        painel.setBackground(corTime);

        DefaultTableModel model = new DefaultTableModel(new String[] { "Nome", "Pos" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        DefaultTableModel alvo = isAzul ? mainModelAzul : mainModelVermelho;
        int totalAdicionado = 0;
        if (alvo != null) {
            for (int i = 0; i < alvo.getRowCount(); i++) {
                model.addRow(new Object[] { alvo.getValueAt(i, 0), alvo.getValueAt(i, 1) });
                totalAdicionado++;
            }
        }
        while (totalAdicionado < 11) {
            model.addRow(new Object[] { (isAzul ? "Azul " : "Vermelho ") + (totalAdicionado + 1), "LIN" });
            totalAdicionado++;
        }

        if (isAzul) {
            this.modelAzul = model;
        } else {
            this.modelVermelho = model;
        }

        JTable tabela = new JTable(model);
        tabela.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("SansSerif", Font.BOLD, 12));
                label.setBackground(Color.WHITE);
                return label;
            }
        });
        tabela.setBackground(corTime);
        tabela.setRowHeight(24);
        tabela.setShowGrid(false);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        if (isAzul)
            this.tabelaAzul = tabela;
        else
            this.tabelaVermelho = tabela;

        tabela.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (isSelectedIndex(index0)) {
                    super.clearSelection();
                    if (btnReservaAtivo != null) {
                        btnReservaAtivo.setBackground(Color.WHITE);
                    }
                    btnReservaAtivo = null;
                    reservaSelecionado = "";
                } else {
                    super.setSelectionInterval(index0, index1);
                }
            }
        });

        tabela.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && tabela.getSelectedRow() >= 0) {
                if (isAzul) {
                    if (tabelaVermelho != null)
                        tabelaVermelho.clearSelection();
                } else {
                    if (tabelaAzul != null)
                        tabelaAzul.clearSelection();
                }
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        centerRenderer.setBackground(corTime);
        centerRenderer.setForeground(Color.BLACK);
        for (int i = 0; i < tabela.getColumnCount(); i++)
            tabela.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(corTime);
        painel.add(scroll, BorderLayout.CENTER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return painel;
    }

    private List<String> carregarReservasDisponiveis() {
        List<String> reservasReais = new ArrayList<>();

        try {
            if (partidaClone != null && partidaClone.getListaGeralPresenca() != null) {
                for (br.com.cana.model.JogadorPartida jp : partidaClone.getListaGeralPresenca()) {
                    if ("Nenhum".equalsIgnoreCase(jp.getTime()) && !"Staff".equalsIgnoreCase(jp.getFuncao())) {
                        br.com.cana.model.Jogador j = jp.getJogador();
                        if (j != null) {
                            String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                                    : j.getNome();
                            String posResumida = apiEncurtarPosicaoInterna(j.getPosicao());
                            reservasReais.add(nomeJ + " (" + posResumida + ")");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao listar reservas: " + ex.getMessage());
        }

        List<String> listaFinalGrid = new ArrayList<>(reservasReais);
        int contadorVazios = 1;
        while (listaFinalGrid.size() < 12) {
            listaFinalGrid.add("Disponível " + contadorVazios + " (-)");
            contadorVazios++;
        }
        return listaFinalGrid;
    }

    private void atualizarGridBotoesReservas(JPanel painelReservas) {
        painelReservas.removeAll();

        for (String nomeReservaComPosicao : carregarReservasDisponiveis()) {
            JButton btnReserva = new JButton(nomeReservaComPosicao);
            btnReserva.setFont(new Font("SansSerif", Font.BOLD, 10));
            btnReserva.setFocusPainted(false);

            String nomeLimpo = apiObterNomeAtivo(nomeReservaComPosicao);

            boolean estaAtuandoNaArbitragem = nomeLimpo.equals(apiObterNomeAtivo(partidaClone.getArbitro()))
                    || nomeLimpo.equals(apiObterNomeAtivo(partidaClone.getBandeira1()))
                    || nomeLimpo.equals(apiObterNomeAtivo(partidaClone.getBandeira2()));

            if (nomeReservaComPosicao.contains("Disponível")) {
                btnReserva.setBackground(Color.LIGHT_GRAY);
                btnReserva.setEnabled(false);
            } else if (estaAtuandoNaArbitragem) {
                btnReserva.setBackground(new Color(0xFFE4B5));
                btnReserva.setText(nomeLimpo + " (APITANDO)");
                btnReserva.setEnabled(false);
            } else {
                btnReserva.setBackground(Color.WHITE);
                btnReserva.setEnabled(true);
            }

            btnReserva.addActionListener(e -> {

                int rowAzul = tabelaAzul.getSelectedRow();
                int rowVermelho = tabelaVermelho.getSelectedRow();

                DefaultTableModel modelAlvo = (rowAzul >= 0) ? modelAzul : (rowVermelho >= 0) ? modelVermelho : null;
                JTable tabelaAlvo = (rowAzul >= 0) ? tabelaAzul : (rowVermelho >= 0) ? tabelaVermelho : null;

                if (modelAlvo != null) {

                    String msgErro = apiValidarRestricaoParaSubstituicao(btnReserva.getText());
                    if (msgErro != null) {
                        JOptionPane.showMessageDialog(this, msgErro, "Restrição", JOptionPane.WARNING_MESSAGE);
                        tabelaAlvo.clearSelection();
                        return;
                    }

                    int linhaSel = tabelaAlvo.getSelectedRow();
                    String nomeSaindo = modelAlvo.getValueAt(linhaSel, 0).toString();
                    String nomeEntrandoRaw = btnReserva.getText();

                    String timeAlvo = (rowAzul >= 0) ? "Azul" : "Vermelho";
                    String nomeEntrandoLimpo = apiObterNomeAtivo(nomeEntrandoRaw);

                    boolean isVagaIncompleta = nomeSaindo.trim().startsWith("Azul ")
                            || nomeSaindo.trim().startsWith("Vermelho ");

                    if (isVagaIncompleta) {
                        if (partidaClone != null && partidaClone.getListaGeralPresenca() != null) {
                            for (br.com.cana.model.JogadorPartida jp : partidaClone.getListaGeralPresenca()) {
                                String nomeObjeto = (jp.getJogador().getApelido() != null
                                        && !jp.getJogador().getApelido().trim().isEmpty())
                                                ? jp.getJogador().getApelido().trim()
                                                : jp.getJogador().getNome().trim();

                                if (nomeObjeto.equalsIgnoreCase(nomeEntrandoLimpo)) {
                                    jp.setTime(timeAlvo);
                                    jp.setStatus("Titular");

                                    String posSigla = modelAlvo.getValueAt(linhaSel, 1).toString().contains("GOL")
                                            ? "GOL"
                                            : "LIN";
                                    jp.setFuncao(timeAlvo + "_" + posSigla + "_" + (linhaSel + 1));
                                    break;
                                }
                            }
                        }

                        modelAlvo.setValueAt(nomeEntrandoLimpo, linhaSel, 0);

                    } else {
                        String[] resultado = apiProcessarSubstituicaoJogador(nomeSaindo, nomeEntrandoRaw,
                                modelAlvo.getValueAt(linhaSel, 1).toString());

                        apiAtualizarSubstituicaoNaListaPresenca(partidaClone, nomeSaindo, nomeEntrandoLimpo, timeAlvo);

                        modelAlvo.setValueAt(resultado[0], linhaSel, 0);
                    }

                    tabelaAlvo.clearSelection();
                    atualizarGridBotoesReservas(painelReservas);

                } else {
                    if (btnReservaAtivo != null)
                        btnReservaAtivo.setBackground(Color.WHITE);
                    btnReservaAtivo = btnReserva;
                    btnReservaAtivo.setBackground(new Color(0xFFCC00));
                    reservaSelecionado = btnReserva.getText();
                }
            });

            painelReservas.add(btnReserva);
        }

        painelReservas.revalidate();
        painelReservas.repaint();
    }

    // =========================================================================
    // 🌐 MÉTODOS DE COMUNICAÇÃO HTTP VIA API CLIENT (SPRING BOOT REST)
    // =========================================================================

    private boolean apiFiltrarJogadoresPorTexto(String termo, String item) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("termo", termo);
            json.addProperty("item", item);
            String resp = ApiClient.post("/jogadores/filtrar-texto", json.toString());
            return Boolean.parseBoolean(resp.trim());
        } catch (Exception e) {
            if (termo == null || termo.trim().isEmpty())
                return true;
            return item.toLowerCase().contains(termo.toLowerCase());
        }
    }

    private br.com.cana.model.Jogador apiBuscarJogadorPorNomeOuApelido(String nome) {
        try {
            String jsonResp = ApiClient.post("/jogadores/buscar-por-nome", nome != null ? nome : "");
            if (jsonResp != null && !jsonResp.trim().isEmpty() && !jsonResp.equals("null")) {
                return gson.fromJson(jsonResp, br.com.cana.model.Jogador.class);
            }
        } catch (Exception e) {
            System.err.println("Erro na API buscarJogadorPorNomeOuApelido: " + e.getMessage());
        }
        return null;
    }

    private String apiValidarRestricaoParaSubstituicao(String nome) {
        try {
            // Prepara o payload do jeito que o seu /valida-linha espera
            JsonObject json = new JsonObject();
            json.addProperty("reserva", nome != null ? nome : "");
            
            // Bate na rota forte que checa cartão E mensalidade!
            String resp = ApiClient.post("/partidas/valida-linha", json.toString());
            
            if (resp != null) {
                String limpo = resp.replace("\"", "").trim();
                // O back-end devolve "OK" se tiver tudo certo
                if (limpo.equals("OK") || limpo.isEmpty()) {
                    return null; 
                }
                return limpo; // Se não for OK, devolve o texto do erro pro Pop-up!
            }
        } catch (Exception e) {
            System.err.println("Erro na API valida-linha: " + e.getMessage());
        }
        return null;
    }

    private String apiValidarRestricaoParaArbitragem(br.com.cana.model.Partida partida,
            br.com.cana.model.Jogador jogador) {
        try {
            JsonObject json = new JsonObject();
            json.add("partida", gson.toJsonTree(partida));
            json.add("jogador", gson.toJsonTree(jogador));
            String resp = ApiClient.post("/partidas/validar-restricao-arbitragem", json.toString());
            if (resp != null) {
                String limpo = resp.replace("\"", "").trim();
                return limpo.isEmpty() ? null : limpo;
            }
        } catch (Exception e) {
            System.err.println("Erro na API validarRestricaoParaArbitragem: " + e.getMessage());
        }
        return null;
    }

    private void apiDefinirArbitragemSemDuplicidade(br.com.cana.model.Partida partida,
            br.com.cana.model.Jogador jogador, String cargo, boolean acumular) {
        try {
            JsonObject json = new JsonObject();
            json.add("partida", gson.toJsonTree(partida));
            json.add("jogador", gson.toJsonTree(jogador));
            json.addProperty("cargo", cargo);
            json.addProperty("acumular", acumular);
            String jsonResp = ApiClient.post("/partidas/definir-arbitragem", json.toString());
            if (jsonResp != null && !jsonResp.trim().isEmpty()) {
                br.com.cana.model.Partida pAtualizada = gson.fromJson(jsonResp, br.com.cana.model.Partida.class);
                if (pAtualizada != null) {
                    partida.setArbitro(pAtualizada.getArbitro());
                    partida.setBandeira1(pAtualizada.getBandeira1());
                    partida.setBandeira2(pAtualizada.getBandeira2());
                }
            }
        } catch (Exception e) {
            System.err.println("Erro na API definirArbitragemSemDuplicidade: " + e.getMessage());
        }
    }

    private void apiRemoverDaArbitragem(br.com.cana.model.Partida partida, String cargo, boolean gerarHistorico) {
        try {
            JsonObject json = new JsonObject();
            json.add("partida", gson.toJsonTree(partida));
            json.addProperty("cargo", cargo);
            json.addProperty("gerarHistorico", gerarHistorico);
            String jsonResp = ApiClient.post("/partidas/remover-arbitragem", json.toString());
            if (jsonResp != null && !jsonResp.trim().isEmpty()) {
                br.com.cana.model.Partida pAtualizada = gson.fromJson(jsonResp, br.com.cana.model.Partida.class);
                if (pAtualizada != null) {
                    partida.setArbitro(pAtualizada.getArbitro());
                    partida.setBandeira1(pAtualizada.getBandeira1());
                    partida.setBandeira2(pAtualizada.getBandeira2());
                }
            }
        } catch (Exception e) {
            System.err.println("Erro na API removerDaArbitragem: " + e.getMessage());
        }
    }

    private String[] apiProcessarSubstituicaoJogador(String nomeSaindo, String nomeEntrandoRaw, String pos) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("nomeSaindo", nomeSaindo);
            json.addProperty("nomeEntrandoRaw", nomeEntrandoRaw);
            json.addProperty("posicao", pos);
            String jsonResp = ApiClient.post("/partidas/processar-substituicao-jogador", json.toString());
            java.lang.reflect.Type type = new TypeToken<String[]>() {
            }.getType();
            String[] res = gson.fromJson(jsonResp, type);
            if (res != null && res.length > 0)
                return res;
        } catch (Exception e) {
            System.err.println("Erro na API processarSubstituicaoJogador: " + e.getMessage());
        }
        return new String[] { nomeSaindo + " / " + apiObterNomeAtivo(nomeEntrandoRaw) };
    }

    private void apiAtualizarSubstituicaoNaListaPresenca(br.com.cana.model.Partida partida, String nomeSaindo,
            String nomeEntrandoLimpo, String timeAlvo) {
        try {
            JsonObject json = new JsonObject();
            json.add("partida", gson.toJsonTree(partida));
            json.addProperty("nomeSaindo", nomeSaindo);
            json.addProperty("nomeEntrandoLimpo", nomeEntrandoLimpo);
            json.addProperty("timeAlvo", timeAlvo);
            String jsonResp = ApiClient.post("/partidas/atualizar-substituicao-lista-presenca", json.toString());
            if (jsonResp != null && !jsonResp.trim().isEmpty()) {
                br.com.cana.model.Partida pAtualizada = gson.fromJson(jsonResp, br.com.cana.model.Partida.class);
                if (pAtualizada != null && pAtualizada.getListaGeralPresenca() != null) {
                    partida.setListaGeralPresenca(pAtualizada.getListaGeralPresenca());
                }
            }
        } catch (Exception e) {
            System.err.println("Erro na API atualizarSubstituicaoNaListaPresenca: " + e.getMessage());
        }
    }

    private String apiEncurtarPosicaoInterna(String posicao) {
        if (posicao == null)
            return "--";
        posicao = posicao.toUpperCase().trim();
        if (posicao.contains("GOLEIRO"))
            return "GOL";
        if (posicao.contains("LATERAL"))
            return "LAT";
        if (posicao.contains("ZAGUEIRO"))
            return "ZAG";
        if (posicao.contains("MEIA"))
            return "MEI";
        if (posicao.contains("ATACANTE"))
            return "ATA";
        return posicao.length() > 3 ? posicao.substring(0, 3) : posicao;
    }

    private String apiRegistrarSubstituicaoNoEvento(String evAtual) {
        if (evAtual == null || evAtual.trim().isEmpty())
            return " / ";
        return evAtual + " / ";
    }

    private String apiObterNomeAtivo(String texto) {
        if (texto == null || texto.trim().isEmpty() || texto.equals("Selecione...") || texto.equals("Não escalado"))
            return "";
        String nomeAtivo = texto;
        if (texto.contains(" / ")) {
            String[] partes = texto.split(" / ");
            nomeAtivo = partes[partes.length - 1].trim();
        }
        if (nomeAtivo.contains("____") || nomeAtivo.isEmpty())
            return "";
        if (nomeAtivo.contains(" ("))
            return nomeAtivo.substring(0, nomeAtivo.indexOf(" (")).trim();
        return nomeAtivo.trim();
    }
}