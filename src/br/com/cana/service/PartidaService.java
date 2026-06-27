package br.com.cana.service;

import br.com.cana.dao.PartidaDAO;
import br.com.cana.dao.JogadorDAO;
import br.com.cana.model.Jogador;
import br.com.cana.model.JogadorPartida;
import br.com.cana.model.Partida;
import br.com.cana.util.SorteioUtil;

import java.util.ArrayList;
import java.util.List;

public class PartidaService {

    private PartidaDAO partidaDAO;
    private JogadorDAO jogadorDAO; // 🛠️ Injetado para validar as RNs de jogadores
    private ContribuicaoService contribuicaoService;

    public PartidaService() {
        this.partidaDAO = new PartidaDAO();
        this.jogadorDAO = new JogadorDAO();
        this.contribuicaoService = new ContribuicaoService();
    }

    public void sortearTimesTatico(Partida partida, List<Jogador> presentes, String formacao) {
        partida.getJogadoresAzul().clear();
        partida.getJogadoresVermelho().clear();

        List<Jogador> titulares = new ArrayList<>();
        List<Jogador> deEspera = new ArrayList<>();
        List<Jogador> todosGoleiros = new ArrayList<>();
        List<Jogador> todosLinha = new ArrayList<>();
        List<JogadorPartida> listaPresenca = new ArrayList<>();

        // 1. FILTRAGEM: O único loop que separa quem joga de quem espera
        for (Jogador j : presentes) {
            if (j.isEstaSuspenso()) {
                deEspera.add(j);
            } else if (j.getPosicao() != null && "GOLEIRO".equalsIgnoreCase(j.getPosicao().trim())) {
                todosGoleiros.add(j);
            } else {
                todosLinha.add(j);
            }
        }

        // 2. DISTRIBUIÇÃO: Separa Goleiros (max 2) e Preenche Titulares
        for (int i = 0; i < todosGoleiros.size(); i++) {
            if (i < 2)
                titulares.add(todosGoleiros.get(i));
            else
                deEspera.add(todosGoleiros.get(i));
        }
        for (Jogador j : todosLinha) {
            if (titulares.size() < 22)
                titulares.add(j);
            else
                deEspera.add(j);
        }

        // 3. O SORTEIO: Agora sim, ele acontece com os titulares definidos
        SorteioUtil.realizarSorteio(partida, titulares, formacao);

        // 4. PREENCHIMENTO DA LISTA DE PRESENÇA (Súmula Final)
        // Azul
        for (Jogador j : partida.getJogadoresAzul()) {
            JogadorPartida jp = new JogadorPartida();
            jp.setJogador(j);
            jp.setTime("Azul");
            jp.setStatus("Titular");
            jp.setFuncao("Azul_" + (j.getPosicao().toUpperCase().contains("GOL") ? "GOL" : "LIN") + "_"
                    + (partida.getJogadoresAzul().indexOf(j) + 1));
            listaPresenca.add(jp);
        }
        // Vermelho
        for (Jogador j : partida.getJogadoresVermelho()) {
            JogadorPartida jp = new JogadorPartida();
            jp.setJogador(j);
            jp.setTime("Vermelho");
            jp.setStatus("Titular");
            jp.setFuncao("Vermelho_" + (j.getPosicao().toUpperCase().contains("GOL") ? "GOL" : "LIN") + "_"
                    + (partida.getJogadoresVermelho().indexOf(j) + 1));
            listaPresenca.add(jp);
        }
        // Reserva (O que sobrou no pote)
        for (Jogador j : deEspera) {
            JogadorPartida jp = new JogadorPartida();
            jp.setJogador(j);
            jp.setTime("Nenhum");
            jp.setStatus("Reserva");
            jp.setFuncao(j.getPosicao() != null && j.getPosicao().toUpperCase().contains("GOL") ? "GOL" : "LIN");
            listaPresenca.add(jp);
        }

        partida.setListaGeralPresenca(listaPresenca);
    }

    public boolean salvarPartida(Partida p) {
        if (p.getListaGeralPresenca().isEmpty()) {
            return false;
        }
        return partidaDAO.salvarCompleto(p);
    }

    public void trocarJogadorDeTime(Partida p, Jogador j) {
        if (p.getJogadoresAzul().contains(j)) {
            p.getJogadoresAzul().remove(j);
            p.getJogadoresVermelho().add(j);
        } else if (p.getJogadoresVermelho().contains(j)) {
            p.getJogadoresVermelho().remove(j);
            p.getJogadoresAzul().add(j);
        }
    }

    public List<Partida> listarPartidasDaTemporada(int temporadaId) {
        return partidaDAO.listarPorTemporada(temporadaId);
    }

    public String[] processarSubstituicaoJogador(String titularSaindo, String reservaCompleto, String posSaindo) {
        String reservaEntrando = reservaCompleto;
        if (reservaCompleto.contains(" (")) {
            reservaEntrando = reservaCompleto.substring(0, reservaCompleto.indexOf(" ("));
        }

        String nomeQuemVaiproBanco;
        if (titularSaindo.contains(" / ")) {
            String[] partes = titularSaindo.split(" / ");
            nomeQuemVaiproBanco = partes[partes.length - 1];
        } else {
            nomeQuemVaiproBanco = titularSaindo;
        }

        String novoTextoTabela = titularSaindo + " / " + reservaEntrando;
        String textoBanco = nomeQuemVaiproBanco + " (" + posSaindo + ")";

        return new String[] { novoTextoTabela, textoBanco };
    }

    public String[] processarSubstituicaoArbitro(String atual, String reservaCompleto) {
        String reservaEntrando = reservaCompleto;
        if (reservaCompleto.contains(" (")) {
            reservaEntrando = reservaCompleto.substring(0, reservaCompleto.indexOf(" ("));
        }

        String novoTexto;
        String nomeQuemVaiproBanco = "";

        // 🌟 Ajustado para .equals() para não passar por cima do histórico se houver "
        // / ____"
        if (atual == null || atual.trim().isEmpty() || atual.trim().equals("Não escalado")
                || atual.trim().equals("Selecione...") || atual.trim().equals("____")) {
            novoTexto = reservaEntrando;
        } else if (atual.endsWith(" / ____")) {
            // 🌟 Se a vaga estava aberta mas tinha histórico, preenchemos o espaço do
            // "____"
            novoTexto = atual.substring(0, atual.lastIndexOf(" / ____")) + " / " + reservaEntrando;
        } else {
            // Fluxo normal: se já tem alguém apitando, acumula gerando a substituição ("Gui
            // / Lucas")
            if (atual.contains(" / ")) {
                String[] partes = atual.split(" / ");
                nomeQuemVaiproBanco = partes[partes.length - 1];
            } else {
                nomeQuemVaiproBanco = atual;
            }
            novoTexto = atual + " / " + reservaEntrando;
        }

        String textoBanco = "";
        if (!nomeQuemVaiproBanco.isEmpty()) {
            String posicao = buscarPosicaoJogador(nomeQuemVaiproBanco);
            textoBanco = nomeQuemVaiproBanco + " (" + posicao + ")";
        }

        return new String[] { novoTexto, textoBanco };
    }

    private String buscarPosicaoJogador(String nome) {
        try {
            List<Jogador> ativos = jogadorDAO.buscarPorStatus("Ativo");
            for (Jogador j : ativos) {
                String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                        : j.getNome();
                if (nomeJ.equals(nome)) {
                    return (j.getPosicao() != null && !j.getPosicao().trim().isEmpty()) ? j.getPosicao() : "-";
                }
            }
        } catch (Exception e) {
        }
        return "LIN";
    }

    public String registrarSubstituicaoNoEvento(String evAtual) {
        if (evAtual == null || evAtual.trim().isEmpty()) {
            return " / ";
        }
        return evAtual + " / ";
    }

    public String adicionarEventoAoJogador(String eventosAtuais, String tokenEvento) {
        if (eventosAtuais == null)
            eventosAtuais = "";
        String[] partes = eventosAtuais.split(" / ", -1);
        int idxUltimo = partes.length - 1;
        String eventosUltimo = partes[idxUltimo].trim();

        if (eventosUltimo.isEmpty()) {
            eventosUltimo = tokenEvento;
        } else {
            eventosUltimo = eventosUltimo + " " + tokenEvento;
        }

        partes[idxUltimo] = ordenarTokensJogador(eventosUltimo);
        return String.join(" / ", partes);
    }

    public String removerEventoDoJogador(String eventosAtuais, String tokenEvento) {
        if (eventosAtuais == null || eventosAtuais.isEmpty())
            return "";
        String[] partes = eventosAtuais.split(" / ", -1);
        int idxUltimo = partes.length - 1;
        String eventosUltimo = partes[idxUltimo].trim();

        if (eventosUltimo.contains(tokenEvento)) {
            eventosUltimo = eventosUltimo.replaceFirst(java.util.regex.Pattern.quote(tokenEvento), "")
                    .replaceAll("  ", " ").trim();
        }

        partes[idxUltimo] = ordenarTokensJogador(eventosUltimo);
        return String.join(" / ", partes);
    }

    private String ordenarTokensJogador(String tokens) {
        int g = 0, gc = 0, a = 0, v = 0;
        String[] arr = tokens.split(" ");
        for (String t : arr) {
            if (t.equals("⚽"))
                g++;
            else if (t.equals("⚽(C)"))
                gc++;
            else if (t.equals("🟨"))
                a++;
            else if (t.equals("🟥"))
                v++;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < g; i++)
            sb.append("⚽ ");
        for (int i = 0; i < gc; i++)
            sb.append("⚽(C) ");
        for (int i = 0; i < a; i++)
            sb.append("🟨 ");
        for (int i = 0; i < v; i++)
            sb.append("🟥 ");
        return sb.toString().trim();
    }

    public int contarTokensNoBlocoAtivo(String eventosAtuais, String token) {
        if (eventosAtuais == null || eventosAtuais.isEmpty())
            return 0;
        String[] partes = eventosAtuais.split(" / ", -1);
        String ultimo = partes[partes.length - 1];

        int count = 0;
        if (token.equals("⚽")) {
            String temp = ultimo;
            while (temp.contains("⚽(C)"))
                temp = temp.replaceFirst("⚽\\(C\\)", "");
            while (temp.contains("⚽")) {
                count++;
                temp = temp.replaceFirst("⚽", "");
            }
        } else {
            String temp = ultimo;
            while (temp.contains(token)) {
                count++;
                temp = temp.replaceFirst(java.util.regex.Pattern.quote(token), "");
            }
        }
        return count;
    }

    public int contarAmarelosDoJogadorAtivo(String eventosAtuais) {
        return contarTokensNoBlocoAtivo(eventosAtuais, "🟨");
    }

    public String obterNomeAtivo(String texto) {
        if (texto == null || texto.trim().isEmpty()
                || texto.equals("Selecione...") || texto.equals("Não escalado")) {
            return "";
        }

        String nomeAtivo = texto;
        // 🌟 Se tiver o padrão de substituição " / ", pega apenas o último (que seria o
        // ativo)
        if (texto.contains(" / ")) {
            String[] partes = texto.split(" / ");
            nomeAtivo = partes[partes.length - 1].trim();
        }

        // 🌟 Agora sim, valida se o último elemento da escala é um indicador de vaga
        // aberta
        if (nomeAtivo.contains("____") || nomeAtivo.isEmpty()) {
            return "";
        }

        // Remove as posições de strings como "Gui (ATA)" se necessário
        if (nomeAtivo.contains(" (")) {
            return nomeAtivo.substring(0, nomeAtivo.indexOf(" (")).trim();
        }

        return nomeAtivo.trim();
    }

    // RN: Valida se um atleta pode entrar em campo como titular ou reserva,
    // verificando pendências financeiras e suspensão.
    public String validarRestricaoParaLinha(String reservaCompleto) {
        try {
            String nomeLimpo = obterNomeAtivo(reservaCompleto);
            Jogador jogador = jogadorDAO.buscarPorNomeOuApelido(nomeLimpo);

            if (jogador != null) {

                if (!contribuicaoService.podeJogar(jogador.getId())) {
                    return "O jogador '" + nomeLimpo + "' possui pendências financeiras e está bloqueado!";
                }

                // Em vez de olhar o atributo antigo, chama a regra blindada baseada na data de
                // admissão
                if (!contribuicaoService.podeJogar(jogador.getId())) {
                    return "O jogador '" + nomeLimpo + "' possui pendências financeiras e está bloqueado!";
                }

                // 2ª Validação: Suspensão baseada no seu atributo do DAO
                if (jogador.isEstaSuspenso()) {
                    return "O jogador '" + nomeLimpo
                            + "' está SUSPENSO! É obrigatório cumprir suspensão na arbitragem.";
                }
            }
        } catch (Exception e) {
            // Silencia para não travar os mocks de teste
            System.err.println("Erro ao validar restrição de linha: " + e.getMessage());
        }
        return null; // Liberado
    }

    // RN: Valida a entrada do reserva COM A BOLA ROLANDO (Substituição).
    // Ignora a parte financeira (pois já foi tolerada no check-in) e foca só na
    // suspensão.
    public String validarRestricaoParaSubstituicao(String reservaCompleto) {
        try {
            String nomeLimpo = obterNomeAtivo(reservaCompleto);
            Jogador jogador = jogadorDAO.buscarPorNomeOuApelido(nomeLimpo);

            if (jogador != null) {
                // Valida apenas se o jogador tomou cartão vermelho/está suspenso
                if (jogador.isEstaSuspenso()) {
                    return "O jogador '" + nomeLimpo
                            + "' está SUSPENSO! É obrigatório cumprir suspensão na arbitragem.";
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao validar restrição de substituição: " + e.getMessage());
        }
        return null; // Liberado para entrar no jogo
    }

    /**
     * Valida se um jogador pode ou não ser escalado para a arbitragem/staff.
     * Retorna uma mensagem de erro ou null se o jogador estiver liberado.
     */
    public String validarRestricaoParaArbitragem(Partida partida, Jogador jogador) {
        if (partida == null || jogador == null) {
            return "Dados inválidos para validação.";
        }

        String nomeJogador = (jogador.getApelido() != null && !jogador.getApelido().trim().isEmpty())
                ? jogador.getApelido()
                : jogador.getNome();

        // 1. REGRA DA VÁRZEA: Só quem está no banco de sobras ou foi substituído pode
        // apitar
        boolean estaNoBanco = false;
        if (partida.getListaGeralPresenca() != null) {
            for (JogadorPartida jp : partida.getListaGeralPresenca()) {
                if (jp.getJogador().getNome().equalsIgnoreCase(jogador.getNome())) {
                    String status = jp.getStatus() != null ? jp.getStatus().trim() : "";
                    String time = jp.getTime() != null ? jp.getTime().trim() : "";

                    // 🌟 LIBERAÇÃO: Aceita a galera original e a galera substituída!
                    boolean noPoteOriginal = "Reserva".equalsIgnoreCase(status) && "Nenhum".equalsIgnoreCase(time);
                    boolean noBancoSubstituido = "Substituido".equalsIgnoreCase(status)
                            || "Substituído".equalsIgnoreCase(status);

                    if (noPoteOriginal || noBancoSubstituido) {
                        estaNoBanco = true;
                    }
                    break;
                }
            }
        }

        if (!estaNoBanco) {
            return "❌ Operação negada! O jogador '" + nomeJogador + "' não está no banco de reservas.";
        }

        // 2. CHECAGEM DE DUPLICIDADE ATUAL: Evita o cara apitar e bandeirar ao mesmo
        // tempo
        if (nomeJogador.equalsIgnoreCase(obterNomeAtivo(partida.getArbitro())) ||
                nomeJogador.equalsIgnoreCase(obterNomeAtivo(partida.getBandeira1())) ||
                nomeJogador.equalsIgnoreCase(obterNomeAtivo(partida.getBandeira2()))) {
            return "❌ Este jogador já está atuando na equipe de arbitragem agora.";
        }

        return null; // Liberado!
    }

    // RN: Após o jogo, processa a limpeza da suspensão dos jogadores que estavam
    // cumprindo suspensão na arbitragem, permitindo que voltem a atuar normalmente.
    public void processarLimpezaDeSuspensaoPosJogo(List<String> nomesDaArbitragem, Partida partidaAtual) {
        try {
            System.out.println("\n➔ [RADAR ARBITRAGEM] Iniciando verificação para: " + nomesDaArbitragem);
            br.com.cana.dao.JogadorPartidaDAO jpDAO = new br.com.cana.dao.JogadorPartidaDAO();

            for (String nomeRaw : nomesDaArbitragem) {
                String nomeLimpo = obterNomeAtivo(nomeRaw);
                if (nomeLimpo.isEmpty())
                    continue;

                Jogador jogador = jogadorDAO.buscarPorNomeOuApelido(nomeLimpo);

                if (jogador == null) {
                    System.out.println("⚠️ [RADAR] Árbitro não encontrado no banco de dados: " + nomeLimpo);
                    continue;
                }

                System.out.println("➔ [RADAR] Lendo Árbitro: " + nomeLimpo + " | Está suspenso no BD? "
                        + jogador.isEstaSuspenso());

                // Só limpa se o cara realmente estiver marcado como suspenso no banco
                if (jogador.isEstaSuspenso()) {

                    // 🛑 REGRA DA EXPULSÃO SIMULTÂNEA: Verifica se ele tomou vermelho HOJE atuando
                    // na linha
                    boolean foiExpulsoNestaPartida = false;
                    for (br.com.cana.model.JogadorPartida jp : partidaAtual.getListaGeralPresenca()) {
                        String nomeObjeto = (jp.getJogador().getApelido() != null
                                && !jp.getJogador().getApelido().trim().isEmpty())
                                        ? jp.getJogador().getApelido().trim()
                                        : jp.getJogador().getNome().trim();

                        if (nomeObjeto.equalsIgnoreCase(nomeLimpo)) {
                            if (jp.getCartaoVermelho() > 0) {
                                foiExpulsoNestaPartida = true;
                            }
                            break;
                        }
                    }

                    if (foiExpulsoNestaPartida) {
                        System.out.println(
                                "➔ [BLOQUEIO] O jogador " + nomeLimpo + " foi EXPULSO hoje. Suspensão MANTIDA!");
                        continue;
                    }

                    // ✅ Se passou ileso, tira da geladeira e zera os cartões!
                    jogadorDAO.atualizarStatus(jogador.getId(), "Ativo");
                    jpDAO.zerarCartoesDoJogador(jogador.getId());

                    System.out.println(
                            "✅ [SUCESSO MÁXIMO] Cartões zerados e jogador liberado da suspensão: " + nomeLimpo);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro durante a limpeza: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para salvar a súmula temporariamente na instância da partida, sem
    // persistir no banco, permitindo que o mesário tenha um rascunho durante o
    // jogo.
    public void salvarSumulaProvisoria(Partida partida, String textoSumula) {
        if (partida != null) {
            partida.setSumula(textoSumula);

            // Nota: Este método NÃO chama o DAO para persistir no banco, pois é apenas um
            // rascunho temporário durante a partida. O salvamento definitivo só ocorre
            // quando o jogo é finalizado.
        }
    }

    // Método utilitário para extrair os nomes de exibição (apelido ou nome) de uma
    // lista de jogadores, garantindo que a interface mostre as informações
    // amigáveis.
    public List<String> extrairNomesParaExibicao(List<Jogador> jogadores) {
        List<String> listaNomes = new ArrayList<>();
        if (jogadores != null) {
            for (Jogador j : jogadores) {
                String nomeExibir = (j.getApelido() != null && !j.getApelido().trim().isEmpty())
                        ? j.getApelido()
                        : j.getNome();
                listaNomes.add(nomeExibir);
            }
        }
        return listaNomes;
    }

    public List<Jogador> obterTitularesDoTime(Partida partida, boolean isAzul) {
        List<Jogador> titulares = new ArrayList<>();
        String timeAlvo = isAzul ? "Azul" : "Vermelho";

        if (partida != null && partida.getListaGeralPresenca() != null) {
            for (JogadorPartida jp : partida.getListaGeralPresenca()) {
                if (timeAlvo.equalsIgnoreCase(jp.getTime()) && "Titular".equalsIgnoreCase(jp.getStatus())) {
                    if (jp.getJogador() != null) {
                        titulares.add(jp.getJogador());
                    }
                }
            }
        }
        return titulares;
    }

    /**
     * 🛠️ PREPARADOR DA TABELA DE ESPERA: Processa a regra da várzea, abrevia as
     * posições e devolve uma lista pronta para a JTable.
     */
    public List<Object[]> obterLinhasTabelaEspera(Partida partida) {
        List<Object[]> linhas = new ArrayList<>();

        if (partida == null || partida.getListaGeralPresenca() == null) {
            return linhas;
        }

        for (JogadorPartida jp : partida.getListaGeralPresenca()) {
            String status = jp.getStatus() != null ? jp.getStatus().trim() : "";
            String time = jp.getTime() != null ? jp.getTime().trim() : "";

            // 🌟 A MÁGICA VOLTOU: O Scanner agora aceita quem estava sobrando E quem foi
            // pro banco
            boolean noPoteOriginal = "Reserva".equalsIgnoreCase(status) && "Nenhum".equalsIgnoreCase(time);
            boolean noBancoSubstituido = "Substituido".equalsIgnoreCase(status)
                    || "Substituído".equalsIgnoreCase(status);

            if (noPoteOriginal || noBancoSubstituido) {
                Jogador j = jp.getJogador();
                if (j != null) {
                    String nome = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                            : j.getNome();
                    String posBanco = j.getPosicao() != null ? j.getPosicao().toUpperCase().trim() : "";
                    String sigla = "-";

                    // A tradução tática acontece aqui na camada de negócio!
                    switch (posBanco) {
                        case "GOLEIRO":
                            sigla = "GOL";
                            break;
                        case "LATERAL":
                            sigla = "LAT";
                            break;
                        case "ZAGUEIRO":
                            sigla = "ZAG";
                            break;
                        case "MEIA":
                            sigla = "MEI";
                            break;
                        case "ATACANTE":
                            sigla = "ATA";
                            break;
                        default:
                            sigla = posBanco;
                    }

                    // Monta a linha perfeita para a estrutura da JTable
                    linhas.add(new Object[] { nome, sigla });
                }
            }
        }
        // 🌟 A MÁGICA DA ORDENAÇÃO: Força o Goleiro para o topo do banco!
        linhas.sort((l1, l2) -> {
            String sigla1 = (String) l1[1];
            String sigla2 = (String) l2[1];

            // Mapeia o peso de cada posição para a ordenação (GOL é o rei e fica no topo =
            // 1)
            java.util.Map<String, Integer> ordemPos = java.util.Map.of(
                    "GOL", 1, "LAT", 2, "ZAG", 3, "MEI", 4, "ATA", 5);

            int peso1 = ordemPos.getOrDefault(sigla1, 6);
            int peso2 = ordemPos.getOrDefault(sigla2, 6);

            return Integer.compare(peso1, peso2);
        });
        return linhas;
    }

    /**
     * PROCESSA A REGRA DE NEGÓCIO DA ARTILHARIA E PERSISTE NO BANCO
     */
    public boolean finalizarEFecharPartida(Partida partida, java.util.Map<String, String> eventosTela) {
        if (partida == null || partida.getListaGeralPresenca() == null || eventosTela == null) {
            return false;
        }

        // 🌟 RESET DE SEGURANÇA: Zera os dados antes de processar para não acumular
        // lixo na memória
        for (br.com.cana.model.JogadorPartida jp : partida.getListaGeralPresenca()) {
            jp.setGols(0);
            jp.setCartaoAmarelo(0);
            jp.setCartaoVermelho(0);
        }

        // 🌟 CORREÇÃO DO PONTO CEGO: Varre as linhas dividindo "JogadorA / JogadorB" e
        // seus eventos "⚽ / 🟨"
        for (java.util.Map.Entry<String, String> entry : eventosTela.entrySet()) {
            String[] nomesRow = entry.getKey().split(" / ");
            String[] eventosRow = entry.getValue().split(" / ", -1);

            // Cada índice K corresponde exatamente ao respectivo jogador daquela linha
            // histórica
            for (int k = 0; k < nomesRow.length; k++) {
                String nomeTelaLimpo = obterNomeAtivo(nomesRow[k]);
                if (nomeTelaLimpo.isEmpty())
                    continue;

                // Captura a fatia de tokens de evento pertencente a este jogador específico
                String tokensDoJogador = (k < eventosRow.length) ? eventosRow[k].trim() : "";

                // Localiza o atleta correspondente na lista geral de presença para injetar as
                // estatísticas
                for (br.com.cana.model.JogadorPartida jp : partida.getListaGeralPresenca()) {
                    String nomeObjeto = (jp.getJogador().getApelido() != null
                            && !jp.getJogador().getApelido().trim().isEmpty())
                                    ? jp.getJogador().getApelido().trim()
                                    : jp.getJogador().getNome().trim();

                    if (nomeObjeto.equalsIgnoreCase(nomeTelaLimpo)) {
                        // 🔥 A MÁGICA DA ACUMULAÇÃO: Usa jp.getGols() + para somar com o que já tem! 🔥
                        // 🔥 A MÁGICA DA ACUMULAÇÃO DE GOLS
                        int qtdGols = contarTokensNoBlocoAtivo(tokensDoJogador, "⚽");
                        jp.setGols(jp.getGols() + qtdGols);

                        // 🟨 REGRA DOS AMARELOS
                        int qtdAmarelos = contarTokensNoBlocoAtivo(tokensDoJogador, "🟨");
                        jp.setCartaoAmarelo(jp.getCartaoAmarelo() + qtdAmarelos);

                        if (qtdAmarelos > 0) {
                            br.com.cana.dao.JogadorPartidaDAO jpDAO = new br.com.cana.dao.JogadorPartidaDAO();
                            int amarelosAnteriores = jpDAO.contarCartao_Amarelo(jp.getJogador().getId());

                            // 🕵️ O ESPIÃO: Olha o console do seu Eclipse/NetBeans quando rodar!
                            System.out.println("➔ [DEBUG AMARELOS] Jogador: " + jp.getJogador().getNome()
                                    + " | Histórico do Banco: " + amarelosAnteriores
                                    + " | Tomou hoje: " + qtdAmarelos);

                            if ((amarelosAnteriores + qtdAmarelos) >= 3) {
                                System.out.println("➔ 🟨🟨🟨 O jogador atingiu 3 amarelos! Aplicando suspensão...");
                                jogadorDAO.atualizarStatus(jp.getJogador().getId(), "Suspenso");
                            }
                        }

                        // 🟥 REGRA DA EXPULSÃO DIRETA
                        int qtdVermelhos = tokensDoJogador.contains("🟥") ? 1 : 0;
                        jp.setCartaoVermelho(jp.getCartaoVermelho() + qtdVermelhos);

                        if (qtdVermelhos > 0) {
                            // ✅ ATUALIZA SÓ O STATUS: Altera direto no banco sem apagar os outros dados
                            jogadorDAO.atualizarStatus(jp.getJogador().getId(), "Suspenso");
                        }

                        break; // Achou o cara na lista de presença, pula para o próximo da linha
                    }
                }
            }
        }

        // Captura os nomes que estão na arbitragem para resetar a suspensão
        java.util.List<String> nomesDaArbitragem = new java.util.ArrayList<>();
        nomesDaArbitragem.add(partida.getArbitro());
        nomesDaArbitragem.add(partida.getBandeira1());
        nomesDaArbitragem.add(partida.getBandeira2());

        // Chama o método para limpar a flag de suspensão e zerar os cartões no banco
        processarLimpezaDeSuspensaoPosJogo(nomesDaArbitragem, partida);

        // Executa a persistência final chamando o seu DAO interno
        return salvarPartida(partida);
    }

    /**
     * Define um jogador da lista de presença como staff/arbitragem e garante
     * que ele saia dos times ou do pote de reservas, evitando duplicidade.
     */
    /**
     * Define um jogador como arbitragem.
     * 
     * @param acumularHistorico Se true, cria a linha de tempo (Nome / Nome). Se
     *                          false, corrige o erro da sessão atual.
     */
    public void definirArbitragemSemDuplicidade(Partida partida, Jogador novoStaff, String cargo,
            boolean acumularHistorico) {
        if (partida == null || novoStaff == null || cargo == null)
            return;

        String nomeNovo = (novoStaff.getApelido() != null && !novoStaff.getApelido().trim().isEmpty())
                ? novoStaff.getApelido()
                : novoStaff.getNome();

        String atual = "";
        if (cargo.equalsIgnoreCase("ARBITRO"))
            atual = partida.getArbitro();
        else if (cargo.equalsIgnoreCase("BANDEIRA1"))
            atual = partida.getBandeira1();
        else if (cargo.equalsIgnoreCase("BANDEIRA2"))
            atual = partida.getBandeira2();

        String novoTexto;

        if (atual == null || atual.trim().isEmpty() || atual.equals("Selecione...") || atual.equals("Não escalado")) {
            // Sem histórico nenhum: coloca o nome direto
            novoTexto = nomeNovo;
        } else if (acumularHistorico) {
            // 🌟 MODO ACÚMULO: O nome antigo já estava confirmado de antes, então gera
            // histórico
            if (atual.endsWith(" / ____")) {
                novoTexto = atual.substring(0, atual.lastIndexOf(" / ____")) + " / " + nomeNovo;
            } else {
                novoTexto = atual + " / " + nomeNovo;
            }
        } else {
            // 🌟 MODO CORREÇÃO (Segurança): O usuário errou o clique na mesma sessão.
            if (atual.contains(" / ")) {
                // Se era "Gui / Lucas" (e o Lucas foi erro), limpa o último e põe o novo: "Gui
                // / João"
                novoTexto = atual.substring(0, atual.lastIndexOf(" / ")).trim() + " / " + nomeNovo;
            } else {
                // Se era só um nome e foi erro de clique, substitui direto
                novoTexto = nomeNovo;
            }
        }

        // Aplica a String corrigida na entidade
        if (cargo.equalsIgnoreCase("ARBITRO"))
            partida.setArbitro(novoTexto);
        else if (cargo.equalsIgnoreCase("BANDEIRA1"))
            partida.setBandeira1(novoTexto);
        else if (cargo.equalsIgnoreCase("BANDEIRA2"))
            partida.setBandeira2(novoTexto);
    }

    /**
     * Remove o jogador da arbitragem e o devolve para o pote de reservas.
     * 
     * @param gerarHistorico Se true, mantém o nome e adiciona " / ____". Se false,
     *                       faz uma limpeza limpa.
     */
    public void removerDaArbitragem(Partida partida, String cargo, boolean gerarHistorico) {
        if (partida == null || cargo == null)
            return;

        String atual = "";
        if (cargo.equalsIgnoreCase("ARBITRO")) {
            atual = partida.getArbitro();
        } else if (cargo.equalsIgnoreCase("BANDEIRA1")) {
            atual = partida.getBandeira1();
        } else if (cargo.equalsIgnoreCase("BANDEIRA2")) {
            atual = partida.getBandeira2();
        }

        // Captura o nome limpo de quem está saindo ativamente agora
        String nomeSaindoLimpo = obterNomeAtivo(atual);

        if (atual != null && !atual.trim().isEmpty() && !nomeSaindoLimpo.isEmpty()) {
            String novoTexto;

            if (gerarHistorico) {
                // 🌟 MODO HISTÓRICO: Mantém o rastro e abre a vaga com "____"
                novoTexto = atual + " / ____";
            } else {
                // 🌟 MODO SEGURANÇA (Correção de erro): Remove o último nome inserido nesta
                // sessão
                if (atual.contains(" / ")) {
                    // Se era "Gui / Lucas" e o Lucas foi erro, volta a ser apenas "Gui"
                    novoTexto = atual.substring(0, atual.lastIndexOf(" / ")).trim();
                } else {
                    // Se era só um jogador e foi erro, zera o campo completamente
                    novoTexto = null;
                }
            }

            if (cargo.equalsIgnoreCase("ARBITRO")) {
                partida.setArbitro(novoTexto);
            } else if (cargo.equalsIgnoreCase("BANDEIRA1")) {
                partida.setBandeira1(novoTexto);
            } else if (cargo.equalsIgnoreCase("BANDEIRA2")) {
                partida.setBandeira2(novoTexto);
            }

            // Sincroniza a lista de presença para devolver o jogador para o pote de sobras
            for (JogadorPartida jp : partida.getListaGeralPresenca()) {
                String nomeObjeto = (jp.getJogador().getApelido() != null
                        && !jp.getJogador().getApelido().trim().isEmpty())
                                ? jp.getJogador().getApelido().trim()
                                : jp.getJogador().getNome().trim();

                if (nomeObjeto.equalsIgnoreCase(nomeSaindoLimpo)) {
                    jp.setTime("Nenhum");
                    jp.setStatus("Reserva");
                    jp.setFuncao("LIN");
                    break;
                }
            }
        }
    }

    /**
     * Realiza a troca manual direta de um jogador entre o time Azul e Vermelho.
     * Válido apenas se nenhum evento de jogo tiver sido registrado.
     */
    public boolean trocarJogadorEntreTimesManual(Partida partida, String nomeJogador, boolean temEventos) {
        if (temEventos) {
            return false; // Bloqueia a alteração se a partida já estiver rolando com eventos
        }

        Jogador alvo = null;
        boolean estavaNoAzul = false;

        // Procura no Time Azul
        for (Jogador j : partida.getJogadoresAzul()) {
            String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido() : j.getNome();
            if (nomeJ.equalsIgnoreCase(nomeJogador)) {
                alvo = j;
                estavaNoAzul = true;
                break;
            }
        }

        // Se não achou, procura no Vermelho
        if (alvo == null) {
            for (Jogador j : partida.getJogadoresVermelho()) {
                String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                        : j.getNome();
                if (nomeJ.equalsIgnoreCase(nomeJogador)) {
                    alvo = j;
                    estavaNoAzul = false;
                    break;
                }
            }
        }

        // Efetua a troca de listas físicas da entidade e sincroniza a lista geral
        if (alvo != null) {
            if (estavaNoAzul) {
                partida.getJogadoresAzul().remove(alvo);
                partida.getJogadoresVermelho().add(alvo);
                atualizarTimeListaGeral(partida, alvo, "Vermelho");
            } else {
                partida.getJogadoresVermelho().remove(alvo);
                partida.getJogadoresAzul().add(alvo);
                atualizarTimeListaGeral(partida, alvo, "Azul");
            }
            return true;
        }
        return false;
    }

    private void atualizarTimeListaGeral(Partida partida, Jogador jogador, String novoTime) {
        if (partida.getListaGeralPresenca() != null) {
            for (JogadorPartida jp : partida.getListaGeralPresenca()) {
                if (jp.getJogador().getNome().equalsIgnoreCase(jogador.getNome())) {
                    jp.setTime(novoTime);
                    break;
                }
            }
        }
    }

    public void atualizarSubstituicaoNaListaPresenca(Partida partida, String nomeSaindo, String nomeEntrando,
            String timeAlvo) {
        if (partida == null || partida.getListaGeralPresenca() == null)
            return;

        String nomeSaindoLimpo = obterNomeAtivo(nomeSaindo);
        String nomeEntrandoLimpo = obterNomeAtivo(nomeEntrando);

        JogadorPartida jSaindo = null;
        JogadorPartida jEntrando = null;

        for (JogadorPartida jp : partida.getListaGeralPresenca()) {
            String nomeJ = (jp.getJogador().getApelido() != null &&
                    !jp.getJogador().getApelido().trim().isEmpty())
                            ? jp.getJogador().getApelido()
                            : jp.getJogador().getNome();

            if (nomeJ.equalsIgnoreCase(nomeSaindoLimpo))
                jSaindo = jp;
            if (nomeJ.equalsIgnoreCase(nomeEntrandoLimpo))
                jEntrando = jp;
        }

        if (jEntrando != null) {
            jEntrando.setTime(timeAlvo);
            jEntrando.setStatus("Reserva");
            if (jSaindo != null) {
                jEntrando.setFuncao(jSaindo.getFuncao()); // 🪑 O Reserva herda a cadeira completa (Ex: Azul_LIN_3)
            }
        }

        if (jSaindo != null) {
            // 🛑 SOLTA A CAMISA! Vira "Nenhum" para driblar o bloqueio e aparecer no banco
            // da Tela!
            jSaindo.setTime("Nenhum");
            jSaindo.setStatus("Substituido");
        }
    }

    /**
     * Busca todos os jogadores ativos do banco, remove os que já fazem parte da
     * partida
     * e ordena o restante priorizando as posições táticas da várzea.
     */
    public List<Jogador> obterAtrasadosDisponiveisOrdenados(Partida partida) {
        List<Jogador> disponiveisDeFato = new ArrayList<>();
        try {
            // Reutiliza o jogadorDAO injetado no seu Service
            List<Jogador> todosAtivos = jogadorDAO.buscarPorStatus("Ativo");

            for (Jogador j : todosAtivos) {
                boolean jaRelacionado = false;
                String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                        : j.getNome();

                if (partida.getListaGeralPresenca() != null) {
                    for (JogadorPartida jp : partida.getListaGeralPresenca()) {
                        String nomeJP = (jp.getJogador().getApelido() != null
                                && !jp.getJogador().getApelido().trim().isEmpty())
                                        ? jp.getJogador().getApelido()
                                        : jp.getJogador().getNome();
                        if (nomeJP.equalsIgnoreCase(nomeJ)) {
                            jaRelacionado = true;
                            break;
                        }
                    }
                }
                if (!jaRelacionado) {
                    disponiveisDeFato.add(j);
                }
            }

            // Ordenação tática: GOL -> LAT -> ZAG -> MEI -> ATA
            disponiveisDeFato.sort((j1, j2) -> {
                String p1 = j1.getPosicao() != null ? j1.getPosicao().toUpperCase().trim() : "";
                String p2 = j2.getPosicao() != null ? j2.getPosicao().toUpperCase().trim() : "";

                java.util.Map<String, Integer> ordemPos = java.util.Map.of(
                        "GOLEIRO", 1, "LATERAL", 2, "ZAGUEIRO", 3, "MEIA", 4, "ATACANTE", 5);

                int peso1 = ordemPos.getOrDefault(p1, 6);
                int peso2 = ordemPos.getOrDefault(p2, 6);

                if (peso1 != peso2)
                    return Integer.compare(peso1, peso2);

                String n1 = (j1.getApelido() != null && !j1.getApelido().trim().isEmpty()) ? j1.getApelido()
                        : j1.getNome();
                String n2 = (j2.getApelido() != null && !j2.getApelido().trim().isEmpty()) ? j2.getApelido()
                        : j2.getNome();
                return n1.compareToIgnoreCase(n2);
            });

        } catch (Exception e) {
            System.err.println("Erro ao processar lista de atrasados no Service: " + e.getMessage());
        }
        return disponiveisDeFato;
    }

    /**
     * Realiza a inversão direta (swap) entre dois jogadores de times opostos.
     * Mantém o equilíbrio de 11 contra 11 intacto.
     */
    public boolean permutarJogadoresEntreTimesManual(Partida partida, String nomeA, String nomeB, boolean temEventos) {
        if (temEventos) {
            return false; // Bloqueia se a partida já tiver gols ou cartões
        }

        Jogador jogadorA = null;
        Jogador jogadorB = null;
        boolean aEstavaNoAzul = false;

        // Localiza o Jogador A nas listas reais
        for (Jogador j : partida.getJogadoresAzul()) {
            String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido() : j.getNome();
            if (nomeJ.equalsIgnoreCase(nomeA)) {
                jogadorA = j;
                aEstavaNoAzul = true;
                break;
            }
        }
        if (jogadorA == null) {
            for (Jogador j : partida.getJogadoresVermelho()) {
                String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                        : j.getNome();
                if (nomeJ.equalsIgnoreCase(nomeA)) {
                    jogadorA = j;
                    aEstavaNoAzul = false;
                    break;
                }
            }
        }

        // Localiza o Jogador B no time oposto ao do Jogador A
        List<Jogador> timeOposto = aEstavaNoAzul ? partida.getJogadoresVermelho() : partida.getJogadoresAzul();
        for (Jogador j : timeOposto) {
            String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido() : j.getNome();
            if (nomeJ.equalsIgnoreCase(nomeB)) {
                jogadorB = j;
                break;
            }
        }

        // Se achou os dois, faz a mágica da inversão nas listas
        if (jogadorA != null && jogadorB != null) {
            if (aEstavaNoAzul) {
                partida.getJogadoresAzul().remove(jogadorA);
                partida.getJogadoresVermelho().remove(jogadorB);

                partida.getJogadoresAzul().add(jogadorB);
                partida.getJogadoresVermelho().add(jogadorA);

                atualizarTimeListaGeral(partida, jogadorA, "Vermelho");
                atualizarTimeListaGeral(partida, jogadorB, "Azul");
            } else {
                partida.getJogadoresVermelho().remove(jogadorA);
                partida.getJogadoresAzul().remove(jogadorB);

                partida.getJogadoresVermelho().add(jogadorB);
                partida.getJogadoresAzul().add(jogadorA);

                atualizarTimeListaGeral(partida, jogadorA, "Azul");
                atualizarTimeListaGeral(partida, jogadorB, "Vermelho");
            }
            return true;
        }
        return false;
    }

    /**
     * Verifica se a partida já possui alguma estatística ou evento registrado
     * para qualquer um dos jogadores na lista geral de presença.
     */
    public boolean partidaPossuiEventos(Partida partida) {
        if (partida == null || partida.getListaGeralPresenca() == null) {
            return false;
        }

        for (JogadorPartida jp : partida.getListaGeralPresenca()) {
            // Se o cara tiver gol, amarelo ou vermelho, a partida já está valendo!
            if (jp.getGols() > 0 || jp.getCartaoAmarelo() > 0 || jp.getCartaoVermelho() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se qualquer uma das linhas da tabela de jogo já possui
     * algum emoji ou texto de evento registrado em tempo real.
     */
    public boolean temEventosNaTela(List<String> todosEventosDaTela) {
        for (String ev : todosEventosDaTela) {
            if (ev != null && !ev.trim().isEmpty()) {
                return true; // Se achou qualquer token (⚽, 🟨, 🟥), bloqueia!
            }
        }
        return false;
    }

    /**
     * RN: Verifica se o time selecionado possui algum goleiro oficial/natural
     * escalado como titular de origem.
     */
    public boolean temGoleiroNaturalNoTime(Partida partida, boolean isAzul) {
        String timeAlvo = isAzul ? "Azul" : "Vermelho";
        if (partida != null && partida.getListaGeralPresenca() != null) {
            for (JogadorPartida jp : partida.getListaGeralPresenca()) {
                if (timeAlvo.equalsIgnoreCase(jp.getTime()) && "Titular".equalsIgnoreCase(jp.getStatus())) {
                    if (jp.getJogador().getPosicao() != null
                            && "GOLEIRO".equalsIgnoreCase(jp.getJogador().getPosicao().trim())) {
                        return true; // Encontrou um goleiro de ofício
                    }
                }
            }
        }
        return false;
    }

    /**
     * RN: Varre as posições atuais da tela para checar se já existe um arqueiro em
     * campo.
     */
    public boolean jaTemAlguemNoGol(List<String> posicoesAtuaisDaTela) {
        for (String pos : posicoesAtuaisDaTela) {
            if ("GOL".equalsIgnoreCase(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * RN: Localiza o jogador na lista de presença e devolve a sua sigla tática
     * original de cadastro.
     */
    public String obterSiglaPosicaoOriginal(Partida partida, String nomeJogador) {
        if (partida == null || partida.getListaGeralPresenca() == null)
            return "LIN";

        for (JogadorPartida jp : partida.getListaGeralPresenca()) {
            if (jp.getJogador() != null) {
                String nomeObjeto = (jp.getJogador().getApelido() != null
                        && !jp.getJogador().getApelido().trim().isEmpty())
                                ? jp.getJogador().getApelido().trim()
                                : jp.getJogador().getNome().trim();

                if (nomeObjeto.equalsIgnoreCase(nomeJogador.trim())) {
                    String pos = jp.getJogador().getPosicao();
                    if (pos == null)
                        return "LIN";
                    switch (pos.toUpperCase().trim()) {
                        case "GOLEIRO":
                            return "GOL";
                        case "LATERAL":
                            return "LAT";
                        case "ZAGUEIRO":
                            return "ZAG";
                        case "MEIA":
                            return "MEI";
                        case "ATACANTE":
                            return "ATA";
                        default:
                            return "LIN";
                    }
                }
            }
        }
        return "LIN";
    }

    /**
     * RN: Carrega a partida do histórico recheando os arrays com os dados do banco.
     * Garante que a lista geral de presença seja salva para uso posterior.
     */
    public Partida carregarPartidaCompletaDoHistorico(Partida partidaIncompleta) {
        if (partidaIncompleta == null)
            return null;

        br.com.cana.dao.JogadorPartidaDAO jpDAO = new br.com.cana.dao.JogadorPartidaDAO();
        List<br.com.cana.model.JogadorPartida> presencaReal = jpDAO.buscarPorPartida(partidaIncompleta.getId());

        // 🌟 FUNDAMENTAL: Salva a lista vinda do banco dentro do objeto partida!
        partidaIncompleta.setListaGeralPresenca(presencaReal);

        // Limpa as listas da entidade para o preenchimento limpo
        partidaIncompleta.getJogadoresAzul().clear();
        partidaIncompleta.getJogadoresVermelho().clear();

        for (br.com.cana.model.JogadorPartida jp : presencaReal) {
            if (jp.getJogador() != null && jp.getTime() != null) {
                String timeSalvo = jp.getTime().trim();
                if ("Azul".equalsIgnoreCase(timeSalvo)) {
                    partidaIncompleta.getJogadoresAzul().add(jp.getJogador());
                } else if ("Vermelho".equalsIgnoreCase(timeSalvo)) {
                    partidaIncompleta.getJogadoresVermelho().add(jp.getJogador());
                }
            }
        }
        return partidaIncompleta;
    }

    /**
     * RN: Agrupa TODOS os jogadores pelo slot de função tática, combinando
     * infinitamente no formato "Nome A / Nome B / Nome C" com seus respectivos
     * emojis.
     */
    public List<Object[]> gerarLinhasGridHistorico(Partida partida, String timeFiltrado) {
        List<Object[]> linhasDoGrid = new ArrayList<>();
        if (partida == null || partida.getListaGeralPresenca() == null || partida.getListaGeralPresenca().isEmpty()) {
            return linhasDoGrid;
        }

        // 1. Filtra os jogadores do time e agrupa pelo slot (Ex: Azul_LIN_3)
        java.util.Map<String, List<br.com.cana.model.JogadorPartida>> agrupadoPorFuncao = new java.util.LinkedHashMap<>();

        for (br.com.cana.model.JogadorPartida jp : partida.getListaGeralPresenca()) {
            String timeBanco = jp.getTime() != null ? jp.getTime().trim() : "";
            String funcBanco = jp.getFuncao() != null ? jp.getFuncao().trim() : "";

            // Pega os ativos do time e também a galera que foi substituída (que fica com o
            // time "Nenhum" mas herda a função)
            if (timeBanco.equalsIgnoreCase(timeFiltrado) || funcBanco.startsWith(timeFiltrado + "_")) {
                String funcaoSlot = funcBanco.isEmpty() ? "Linha" : funcBanco;
                agrupadoPorFuncao.computeIfAbsent(funcaoSlot, k -> new ArrayList<>()).add(jp);
            }
        }

        // 2. Monta as linhas da JTable dinamicamente
        for (java.util.Map.Entry<String, List<br.com.cana.model.JogadorPartida>> entry : agrupadoPorFuncao.entrySet()) {
            List<br.com.cana.model.JogadorPartida> jogadoresNoSlot = entry.getValue();

            StringBuilder nomesBuilder = new StringBuilder();
            StringBuilder eventosBuilder = new StringBuilder();
            String posFinal = entry.getKey();

            for (int i = 0; i < jogadoresNoSlot.size(); i++) {
                br.com.cana.model.JogadorPartida jp = jogadoresNoSlot.get(i);
                br.com.cana.model.Jogador j = jp.getJogador();

                if (j == null)
                    continue;

                String nome = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                        : j.getNome();
                String ev = formatarEmojisHistoricos(jp);

                // Garante um espaço vazio visual para alinhar as barras caso o jogador não
                // tenha gols/cartões
                if (ev.isEmpty())
                    ev = " ";

                if (i == 0) {
                    posFinal = j.getPosicao();
                    nomesBuilder.append(nome);
                    eventosBuilder.append(ev);
                } else {
                    nomesBuilder.append(" / ").append(nome);
                    eventosBuilder.append(" / ").append(ev);
                }
            }

            String eventosFinal = eventosBuilder.toString();

            // Se absolutamente ninguém dessa linha fez gol ou tomou cartão, limpa tudo para
            // não ficar uma barra "/" voando na tela
            if (eventosFinal.replace("/", "").trim().isEmpty()) {
                eventosFinal = "";
            }

            String posCurta = encurtarPosicaoInterna(posFinal);
            linhasDoGrid.add(new Object[] { nomesBuilder.toString(), posCurta, eventosFinal });
        }

        return linhasDoGrid;
    }

    private String formatarEmojisHistoricos(br.com.cana.model.JogadorPartida jp) {
        StringBuilder emojis = new StringBuilder();
        // 🌟 CORREÇÃO: Agora os emojis têm um ESPAÇO no final para o HTML conseguir ler
        // e separar!
        for (int g = 0; g < jp.getGols(); g++)
            emojis.append("⚽ ");
        for (int a = 0; a < jp.getCartaoAmarelo(); a++)
            emojis.append("🟨 ");
        for (int v = 0; v < jp.getCartaoVermelho(); v++)
            emojis.append("🟥 ");

        return emojis.toString().trim(); // Retira o espaço sobrando do último emoji
    }

    private String encurtarPosicaoInterna(String posicao) {
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
        if (posicao.contains("FIXO"))
            return "FIX";
        if (posicao.contains("ALA"))
            return "ALA";
        if (posicao.contains("PIVÔ") || posicao.contains("PIVO"))
            return "PIV";
        return posicao.length() > 3 ? posicao.substring(0, 3) : posicao;
    }

    // 📊 PREPARADOR DA TELA DE ESTATÍSTICAS: Calcula a presença e converte para
    // String
    public List<Object[]> carregarDadosTelaEstatisticas(int temporadaId) {
        List<Object[]> linhasParaTabela = new ArrayList<>();

        // 1. Descobre o total de partidas válidas dessa temporada
        // Reutilizando o seu método listarPartidasDaTemporada que já está pronto!
        List<Partida> partidasDaTemporada = listarPartidasDaTemporada(temporadaId);
        int totalPartidas = partidasDaTemporada.size();

        // Se não teve nenhuma partida, retorna a lista vazia
        if (totalPartidas == 0) {
            return linhasParaTabela;
        }

        // 2. Busca os dados brutos de gols e cartões do banco
        br.com.cana.dao.JogadorPartidaDAO jpDAO = new br.com.cana.dao.JogadorPartidaDAO();
        List<Object[]> dadosBrutos = jpDAO.buscarEstatisticasGeraisDaTemporada(temporadaId);

        // 3. Calcula a % e converte tudo para String (exigência do TableRowSorter que
        // criamos)
        for (Object[] dado : dadosBrutos) {
            String nome = (String) dado[0];
            int gols = (int) dado[1];
            int ca = (int) dado[2];
            int cv = (int) dado[3];
            int partidasJogadas = (int) dado[4];

            // A matemática da Presença: (Partidas Participadas * 100) / Total de Partidas
            // do Ano
            int presenca = (partidasJogadas * 100) / totalPartidas;

            linhasParaTabela.add(new Object[] {
                    nome,
                    String.valueOf(gols),
                    String.valueOf(ca),
                    String.valueOf(cv),
                    presenca + "%" // Adiciona o símbolo para ficar perfeito no grid
            });
        }

        return linhasParaTabela;
    }

    /**
     * 🛠️ IMPROVISAR GOLEIRO: Altera temporariamente a posição do jogador para
     * Goleiro
     * na sessão atual da partida, garantindo que ele vá para o topo das listas.
     */
    public boolean improvisarGoleiroTemporario(Partida partida, String nomeJogador) {
        if (partida == null || partida.getListaGeralPresenca() == null)
            return false;

        String nomeLimpo = obterNomeAtivo(nomeJogador);

        for (br.com.cana.model.JogadorPartida jp : partida.getListaGeralPresenca()) {
            String nomeObjeto = (jp.getJogador().getApelido() != null && !jp.getJogador().getApelido().trim().isEmpty())
                    ? jp.getJogador().getApelido().trim()
                    : jp.getJogador().getNome().trim();

            if (nomeObjeto.equalsIgnoreCase(nomeLimpo)) {
                // 1. Muda a posição na instância da memória (vai refletir nas JTables)
                jp.getJogador().setPosicao("GOLEIRO");

                // 2. Atualiza a função tática da súmula de LIN para GOL
                String funcaoAtual = jp.getFuncao();
                if (funcaoAtual != null && funcaoAtual.contains("LIN")) {
                    jp.setFuncao(funcaoAtual.replace("LIN", "GOL"));
                } else if (funcaoAtual == null || funcaoAtual.isEmpty()) {
                    jp.setFuncao("GOL");
                }
                return true;
            }
        }
        return false;
    }

}