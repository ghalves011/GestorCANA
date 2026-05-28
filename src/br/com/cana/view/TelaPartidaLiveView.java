package br.com.cana.view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import br.com.cana.util.ImagemUtil;
import br.com.cana.service.PartidaService;
import java.util.List;

public class TelaPartidaLiveView extends JFrame {

    private JLabel lblPlacar;
    private JLabel lblArb, lblB1, lblB2; // 🛠️ AGORA GLOBAIS: Para a tela de trocas conseguir atualizar
    private int golsAzul = 0;
    private int golsVermelho = 0;
    private DefaultTableModel modelAzul;
    private DefaultTableModel modelVermelho;

    // 🛠️ ATRIBUTOS DE CONTROLE REAL DA ENTIDADE:
    private br.com.cana.model.Partida partidaObjeto;
    private PartidaService partidaService;
    private boolean partidaFinalizada = false;

    private boolean modoHistorico;

    private String jogadorPendenteTroca = null;
    private boolean isAzulOrigemTroca = false;
    private int linhaOrigemTroca = -1;

    private JTable tabelaAzul;
    private JTable tabelaVermelho;

    // 🛠️ CONSTRUTOR SIMPLES: Recebe apenas o nome e data para casos de teste ou
    // visualização rápida
    public TelaPartidaLiveView(br.com.cana.model.Partida partida) {
        this(partida, false);
    }

    // 🛠️ CONSTRUTOR INTEGRADO: Recebe a entidade Partida completa do Banco/Sorteio
    public TelaPartidaLiveView(br.com.cana.model.Partida partida, boolean isHistoricoView) {
        this.partidaObjeto = partida;
        this.partidaService = new br.com.cana.service.PartidaService();
        this.modoHistorico = isHistoricoView;

        // SINCRONIZAÇÃO DE PLACAR HISTÓRICO
        this.golsAzul = partida.getGolsTimeAzul();
        this.golsVermelho = partida.getGolsTimeVermelho();

        // Extrai as informações direto do objeto
        String nomePartida = partida.getNomePartida() != null ? partida.getNomePartida().toUpperCase() : "JOGO CANA";

        String devalfArb = (partida.getArbitro() != null && !partida.getArbitro().trim().isEmpty())
                ? partida.getArbitro()
                : "___________________________";
        String devalfB1 = (partida.getBandeira1() != null && !partida.getBandeira1().trim().isEmpty())
                ? partida.getBandeira1()
                : "___________________________";
        String devalfB2 = (partida.getBandeira2() != null && !partida.getBandeira2().trim().isEmpty())
                ? partida.getBandeira2()
                : "___________________________";

        String dataPartida = "S/D";
        if (partida.getDataPartida() != null) {
            java.time.format.DateTimeFormatter formatoBR = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            dataPartida = partida.getDataPartida().format(formatoBR);
        }

        String temporada = String.valueOf(partida.getTemporadaId());

        setTitle("CANA Live - " + nomePartida);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(null);
        getContentPane().setBackground(ImagemUtil.COR_FUNDO);
        ImagemUtil.configurarIcone(this);

        // --- CABEÇALHO ---
        JLabel lblTempTitle = new JLabel("Temporada:");
        lblTempTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTempTitle.setBounds(50, 20, 100, 30);
        add(lblTempTitle);

        JLabel lblTempValor = new JLabel(temporada);
        lblTempValor.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblTempValor.setBounds(145, 20, 150, 30);
        add(lblTempValor);

        ImagemUtil.LogoPainelCANA miniLogo = new ImagemUtil.LogoPainelCANA(60, 60);
        miniLogo.setBounds(470, 5, 60, 60);
        add(miniLogo);

        JLabel lblDataTitle = new JLabel("Data:");
        lblDataTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDataTitle.setBounds(750, 20, 50, 30);
        add(lblDataTitle);

        JLabel lblDataValor = new JLabel(dataPartida);
        lblDataValor.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblDataValor.setBounds(800, 20, 150, 30);
        add(lblDataValor);

        JLabel lblNomePartida = new JLabel(nomePartida.toUpperCase(), SwingConstants.CENTER);
        lblNomePartida.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblNomePartida.setForeground(new Color(0x333333));
        lblNomePartida.setBounds(250, 65, 500, 35);
        add(lblNomePartida);

        lblArb = new JLabel("Árbitro: " + devalfArb, SwingConstants.CENTER);
        lblArb.setBounds(350, 105, 300, 20);
        add(lblArb);

        lblB1 = new JLabel("Bandeira 1: " + devalfB1, SwingConstants.CENTER);
        lblB1.setBounds(350, 125, 300, 20);
        add(lblB1);

        lblB2 = new JLabel("Bandeira 2: " + devalfB2, SwingConstants.CENTER);
        lblB2.setBounds(350, 145, 300, 20);
        add(lblB2);

        // --- TIMES ---
        add(criarPainelTime(true, 30, 180, 380, 380, partida.getJogadoresAzul()));
        add(criarPainelTime(false, 590, 180, 380, 380, partida.getJogadoresVermelho()));

        // --- PLACAR ---
        lblPlacar = new JLabel(golsAzul + " x " + golsVermelho, SwingConstants.CENTER);
        lblPlacar.setFont(new Font("SansSerif", Font.BOLD, 60));
        lblPlacar.setBounds(410, 350, 180, 80);
        add(lblPlacar);

        // --- LEGENDA ---
        JLabel lblLegenda = new JLabel("<html><table border='0' cellpadding='2' cellspacing='0' align='center'><tr>"
                + "<td valign='middle'>⚽</td><td valign='middle'>Gol</td>"
                + "<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>"
                + "<td valign='middle'>⚽(C)</td><td valign='middle'>Gol Contra</td>"
                + "<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>"
                + "<td bgcolor='#FFCC00' width='8' height='10' valign='middle'><font size='1'>&nbsp;</font></td><td valign='middle'>&nbsp;Amarelo</td>"
                + "<td>&nbsp;&nbsp;&nbsp;&nbsp;</td>"
                + "<td bgcolor='#8B0000' width='8' height='10' valign='middle'><font size='1'>&nbsp;</font></td><td valign='middle'>&nbsp;Vermelho</td>"
                + "<td>&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;</td>"
                + "<td valign='middle'><i>Clique no jogador para gerenciar</i></td>"
                + "</tr></table></html>");
        lblLegenda.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblLegenda.setBounds(0, 560, 1000, 30);
        lblLegenda.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblLegenda);

        // --- BOTÕES ---
        int posYBotoes = 610;

        ImagemUtil.BotaoGradienteCANA btnSub = new ImagemUtil.BotaoGradienteCANA("SUBSTITUIÇÕES");
        btnSub.setBounds(150, posYBotoes, 200, 60);
        btnSub.addActionListener(
                e -> new TelaSubstituicaoView(partidaObjeto, modelAzul, modelVermelho, lblArb, lblB1, lblB2)
                        .setVisible(true));
        add(btnSub);

        // --- BOTÃO SÚMULA (CHAMANDO A TELA OFICIAL DO PROJETO) ---
        ImagemUtil.BotaoGradienteCANA btnSumula = new ImagemUtil.BotaoGradienteCANA("SÚMULA");
        btnSumula.setBounds(415, posYBotoes, 200, 60);
        btnSumula.addActionListener(e -> new TelaSumulaView(this, partidaObjeto, partidaFinalizada).setVisible(true));
        add(btnSumula);

        // --- BOTÃO FINALIZAR (VERSÃO CLEAN CODE) ---
        // --- BOTÃO FINALIZAR (ARQUITETURA CORRETA E LIMPA) ---
        ImagemUtil.BotaoGradienteCANA btnFinalizar = new ImagemUtil.BotaoGradienteCANA("FINALIZAR");
        btnFinalizar.setBounds(680, posYBotoes, 200, 60);

        btnFinalizar.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(this, "Deseja finalizar a partida e gravar os dados?", "Encerrar",
                    JOptionPane.YES_NO_OPTION);

            if (resp == JOptionPane.YES_OPTION) {
                // 1. A View apenas junta os dados textuais puros da tela em um Mapa
                java.util.Map<String, String> dadosDaTela = new java.util.HashMap<>();

                // 🌟 PROTEÇÃO CONTRA NULOS NO BOTÃO FINALIZAR:
                for (int i = 0; i < modelAzul.getRowCount(); i++) {
                    Object cellNome = modelAzul.getValueAt(i, 0);
                    Object cellEv = modelAzul.getValueAt(i, 2);
                    String nome = (cellNome != null) ? cellNome.toString() : "";
                    String evento = (cellEv != null) ? cellEv.toString() : "";
                    if (!nome.isEmpty())
                        dadosDaTela.put(nome, evento);
                }
                for (int i = 0; i < modelVermelho.getRowCount(); i++) {
                    Object cellNome = modelVermelho.getValueAt(i, 0);
                    Object cellEv = modelVermelho.getValueAt(i, 2);
                    String nome = (cellNome != null) ? cellNome.toString() : "";
                    String evento = (cellEv != null) ? cellEv.toString() : "";
                    if (!nome.isEmpty())
                        dadosDaTela.put(nome, evento);
                }

                // 2. Alimenta o placar básico no objeto
                partidaObjeto.setGolsTimeAzul(golsAzul);
                partidaObjeto.setGolsTimeVermelho(golsVermelho);

                // 3. Passa a responsabilidade pro Service processar os gols e salvar no SQLite
                if (partidaService.finalizarEFecharPartida(partidaObjeto, dadosDaTela)) {

                    // 1. ESCONDE O BOTÃO PARA O PRINT SAIR LIMPO
                    btnFinalizar.setVisible(false);
                    btnSub.setVisible(false);
                    btnSumula.setVisible(false);

                    // 2. TIRA O PRINT DA TELA INTEIRA
                    java.io.File arquivoPrint = br.com.cana.util.ImagemUtil
                            .tirarPrintPainel((JPanel) this.getContentPane(), "Resumo_CANA");

                    // 3. DEVOLVE O BOTÃO
                    btnFinalizar.setVisible(true);
                    btnSub.setVisible(true);
                    btnSumula.setVisible(true);

                    // 4. AVISA QUE SALVOU E JÁ PERGUNTA DO WHATSAPP
                    int opcZap = JOptionPane.showConfirmDialog(this,
                            "⚽ Partida salva com sucesso!\nDeseja compartilhar o placar no WhatsApp?",
                            "Sucesso e Compartilhamento",
                            JOptionPane.YES_NO_OPTION);

                    if (opcZap == JOptionPane.YES_OPTION && arquivoPrint != null) {

                        // Joga a imagem direto para o "Ctrl+C" / Área de Transferência do Windows!
                        br.com.cana.util.ImagemUtil.copiarParaClipboard(arquivoPrint);

                        JOptionPane.showMessageDialog(this,
                                "A imagem do placar foi copiada!\nQuando o WhatsApp abrir, basta apertar 'Ctrl + V' na conversa para colar a foto.",
                                "Copiado!", JOptionPane.INFORMATION_MESSAGE);

                        // Aqui você chama o seu utilitário atual do WhatsApp
                        // WhatsappUtil.abrirConversa(...);
                    }

                    this.dispose(); // Por fim, fecha a tela
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao salvar: Verifique a lista de presença.", "Erro",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        add(btnFinalizar);

        // TRAVA DO MODO HISTÓRICO: Esconde as ações e força a súmula em apenas
        // leitura
        if (modoHistorico) {
            this.partidaFinalizada = true; // Trava súmula para leitura

            modelAzul.setRowCount(0);
            modelVermelho.setRowCount(0);

            // Alimenta o grid com os dados mastigados pelo Service
            for (Object[] linha : partidaService.gerarLinhasGridHistorico(partidaObjeto, "Azul")) {
                modelAzul.addRow(linha);
            }

            for (Object[] linha : partidaService.gerarLinhasGridHistorico(partidaObjeto, "Vermelho")) {
                modelVermelho.addRow(linha);
            }

            // Esconde e rearranja os botões originais com segurança
            btnSub.setVisible(false);
            btnFinalizar.setVisible(false);

            // INÍCIO DA NOSSA MÁGICA DE COMPARTILHAMENTO

            // 1. Movemos o botão Súmula um pouco para a esquerda para dar espaço
            btnSumula.setBounds(265, posYBotoes, 200, 60);

            // 2. Criamos o botão do WhatsApp estilizado no padrão do seu sistema
            ImagemUtil.BotaoGradienteCANA btnCompartilharHistorico = new ImagemUtil.BotaoGradienteCANA(
                    "COMPARTILHAR PLACAR");
            btnCompartilharHistorico.setBounds(485, posYBotoes, 300, 60);

            btnCompartilharHistorico.addActionListener(evt -> {
                // Escondemos os DOIS botões para o print da tela ficar 100% focado no futebol
                btnCompartilharHistorico.setVisible(false);
                btnSumula.setVisible(false);

                // Tira a foto
                java.io.File arquivoPrint = br.com.cana.util.ImagemUtil.tirarPrintPainel((JPanel) this.getContentPane(),
                        "Historico_CANA");

                // Devolve os botões para a tela instantaneamente
                btnCompartilharHistorico.setVisible(true);
                btnSumula.setVisible(true);

                // Joga para o Ctrl+V
                if (arquivoPrint != null) {
                    br.com.cana.util.ImagemUtil.copiarParaClipboard(arquivoPrint);
                    JOptionPane.showMessageDialog(this,
                            "A imagem do placar antigo foi copiada!\nAbra a conversa no WhatsApp e aperte 'Ctrl + V'.",
                            "Copiado!", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            // 3. Adicionamos na tela
            add(btnCompartilharHistorico);

            // 4. Por fim, avisamos o usuário que ele está no modo histórico para evitar
            // confusões
        }
    }

    private JPanel criarPainelTime(boolean isAzul, int x, int y, int w, int h,
            List<br.com.cana.model.Jogador> jogadores) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBounds(x, y, w, h);
        Color corTime = isAzul ? new Color(0x1A1AFF) : new Color(0xEF3333);
        painel.setBackground(corTime);

        String[] colunas = { "Nome", "Pos", "Eventos" };
        DefaultTableModel model = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (jogadores != null) {
            boolean jaTemGoleiroNoTime = false; // Flag de controle interna do painel

            for (br.com.cana.model.Jogador j : jogadores) {
                String nomeExibir = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                        : j.getNome();

                String posBanco = j.getPosicao() != null ? j.getPosicao().toUpperCase().trim() : "";
                String posicaoEncurtada = "-";

                switch (posBanco) {
                    case "GOLEIRO":
                        // Se o time já tem um goleiro oficial no campo, o segundo vira jogador de linha
                        // ("LIN")
                        if (!jaTemGoleiroNoTime) {
                            posicaoEncurtada = "GOL";
                            jaTemGoleiroNoTime = true;
                        } else {
                            posicaoEncurtada = "LIN";
                        }
                        break;
                    case "LATERAL":
                        posicaoEncurtada = "LAT";
                        break;
                    case "ZAGUEIRO":
                        posicaoEncurtada = "ZAG";
                        break;
                    case "MEIA":
                        posicaoEncurtada = "MEI";
                        break;
                    case "ATACANTE":
                        posicaoEncurtada = "ATA";
                        break;
                    default:
                        posicaoEncurtada = posBanco;
                }

                // RECONSTRUÇÃO HISTÓRICA DE EVENTOS (Gols e Cartões)
                String eventosReconstruídos = "";
                if (partidaObjeto.getListaGeralPresenca() != null) {
                    for (br.com.cana.model.JogadorPartida jp : partidaObjeto.getListaGeralPresenca()) {
                        if (jp.getJogador() != null && jp.getJogador().getId() == j.getId()) {
                            // Loop para desenhar a quantidade exata de gols marcados
                            for (int g = 0; g < jp.getGols(); g++)
                                eventosReconstruídos += "⚽";
                            // Loop para desenhar os cartões amarelos aplicados
                            for (int a = 0; a < jp.getCartaoAmarelo(); a++)
                                eventosReconstruídos += "🟨";
                            // Loop para desenhar se houve cartão vermelho
                            for (int v = 0; v < jp.getCartaoVermelho(); v++)
                                eventosReconstruídos += "🟥";
                            break;
                        }
                    }
                }

                // Modificado para exibir as estatísticas corretas em vez de ""
                model.addRow(new Object[] { nomeExibir, posicaoEncurtada, eventosReconstruídos });
            }
        }

        if (isAzul)
            this.modelAzul = model;
        else
            this.modelVermelho = model;

        JTable tabela = new JTable(model);
        if (isAzul)
            this.tabelaAzul = tabela;
        else
            this.tabelaVermelho = tabela;

        // REGRA 1: O DESCLIQUE PERFEITO (TOGGLE NATIVO)
        // Se a linha clicada já estiver selecionada, o modelo desmarca ela na hora!
        tabela.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (isSelectedIndex(index0)) {
                    super.clearSelection();
                } else {
                    super.setSelectionInterval(index0, index1);
                }
            }
        });

        // REGRA 2: SELEÇÃO EXCLUSIVA ENTRE AS DUAS TABELAS
        // Clicar em um jogador de um time apaga INSTANTANEAMENTE o destaque do outro
        // time
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
        DefaultTableCellRenderer renderizadorHeader = (DefaultTableCellRenderer) tabela.getTableHeader()
                .getDefaultRenderer();
        renderizadorHeader.setHorizontalAlignment(JLabel.CENTER);
        tabela.setBackground(corTime);
        tabela.setFont(new Font("SansSerif", Font.BOLD, 12));
        tabela.setRowHeight(32);
        tabela.setShowGrid(false);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabela.getColumnModel().getColumn(0).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(150);

        DefaultTableCellRenderer renderizadorCentral = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setHorizontalAlignment(JLabel.CENTER);

                if (!isSelected) {
                    label.setBackground(corTime);
                    label.setForeground(Color.BLACK);
                }

                if (column == 2 && value != null) {
                    // Agora chamamos direto do nosso Utilitário visual!
                    label.setText("<html>" + br.com.cana.util.ImagemUtil.converterTokensParaHtml(value.toString())
                            + "</html>");
                }

                return label;
            }
        };

        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderizadorCentral);
        }

        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                processarClique(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                processarClique(e);
            }

            private void processarClique(MouseEvent e) {
                int row = tabela.rowAtPoint(e.getPoint());
                if (row >= 0 && row < tabela.getRowCount()) {

                    // 🌟 SÓ FORÇA A SELEÇÃO MANUAL SE FOR BOTÃO DIREITO (POPUP)
                    // O botão esquerdo comum a gente deixa o Swing cuidar sozinho agora!
                    if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
                        tabela.setRowSelectionInterval(row, row);
                    }

                    String nomeJogador = model.getValueAt(row, 0).toString();
                    String eventosAtuais = model.getValueAt(row, 2).toString();

                    PartidaService service = new PartidaService();
                    int amAtivo = service.contarAmarelosDoJogadorAtivo(eventosAtuais);
                    boolean isExpulso = eventosAtuais.contains("🟥");

                    // 🌟 CAPTURA O ESTADO REAL DA TELA EM TEMPO REAL:
                    java.util.List<String> todosEventos = new java.util.ArrayList<>();
                    for (int i = 0; i < modelAzul.getRowCount(); i++) {
                        Object ev = modelAzul.getValueAt(i, 2);
                        todosEventos.add(ev != null ? ev.toString() : "");
                    }
                    for (int i = 0; i < modelVermelho.getRowCount(); i++) {
                        Object ev = modelVermelho.getValueAt(i, 2);
                        todosEventos.add(ev != null ? ev.toString() : "");
                    }

                    // O Service agora avalia os textos e decide se bloqueia ou não!
                    final boolean temEventosNaPartida = service.temEventosNaTela(todosEventos);

                    // 🔄 LÓGICA DO CLIQUE CASADO: Intercepta o clique comum (Esquerdo) para fechar
                    // a inversão
                    if (jogadorPendenteTroca != null && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                        if (isAzul == isAzulOrigemTroca) {
                            jogadorPendenteTroca = null;
                            JOptionPane.showMessageDialog(TelaPartidaLiveView.this,
                                    "Inversão manual cancelada (clique feito no mesmo time).");
                            return;
                        }

                        if (temEventosNaPartida) {
                            JOptionPane.showMessageDialog(TelaPartidaLiveView.this,
                                    "A partida já possui eventos! Ajustes manuais bloqueados.", "Aviso",
                                    JOptionPane.WARNING_MESSAGE);
                            jogadorPendenteTroca = null;
                            return;
                        }

                        // Executa a inversão direta na entidade (Service)
                        boolean sucesso = service.permutarJogadoresEntreTimesManual(partidaObjeto, jogadorPendenteTroca,
                                nomeJogador, temEventosNaPartida);
                        if (sucesso) {
                            DefaultTableModel modelOrigem = isAzulOrigemTroca ? modelAzul : modelVermelho;

                            // Captura as informações originais da origem (Jogador A)
                            Object nomeA = modelOrigem.getValueAt(linhaOrigemTroca, 0);
                            Object numA = modelOrigem.getValueAt(linhaOrigemTroca, 1);
                            Object posA = modelOrigem.getValueAt(linhaOrigemTroca, 2);

                            // Seta os dados do Jogador B na linha da Origem A
                            modelOrigem.setValueAt(model.getValueAt(row, 0), linhaOrigemTroca, 0);
                            modelOrigem.setValueAt(model.getValueAt(row, 1), linhaOrigemTroca, 1);
                            modelOrigem.setValueAt(model.getValueAt(row, 2), linhaOrigemTroca, 2);

                            // Seta os dados originais do Jogador A na linha do Destino B
                            model.setValueAt(nomeA, row, 0);
                            model.setValueAt(numA, row, 1);
                            model.setValueAt(posA, row, 2);

                            JOptionPane.showMessageDialog(TelaPartidaLiveView.this,
                                    "Inversão realizada com sucesso mantendo as linhas!");
                        }
                        jogadorPendenteTroca = null; // Reseta o fluxo
                        return; // Interrompe para não abrir o menu popup de gols/cartões
                    }

                    // --- TRATAMENTO DO MENU POPUP (Abre com o botão direito do mouse) ---
                    if (e.isPopupTrigger() || (e.getClickCount() == 1 && SwingUtilities.isRightMouseButton(e))) {
                        JPopupMenu menu = new JPopupMenu();

                        // 🧤 REGRA DE NEGÓCIO: IMPROVISO DE GOLEIRO DA VÁRZEA (CANA)
                        boolean temGoleiroNatural = partidaService.temGoleiroNaturalNoTime(partidaObjeto, isAzul);
                        String posAtual = model.getValueAt(row, 2).toString();

                        if (temGoleiroNatural) {
                            JMenuItem itemBloqueado = new JMenuItem("❌ Bloqueado: Time possui Goleiro Oficial");
                            itemBloqueado.setEnabled(false);
                            menu.add(itemBloqueado);
                            menu.add(new JSeparator());
                        } else {
                            if ("GOL".equals(posAtual)) {
                                JMenuItem itemVoltarLinha = new JMenuItem("↩️ Destituir do Gol (Voltar para Origem)");
                                itemVoltarLinha.addActionListener(al -> {
                                    // 🌟 Recupera a posição real dele e aplica a reordenação automática
                                    String siglaOriginal = partidaService.obterSiglaPosicaoOriginal(partidaObjeto,
                                            nomeJogador);
                                    model.setValueAt(siglaOriginal, row, 2);
                                    reordenarTabelaTaticamente(model);
                                });
                                menu.add(itemVoltarLinha);
                                menu.add(new JSeparator());
                            } else {
                                java.util.List<String> posicoesTela = new java.util.ArrayList<>();
                                for (int i = 0; i < model.getRowCount(); i++)
                                    posicoesTela.add(model.getValueAt(i, 2).toString());
                                boolean jaTemGoleiro = partidaService.jaTemAlguemNoGol(posicoesTela);

                                JMenuItem itemImprovisar = new JMenuItem("🧤 Improvisar como Goleiro");
                                if (jaTemGoleiro) {
                                    itemImprovisar.setEnabled(false);
                                    itemImprovisar.setText("❌ Bloqueado: Já existe um goleiro em campo");
                                } else {
                                    itemImprovisar.addActionListener(al -> {
                                        // 1. Chama o Service para garantir que a Súmula seja alterada na memória real
                                        boolean sucessoNaMemoria = service.improvisarGoleiroTemporario(partidaObjeto,
                                                nomeJogador);

                                        if (sucessoNaMemoria) {
                                            // 2. 🌟 Seta a flag GOL na COLUNA 1 (Posição), não na 2 (Eventos)!
                                            model.setValueAt("GOL", row, 1);

                                            // 3. Empurra o Goleiro pro topo da tabela atual!
                                            reordenarTabelaTaticamente(model);

                                            JOptionPane.showMessageDialog(TelaPartidaLiveView.this,
                                                    "Jogador " + nomeJogador + " improvisado como goleiro com sucesso!",
                                                    "Improviso Concluído", JOptionPane.INFORMATION_MESSAGE);
                                        } else {
                                            JOptionPane.showMessageDialog(TelaPartidaLiveView.this,
                                                    "Erro ao improvisar goleiro na memória.",
                                                    "Erro Interno", JOptionPane.ERROR_MESSAGE);
                                        }
                                    });
                                }
                                menu.add(itemImprovisar);
                                menu.add(new JSeparator());
                            }
                        }

                        // --- OPÇÕES DE ADIÇÃO ---
                        JMenuItem itemGol = new JMenuItem("⚽ Registrar Gol");
                        itemGol.setEnabled(!isExpulso);
                        itemGol.addActionListener(al -> {
                            String novoEv = service.adicionarEventoAoJogador(eventosAtuais, "⚽");
                            model.setValueAt(novoEv, row, 2);
                            if (isAzul)
                                golsAzul++;
                            else
                                golsVermelho++;
                            lblPlacar.setText(golsAzul + " x " + golsVermelho);

                            tabelaAzul.clearSelection();
                            tabelaVermelho.clearSelection();
                        });

                        JMenuItem itemGolContra = new JMenuItem("⚽(C) Registrar Gol Contra");
                        itemGolContra.setEnabled(!isExpulso);
                        itemGolContra.addActionListener(al -> {
                            String novoEv = service.adicionarEventoAoJogador(eventosAtuais, "⚽(C)");
                            model.setValueAt(novoEv, row, 2);
                            if (isAzul)
                                golsVermelho++;
                            else
                                golsAzul++;
                            lblPlacar.setText(golsAzul + " x " + golsVermelho);

                            tabelaAzul.clearSelection();
                            tabelaVermelho.clearSelection();
                        });

                        JMenuItem itemAmarelo = new JMenuItem(
                                "<html><font color='#FFCC00'>█</font> <font color='#333333'>Cartão Amarelo</font></html>");
                        itemAmarelo.setEnabled(!isExpulso);
                        itemAmarelo.addActionListener(al -> {
                            if (amAtivo == 1) {
                                String evFinal = service.adicionarEventoAoJogador(eventosAtuais, "🟨");
                                evFinal = service.adicionarEventoAoJogador(evFinal, "🟥");
                                model.setValueAt(evFinal, row, 2);
                                JOptionPane.showMessageDialog(TelaPartidaLiveView.this,
                                        "2º Cartão Amarelo! O jogador '" + nomeJogador + "' foi expulso da partida.",
                                        "🟥 Expulsão por Acúmulo", JOptionPane.WARNING_MESSAGE);
                            } else {
                                String novoEv = service.adicionarEventoAoJogador(eventosAtuais, "🟨");
                                model.setValueAt(novoEv, row, 2);
                            }

                            tabelaAzul.clearSelection();
                            tabelaVermelho.clearSelection();
                        });

                        JMenuItem itemVermelho = new JMenuItem(
                                "<html><font color='#8B0000'>█</font> <font color='#333333'>Cartão Vermelho Direto</font></html>");
                        itemVermelho.setEnabled(!isExpulso);
                        itemVermelho.addActionListener(al -> {
                            String novoEv = service.adicionarEventoAoJogador(eventosAtuais, "🟥");
                            model.setValueAt(novoEv, row, 2);
                            JOptionPane.showMessageDialog(TelaPartidaLiveView.this,
                                    "Cartão Vermelho Direto! O jogador '" + nomeJogador + "' foi expulso.",
                                    "🟥 Expulsão Direta", JOptionPane.WARNING_MESSAGE);

                            tabelaAzul.clearSelection();
                            tabelaVermelho.clearSelection();
                        });

                        menu.add(itemGol);
                        menu.add(itemGolContra);
                        menu.add(itemAmarelo);
                        menu.add(itemVermelho);

                        // --- OPÇÕES DE REMOÇÃO CIRÚRGICA ---
                        int gAtivo = service.contarTokensNoBlocoAtivo(eventosAtuais, "⚽");
                        int gcAtivo = service.contarTokensNoBlocoAtivo(eventosAtuais, "⚽(C)");
                        int amTotalAtivo = service.contarTokensNoBlocoAtivo(eventosAtuais, "🟨");
                        int vmAtivo = service.contarTokensNoBlocoAtivo(eventosAtuais, "🟥");

                        if (gAtivo > 0 || gcAtivo > 0 || amTotalAtivo > 0 || vmAtivo > 0) {
                            menu.add(new JSeparator());

                            if (gAtivo > 0) {
                                JMenuItem remGol = new JMenuItem("❌ Remover 1 Gol");
                                remGol.addActionListener(al -> {
                                    String novoEv = service.removerEventoDoJogador(eventosAtuais, "⚽");
                                    model.setValueAt(novoEv, row, 2);
                                    if (isAzul)
                                        golsAzul--;
                                    else
                                        golsVermelho--;
                                    salvaguardarPlacar();
                                });
                                menu.add(remGol);
                            }

                            if (gcAtivo > 0) {
                                JMenuItem remGolContra = new JMenuItem("❌ Remover 1 Gol Contra");
                                remGolContra.addActionListener(al -> {
                                    String novoEv = service.removerEventoDoJogador(eventosAtuais, "⚽(C)");
                                    model.setValueAt(novoEv, row, 2);
                                    if (isAzul)
                                        golsVermelho--;
                                    else
                                        golsAzul--;
                                    salvaguardarPlacar();
                                });
                                menu.add(remGolContra);
                            }

                            if (amTotalAtivo == 2 && vmAtivo == 1) {
                                JMenuItem remDuplo = new JMenuItem("❌ Anular 2º Amarelo / Trazer de Volta");
                                remDuplo.addActionListener(al -> {
                                    String novoEv = service.removerEventoDoJogador(eventosAtuais, "🟥");
                                    novoEv = service.removerEventoDoJogador(novoEv, "🟨");
                                    model.setValueAt(novoEv, row, 2);
                                });
                                menu.add(remDuplo);
                            } else {
                                if (vmAtivo == 1) {
                                    JMenuItem remVermelho = new JMenuItem(
                                            "❌ Remover Vermelho Direto / Trazer de Volta");
                                    remVermelho.addActionListener(al -> {
                                        String novoEv = service.removerEventoDoJogador(eventosAtuais, "🟥");
                                        model.setValueAt(novoEv, row, 2);
                                    });
                                    menu.add(remVermelho);
                                }
                                if (amTotalAtivo == 1) {
                                    JMenuItem remAmarelo = new JMenuItem("❌ Remover 1 Cartão Amarelo");
                                    remAmarelo.addActionListener(al -> {
                                        String novoEv = service.removerEventoDoJogador(eventosAtuais, "🟨");
                                        model.setValueAt(novoEv, row, 2);
                                    });
                                    menu.add(remAmarelo);
                                }
                            }
                        }

                        // --- ITEM DO POPUP: ATIVA O MODO DE INVERSÃO ---
                        JMenuItem itemTrocarTime = new JMenuItem("🔄 Inverter Posição com Adversário...");
                        itemTrocarTime.setEnabled(!temEventosNaPartida);
                        itemTrocarTime.addActionListener(al -> {
                            jogadorPendenteTroca = nomeJogador;
                            isAzulOrigemTroca = isAzul;
                            linhaOrigemTroca = row;
                            JOptionPane.showMessageDialog(TelaPartidaLiveView.this,
                                    "Modo inversão ativado para: " + nomeJogador
                                            + "\nAgora basta fechar este menu e dar um clique COMUM (esquerdo) no jogador do outro time.");
                        });
                        menu.add(new JSeparator());
                        menu.add(itemTrocarTime);

                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            private void salvaguardarPlacar() {
                if (golsAzul < 0)
                    golsAzul = 0;
                if (golsVermelho < 0)
                    golsVermelho = 0;
                lblPlacar.setText(golsAzul + " x " + golsVermelho);
            }
        });

        JScrollPane scroll = new JScrollPane(tabela);
        scroll.getViewport().setBackground(corTime);
        scroll.setBorder(null);
        painel.add(scroll, BorderLayout.CENTER);

        return painel;
    }

    /**
     * Utilitário Visual: Organiza as linhas do grid automaticamente seguindo a
     * hierarquia tática do CANA.
     */
    private void reordenarTabelaTaticamente(DefaultTableModel model) {
        List<Object[]> linhas = new java.util.ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            linhas.add(new Object[] {
                    model.getValueAt(i, 0),
                    model.getValueAt(i, 1),
                    model.getValueAt(i, 2)
            });
        }

        java.util.Map<String, Integer> pesos = java.util.Map.of(
                "GOL", 1, "LAT", 2, "ZAG", 3, "MEI", 4, "ATA", 5);

        linhas.sort((o1, o2) -> {
            // Pegamos o peso pela Posição (que agora está no índice 1)
            int p1 = pesos.getOrDefault(o1[1].toString(), 6);
            int p2 = pesos.getOrDefault(o2[1].toString(), 6);

            // Se as posições forem diferentes, ordena pela posição
            if (p1 != p2) {
                return Integer.compare(p1, p2);
            }
            // Se forem a mesma posição, mantém a ordem original (ou pode ordenar por nome
            // se quiser)
            return 0;
        });

        model.setRowCount(0);
        for (Object[] linha : linhas) {
            model.addRow(linha);
        }
    }
}