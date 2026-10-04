package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.Jogador;
import br.com.cana.gestorcana_api.entity.Partida;
import br.com.cana.gestorcana_api.repository.JogadorRepository;
import br.com.cana.gestorcana_api.repository.PartidaRepository;
import br.com.cana.gestorcana_api.util.SorteioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PartidaService {

    @Autowired
    private PartidaRepository partidaRepository;

    @Autowired
    private JogadorRepository jogadorRepository;

    // @Autowired
    // private ContribuicaoService contribuicaoService;

    @Autowired
    private br.com.cana.gestorcana_api.repository.JogadorPartidaRepository jogadorPartidaRepository;

    @Autowired
    private br.com.cana.gestorcana_api.repository.EventoRepository eventoRepository;

    public PartidaService() {
    }

    public PartidaService(PartidaRepository partidaRepository, JogadorRepository jogadorRepository,
            ContribuicaoService contribuicaoService,
            br.com.cana.gestorcana_api.repository.JogadorPartidaRepository jogadorPartidaRepository) {
        this.partidaRepository = partidaRepository;
        this.jogadorRepository = jogadorRepository;
        // this.contribuicaoService = contribuicaoService;
        this.jogadorPartidaRepository = jogadorPartidaRepository;
    }

    public List<Partida> listarTodas() {
        return partidaRepository.findAll();
    }

    public Optional<Partida> buscarPorId(Integer id) {
        if (id == null)
            return Optional.empty();
        return partidaRepository.findById(id);
    }

    public void sortearTimesTatico(Partida partida, List<Jogador> presentes, String formacaoAzul,
            String formacaoVermelho) {
        partida.getJogadoresAzul().clear();
        partida.getJogadoresVermelho().clear();

        List<Jogador> titulares = new ArrayList<>();
        List<Jogador> deEspera = new ArrayList<>();
        List<Jogador> todosGoleiros = new ArrayList<>();
        List<Jogador> todosLinha = new ArrayList<>();

        for (Jogador j : presentes) {
            if (Boolean.TRUE.equals(j.getEstaSuspenso()))
                deEspera.add(j);
            else if (j.getPosicao() != null && "GOLEIRO".equalsIgnoreCase(j.getPosicao().trim()))
                todosGoleiros.add(j);
            else
                todosLinha.add(j);
        }

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

        SorteioUtil.realizarSorteio(partida, titulares, formacaoAzul);

        // 🌟 A MÁGICA RESTAURADA: Encaixa os 11 de cada lado na formação cravada!
        List<br.com.cana.gestorcana_api.entity.JogadorPartida> listaPresenca = new ArrayList<>();
        
        alocarPosicoesFixas(partida.getJogadoresAzul(), listaPresenca, "Azul", formacaoAzul);
        alocarPosicoesFixas(partida.getJogadoresVermelho(), listaPresenca, "Vermelho", formacaoVermelho);

        // O que sobrou vai pro banco de reservas
        for (Jogador j : deEspera) {
            br.com.cana.gestorcana_api.entity.JogadorPartida jp = new br.com.cana.gestorcana_api.entity.JogadorPartida();
            jp.setJogadorId(j.getId());
            jp.setJogador(j);
            jp.setTime("Nenhum");
            jp.setStatus("Reserva");
            jp.setFuncao(j.getPosicao() != null && j.getPosicao().toUpperCase().contains("GOL") ? "GOL" : "LIN");
            listaPresenca.add(jp);
        }

        partida.setListaGeralPresenca(listaPresenca);
    }

    public boolean salvarPartida(Partida p) {
        if (p == null) {
            return false;
        }
        partidaRepository.save(p);
        return true;
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
        return partidaRepository.findByTemporadaId(temporadaId);
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

        if (atual == null || atual.trim().isEmpty() || atual.trim().equals("Não escalado")
                || atual.trim().equals("Selecione...") || atual.trim().equals("____")) {
            novoTexto = reservaEntrando;
        } else if (atual.endsWith(" / ____")) {
            novoTexto = atual.substring(0, atual.lastIndexOf(" / ____")) + " / " + reservaEntrando;
        } else {
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
            Jogador j = buscarJogadorPorNomeOuApelido(nome);
            if (j != null && j.getPosicao() != null && !j.getPosicao().trim().isEmpty()) {
                return j.getPosicao();
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
            eventosUltimo = eventosUltimo.replaceFirst(java.util.regex.Pattern.quote(tokenEvento),
                    "").replaceAll("  ", " ").trim();
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
        if (texto == null || texto.trim().isEmpty() || texto.equals("Selecione...") || texto.equals("Não escalado")) {
            return "";
        }

        String nomeAtivo = texto;
        if (texto.contains(" / ")) {
            String[] partes = texto.split(" / ");
            nomeAtivo = partes[partes.length - 1].trim();
        }

        if (nomeAtivo.contains("____") || nomeAtivo.isEmpty()) {
            return "";
        }

        if (nomeAtivo.contains(" (")) {
            return nomeAtivo.substring(0, nomeAtivo.indexOf(" (")).trim();
        }

        return nomeAtivo.trim();
    }

    public String validarRestricaoParaLinha(String reservaCompleto) {
        try {
            String nomeLimpo = obterNomeAtivo(reservaCompleto);
            Jogador jogador = buscarJogadorPorNomeOuApelido(nomeLimpo);

            if (jogador != null) {
                if (Boolean.TRUE.equals(jogador.getEstaSuspenso())) {
                    return "O jogador '" + nomeLimpo
                            + "' está SUSPENSO! É obrigatório cumprir suspensão na arbitragem.";
                }

            }
        } catch (Exception e) {
            System.err.println("Erro ao validar restrição de linha: " + e.getMessage());
        }
        return null;
    }

    public String validarRestricaoParaSubstituicao(String reservaCompleto) {
        try {
            String nomeLimpo = obterNomeAtivo(reservaCompleto);
            Jogador jogador = buscarJogadorPorNomeOuApelido(nomeLimpo);

            if (jogador != null && Boolean.TRUE.equals(jogador.getEstaSuspenso())) {
                return "O jogador '" + nomeLimpo + "' está SUSPENSO! É obrigatório cumprir suspensão na arbitragem.";
            }
        } catch (Exception e) {
            System.err.println("Erro ao validar restrição de substituição: " + e.getMessage());
        }
        return null;
    }

    public String validarRestricaoParaArbitragem(Partida partida, Jogador jogador) {
        if (partida == null || jogador == null) {
            return "Dados inválidos para validação.";
        }

        String nomeJogador = (jogador.getApelido() != null && !jogador.getApelido().trim().isEmpty())
                ? jogador.getApelido()
                : jogador.getNome();

        if (nomeJogador.equalsIgnoreCase(obterNomeAtivo(partida.getArbitro())) ||
                nomeJogador.equalsIgnoreCase(obterNomeAtivo(partida.getBandeira1())) ||
                nomeJogador.equalsIgnoreCase(obterNomeAtivo(partida.getBandeira2()))) {
            return "❌ Este jogador já está atuando na equipe de arbitragem agora.";
        }

        return null;
    }

    public void processarLimpezaDeSuspensaoPosJogo(List<String> nomesDaArbitragem, Partida partidaAtual) {
        try {
            for (String nomeRaw : nomesDaArbitragem) {
                String nomeLimpo = obterNomeAtivo(nomeRaw);
                if (nomeLimpo.isEmpty())
                    continue;

                Jogador jogador = buscarJogadorPorNomeOuApelido(nomeLimpo);

                if (jogador != null && Boolean.TRUE.equals(jogador.getEstaSuspenso())) {
                    jogador.setEstaSuspenso(false);
                    jogador.setStatus("Ativo");
                    
                    // Zera os cartões iniciais de cadastro (só por garantia)
                    jogador.setcAmarelosIniciais(0);
                    jogador.setcVermelhosIniciais(0);
                    jogadorRepository.save(jogador);

                    // 👇 A MÁGICA: ZERA OS CARTÕES DO HISTÓRICO, MANTENDO GOLS E PRESENÇA INTACTOS
                    List<br.com.cana.gestorcana_api.entity.JogadorPartida> atuacoes = jogadorPartidaRepository.findByJogadorId(jogador.getId());
                    for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : atuacoes) {
                        if ((jp.getCartaoAmarelo() != null && jp.getCartaoAmarelo() > 0) || 
                            (jp.getCartaoVermelho() != null && jp.getCartaoVermelho() > 0)) {
                            
                            jp.setCartaoAmarelo(0);
                            jp.setCartaoVermelho(0);
                            jogadorPartidaRepository.save(jp);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro durante a limpeza de suspensão: " + e.getMessage());
        }
    }

    public void salvarSumulaProvisoria(Partida partida, String textoSumula) {
        if (partida != null) {
            partida.setSumula(textoSumula);
            partidaRepository.save(partida);
        }
    }

    public List<String> extrairNomesParaExibicao(List<Jogador> jogadores) {
        List<String> listaNomes = new ArrayList<>();
        if (jogadores != null) {
            for (Jogador j : jogadores) {
                String nomeExibir = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                        : j.getNome();
                listaNomes.add(nomeExibir);
            }
        }
        return listaNomes;
    }

    public boolean finalizarEFecharPartida(Partida partida, String gridAzulJson, String gridVermelhoJson) {
        if (partida == null)
            return false;

        List<String> nomesDaArbitragem = new ArrayList<>();
        nomesDaArbitragem.add(partida.getArbitro());
        nomesDaArbitragem.add(partida.getBandeira1());
        nomesDaArbitragem.add(partida.getBandeira2());

        processarLimpezaDeSuspensaoPosJogo(nomesDaArbitragem, partida);

        partida.setGridAzul(gridAzulJson);
        partida.setGridVermelho(gridVermelhoJson);

        // 1. Salva a Partida no banco primeiro para gerar um ID real
        Partida partidaSalva = partidaRepository.save(partida);

        // 2. Limpa dados antigos dessa partida para evitar duplicidade (caso o usuário
        // finalize 2 vezes)
        jogadorPartidaRepository.deleteByPartidaId(partidaSalva.getId());

        // 3. Destrincha o visual da JTable e salva os Gols/Cartões no banco para a Tela
        // de Estatísticas!
        consolidarEstatisticasJTable(partidaSalva, gridAzulJson, "Azul");
        consolidarEstatisticasJTable(partidaSalva, gridVermelhoJson, "Vermelho");

        return true;
    }

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
            novoTexto = nomeNovo;
        } else if (acumularHistorico) {
            if (atual.endsWith(" / ____")) {
                novoTexto = atual.substring(0, atual.lastIndexOf(" / ____")) + " / " + nomeNovo;
            } else {
                novoTexto = atual + " / " + nomeNovo;
            }
        } else {
            if (atual.contains(" / ")) {
                novoTexto = atual.substring(0, atual.lastIndexOf(" / ")).trim() + " / " + nomeNovo;
            } else {
                novoTexto = nomeNovo;
            }
        }

        if (cargo.equalsIgnoreCase("ARBITRO"))
            partida.setArbitro(novoTexto);
        else if (cargo.equalsIgnoreCase("BANDEIRA1"))
            partida.setBandeira1(novoTexto);
        else if (cargo.equalsIgnoreCase("BANDEIRA2"))
            partida.setBandeira2(novoTexto);
    }

    public void removerDaArbitragem(Partida partida, String cargo, boolean gerarHistorico) {
        if (partida == null || cargo == null)
            return;

        String atual = "";
        if (cargo.equalsIgnoreCase("ARBITRO"))
            atual = partida.getArbitro();
        else if (cargo.equalsIgnoreCase("BANDEIRA1"))
            atual = partida.getBandeira1();
        else if (cargo.equalsIgnoreCase("BANDEIRA2"))
            atual = partida.getBandeira2();

        if (atual != null && !atual.trim().isEmpty()) {
            String novoTexto;

            // Se já tem histórico (ex: "Gui / Lucas"), ao remover o Lucas, mantém o Gui e abre a vaga com ____
            if (atual.contains(" / ")) {
                novoTexto = atual.substring(0, atual.lastIndexOf(" / ")).trim() + " / ____";
            } else if (gerarHistorico) {
                // Aplica a regra de preservar o histórico do titular que foi pro banco
                novoTexto = atual + " / ____";
            } else {
                novoTexto = "____";
            }

            if (cargo.equalsIgnoreCase("ARBITRO"))
                partida.setArbitro(novoTexto);
            else if (cargo.equalsIgnoreCase("BANDEIRA1"))
                partida.setBandeira1(novoTexto);
            else if (cargo.equalsIgnoreCase("BANDEIRA2"))
                partida.setBandeira2(novoTexto);
        }
    }

    public boolean trocarJogadorEntreTimesManual(Partida partida, String nomeJogador, boolean temEventos) {
        if (temEventos)
            return false;

        Jogador alvo = null;
        boolean estavaNoAzul = false;

        for (Jogador j : partida.getJogadoresAzul()) {
            String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido() : j.getNome();
            if (nomeJ.equalsIgnoreCase(nomeJogador)) {
                alvo = j;
                estavaNoAzul = true;
                break;
            }
        }

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

        if (alvo != null) {
            if (estavaNoAzul) {
                partida.getJogadoresAzul().remove(alvo);
                partida.getJogadoresVermelho().add(alvo);
            } else {
                partida.getJogadoresVermelho().remove(alvo);
                partida.getJogadoresAzul().add(alvo);
            }
            return true;
        }
        return false;
    }

    public List<Jogador> obterAtrasadosDisponiveisOrdenados(Partida partida) {
        List<Jogador> disponiveisDeFato = new ArrayList<>();
        try {
            List<Jogador> todosAtivos = jogadorRepository.findAll().stream()
                    .filter(Jogador::getAtivo)
                    .collect(java.util.stream.Collectors.toList());

            for (Jogador j : todosAtivos) {
                String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                        : j.getNome();
                boolean jaRelacionado = false;

                for (Jogador azul : partida.getJogadoresAzul()) {
                    String n = (azul.getApelido() != null && !azul.getApelido().trim().isEmpty()) ? azul.getApelido()
                            : azul.getNome();
                    if (n.equalsIgnoreCase(nomeJ)) {
                        jaRelacionado = true;
                        break;
                    }
                }
                if (!jaRelacionado) {
                    for (Jogador vermelho : partida.getJogadoresVermelho()) {
                        String n = (vermelho.getApelido() != null && !vermelho.getApelido().trim().isEmpty())
                                ? vermelho.getApelido()
                                : vermelho.getNome();
                        if (n.equalsIgnoreCase(nomeJ)) {
                            jaRelacionado = true;
                            break;
                        }
                    }
                }

                // Tambem exclui quem ja esta no banco de reservas (ou em qualquer outro
                // status) da partida -- so estar em campo (jogadoresAzul/Vermelho) nao
                // bastava: um jogador ja substituido para o banco continuava aparecendo
                // como "disponivel" para o chegou-atrasado.
                if (!jaRelacionado && partida.getListaGeralPresenca() != null) {
                    for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : partida.getListaGeralPresenca()) {
                        if (jp.getJogador() == null)
                            continue;
                        String n = (jp.getJogador().getApelido() != null && !jp.getJogador().getApelido().trim().isEmpty())
                                ? jp.getJogador().getApelido()
                                : jp.getJogador().getNome();
                        if (n != null && n.equalsIgnoreCase(nomeJ)) {
                            jaRelacionado = true;
                            break;
                        }
                    }
                }

                if (!jaRelacionado) {
                    disponiveisDeFato.add(j);
                }
            }

            disponiveisDeFato.sort((j1, j2) -> {
                String p1 = j1.getPosicao() != null ? j1.getPosicao().toUpperCase().trim() : "";
                String p2 = j2.getPosicao() != null ? j2.getPosicao().toUpperCase().trim() : "";

                Map<String, Integer> ordemPos = Map.of("GOLEIRO", 1, "LATERAL", 2, "ZAGUEIRO", 3, "VOLANTE", 4, "MEIA", 5, "ATACANTE",
                        5);
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

    public boolean permutarJogadoresEntreTimesManual(Partida partida, String nomeA, String nomeB, boolean temEventos) {
        if (temEventos)
            return false;

        Jogador jogadorA = null;
        Jogador jogadorB = null;
        boolean aEstavaNoAzul = false;

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

        List<Jogador> timeOposto = aEstavaNoAzul ? partida.getJogadoresVermelho() : partida.getJogadoresAzul();
        for (Jogador j : timeOposto) {
            String nomeJ = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido() : j.getNome();
            if (nomeJ.equalsIgnoreCase(nomeB)) {
                jogadorB = j;
                break;
            }
        }

        if (jogadorA != null && jogadorB != null) {
            if (aEstavaNoAzul) {
                partida.getJogadoresAzul().remove(jogadorA);
                partida.getJogadoresVermelho().remove(jogadorB);
                partida.getJogadoresAzul().add(jogadorB);
                partida.getJogadoresVermelho().add(jogadorA);
            } else {
                partida.getJogadoresVermelho().remove(jogadorA);
                partida.getJogadoresAzul().remove(jogadorB);
                partida.getJogadoresVermelho().add(jogadorB);
                partida.getJogadoresAzul().add(jogadorA);
            }
            return true;
        }
        return false;
    }

    public boolean temEventosNaTela(List<String> todosEventosDaTela) {
        for (String ev : todosEventosDaTela) {
            if (ev != null && !ev.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public String encurtarPosicaoInterna(String posicao) {
        if (posicao == null)
            return "--";
        posicao = posicao.toUpperCase().trim();
        if (posicao.contains("GOLEIRO"))
            return "GOL";
        if (posicao.contains("LATERAL"))
            return "LAT";
        if (posicao.contains("ZAGUEIRO"))
            return "ZAG";
        if (posicao.contains("VOLANTE"))
            return "VOL";
        if (posicao.contains("MEIA"))
            return "MEI";
        if (posicao.contains("ATACANTE"))
            return "ATA";
        return posicao.length() > 3 ? posicao.substring(0, 3) : posicao;
    }

    public boolean filtrarJogadoresPorTexto(String busca, String textoJogador) {
        if (busca == null || busca.trim().isEmpty())
            return true;
        String b = java.text.Normalizer.normalize(busca.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        String t = java.text.Normalizer.normalize(textoJogador.toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        return t.contains(b);
    }

    private Jogador buscarJogadorPorNomeOuApelido(String nome) {
        if (nome == null || nome.trim().isEmpty())
            return null;
        List<Jogador> porApelido = jogadorRepository.findByApelidoIgnoreCase(nome.trim());
        if (!porApelido.isEmpty()) {
            return preferirAtivo(porApelido);
        }
        List<Jogador> porNome = jogadorRepository.findByNomeIgnoreCase(nome.trim());
        if (!porNome.isEmpty()) {
            return preferirAtivo(porNome);
        }
        return null;
    }

    // Um jogador inativo pode ter o mesmo apelido/nome de um jogador atual
    private Jogador preferirAtivo(List<Jogador> candidatos) {
        return candidatos.stream().filter(Jogador::getAtivo).findFirst().orElse(candidatos.get(0));
    }

    // --- MÉTODO PARA ALIMENTAR A TELA DE ESTATÍSTICAS DO SWING ---
    public List<Object[]> carregarDadosTelaEstatisticas(int temporadaId) {
        List<Jogador> jogadores = jogadorRepository.findAll();
        List<Partida> partidasTemporada = partidaRepository.findByTemporadaId(temporadaId);
        List<Object[]> resultado = new ArrayList<>();

        int totalPartidas = partidasTemporada.size();
        List<Integer> idsPartidasTemporada = new ArrayList<>();
        for (Partida p : partidasTemporada) {
            idsPartidasTemporada.add(p.getId());
        }

        for (Jogador j : jogadores) {
            String apelido = (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido()
                    : j.getNome();

            // Busca todas as atuações do jogador salvas no banco
            List<br.com.cana.gestorcana_api.entity.JogadorPartida> atuacoes = jogadorPartidaRepository
                    .findByJogadorId(j.getId());

            int gols = 0, ca = 0, cv = 0, jogosPresente = 0;

            for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : atuacoes) {
                // Conta as estatísticas apenas das partidas desta temporada
                if (idsPartidasTemporada.contains(jp.getPartidaId())) {
                    gols += jp.getGols() != null ? jp.getGols() : 0;
                    ca += jp.getCartaoAmarelo() != null ? jp.getCartaoAmarelo() : 0;
                    cv += jp.getCartaoVermelho() != null ? jp.getCartaoVermelho() : 0;
                    jogosPresente++;
                }
            }

            // Inativo só aparece nas temporadas em que jogou (preserva o histórico)
            if (!j.getAtivo() && jogosPresente == 0) {
                continue;
            }

            String presenca = "0%";
            if (totalPartidas > 0) {
                int porcentagem = (jogosPresente * 100) / totalPartidas;
                presenca = porcentagem + "%";
            }

            resultado.add(
                    new Object[] { apelido, String.valueOf(gols), String.valueOf(ca), String.valueOf(cv), presenca });
        }
        return resultado;
    }

    public void preencherElencosTransientes(Partida partida) {
        if (partida == null) return;
        List<br.com.cana.gestorcana_api.entity.JogadorPartida> relacoes = jogadorPartidaRepository.findByPartidaId(partida.getId());

        for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : relacoes) {
            Optional<Jogador> optJ = jogadorRepository.findById(jp.getJogadorId());
            if (optJ.isPresent()) {
                jp.setJogador(optJ.get()); // 🌟 ESSA É A LINHA QUE FALTAVA (O GSON mandava nulo sem ela!)
                
                if ("Azul".equalsIgnoreCase(jp.getTime())) {
                    partida.getJogadoresAzul().add(optJ.get());
                } else if ("Vermelho".equalsIgnoreCase(jp.getTime())) {
                    partida.getJogadoresVermelho().add(optJ.get());
                }
            } else if (jp.getJogadorId() == 0 || jp.getJogadorId() == null) {
                // Se for um registro genérico "Azul 1" (Incompleto)
                Jogador jGen = new Jogador();
                jGen.setId(0);
                jGen.setNome("Vaga Incompleta");
                jGen.setApelido(jp.getTime() + " Incompleto");
                jp.setJogador(jGen);
            }
            partida.getListaGeralPresenca().add(jp);
        }
    }

    // Destrincha o grid salvo ("J1 / J2" | pos | "ev1 / ev2") em uma atuação
    // por jogador, sem gravar nada.
    private List<br.com.cana.gestorcana_api.entity.JogadorPartida> montarAtuacoesDoGrid(Partida partida, String gridJson,
            String time) {
        List<br.com.cana.gestorcana_api.entity.JogadorPartida> atuacoes = new ArrayList<>();
        if (gridJson == null || gridJson.isEmpty())
            return atuacoes;

        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<Object[]>>() {
        }.getType();
        List<Object[]> linhas = new com.google.gson.Gson().fromJson(gridJson, type);
        if (linhas == null)
            return atuacoes;

        for (Object[] linha : linhas) {
            if (linha == null || linha.length < 3 || linha[0] == null)
                continue;
            String nomes = linha[0].toString();
            String pos = linha[1] != null ? linha[1].toString() : "LIN";
            String eventos = linha[2] != null ? linha[2].toString() : "";

            String[] arrayNomes = nomes.split(" / ");
            String[] arrayEventos = eventos.split(" / ", -1);

            for (int i = 0; i < arrayNomes.length; i++) {
                String nomeLimpo = obterNomeAtivo(arrayNomes[i]);
                Jogador jogador = buscarJogadorPorNomeOuApelido(nomeLimpo);

                if (jogador != null) {
                    br.com.cana.gestorcana_api.entity.JogadorPartida jp = new br.com.cana.gestorcana_api.entity.JogadorPartida();
                    jp.setJogadorId(jogador.getId());
                    jp.setJogador(jogador);
                    jp.setPartidaId(partida.getId());
                    jp.setTime(time);
                    jp.setFuncao(time + "_" + pos);
                    jp.setStatus(i == 0 && arrayNomes.length == 1 ? "TITULAR" : (i == 0 ? "SUBSTITUIDO" : "RESERVA"));

                    String evs = (i < arrayEventos.length) ? arrayEventos[i] : "";

                    // Atenção aqui: Use os emojis ou a codificação ISO que estiver funcionando no seu Java
                    jp.setGols(contarTokensNoBlocoAtivo(evs, "⚽") + contarTokensNoBlocoAtivo(evs, "⚽(C)"));
                    jp.setCartaoAmarelo(contarTokensNoBlocoAtivo(evs, "🟨"));
                    jp.setCartaoVermelho(contarTokensNoBlocoAtivo(evs, "🟥"));
                    atuacoes.add(jp);
                }
            }
        }
        return atuacoes;
    }

    private void consolidarEstatisticasJTable(Partida partida, String gridJson, String time) {
        for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : montarAtuacoesDoGrid(partida, gridJson, time)) {
            Jogador jogador = jp.getJogador();
            int amarelosNaPartida = jp.getCartaoAmarelo();
            int vermelhosNaPartida = jp.getCartaoVermelho();

            // 1. Salva a atuação da partida atual no banco
            jogadorPartidaRepository.save(jp);

            // =========================================================================
            // 2. AVALIAÇÃO DE SUSPENSÃO (Sem o "if" limitador)
            // =========================================================================
            int totalAmarelosAtuais = jogador.getcAmarelosIniciais() != null ? jogador.getcAmarelosIniciais() : 0;

            List<br.com.cana.gestorcana_api.entity.JogadorPartida> historico = jogadorPartidaRepository.findByJogadorId(jogador.getId());

            for (br.com.cana.gestorcana_api.entity.JogadorPartida histJp : historico) {
                // Ignora a partida atual que acabou de ser gravada para não somar em dobro
                if (!histJp.getPartidaId().equals(partida.getId())) {
                    totalAmarelosAtuais += (histJp.getCartaoAmarelo() != null ? histJp.getCartaoAmarelo() : 0);
                }
            }

            int amarelosFinais = totalAmarelosAtuais + amarelosNaPartida;

            // Se tomou vermelho direto OU acumulou 3 amarelos no total geral do campeonato:
            if (vermelhosNaPartida > 0 || amarelosFinais >= 3) {
                jogador.setEstaSuspenso(true);
                jogador.setStatus("Suspenso");
                jogadorRepository.save(jogador);
            }
            // =========================================================================
        }
    }

    public List<Object[]> obterGridHistoricoSalvo(String time, Partida partida) {
        String json = "Azul".equalsIgnoreCase(time) ? partida.getGridAzul() : partida.getGridVermelho();
        if (json != null && !json.isEmpty()) {
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<List<Object[]>>() {
            }.getType();
            return new com.google.gson.Gson().fromJson(json, type);
        }
        return new ArrayList<>(); // Retorna vazio se não tiver o grid salvo na V3
    }

    private List<String> obterTemplateFormacao(String formacao) {
        List<String> pos = new ArrayList<>();
        pos.add("GOL");

        String f = (formacao != null) ? formacao.replaceAll("\\s+", "").trim() : "";
        switch (f) {
            case "4-4-2":
                pos.addAll(
                        java.util.Arrays.asList("LAT", "LAT", "ZAG", "ZAG", "VOL", "VOL", "MEI", "MEI", "ATA", "ATA"));
                break;
            case "4-3-3":
                pos.addAll(
                        java.util.Arrays.asList("LAT", "LAT", "ZAG", "ZAG", "VOL", "MEI", "MEI", "ATA", "ATA", "ATA"));
                break;
            case "3-5-2":
                pos.addAll(
                        java.util.Arrays.asList("ZAG", "ZAG", "ZAG", "VOL", "VOL", "MEI", "MEI", "MEI", "ATA", "ATA"));
                break;
            case "4-5-1":
                pos.addAll(
                        java.util.Arrays.asList("LAT", "LAT", "ZAG", "ZAG", "VOL", "VOL", "MEI", "MEI", "MEI", "ATA"));
                break;
            case "3-4-3":
                pos.addAll(
                        java.util.Arrays.asList("ZAG", "ZAG", "ZAG", "VOL", "VOL", "MEI", "MEI", "MEI", "ATA", "ATA"));
                break;
            case "5-3-2":
                pos.addAll(
                        java.util.Arrays.asList("LAT", "LAT", "ZAG", "ZAG", "ZAG", "VOL", "VOL", "MEI", "ATA", "ATA"));
                break;
            default:
                for (int i = 0; i < 10; i++)
                    pos.add("LIN");
                break;
        }
        return pos;
    }

    private void alocarPosicoesFixas(List<Jogador> time, List<br.com.cana.gestorcana_api.entity.JogadorPartida> listaPresenca, String nomeTime, String formacao) {
        List<String> vagasFixas = obterTemplateFormacao(formacao);
        List<Jogador> naoAlocados = new ArrayList<>(time);
        br.com.cana.gestorcana_api.entity.JogadorPartida[] timeFormatado = new br.com.cana.gestorcana_api.entity.JogadorPartida[vagasFixas.size()];

        // 1. Encaixa os Especialistas
        for (int i = 0; i < vagasFixas.size(); i++) {
            String vagaSigla = vagasFixas.get(i);
            Jogador especialista = null;
            for (Jogador j : naoAlocados) {
                if (encurtarPosicaoInterna(j.getPosicao()).equalsIgnoreCase(vagaSigla)) {
                    especialista = j;
                    break;
                }
            }
            if (especialista != null) {
                br.com.cana.gestorcana_api.entity.JogadorPartida jp = new br.com.cana.gestorcana_api.entity.JogadorPartida();
                jp.setJogadorId(especialista.getId());
                jp.setJogador(especialista);
                jp.setTime(nomeTime);
                jp.setStatus("Titular");
                jp.setFuncao(nomeTime + "_" + vagaSigla + "_" + (i + 1));
                timeFormatado[i] = jp;
                naoAlocados.remove(especialista);
            }
        }

        // 2. Improvisa com a sobra, priorizando a posição taticamente mais próxima
        // da vaga vazia (ex.: falta ATA, tenta MEI antes de LAT/ZAG) em vez de
        // pegar qualquer um da lista.
        for (int i = 0; i < vagasFixas.size(); i++) {
            if (timeFormatado[i] == null && !naoAlocados.isEmpty()) {
                Jogador improvisado = removerMaisProximoTaticamente(naoAlocados, vagasFixas.get(i));
                br.com.cana.gestorcana_api.entity.JogadorPartida jp = new br.com.cana.gestorcana_api.entity.JogadorPartida();
                jp.setJogadorId(improvisado.getId());
                jp.setJogador(improvisado);
                jp.setTime(nomeTime);
                jp.setStatus("Titular");
                jp.setFuncao(nomeTime + "_" + vagasFixas.get(i) + "_" + (i + 1));
                timeFormatado[i] = jp;
            }
        }

        // 3. Completa com um titular "Incompleto" nomeado pela posição da vaga
        // (ex.: "Mei"), disponível para receber um jogador que chegar atrasado.
        for (int i = 0; i < vagasFixas.size(); i++) {
            if (timeFormatado[i] == null) {
                String nomePlaceholder = formatarSiglaPosicao(vagasFixas.get(i));
                Jogador jGen = new Jogador();
                jGen.setId(0);
                jGen.setNome(nomePlaceholder);
                jGen.setApelido(nomePlaceholder);

                br.com.cana.gestorcana_api.entity.JogadorPartida jp = new br.com.cana.gestorcana_api.entity.JogadorPartida();
                jp.setJogadorId(0);
                jp.setJogador(jGen);
                jp.setTime(nomeTime);
                jp.setStatus("Incompleto");
                jp.setFuncao(nomeTime + "_" + vagasFixas.get(i) + "_" + (i + 1));
                timeFormatado[i] = jp;
            }
        }

        for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : timeFormatado) {
            if (jp != null)
                listaPresenca.add(jp);
        }
    }

    // Ordem tática linear (defesa -> ataque) usada para achar, dentre os jogadores
    // que sobraram, o mais próximo da posição vazia quando não há um especialista.
    private static final List<String> ORDEM_TATICA = java.util.Arrays.asList("GOL", "ZAG", "LAT", "VOL", "MEI", "ATA");

    private Jogador removerMaisProximoTaticamente(List<Jogador> naoAlocados, String vagaSigla) {
        int indiceVaga = ORDEM_TATICA.indexOf(vagaSigla);
        if (indiceVaga == -1) {
            return naoAlocados.remove(0);
        }

        Jogador melhor = null;
        int menorDistancia = Integer.MAX_VALUE;
        for (Jogador j : naoAlocados) {
            int indiceJogador = ORDEM_TATICA.indexOf(encurtarPosicaoInterna(j.getPosicao()));
            int distancia = (indiceJogador == -1) ? Integer.MAX_VALUE - 1 : Math.abs(indiceJogador - indiceVaga);
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                melhor = j;
            }
        }
        if (melhor == null) {
            melhor = naoAlocados.get(0);
        }
        naoAlocados.remove(melhor);
        return melhor;
    }

    // "MEI" -> "Mei", para o nome de exibição da vaga incompleta.
    private String formatarSiglaPosicao(String sigla) {
        if (sigla == null || sigla.isEmpty()) return sigla;
        return sigla.substring(0, 1).toUpperCase() + sigla.substring(1).toLowerCase();
    }

    // =========================================================================
    // REMOVER JOGADOR ESCALADO ERRONEAMENTE (Tela Live)
    // =========================================================================

    private String nomeExibicao(Jogador j) {
        if (j == null) return "";
        if (j.getApelido() != null && !j.getApelido().trim().isEmpty()) return j.getApelido().trim();
        return j.getNome() != null ? j.getNome().trim() : "";
    }

    private boolean ehGoleiro(Jogador j) {
        return j != null && j.getPosicao() != null && j.getPosicao().toUpperCase().contains("GOL");
    }

    private boolean estaNaArbitragem(Partida partida, String nome) {
        return nome.equalsIgnoreCase(obterNomeAtivo(partida.getArbitro()))
                || nome.equalsIgnoreCase(obterNomeAtivo(partida.getBandeira1()))
                || nome.equalsIgnoreCase(obterNomeAtivo(partida.getBandeira2()));
    }

    private br.com.cana.gestorcana_api.entity.JogadorPartida buscarNaListaPresenca(Partida partida, String nome,
            String timePreferido) {
        br.com.cana.gestorcana_api.entity.JogadorPartida qualquerTime = null;
        for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : partida.getListaGeralPresenca()) {
            if (jp == null || jp.getJogador() == null || !nomeExibicao(jp.getJogador()).equalsIgnoreCase(nome))
                continue;
            if (timePreferido == null || timePreferido.equalsIgnoreCase(jp.getTime()))
                return jp;
            if (qualquerTime == null)
                qualquerTime = jp;
        }
        return qualquerTime;
    }

    /**
     * Tira da partida um jogador escalado por engano e coloca na vaga dele o
     * próximo do banco por ordem de chegada (a ordem da listaGeralPresenca):
     * vaga de GOL recebe o próximo goleiro, vaga de linha o próximo jogador de
     * linha. Suspensos e quem está apitando são pulados. Sem ninguém
     * disponível a vaga fica vazia e o retorno é null.
     */
    public Jogador removerJogadorEscalado(Partida partida, String nomeJogador, String time, String posicaoSlot) {
        if (partida == null) return null;
        if (partida.getListaGeralPresenca() == null) partida.setListaGeralPresenca(new ArrayList<>());

        String nomeLimpo = obterNomeAtivo(nomeJogador);
        br.com.cana.gestorcana_api.entity.JogadorPartida removido = buscarNaListaPresenca(partida, nomeLimpo, time);
        String funcao = null;
        if (removido != null) {
            funcao = removido.getFuncao();
            partida.getListaGeralPresenca().remove(removido);
        }

        boolean vagaDeGol = "GOL".equalsIgnoreCase(posicaoSlot != null ? posicaoSlot.trim() : "");

        for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : partida.getListaGeralPresenca()) {
            if (jp == null || jp.getJogador() == null || jp.getJogadorId() == null || jp.getJogadorId() == 0)
                continue;
            if (!"Nenhum".equalsIgnoreCase(jp.getTime()) || !"Reserva".equalsIgnoreCase(jp.getStatus()))
                continue;

            Jogador candidato = jp.getJogador();
            if (ehGoleiro(candidato) != vagaDeGol)
                continue;
            if (estaNaArbitragem(partida, nomeExibicao(candidato)))
                continue;
            boolean suspenso = Boolean.TRUE.equals(candidato.getEstaSuspenso())
                    || jogadorRepository.findById(jp.getJogadorId())
                            .map(j -> Boolean.TRUE.equals(j.getEstaSuspenso())).orElse(false);
            if (suspenso)
                continue;

            jp.setTime(time);
            jp.setStatus("Titular");
            jp.setFuncao(funcao != null ? funcao : time + "_" + (vagaDeGol ? "GOL" : "LIN") + "_0");
            return candidato;
        }
        return null;
    }

    // =========================================================================
    // DESFAZER A ÚLTIMA SUBSTITUIÇÃO DE UMA VAGA ("J1 / J2" -> "J1")
    // =========================================================================

    /**
     * Ajusta a lista de presença ao desfazer a última troca de uma vaga: quem
     * tinha entrado volta para o banco e quem tinha saído volta para a vaga.
     */
    public boolean desfazerSubstituicao(Partida partida, String nomesDaVaga, String time) {
        if (partida == null || nomesDaVaga == null || !nomesDaVaga.contains(" / ")) return false;
        if (partida.getListaGeralPresenca() == null) partida.setListaGeralPresenca(new ArrayList<>());

        String[] partes = nomesDaVaga.split(" / ");
        String nomeSaindo = obterNomeAtivo(partes[partes.length - 1]);
        String nomeVoltando = obterNomeAtivo(partes[partes.length - 2]);

        br.com.cana.gestorcana_api.entity.JogadorPartida jpSaindo = buscarNaListaPresenca(partida, nomeSaindo, time);
        br.com.cana.gestorcana_api.entity.JogadorPartida jpVoltando = nomeVoltando.isEmpty() ? null
                : buscarNaListaPresenca(partida, nomeVoltando, "Nenhum");

        String funcao = null;
        if (jpSaindo != null) {
            funcao = jpSaindo.getFuncao();
            jpSaindo.setTime("Nenhum");
            jpSaindo.setStatus("Reserva");
            jpSaindo.setFuncao(ehGoleiro(jpSaindo.getJogador()) ? "GOL" : "LIN");
        }

        if (jpVoltando == null && !nomeVoltando.isEmpty()) {
            // Saiu do banco (ex.: excluído) depois da troca: recria o registro
            Jogador j = buscarJogadorPorNomeOuApelido(nomeVoltando);
            if (j != null) {
                jpVoltando = new br.com.cana.gestorcana_api.entity.JogadorPartida();
                jpVoltando.setJogadorId(j.getId());
                jpVoltando.setJogador(j);
                jpVoltando.setPartidaId(partida.getId() != null ? partida.getId() : 0);
                partida.getListaGeralPresenca().add(jpVoltando);
            }
        }
        if (jpVoltando != null) {
            jpVoltando.setTime(time);
            jpVoltando.setStatus("Titular");
            if (funcao != null) jpVoltando.setFuncao(funcao);
        }
        return true;
    }

    // =========================================================================
    // EDIÇÃO E EXCLUSÃO DE PARTIDA JÁ FINALIZADA (Tela de Consulta)
    // =========================================================================

    /**
     * Regrava uma partida finalizada. Os cartões NÃO são regravados direto do
     * grid: ao cumprir suspensão o histórico de cartões é zerado, então só a
     * diferença (grid novo - grid antigo) é aplicada sobre o que está gravado,
     * e a suspensão é reavaliada apenas para quem teve cartão alterado.
     */
    @org.springframework.transaction.annotation.Transactional
    public boolean editarPartida(Integer id, Partida dados, String gridAzulJson, String gridVermelhoJson) {
        if (id == null || dados == null) return false;
        Optional<Partida> opt = partidaRepository.findById(id);
        if (opt.isEmpty()) return false;
        Partida partida = opt.get();

        Map<Integer, int[]> cartoesGridAntigo = somarCartoesPorJogador(partida, partida.getGridAzul(), partida.getGridVermelho());
        Map<Integer, int[]> cartoesGravados = somarCartoesGravados(jogadorPartidaRepository.findByPartidaId(id));
        java.util.Set<String> arbitragemAntiga = nomesDaArbitragem(partida);

        partida.setNomePartida(dados.getNomePartida());
        if (dados.getDataPartida() != null) partida.setDataPartida(dados.getDataPartida());
        partida.setGolsTimeAzul(dados.getGolsTimeAzul());
        partida.setGolsTimeVermelho(dados.getGolsTimeVermelho());
        partida.setArbitro(dados.getArbitro());
        partida.setBandeira1(dados.getBandeira1());
        partida.setBandeira2(dados.getBandeira2());
        partida.setSumula(dados.getSumula());
        partida.setGridAzul(gridAzulJson);
        partida.setGridVermelho(gridVermelhoJson);
        partidaRepository.save(partida);

        List<br.com.cana.gestorcana_api.entity.JogadorPartida> novasAtuacoes = new ArrayList<>();
        novasAtuacoes.addAll(montarAtuacoesDoGrid(partida, gridAzulJson, "Azul"));
        novasAtuacoes.addAll(montarAtuacoesDoGrid(partida, gridVermelhoJson, "Vermelho"));
        Map<Integer, int[]> cartoesGridNovo = somarCartoes(novasAtuacoes);

        java.util.Set<Integer> jogadoresAfetados = new java.util.HashSet<>();
        java.util.Set<Integer> jogadoresComCartaoReduzido = new java.util.HashSet<>();
        java.util.Set<Integer> todos = new java.util.HashSet<>(cartoesGridAntigo.keySet());
        todos.addAll(cartoesGridNovo.keySet());

        Map<Integer, int[]> cartoesFinais = new java.util.HashMap<>();
        for (Integer jogadorId : todos) {
            int[] antigo = cartoesGridAntigo.getOrDefault(jogadorId, new int[2]);
            int[] novo = cartoesGridNovo.getOrDefault(jogadorId, new int[2]);
            int[] gravado = cartoesGravados.getOrDefault(jogadorId, new int[2]);
            int deltaAm = novo[0] - antigo[0];
            int deltaVm = novo[1] - antigo[1];
            cartoesFinais.put(jogadorId, new int[] { Math.max(0, gravado[0] + deltaAm), Math.max(0, gravado[1] + deltaVm) });
            if (deltaAm != 0 || deltaVm != 0) jogadoresAfetados.add(jogadorId);
            if (deltaAm < 0 || deltaVm < 0) jogadoresComCartaoReduzido.add(jogadorId);
        }

        // O total de cartões do jogador na partida fica na sua 1ª atuação
        java.util.Set<Integer> jaAplicado = new java.util.HashSet<>();
        for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : novasAtuacoes) {
            if (jaAplicado.add(jp.getJogadorId())) {
                int[] finais = cartoesFinais.getOrDefault(jp.getJogadorId(), new int[2]);
                jp.setCartaoAmarelo(finais[0]);
                jp.setCartaoVermelho(finais[1]);
            } else {
                jp.setCartaoAmarelo(0);
                jp.setCartaoVermelho(0);
            }
        }

        jogadorPartidaRepository.deleteByPartidaId(id);
        jogadorPartidaRepository.flush();
        jogadorPartidaRepository.saveAll(novasAtuacoes);
        jogadorPartidaRepository.flush();

        for (Integer jogadorId : jogadoresAfetados) {
            reavaliarSuspensao(jogadorId, jogadoresComCartaoReduzido.contains(jogadorId));
        }

        // Quem entrou na arbitragem pela edição cumpre a suspensão, como no Finalizar
        java.util.Set<String> arbitragemNova = nomesDaArbitragem(partida);
        List<String> novosNaArbitragem = new ArrayList<>();
        for (String nome : arbitragemNova) {
            boolean jaEstava = arbitragemAntiga.stream().anyMatch(n -> n.equalsIgnoreCase(nome));
            if (!jaEstava) novosNaArbitragem.add(nome);
        }
        if (!novosNaArbitragem.isEmpty()) {
            processarLimpezaDeSuspensaoPosJogo(novosNaArbitragem, partida);
        }
        return true;
    }

    @org.springframework.transaction.annotation.Transactional
    public boolean excluirPartida(Integer id) {
        if (id == null) return false;
        Optional<Partida> opt = partidaRepository.findById(id);
        if (opt.isEmpty()) return false;
        Partida partida = opt.get();

        Map<Integer, int[]> cartoesGravados = somarCartoesGravados(jogadorPartidaRepository.findByPartidaId(id));

        jogadorPartidaRepository.deleteByPartidaId(id);
        jogadorPartidaRepository.flush();
        if (eventoRepository != null) {
            eventoRepository.deleteAll(eventoRepository.findByPartidaId(id));
        }
        partidaRepository.delete(partida);
        partidaRepository.flush();

        for (Map.Entry<Integer, int[]> e : cartoesGravados.entrySet()) {
            if (e.getValue()[0] > 0 || e.getValue()[1] > 0) {
                reavaliarSuspensao(e.getKey(), true);
            }
        }
        return true;
    }

    private java.util.Set<String> nomesDaArbitragem(Partida partida) {
        java.util.Set<String> nomes = new java.util.LinkedHashSet<>();
        for (String campo : new String[] { partida.getArbitro(), partida.getBandeira1(), partida.getBandeira2() }) {
            if (campo == null) continue;
            for (String parte : campo.split(" / ")) {
                String nome = obterNomeAtivo(parte);
                if (!nome.isEmpty()) nomes.add(nome);
            }
        }
        return nomes;
    }

    private Map<Integer, int[]> somarCartoesPorJogador(Partida partida, String gridAzulJson, String gridVermelhoJson) {
        List<br.com.cana.gestorcana_api.entity.JogadorPartida> atuacoes = new ArrayList<>();
        atuacoes.addAll(montarAtuacoesDoGrid(partida, gridAzulJson, "Azul"));
        atuacoes.addAll(montarAtuacoesDoGrid(partida, gridVermelhoJson, "Vermelho"));
        return somarCartoes(atuacoes);
    }

    private Map<Integer, int[]> somarCartoesGravados(List<br.com.cana.gestorcana_api.entity.JogadorPartida> gravadas) {
        return somarCartoes(gravadas != null ? gravadas : new ArrayList<>());
    }

    private Map<Integer, int[]> somarCartoes(List<br.com.cana.gestorcana_api.entity.JogadorPartida> atuacoes) {
        Map<Integer, int[]> soma = new java.util.HashMap<>();
        for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : atuacoes) {
            if (jp.getJogadorId() == null) continue;
            int[] total = soma.computeIfAbsent(jp.getJogadorId(), k -> new int[2]);
            total[0] += jp.getCartaoAmarelo();
            total[1] += jp.getCartaoVermelho();
        }
        return soma;
    }

    /**
     * Suspensão pela regra do Finalizar, olhando os cartões ainda pendentes no
     * histórico (os já cumpridos foram zerados). Só tira a suspensão quando a
     * edição reduziu cartões do jogador.
     */
    private void reavaliarSuspensao(Integer jogadorId, boolean podeRetirarSuspensao) {
        Optional<Jogador> opt = jogadorRepository.findById(jogadorId);
        if (opt.isEmpty()) return;
        Jogador jogador = opt.get();

        int amarelos = jogador.getcAmarelosIniciais() != null ? jogador.getcAmarelosIniciais() : 0;
        int vermelhos = 0;
        for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : jogadorPartidaRepository.findByJogadorId(jogadorId)) {
            amarelos += jp.getCartaoAmarelo();
            vermelhos += jp.getCartaoVermelho();
        }

        boolean deveEstarSuspenso = vermelhos > 0 || amarelos >= 3;
        boolean suspenso = Boolean.TRUE.equals(jogador.getEstaSuspenso());

        if (deveEstarSuspenso && !suspenso) {
            jogador.setEstaSuspenso(true);
            jogador.setStatus("Suspenso");
            jogadorRepository.save(jogador);
        } else if (!deveEstarSuspenso && suspenso && podeRetirarSuspensao) {
            jogador.setEstaSuspenso(false);
            jogador.setStatus("Ativo");
            jogadorRepository.save(jogador);
        }
    }

    public void atualizarSubstituicaoNaListaPresenca(Partida partida, String nomeSaindo, String nomeEntrando, String timeAlvo) {
        if (partida == null || partida.getListaGeralPresenca() == null) return;

        String nomeSaindoLimpo = obterNomeAtivo(nomeSaindo);
        String nomeEntrandoLimpo = obterNomeAtivo(nomeEntrando);

        br.com.cana.gestorcana_api.entity.JogadorPartida jSaindo = null;
        br.com.cana.gestorcana_api.entity.JogadorPartida jEntrando = null;

        for (br.com.cana.gestorcana_api.entity.JogadorPartida jp : partida.getListaGeralPresenca()) {
            String nomeJ = (jp.getJogador().getApelido() != null && !jp.getJogador().getApelido().trim().isEmpty())
                            ? jp.getJogador().getApelido() : jp.getJogador().getNome();
            if (nomeJ.equalsIgnoreCase(nomeSaindoLimpo)) jSaindo = jp;
            if (nomeJ.equalsIgnoreCase(nomeEntrandoLimpo)) jEntrando = jp;
        }

        if (jEntrando != null) {
            jEntrando.setTime(timeAlvo);
            jEntrando.setStatus("Reserva");
            if (jSaindo != null) jEntrando.setFuncao(jSaindo.getFuncao()); 
        }
        if (jSaindo != null) {
            jSaindo.setTime("Nenhum");
            jSaindo.setStatus("Substituido");
        }
    }
}