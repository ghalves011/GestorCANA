package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.Contribuicao;
import br.com.cana.gestorcana_api.entity.Jogador;
import br.com.cana.gestorcana_api.repository.ContribuicaoRepository;
import br.com.cana.gestorcana_api.repository.JogadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ContribuicaoService {

    @Autowired
    private ContribuicaoRepository contribuicaoRepository;

    @Autowired
    private JogadorRepository jogadorRepository;

    public ContribuicaoService() {
    }

    public ContribuicaoService(ContribuicaoRepository contribuicaoRepository, JogadorRepository jogadorRepository) {
        this.contribuicaoRepository = contribuicaoRepository;
        this.jogadorRepository = jogadorRepository;
    }

    public String gerarCobranca(Contribuicao c) {
        if (c == null || c.getMes() == null || c.getMes() < 1 || c.getMes() > 12) {
            return "Mês inválido.";
        }
        if (c.getValor() == null || c.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            return "Valor inválido.";
        }

        Optional<Contribuicao> opt = contribuicaoRepository.findByJogadorIdAndMesAndAno(c.getJogadorId(), c.getMes(),
                c.getAno());
        if (opt.isPresent() && opt.get().getPago() != null && opt.get().getPago() == 1) {
            return "Já existe cobrança para este período.";
        }

        contribuicaoRepository.save(c);
        return "OK";
    }

    public String gerarLoteAnual(int ano, double valor) {
        List<Jogador> jogadores = jogadorRepository.findAll();
        if (jogadores == null || jogadores.isEmpty()) {
            return "Nenhum jogador cadastrado para gerar o lote.";
        }

        // Busca todas as cobranças existentes do ano em 1 única query
        List<Contribuicao> jaExistem = contribuicaoRepository.findByAno(ano);

        // Cria um mapa rápido na memória para não bater no banco a cada loop
        java.util.Set<String> chavesExistentes = new java.util.HashSet<>();
        for (Contribuicao c : jaExistem) {
            chavesExistentes.add(c.getJogadorId() + "-" + c.getMes());
        }

        List<Contribuicao> loteNovos = new java.util.ArrayList<>();
        for (Jogador jogador : jogadores) {
            for (int mes = 1; mes <= 12; mes++) {
                // Se a chave não existir, adiciona na lista de lote
                if (!chavesExistentes.contains(jogador.getId() + "-" + mes)) {
                    Contribuicao c = new Contribuicao();
                    c.setJogadorId(jogador.getId());
                    c.setAno(ano);
                    c.setMes(mes);
                    c.setValor(java.math.BigDecimal.valueOf(valor));
                    c.setPago(0);
                    loteNovos.add(c);
                }
            }
        }

        // Salva tudo de uma vez só! (Demora 1 segundo no máximo)
        if (!loteNovos.isEmpty()) {
            contribuicaoRepository.saveAll(loteNovos);
            return "OK";
        }

        return "Nenhuma nova cobrança precisou ser gerada (todas já existem).";
    }

    public String registrarPagamento(int jogadorId, int mes, int ano, double valor) {
        if (valor <= 0) {
            return "Valor inválido.";
        }

        Optional<Contribuicao> opt = contribuicaoRepository.findByJogadorIdAndMesAndAno(jogadorId, mes, ano);
        Contribuicao c = opt.orElseGet(Contribuicao::new);

        c.setJogadorId(jogadorId);
        c.setMes(mes);
        c.setAno(ano);
        c.setPago(1);
        c.setDataPagamento(LocalDate.now());
        c.setValor(BigDecimal.valueOf(valor));

        contribuicaoRepository.save(c);

        // Atualiza a flag mensalidadeEmDia do Jogador
        Optional<Jogador> jOpt = jogadorRepository.findById(jogadorId);
        if (jOpt.isPresent()) {
            Jogador j = jOpt.get();
            j.setMensalidadeEmDia(true);
            jogadorRepository.save(j);
        }

        return "OK";
    }

    public boolean podeJogar(int jogadorId) {
        Optional<Jogador> jOpt = jogadorRepository.findById(jogadorId);
        if (jOpt.isPresent()) {
            Jogador j = jOpt.get();
            if (Boolean.TRUE.equals(j.getEstaSuspenso()))
                return false; // Barrado por suspensÃ£o de cartões!

            // Goleiros são isentos de mensalidade: cadastro (e não estar suspenso) já
            // basta para poderem ser escalados.
            if (j.getPosicao() != null && j.getPosicao().toUpperCase().trim().contains("GOLEIRO"))
                return true;

            java.time.LocalDate hoje = java.time.LocalDate.now();
            int mesAtual = hoje.getMonthValue();
            int anoAtual = hoje.getYear();

            // Identifica o mÃªs de admissÃ£o (se entrou no ano passado, a cobrança começa
            // em 1)
            java.time.LocalDate admissao = j.getDataAdmissao();
            int mesAdmissao = (admissao != null && admissao.getYear() == anoAtual) ? admissao.getMonthValue() : 1;

            List<Contribuicao> cobrancas = contribuicaoRepository.findByAno(anoAtual);

            // Cria um mapa rápido dos meses que ESTÃO PAGOS de fato
            boolean[] mesesPagos = new boolean[13];
            for (Contribuicao c : cobrancas) {
                if (c.getJogadorId().equals(jogadorId)) {
                    if (c.getPago() != null && c.getPago() == 1) {
                        mesesPagos[c.getMes()] = true;
                    }
                }
            }

            // Varredura estrita: Tem algum buraco entre a admissão e o mês atual?
            for (int m = mesAdmissao; m <= mesAtual; m++) {
                if (!mesesPagos[m]) {
                    return false; // Achou um mês não pago ou não gerado na base = DEVEDOR!
                }
            }

            return true; // Passou limpo por todos os meses
        }
        return false;
    }

    public List<Object[]> obterMatrizContribuicoes(int ano, String busca) {
        List<Object[]> matriz = new ArrayList<>();

        List<Jogador> jogadores = jogadorRepository.findAll();
        if (jogadores == null)
            return matriz;

        List<Contribuicao> contribuicoesDoAno = contribuicaoRepository.findByAno(ano);

        Map<Integer, Map<Integer, Boolean>> pagamentosMap = new HashMap<>();
        for (Contribuicao c : contribuicoesDoAno) {
            boolean estaPago = c.getPago() != null && c.getPago() == 1;
            pagamentosMap.computeIfAbsent(c.getJogadorId(), k -> new HashMap<>()).put(c.getMes(), estaPago);
        }

        String buscaLimpa = (busca != null) ? busca.trim().toLowerCase() : "";

        for (Jogador j : jogadores) {
            String nomeExibir = (j.getApelido() != null && !j.getApelido().trim().isEmpty())
                    ? j.getApelido().trim()
                    : (j.getNome() != null ? j.getNome().trim() : "Jogador " + j.getId());

            if (!buscaLimpa.isEmpty() && !nomeExibir.toLowerCase().contains(buscaLimpa)) {
                continue;
            }

            Object[] linha = new Object[14];
            linha[0] = j.getId();
            linha[1] = nomeExibir;

            Map<Integer, Boolean> mesesJogador = pagamentosMap.getOrDefault(j.getId(), Collections.emptyMap());

            for (int mes = 1; mes <= 12; mes++) {
                linha[mes + 1] = mesesJogador.getOrDefault(mes, false);
            }

            matriz.add(linha);
        }

        matriz.sort((a, b) -> ((String) a[1]).compareToIgnoreCase((String) b[1]));
        return matriz;
    }

    public String obterTextoRecibo(int idJogador, int mes, int ano) {
        Optional<Jogador> jOpt = jogadorRepository.findById(idJogador);
        String nome = jOpt
                .map(j -> (j.getApelido() != null && !j.getApelido().trim().isEmpty()) ? j.getApelido() : j.getNome())
                .orElse("Jogador");

        String[] mesesNomes = { "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto",
                "Setembro", "Outubro", "Novembro", "Dezembro" };
        String nomeMes = (mes >= 1 && mes <= 12) ? mesesNomes[mes] : String.valueOf(mes);

        return "🧾 *RECIBO DE CONTRIBUIÇÃO - CANA*\n\n" +
                "👤 *Jogador:* " + nome + "\n" +
                "📅 *Referência:* " + nomeMes + "/" + ano + "\n" +
                "✅ *Status:* PAGO\n\n" +
                "Obrigado por manter sua contribuição em dia com a associação!";
    }

    public String excluirPagamento(int idJogador, int mes, int ano) {
        contribuicaoRepository.deletarPorJogadorMesAno(idJogador, mes, ano);
        return "OK";
    }
}