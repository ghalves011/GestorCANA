package br.com.cana.view;

import br.com.cana.model.Endereco;
import br.com.cana.model.Jogador;
import br.com.cana.service.JogadorService;
import br.com.cana.util.*;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;

public class FormJogadorView extends JFrame {

    private JogadorService service = new JogadorService();
    private Jogador jogadorAtual = new Jogador();

    // Toolbar
    private JButton btnGravar, btnCancelar, btnAlterar, btnNovo, btnExcluir, btnPesquisa;

    // Aba 1 - Infos Pessoais
    private JTextField txtNome, txtApelido, txtLogradouro, txtNum, txtComplemento, txtBairro, txtCidade, txtUF;
    private JFormattedTextField txtDataNasc, txtCPF, txtRG, txtTelefone, txtTelEmergencia, txtCEP;

    // Aba 2 - Infos Esportivas
    private JComboBox<String> cbPosicao, cbPeDominante;
    private JTextField txtTimeAnterior;
    private JSpinner spNivel, spAltura, spPeso, spNumCamisa, spTempoExp;
    private JComboBox<Jogador> cbPadrinho;
    private JTextField txtGrauRelacao;
    private JFormattedTextField txtDataAdmissao;

    public FormJogadorView() {
        setTitle("Gestor CANA - Cadastro de Jogador");
        ImagemUtil.configurarIcone(this);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        configurarToolbar();

        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));
        UIManager.put("ComboBox.focus", new Color(0, 0, 0, 0));
        UIManager.put("CheckBox.focus", new Color(0, 0, 0, 0));
        UIManager.put("RadioButton.focus", new Color(0, 0, 0, 0));
        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Infos Pessoais", criarPainelPessoais());
        abas.addTab("Infos Esportivas", criarPainelEsportivas());

        add(abas, BorderLayout.CENTER);

        bloquearCampos(true);
    }

    private void configurarToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBackground(new Color(230, 230, 230));

        btnGravar = criarBotaoToolbar("Gravar");
        btnCancelar = criarBotaoToolbar("Cancelar");
        btnAlterar = criarBotaoToolbar("Alterar");
        btnNovo = criarBotaoToolbar("Novo");
        btnExcluir = criarBotaoToolbar("Excluir");
        btnPesquisa = criarBotaoToolbar("Pesquisa");

        toolbar.add(btnGravar);
        toolbar.add(btnCancelar);
        toolbar.add(btnAlterar);
        toolbar.add(btnNovo);
        toolbar.add(btnExcluir);
        toolbar.add(btnPesquisa);

        add(toolbar, BorderLayout.NORTH);

        // Ações
        btnNovo.addActionListener(e -> acaoNovo());
        btnGravar.addActionListener(e -> acaoSalvar());
        btnPesquisa.addActionListener(e -> acaoPesquisar());
        btnAlterar.addActionListener(e -> bloquearCampos(false));
        btnCancelar.addActionListener(e -> bloquearCampos(true));
        btnExcluir.addActionListener(e -> acaoExcluir());
    }

    private JPanel criarPainelPessoais() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 2, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Linha 1: Nome, Apelido, Data Nasc
        txtNome = new JTextField(30);
        txtApelido = new JTextField(15);
        txtDataNasc = criarCampoFormatado("##/##/####");
        txtDataAdmissao = criarCampoFormatado("##/##/####");

        adicionarCampo(p, "Nome", txtNome, gbc, 0, 0, 2);
        adicionarCampo(p, "Apelido", txtApelido, gbc, 0, 2, 1);
        adicionarCampo(p, "Data de nasc.", txtDataNasc, gbc, 0, 3, 1);

        // Linha 2: CPF, RG, Telefone, Emergência
        txtCPF = criarCampoFormatado("###.###.###-##");
        txtRG = new JFormattedTextField();
        txtTelefone = criarCampoFormatado("(##) #####-####");
        txtTelEmergencia = criarCampoFormatado("(##) #####-####");

        adicionarCampo(p, "CPF", txtCPF, gbc, 1, 0, 1);
        adicionarCampo(p, "RG", txtRG, gbc, 1, 1, 1);
        adicionarCampo(p, "Telefone/cel", txtTelefone, gbc, 1, 2, 1);
        adicionarCampo(p, "Telefone emergência", txtTelEmergencia, gbc, 1, 3, 1);

        // Separador Endereço
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        p.add(new JLabel("<html><br><b>Endereço residencial:</b><br></html>"), gbc);

        // Linha 3: CEP, Logradouro, Num, Complemento
        txtCEP = criarCampoFormatado("#####-###");
        txtCEP.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                buscarCepNaInternet();
            }
        });
        txtLogradouro = new JTextField();
        txtNum = new JTextField(5);
        txtComplemento = new JTextField();

        adicionarCampo(p, "CEP", txtCEP, gbc, 3, 0, 1);
        adicionarCampo(p, "Logradouro", txtLogradouro, gbc, 3, 1, 1);
        adicionarCampo(p, "Num", txtNum, gbc, 3, 2, 1);
        adicionarCampo(p, "Complemento", txtComplemento, gbc, 3, 3, 1);

        // Linha 4: Bairro, Cidade, UF
        txtBairro = new JTextField();
        txtCidade = new JTextField();
        txtUF = criarCampoFormatado("UU");

        adicionarCampo(p, "Bairro", txtBairro, gbc, 4, 0, 1);
        adicionarCampo(p, "Cidade", txtCidade, gbc, 4, 1, 2);
        adicionarCampo(p, "UF", txtUF, gbc, 4, 3, 1);

        return p;
    }

    private JPanel criarPainelEsportivas() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 10, 2, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbPosicao = new JComboBox<>(new String[] { "Goleiro", "Zagueiro", "Lateral", "Meia", "Atacante" });
        cbPosicao.setFocusable(false);
        cbPeDominante = new JComboBox<>(new String[] { "Destro", "Canhoto", "Ambidestro" });
        cbPeDominante.setFocusable(false);
        spAltura = new JSpinner(new SpinnerNumberModel(1.75, 0.50, 2.50, 0.01));
        spPeso = new JSpinner(new SpinnerNumberModel(75.0, 30.0, 200.0, 0.1));

        cbPadrinho = new JComboBox<>();
        txtGrauRelacao = new JTextField();
        carregarPadrinhos();

        adicionarCampo(p, "Posição", cbPosicao, gbc, 0, 0, 1);
        adicionarCampo(p, "Pé dominante", cbPeDominante, gbc, 0, 1, 1);
        adicionarCampo(p, "Altura (m)", spAltura, gbc, 0, 2, 1);
        adicionarCampo(p, "Peso (kg)", spPeso, gbc, 0, 3, 1);
        adicionarCampo(p, "Padrinho (Membro do CANA)", cbPadrinho, gbc, 2, 0, 2);
        adicionarCampo(p, "Grau de relação", txtGrauRelacao, gbc, 2, 2, 1);
        adicionarCampo(p, "Data de admissão", txtDataAdmissao, gbc, 2, 3, 1);

        spNivel = new JSpinner(new SpinnerNumberModel(50, 1, 100, 1));
        txtTimeAnterior = new JTextField();
        spNumCamisa = new JSpinner(new SpinnerNumberModel(10, 1, 999, 1));
        spTempoExp = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));

        adicionarCampo(p, "Nível (1-100)", spNivel, gbc, 1, 0, 1);
        adicionarCampo(p, "Time anterior", txtTimeAnterior, gbc, 1, 1, 1);
        adicionarCampo(p, "Num da camisa", spNumCamisa, gbc, 1, 2, 1);
        adicionarCampo(p, "Tempo experiência (anos)", spTempoExp, gbc, 1, 3, 1);

        return p;
    }

    // --- Lógica de Ações ---

    private void acaoNovo() {
        jogadorAtual = new Jogador();
        jogadorAtual.setEndereco(new Endereco());
        limparCampos();
        carregarPadrinhos();
        bloquearCampos(false);
        txtNome.requestFocus();
    }

    private void acaoSalvar() {

        btnGravar.requestFocusInWindow();

        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome do jogador é obrigatório.", "Erro de Validação",
                    JOptionPane.ERROR_MESSAGE);
            txtNome.requestFocus();
            return;
        }

        Jogador padrinho = (Jogador) cbPadrinho.getSelectedItem();

        if (jogadorAtual.getId() > 0 && padrinho != null && padrinho.getId() == jogadorAtual.getId()) {
            JOptionPane.showMessageDialog(this, "Um jogador não pode ser seu próprio padrinho!", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        popularObjeto();

        System.out.println("----- DEBUG DO SALVAR -----");
        System.out.println("O que o Java leu da tela: " + jogadorAtual.getNumCamisa());
        System.out.println("---------------------------");

        jogadorAtual.setDataNascimento(br.com.cana.util.DateUtil.ler(txtDataNasc.getText()));
        jogadorAtual.setDataAdmissao(br.com.cana.util.DateUtil.ler(txtDataAdmissao.getText()));

        String resultado = service.salvarJogador(jogadorAtual);

        if ("OK".equals(resultado)) {
            JOptionPane.showMessageDialog(this, "Jogador cadastrado com sucesso!");

            limparCampos();
            jogadorAtual = new Jogador();
            jogadorAtual.setEndereco(new br.com.cana.model.Endereco());

            bloquearCampos(true);
            carregarPadrinhos();
        } else {
            JOptionPane.showMessageDialog(this, resultado, "Aviso", JOptionPane.WARNING_MESSAGE);
        }

    }

    private void acaoPesquisar() {
        DialogBuscaView dialog = new DialogBuscaView(this);
        dialog.setVisible(true);

        Jogador escolhido = dialog.getJogadorSelecionado();
        if (escolhido != null) {
            this.jogadorAtual = escolhido;
            carregarPadrinhos();
            preencherCampos();
            bloquearCampos(false);
        }
    }

    private void acaoExcluir() {
        if (jogadorAtual.getId() == 0) {
            JOptionPane.showMessageDialog(this, "Selecione um jogador para excluir.");
            return;
        }

        int confirma = JOptionPane.showConfirmDialog(this, "Deseja realmente excluir " + jogadorAtual.getNome() + "?",
                "Confirmação", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            String res = service.excluir(jogadorAtual.getId());
            if ("OK".equals(res)) {
                limparCampos();
                bloquearCampos(true);
                JOptionPane.showMessageDialog(this, "Excluído com sucesso!");
            }
        }
    }

    private void popularObjeto() {
        // 1. DADOS PESSOAIS BÁSICOS
        jogadorAtual.setNome(txtNome.getText().trim());
        jogadorAtual.setApelido(txtApelido.getText().trim());
        jogadorAtual.setDataNascimento(DateUtil.ler(txtDataNasc.getText()));
        jogadorAtual.setCpf(TextoUtil.apenasNumeros(txtCPF.getText()));
        jogadorAtual.setRg(txtRG.getText().trim());
        jogadorAtual.setTelefone(txtTelefone.getText());
        jogadorAtual.setTelefoneEmergencia(txtTelEmergencia.getText());

        // 2. ENDEREÇO (Acessando o objeto interno)
        Endereco end = jogadorAtual.getEndereco();
        if (end == null) {
            end = new Endereco();
            jogadorAtual.setEndereco(end);
        }
        end.setCep(txtCEP.getText());
        end.setLogradouro(txtLogradouro.getText().trim());
        end.setNumero(txtNum.getText().trim());
        end.setComplemento(txtComplemento.getText().trim());
        end.setBairro(txtBairro.getText().trim());
        end.setCidade(txtCidade.getText().trim());
        end.setEstado(txtUF.getText().trim());

        // 3. INFORMAÇÕES ESPORTIVAS (ComboBoxes)
        jogadorAtual.setPosicao(cbPosicao.getSelectedItem().toString());
        jogadorAtual.setPeDominante(cbPeDominante.getSelectedItem().toString());
        jogadorAtual.setTimeAnterior(txtTimeAnterior.getText().trim());

        // 4. VALIDAÇÃO DOS SPINNERS (O "Pulo do Gato" para não travar no número 11)
        try {
            spAltura.commitEdit();
            spPeso.commitEdit();
            spNivel.commitEdit();
            spNumCamisa.commitEdit();
            spTempoExp.commitEdit();
        } catch (java.text.ParseException e) {
            // Se o usuário digitar algo inválido, o Java ignora e mantém o último valor
            // válido
            System.err.println("Aviso: Erro na validação manual dos Spinners.");
        }

        // Agora sim, pega os valores (se o usuário digitou algo inválido, ele vai usar o último valor válido, evitando travar a tela)
        jogadorAtual.setAltura((double) spAltura.getValue());
        jogadorAtual.setPeso((double) spPeso.getValue());
        jogadorAtual.setNivel((int) spNivel.getValue());
        jogadorAtual.setNumCamisa((int) spNumCamisa.getValue());
        jogadorAtual.setTempoExperiencia((int) spTempoExp.getValue());

        // 5. REGRA DO PADRINHO
        Jogador padrinhoSelecionado = (Jogador) cbPadrinho.getSelectedItem();
        if (padrinhoSelecionado != null && padrinhoSelecionado.getId() > 0) {
            jogadorAtual.setPadrinhoId(padrinhoSelecionado.getId());
        } else {
            jogadorAtual.setPadrinhoId(null); // Usar null se não houver padrinho
        }
        jogadorAtual.setGrauRelacaoPadrinho(txtGrauRelacao.getText().trim());
    }

    private void preencherCampos() {
        txtNome.setText(jogadorAtual.getNome());
        txtApelido.setText(jogadorAtual.getApelido());
        txtDataNasc.setText(br.com.cana.util.DateUtil.paraUsuario(jogadorAtual.getDataNascimento()));
        txtDataAdmissao.setText(br.com.cana.util.DateUtil.paraUsuario(jogadorAtual.getDataAdmissao()));
        txtCPF.setText(jogadorAtual.getCpf());
        txtRG.setText(jogadorAtual.getRg());
        txtTelefone.setValue(jogadorAtual.getTelefone());
        txtTelEmergencia.setValue(jogadorAtual.getTelefoneEmergencia());

        Endereco end = jogadorAtual.getEndereco();
        if (end != null) {
            txtCEP.setValue(end.getCep());
            txtLogradouro.setText(end.getLogradouro());
            txtNum.setText(end.getNumero());
            txtComplemento.setText(end.getComplemento());
            txtBairro.setText(end.getBairro());
            txtCidade.setText(end.getCidade());
            txtUF.setText(end.getEstado());
        }

        cbPosicao.setSelectedItem(jogadorAtual.getPosicao());
        cbPeDominante.setSelectedItem(jogadorAtual.getPeDominante());
        spAltura.setValue(jogadorAtual.getAltura());
        spPeso.setValue(jogadorAtual.getPeso());
        spNivel.setValue(jogadorAtual.getNivel());
        txtTimeAnterior.setText(jogadorAtual.getTimeAnterior());
        spNumCamisa.setValue(jogadorAtual.getNumCamisa());
        spTempoExp.setValue(jogadorAtual.getTempoExperiencia());
        txtGrauRelacao.setText(jogadorAtual.getGrauRelacaoPadrinho());

        if (jogadorAtual.getPadrinhoId() != null && jogadorAtual.getPadrinhoId() > 0) {
            for (int i = 0; i < cbPadrinho.getItemCount(); i++) {
                Jogador p = cbPadrinho.getItemAt(i);

                if (p != null && p.getId() == jogadorAtual.getPadrinhoId().intValue()) {
                    cbPadrinho.setSelectedIndex(i);
                    System.out.println("DEBUG: Padrinho selecionado na pesquisa: " + p.getNome());
                    break;
                }
            }
        } else {
            cbPadrinho.setSelectedIndex(0);
        }
    }

    // --- Métodos Auxiliares ---

    private void adicionarCampo(JPanel p, String label, JComponent comp, GridBagConstraints gbc, int y, int x,
            int width) {
        // --- 1. CONFIGURAÇÃO DO TÍTULO (LABEL) ---
        gbc.gridy = y * 2;
        gbc.gridx = x;
        gbc.gridwidth = width;
        gbc.weightx = 1.0; // Faz o campo esticar para ocupar a largura da tela

        // Insets: (topo, esquerda, baixo, direita)
        // Deixamos apenas 2 pixels de margem embaixo para "colar" no campo
        gbc.insets = new Insets(12, 10, 1, 10);

        p.add(new JLabel(label), gbc);

        // --- 2. CONFIGURAÇÃO DO CAMPO (TEXTFIELD / COMBO) ---
        gbc.gridy = (y * 2) + 1;

        // Zeramos a margem de cima (0) para não ter espaço vindo do campo
        gbc.insets = new Insets(0, 10, 12, 10);

        // Deixa o campo mais alto/robusto (preenche mais a tela verticalmente)
        gbc.ipady = 12;

        p.add(comp, gbc);

        // Reset importante para o próximo campo não herdar a altura extra
        gbc.ipady = 0;
    }

    private JButton criarBotaoToolbar(String texto) {
        JButton btn = new JButton(texto);
        btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        btn.setHorizontalTextPosition(SwingConstants.CENTER);
        btn.setFocusable(false);
        return btn;
    }

    private JFormattedTextField criarCampoFormatado(String mascara) {
        try {
            MaskFormatter mf = new MaskFormatter(mascara);
            mf.setPlaceholderCharacter('_');
            return new JFormattedTextField(mf);
        } catch (Exception e) {
            return new JFormattedTextField();
        }
    }

    private void bloquearCampos(boolean bloquear) {
        // Percorre todos os componentes dentro do JTabbedPane (ou do JFrame)
        configurarEstadoComponentes(getContentPane(), !bloquear);

        // Regra específica para os botões da Toolbar
        btnGravar.setEnabled(!bloquear);
        btnCancelar.setEnabled(!bloquear);
        btnAlterar.setEnabled(bloquear); // Só altera se estiver bloqueado (modo visualização)
        btnNovo.setEnabled(bloquear);
        btnPesquisa.setEnabled(bloquear);
        btnExcluir.setEnabled(bloquear);
    }

    // Método recursivo auxiliar para não repetir código
    private void configurarEstadoComponentes(Container container, boolean estado) {
        for (Component c : container.getComponents()) {
            if (c instanceof JTextField || c instanceof JComboBox || c instanceof JSpinner
                    || c instanceof JFormattedTextField) {
                c.setEnabled(estado);
            } else if (c instanceof Container) {
                // Se for um painel, chama o método para os filhos dele
                configurarEstadoComponentes((Container) c, estado);
            }
        }
    }

    private void limparCampos() {
        // Chama a função passando o container principal da tela
        limparRecursivo(getContentPane());
    }

    private void limparRecursivo(Container container) {
        for (Component c : container.getComponents()) {
            // Limpa campos de texto e campos formatados (CPF, RG, etc)
            if (c instanceof JTextField) {
                ((JTextField) c).setText("");
            }
            // Reseta as seleções dos ComboBox (Posição, Pé Dominante)
            else if (c instanceof JComboBox) {
                ((JComboBox<?>) c).setSelectedIndex(0);
            }
            // Reseta os Spinners (Nível, Peso, Altura) para o valor mínimo
            else if (c instanceof JSpinner) {
                JSpinner spinner = (JSpinner) c;
                spinner.setValue(((SpinnerNumberModel) spinner.getModel()).getMinimum());
            }

            // Se encontrar outro painel ou aba dentro da tela, entra nele também
            else if (c instanceof Container) {
                limparRecursivo((Container) c);
            }
        }
    }

    private void carregarPadrinhos() {
        cbPadrinho.removeAllItems();

        // Adiciona uma opção "Nenhum" no topo
        Jogador nenhum = new Jogador();
        nenhum.setId(0);
        nenhum.setNome(" - Selecione um Padrinho (Opcional) - ");
        cbPadrinho.addItem(nenhum);

        // Busca todos os jogadores via Service
        java.util.List<Jogador> lista = service.listarTodos();
        for (Jogador j : lista) {
            // Regra: só adiciona se o jogador não for o próprio que está sendo editado
            if (jogadorAtual.getId() == 0 || j.getId() != jogadorAtual.getId()) {
                cbPadrinho.addItem(j);
            }
        }

        cbPadrinho.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Jogador) {
                    setText(((Jogador) value).getNome());
                }
                return this;
            }
        });
    }

    private void buscarCepNaInternet() {
        // Pega o texto e limpa a máscara
        String cep = txtCEP.getText().replaceAll("[^0-9]", ""); 
        
        if (cep.length() == 8) {
            // Executa em uma Thread separada para a tela não dar uma "travadinha" enquanto a internet carrega
            new Thread(() -> {
                java.util.Map<String, String> dados = br.com.cana.util.CepUtil.buscar(cep);
                
                if (!dados.isEmpty()) {
                    // O Swing exige que a atualização visual seja feita na Thread principal
                    SwingUtilities.invokeLater(() -> {
                        txtLogradouro.setText(dados.get("logradouro"));
                        txtBairro.setText(dados.get("bairro"));
                        txtCidade.setText(dados.get("cidade"));
                        txtUF.setText(dados.get("uf"));
                        
                        // Joga o cursor automaticamente para o campo de "Número" para o usuário continuar digitando
                        txtNum.requestFocus(); 
                    });
                }
            }).start();
        }
    }
}