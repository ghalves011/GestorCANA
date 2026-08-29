package br.com.cana.view;

import javax.swing.*;
import java.awt.*;

import br.com.cana.model.Partida;
import br.com.cana.service.ApiClient;
import br.com.cana.util.ImagemUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * Projeto: Gestor de Partidas CANA
 * Tela de Configuração e Geração de Partida - ADS 2026
 * 
 * @author Guilherme Alves
 */
public class TelaGeracaoPartidaView extends JFrame {

    private JTextField txtNomePartida, txtData;
    private JComboBox<String> cbFormacaoA, cbFormacaoB;

    // ⚽ GUARDA DA ESCALAÇÃO: Guarda os nomes selecionados para usar na hora de
    // gerar
    private List<String> presentesSelecionados = new ArrayList<>();

    public TelaGeracaoPartidaView() {
        // --- CONFIGURAÇÕES DA JANELA ---
        setTitle("CANA - Gerar Nova Partida");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setLayout(null); // Layout absoluto para seguir o Figma

        // Estilo centralizado
        getContentPane().setBackground(ImagemUtil.COR_FUNDO);
        ImagemUtil.configurarIcone(this);

        // --- TÍTULO DA TELA ---
        JLabel lblTitulo = new JLabel("CONFIGURAÇÃO DA PARTIDA");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(0x333333));
        lblTitulo.setBounds(50, 20, 400, 40);
        add(lblTitulo);

        // --- CAMPO: NOME DA PARTIDA ---
        JLabel lblNome = new JLabel("Nome do Evento/Partida:");
        lblNome.setBounds(50, 80, 200, 28);
        add(lblNome);

        txtNomePartida = new JTextField();
        txtNomePartida.setBounds(50, 110, 300, 36);
        add(txtNomePartida);

        // --- CAMPO: DATA DA PARTIDA ---
        JLabel lblData = new JLabel("Data (DD/MM/AAAA):");
        lblData.setBounds(50, 160, 200, 28);
        add(lblData);

        txtData = new JTextField();
        txtData.setBounds(50, 190, 200, 36);
        add(txtData);

        // --- SELEÇÃO DE FORMAÇÕES ---
        String[] formacoes = { "4-4-2", "4-3-3", "3-5-2", "4-5-1", "3-4-3", "5-3-2" };

        JLabel lblFormA = new JLabel("Formação Time AZUL:");
        lblFormA.setBounds(50, 240, 150, 28);
        add(lblFormA);

        cbFormacaoA = new JComboBox<>(formacoes);
        cbFormacaoA.setBounds(50, 270, 200, 36);
        add(cbFormacaoA);

        JLabel lblFormB = new JLabel("Formação Time VERMELHO:");
        lblFormB.setBounds(300, 240, 150, 28);
        add(lblFormB);

        cbFormacaoB = new JComboBox<>(formacoes);
        cbFormacaoB.setBounds(300, 270, 200, 36);
        add(cbFormacaoB);

        // --- BOTÃO: SELECIONAR JOGADORES ---
        JButton btnSelecionar = new JButton("SELECIONAR JOGADORES PRESENTES");
        btnSelecionar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSelecionar.setFocusPainted(false);
        btnSelecionar.setBounds(50, 340, 450, 48);
        ImagemUtil.ImagemBotoes(btnSelecionar);
        btnSelecionar.setBackground(ImagemUtil.COR_AZUL_GRADIENTE);

        // Evento de clique integrado com o validador de débitos dinâmico
        btnSelecionar.addActionListener(e -> {

            // ✅ Busca os jogadores ativos via API Spring Boot
            List<br.com.cana.model.Jogador> ativos = new java.util.ArrayList<>();

            try {
                // 🌟 1. Busca TODOS os jogadores do banco (sem o filtro restritivo de status)
                String json = ApiClient.get("/jogadores");

                java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<br.com.cana.model.Jogador>>() {
                }.getType();
                List<br.com.cana.model.Jogador> resultado = ApiClient.GSON.fromJson(json, type);

                if (resultado != null) {
                    for (br.com.cana.model.Jogador j : resultado) {
                        // 🌟 2. Filtra permitindo Ativos e Suspensos (trazendo ambos para a tela)
                        String st = j.getStatus() != null ? j.getStatus().trim() : "";
                        if (st.equalsIgnoreCase("Ativo") || st.equalsIgnoreCase("Suspenso")) {
                            ativos.add(j);
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao buscar jogadores da API: " + ex.getMessage(),
                        "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
            }

            if (ativos != null) {
                // Ordenação tática (Goleiro, Lateral, etc)
                List<String> ordemPosicoes = java.util.Arrays.asList("GOLEIRO", "LATERAL", "ZAGUEIRO", "VOLANTE",
                        "MEIA", "ATACANTE");
                ativos.sort((j1, j2) -> {
                    String p1 = j1.getPosicao() != null ? j1.getPosicao().toUpperCase().trim() : "";
                    String p2 = j2.getPosicao() != null ? j2.getPosicao().toUpperCase().trim() : "";
                    int index1 = ordemPosicoes.indexOf(p1);
                    int index2 = ordemPosicoes.indexOf(p2);
                    return Integer.compare((index1 != -1 ? index1 : 99), (index2 != -1 ? index2 : 99));
                });
            }

            // 🌟 AJUSTE API: Sincroniza a memória e monta a lista de devedores via ApiClient
            List<Integer> idsDevedores = new ArrayList<>();
            if (ativos != null) {
                for (br.com.cana.model.Jogador j : ativos) {
                    boolean pode = true;
                    try {
                        // Consulta a API para verificar se o jogador pode jogar (contribuição em dia)
                        String resposta = ApiClient.get("/contribuicoes/pode-jogar/" + j.getId());
                        pode = Boolean.parseBoolean(resposta.trim());
                    } catch (Exception ex) {
                        System.err.println(
                                "Erro ao verificar contribuição do jogador ID " + j.getId() + ": " + ex.getMessage());
                    }

                    j.setMensalidadeEmDia(pode);

                    if (!pode) {
                        idsDevedores.add(j.getId());
                    }
                }
            }

            // 2. Abre a janela de seleção com as informações sincronizadas (passando apenas os devedores)
            DialogSelecaoJogadoresView dialog = new DialogSelecaoJogadoresView(this, ativos, idsDevedores,
                    presentesSelecionados);
            dialog.setVisible(true);

            if (dialog.isConfirmado()) {
                this.presentesSelecionados = dialog.getJogadoresSelecionados();
                btnSelecionar.setText(presentesSelecionados.size() + " JOGADORES SELECIONADOS");
            }
        });

        add(btnSelecionar);

        // --- BOTÃO FINAL: GERAR PARTIDA (GRADIENTE) ---
        ImagemUtil.BotaoGradienteCANA btnGerar = new ImagemUtil.BotaoGradienteCANA("SORTEAR E GERAR");
        btnGerar.setBounds(200, 450, 300, 80);
        btnGerar.addActionListener(e -> acaoGerarPartida());
        add(btnGerar);
    }

    /**
     * Lógica para validar os campos, rodar o sorteio no Service e abrir a Partida
     * Live
     */
    /**
     * Lógica para validar os campos, rodar o sorteio no Service e abrir a Partida
     * Live
     */
    private void acaoGerarPartida() {
        String nome = txtNomePartida.getText().trim();
        String data = txtData.getText().trim();

        // 1. Validações básicas
        if (nome.isEmpty() || data.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Atenção: O nome e a data são obrigatórios!",
                    "Erro de Validação",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (presentesSelecionados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Atenção: Você precisa selecionar os jogadores presentes antes de gerar o jogo!",
                    "Erro de Escalação",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Deseja realizar o sorteio equilibrado agora com os " + presentesSelecionados.size() + " jogadores?",
                "Confirmar Geração",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {

            // 2. MONTAGEM DO OBJETO PARTIDA
            br.com.cana.model.Partida novaPartida = new br.com.cana.model.Partida();
            novaPartida.setNomePartida(nome);
            novaPartida.setTemporadaId(2026); // Temporada ADS 2026

            // 📆 CONVERSÃO DO LOCALDATE
            try {
                java.time.format.DateTimeFormatter formatoBR = java.time.format.DateTimeFormatter
                        .ofPattern("dd/MM/yyyy");
                java.time.LocalDate dataConvertida = java.time.LocalDate.parse(data, formatoBR);
                novaPartida.setDataPartida(dataConvertida);
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Formato de data inválido! Use DD/MM/AAAA", "Erro de Data",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

           // 3. CONVERSÃO: Busca jogadores ativos via API e ordena conforme seleção
            List<br.com.cana.model.Jogador> ativos = new ArrayList<>();
            try {
                // 🌟 REMOVE O FILTRO RESTRITIVO AQUI TAMBÉM PARA NÃO PERDER OS SUSPENSOS
                String jsonAtivos = ApiClient.get("/jogadores");
                com.google.gson.Gson gson = ApiClient.GSON;
                java.lang.reflect.Type typeList = new com.google.gson.reflect.TypeToken<List<br.com.cana.model.Jogador>>() {
                }.getType();
                List<br.com.cana.model.Jogador> resAtivos = gson.fromJson(jsonAtivos, typeList);
                
                if (resAtivos != null) {
                    for (br.com.cana.model.Jogador j : resAtivos) {
                        String st = j.getStatus() != null ? j.getStatus().trim() : "";
                        // 🌟 Traz os ativos e suspensos para a montagem da lista oficial a ser enviada ao Back-end
                        if (st.equalsIgnoreCase("Ativo") || st.equalsIgnoreCase("Suspenso")) {
                            ativos.add(j);
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao buscar jogadores para o sorteio: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<br.com.cana.model.Jogador> jogadoresOrdenados = new ArrayList<>();

            for (String apelido : presentesSelecionados) {
                for (br.com.cana.model.Jogador j : ativos) {
                    String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                            : j.getNome();
                    if (nomeJ != null && nomeJ.equals(apelido)) {
                        jogadoresOrdenados.add(j);
                        break;
                    }
                }
            }

            // 4. DISPARA O BACK-END: A API executa o partidaService.sortearTimesTatico(...)
            // no servidor
            String formA = cbFormacaoA.getSelectedItem().toString();
            String formB = cbFormacaoB.getSelectedItem().toString();

            novaPartida.setFormacaoAzul(formA);
            novaPartida.setFormacaoVermelho(formB);

            try {
                com.google.gson.Gson gson = ApiClient.GSON;

                // Empacota os dados selecionados na View para enviar pro servidor
                com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
                payload.add("partida", gson.toJsonTree(novaPartida));
                payload.add("jogadores", gson.toJsonTree(jogadoresOrdenados));

                // Faz a chamada HTTP pro Spring Boot
                String jsonPartidaSorteada = ApiClient.post("/partidas/sortear", payload.toString());

                if (jsonPartidaSorteada != null && !jsonPartidaSorteada.isEmpty()) {
                    // Recebe a partida com os times já sorteados pelo Service do Back-end
                    novaPartida = gson.fromJson(jsonPartidaSorteada, Partida.class);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao sortear times na API: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 5. ABRE A TELA LIVE COM OS DADOS PROCESSADOS PELO BACK-END
            TelaPartidaLiveView telaLive = new TelaPartidaLiveView(novaPartida);
            telaLive.setVisible(true);

            this.dispose();
        }
    }
}