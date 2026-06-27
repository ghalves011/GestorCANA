package br.com.cana.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import br.com.cana.util.ImagemUtil;
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
    private br.com.cana.service.PartidaService partidaService;

    private String arbOriginal = "";
    private String b1Original = "";
    private String b2Original = "";

    public TelaSubstituicaoView() {
        this(null, null, null, null, null, null);
    }

    public TelaSubstituicaoView(br.com.cana.model.Partida partida, DefaultTableModel mainModelAzul,
            DefaultTableModel mainModelVermelho,
            JLabel mainLblArb, JLabel mainLblB1, JLabel mainLblB2) {

        this.partidaObjeto = partida;
        this.partidaService = new br.com.cana.service.PartidaService();
        this.mainModelAzul = mainModelAzul;
        this.mainModelVermelho = mainModelVermelho;

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

        // 🛠️ ARQUITETURA DE BANCO DINÂMICO: Linhas '0' significam que o grid crescerá
        // infinitamente em linhas mantendo 4 colunas
        JPanel painelReservas = new JPanel(new java.awt.GridLayout(0, 4, 5, 5));
        painelReservas.setBackground(ImagemUtil.COR_FUNDO);

        // --- GRID DE RESERVAS ---
        JLabel lblReservas = new JLabel("JOGADORES DISPONÍVEIS (BANCO)", SwingConstants.CENTER);
        lblReservas.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblReservas.setBounds(0, 480, 1000, 20);
        add(lblReservas);

        // 🌟 EMBALAGEM DE SCROLL: Adiciona o painel dentro de um scroll para permitir
        // infinitos reservas
        JScrollPane scrollReservas = new JScrollPane(painelReservas);
        scrollReservas.setBounds(150, 510, 700, 130);
        scrollReservas.setBorder(null);
        scrollReservas.getViewport().setBackground(ImagemUtil.COR_FUNDO);
        scrollReservas.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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

        // 🌟 LOCK DE SEGURANÇA: Registra em memória o estado original vindo do banco de
        // dados
        if (partidaObjeto != null) {
            this.arbOriginal = partidaObjeto.getArbitro() != null ? partidaObjeto.getArbitro() : "";
            this.b1Original = partidaObjeto.getBandeira1() != null ? partidaObjeto.getBandeira1() : "";
            this.b2Original = partidaObjeto.getBandeira2() != null ? partidaObjeto.getBandeira2() : "";
        }

        // Agora passando o 'painelReservas' que já foi declarado acima!
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

        // Alimenta o painel usando o método dinâmico que você criou
        atualizarGridBotoesReservas(painelReservas);

        // --- BOTÃO CONFIRMAR ---
        br.com.cana.util.ImagemUtil.BotaoGradienteCANA btnConfirmar = new br.com.cana.util.ImagemUtil.BotaoGradienteCANA(
                "CONFIRMAR TROCAS");
        btnConfirmar.setBounds(415, 648, 240, 36);
        btnConfirmar.addActionListener(e -> {
            if (mainModelAzul != null) {
                for (int i = 0; i < modelAzul.getRowCount(); i++) {
                    String nomeAntigo = mainModelAzul.getValueAt(i, 0).toString();
                    String nomeNovo = modelAzul.getValueAt(i, 0).toString();
                    if (!nomeAntigo.equals(nomeNovo)) {
                        mainModelAzul.setValueAt(nomeNovo, i, 0);
                        String evAtual = mainModelAzul.getValueAt(i, 2).toString();
                        mainModelAzul.setValueAt(partidaService.registrarSubstituicaoNoEvento(evAtual), i, 2);
                    }
                }
                for (int i = 0; i < modelVermelho.getRowCount(); i++) {
                    String nomeAntigo = mainModelVermelho.getValueAt(i, 0).toString();
                    String nomeNovo = modelVermelho.getValueAt(i, 0).toString();
                    if (!nomeAntigo.equals(nomeNovo)) {
                        mainModelVermelho.setValueAt(nomeNovo, i, 0);
                        String evAtual = mainModelVermelho.getValueAt(i, 2).toString();
                        mainModelVermelho.setValueAt(partidaService.registrarSubstituicaoNoEvento(evAtual), i, 2);
                    }
                }
            }
            this.dispose();
        });
        add(btnConfirmar);

        // Código do botão atrasado (Mantido inline para evitar mexer no escopo de fora)
        btnAdicionarAtrasado.addActionListener(al -> {
            // Busca a lista de atletas ativos que ainda não estão no racha, já ordenados
            // por posição pelo Service
            List<br.com.cana.model.Jogador> disponiveis = partidaService
                    .obterAtrasadosDisponiveisOrdenados(partidaObjeto);

            if (disponiveis.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos os jogadores ativos já estão relacionados nesta partida!");
                return;
            }

            // Monta o JComboBox organizadinho por posições
            JComboBox<String> comboAtrasados = new JComboBox<>();
            for (br.com.cana.model.Jogador j : disponiveis) {
                String nomeExibicao = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                        : j.getNome();
                comboAtrasados.addItem("[" + (j.getPosicao() != null ? j.getPosicao().toUpperCase().trim() : "-") + "] "
                        + nomeExibicao);
            }

            // Abre o modal de escolha do jogador da várzea
            int result = JOptionPane.showConfirmDialog(this, comboAtrasados, "Jogador Atrasado",
                    JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION && comboAtrasados.getSelectedIndex() != -1) {
                br.com.cana.model.Jogador jLista = disponiveis.get(comboAtrasados.getSelectedIndex());
                String nomeAtrasado = (jLista.getApelido() != null && !jLista.getApelido().trim().isEmpty())
                        ? jLista.getApelido()
                        : jLista.getNome();

                // Instancia o SERVICE para buscar o registro atualizado
                br.com.cana.service.JogadorService jogadorService = new br.com.cana.service.JogadorService();

                // Recupera o objeto do jogador completo
                br.com.cana.model.Jogador jModel = jogadorService.buscarPorNomeOuApelido(nomeAtrasado);

                if (jModel != null) {
                    // 🎯 VALIDAÇÃO FINANCEIRA: Verifica a flag vinda do seu DAO
                    if (!jModel.isMensalidadeEmDia()) {
                        JOptionPane.showMessageDialog(this,
                                "O jogador '" + nomeAtrasado
                                        + "' possui pendências financeiras e está impedido de jogar!",
                                "Erro Financeiro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // Se passou na validação, adiciona o atrasado no pote de reservas (Nenhum)
                if (partidaObjeto != null && jModel != null) {
                    br.com.cana.model.JogadorPartida jpNew = new br.com.cana.model.JogadorPartida();
                    jpNew.setJogador(jModel);
                    jpNew.setTime("Nenhum");
                    jpNew.setStatus("Reserva");
                    jpNew.setFuncao(
                            jModel.getPosicao() != null && jModel.getPosicao().toUpperCase().contains("GOL") ? "GOL"
                                    : "LIN");

                    partidaObjeto.getListaGeralPresenca().add(jpNew);

                    // Atualiza o grid de botões para o atrasado aparecer imediatamente no banco
                    atualizarGridBotoesReservas(painelReservas);
                    JOptionPane.showMessageDialog(this,
                            "O jogador '" + nomeAtrasado + "' foi adicionado ao banco de reservas!");
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
                String nomeReservaLimpo = partidaService.obterNomeAtivo(reservaSelecionado);
                br.com.cana.service.JogadorService jService = new br.com.cana.service.JogadorService();
                br.com.cana.model.Jogador jogadorModel = jService.buscarPorNomeOuApelido(nomeReservaLimpo);

                String msgErro = partidaService.validarRestricaoParaArbitragem(partidaObjeto, jogadorModel);
                if (msgErro != null) {
                    JOptionPane.showMessageDialog(this, msgErro, "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 🌟 TRAVA DE SESSÃO: Captura o nome de quem está atualmente ocupando a vaga na
                // entidade
                String textoAtualEntidade = cargo.equalsIgnoreCase("BANDEIRA1") ? partidaObjeto.getBandeira1()
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? partidaObjeto.getBandeira2()
                                : partidaObjeto.getArbitro();
                String nomeAtivoAtual = partidaService.obterNomeAtivo(textoAtualEntidade);

                // 🌟 Só acumula se o nome ativo atual fizer parte do histórico que já veio
                // confirmado de fora
                String originalSessao = cargo.equalsIgnoreCase("BANDEIRA1") ? b1Original
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? b2Original : arbOriginal;

                boolean acumularHistorico = false;
                if (originalSessao != null && !originalSessao.trim().isEmpty() && !nomeAtivoAtual.isEmpty()) {
                    acumularHistorico = originalSessao.contains(nomeAtivoAtual);
                }

                // Injeta a nova lógica passando o veredito do acúmulo
                partidaService.definirArbitragemSemDuplicidade(partidaObjeto, jogadorModel, cargo, acumularHistorico);

                String textoAcumulado = cargo.equalsIgnoreCase("BANDEIRA1") ? partidaObjeto.getBandeira1()
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? partidaObjeto.getBandeira2()
                                : partidaObjeto.getArbitro();

                localLabel.setText(textoAcumulado);
                if (mainLabel != null) {
                    mainLabel.setText(prefixo + textoAcumulado);
                }

                reservaSelecionado = "";
                btnReservaAtivo = null;
            } else {
                // 🌟 LOGICA DE REMOÇÃO HÍBRIDA DA ARBITRAGEM COM LOCK DE SEGURANÇA

                // 1. Pega os dados históricos de antes da tela abrir
                String originalSessao = cargo.equalsIgnoreCase("BANDEIRA1") ? b1Original
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? b2Original : arbOriginal;

                // 2. Descobre quem é o cara ativo na entidade neste momento
                String textoAtualEntidade = cargo.equalsIgnoreCase("BANDEIRA1") ? partidaObjeto.getBandeira1()
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? partidaObjeto.getBandeira2()
                                : partidaObjeto.getArbitro();

                String nomeSaindoLimpo = partidaService.obterNomeAtivo(textoAtualEntidade);

                // 3. Valida se o cara de fato já estava escalado quando o modal abriu
                boolean gerarHistorico = false;
                if (originalSessao != null && !originalSessao.trim().isEmpty() && !nomeSaindoLimpo.isEmpty()) {
                    gerarHistorico = originalSessao.contains(nomeSaindoLimpo);
                }

                // 4. Executa a remoção no Service enviando o veredito da flag
                partidaService.removerDaArbitragem(partidaObjeto, cargo, gerarHistorico);

                // 5. Captura o resultado textual final e atualiza os componentes gráficos
                String textoResultado = cargo.equalsIgnoreCase("BANDEIRA1") ? partidaObjeto.getBandeira1()
                        : cargo.equalsIgnoreCase("BANDEIRA2") ? partidaObjeto.getBandeira2()
                                : partidaObjeto.getArbitro();

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
        if (alvo != null) {
            for (int i = 0; i < alvo.getRowCount(); i++) {
                model.addRow(new Object[] { alvo.getValueAt(i, 0), alvo.getValueAt(i, 1) });
            }
        } else {
            for (int i = 1; i <= 11; i++)
                model.addRow(new Object[] { (isAzul ? "Azul " : "Vermelho ") + i, "" + i, "LIN" });
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
                label.setBackground(Color.WHITE); // Opcional: define um fundo limpo pro header
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

        // REGRA 1: O DESCLIQUE PERFEITO (TOGGLE NATIVO)
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

        // 🌟 REGRA 2: EXCLUSIVIDADE ABSOLUTA ENTRE TIMES
        // Clicar em um jogador do time Azul apaga instantaneamente o destaque do
        // Vermelho e vice-versa
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
        return painel;
    }

    private List<String> carregarReservasDisponiveis() {
        List<String> reservasReais = new ArrayList<>();

        try {
            if (partidaObjeto != null && partidaObjeto.getListaGeralPresenca() != null) {
                for (br.com.cana.model.JogadorPartida jp : partidaObjeto.getListaGeralPresenca()) {
                    // 🛠️ FILTRO ATUALIZADO:
                    // Se o time é "Nenhum" E ele não é Staff, ele TEM que estar disponível,
                    // seja reserva desde o começo ou um titular que acabou de ser substituído.
                    if ("Nenhum".equalsIgnoreCase(jp.getTime()) && !"Staff".equalsIgnoreCase(jp.getFuncao())) {

                        br.com.cana.model.Jogador j = jp.getJogador();
                        if (j != null) {
                            String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                                    : j.getNome();
                            String posJ = (j.getPosicao() != null && !j.getPosicao().trim().isEmpty()) ? j.getPosicao()
                                    : "-";
                            reservasReais.add(nomeJ + " (" + posJ + ")");
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

            String nomeLimpo = partidaService.obterNomeAtivo(nomeReservaComPosicao);

            boolean estaAtuandoNaArbitragem = nomeLimpo
                    .equals(partidaService.obterNomeAtivo(partidaObjeto.getArbitro())) ||
                    nomeLimpo.equals(partidaService.obterNomeAtivo(partidaObjeto.getBandeira1())) ||
                    nomeLimpo.equals(partidaService.obterNomeAtivo(partidaObjeto.getBandeira2()));

            // 🛡️ TRAVA CRUCIAL: Slots vazios ficam cinzas e não aceitam clique!
            if (nomeReservaComPosicao.contains("Disponível")) {
                btnReserva.setBackground(Color.LIGHT_GRAY);
                btnReserva.setEnabled(false);
            } else if (estaAtuandoNaArbitragem) {
                // 🔏 TRAVA DA ARBITRAGEM: Continua aparecendo no pote, mas fica destacado e
                // travado para jogo
                btnReserva.setBackground(new Color(0xFFE4B5)); // Um tom de laranja/bege claro indicando atuação externa
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
                    String msgErro = partidaService.validarRestricaoParaSubstituicao(btnReserva.getText());
                    if (msgErro != null) {
                        JOptionPane.showMessageDialog(this, msgErro, "Restrição", JOptionPane.WARNING_MESSAGE);
                        tabelaAlvo.clearSelection();
                        return;
                    }

                    int linhaSel = tabelaAlvo.getSelectedRow();
                    String nomeSaindo = modelAlvo.getValueAt(linhaSel, 0).toString();
                    String nomeEntrandoRaw = btnReserva.getText();

                    String[] resultado = partidaService.processarSubstituicaoJogador(
                            nomeSaindo, nomeEntrandoRaw, modelAlvo.getValueAt(linhaSel, 1).toString());

                    // ⚡ AS LINHAS NOVAS QUE ADICIONAMOS ENTRAM BEM AQUI:
                    String timeAlvo = (rowAzul >= 0) ? "Azul" : "Vermelho";
                    String nomeEntrandoLimpo = partidaService.obterNomeAtivo(nomeEntrandoRaw);
                    partidaService.atualizarSubstituicaoNaListaPresenca(partidaObjeto, nomeSaindo, nomeEntrandoLimpo,
                            timeAlvo);

                    // Aplica as alterações visuais na tabela do Swing
                    modelAlvo.setValueAt(resultado[0], linhaSel, 0);
                    btnReserva.setText(resultado[1]);
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

        // 🛠️ FORÇAR ATUALIZAÇÃO DO LAYOUT
        painelReservas.revalidate();
        painelReservas.repaint();
    }
}