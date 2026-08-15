package br.com.cana.gestorcana_api.service;

import br.com.cana.gestorcana_api.entity.JogadorPartida;
import br.com.cana.gestorcana_api.repository.JogadorPartidaRepository;
import br.com.cana.gestorcana_api.repository.JogadorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class JogadorPartidaService {

    private final JogadorPartidaRepository jpRepository;

    @Autowired
    private JogadorRepository jogadorRepository;

    @Autowired
    public JogadorPartidaService(JogadorPartidaRepository jpRepository, JogadorRepository jogadorRepository) {
        this.jpRepository = jpRepository;
        this.jogadorRepository = jogadorRepository;
    }

    public JogadorPartida salvar(JogadorPartida jp) {
        if (jp == null)
            return null;
        return jpRepository.save(jp);
    }

    // Regra: Adicionar um gol
    public JogadorPartida adicionarGol(JogadorPartida jp) {
        if (jp == null)
            return null;

        if (jp.getCartaoVermelho() != null && jp.getCartaoVermelho() > 0) {
            System.out.println("⚠️ Jogador expulso não pode marcar gol!");
            return jp;
        }

        int golsAtuais = jp.getGols() != null ? jp.getGols() : 0;
        jp.setGols(golsAtuais + 1);
        return jpRepository.save(jp);
    }

    // Regra: Controle de cartões
    public String aplicarCartaoAmarelo(JogadorPartida jp) {
        if (jp == null)
            return "Erro: Estatística não informada.";

        int amarelosAtuais = jp.getCartaoAmarelo() != null ? jp.getCartaoAmarelo() : 0;
        jp.setCartaoAmarelo(amarelosAtuais + 1);
        String mensagem = "Cartão amarelo aplicado.";

        if (jp.getCartaoAmarelo() >= 2) {
            jp.setCartaoVermelho(1);
            mensagem = "Jogador expulso pelo segundo amarelo!";
        }
        jpRepository.save(jp);

        // 🌟 Verifica acúmulo total do jogador em todas as partidas
        List<JogadorPartida> historico = jpRepository.findByJogadorId(jp.getJogadorId());
        int totalAmarelos = historico.stream()
                .mapToInt(h -> h.getCartaoAmarelo() != null ? h.getCartaoAmarelo() : 0)
                .sum();

        if (totalAmarelos >= 3) {
            jogadorRepository.findById(jp.getJogadorId()).ifPresent(j -> {
                j.setEstaSuspenso(true);
                j.setStatus("Suspenso");
                jogadorRepository.save(j);
            });
            mensagem += " Jogador atingiu 3 amarelos e está suspenso!";
        }

        return mensagem;
    }

    public boolean registrarSubstituicao(Integer saiId, Integer entraId) {
        Optional<JogadorPartida> optSai = jpRepository.findById(saiId);
        Optional<JogadorPartida> optEntra = jpRepository.findById(entraId);

        if (optSai.isPresent() && optEntra.isPresent()) {
            JogadorPartida sai = optSai.get();
            JogadorPartida entra = optEntra.get();

            sai.setStatus("SUBSTITUIDO");
            entra.setStatus("TITULAR");

            jpRepository.save(sai);
            jpRepository.save(entra);
            return true;
        }
        return false;
    }

    public List<JogadorPartida> listarPorPartida(Integer partidaId) {
        if (partidaId == null)
            return List.of();
        return jpRepository.findByPartidaId(partidaId);
    }

    public Optional<JogadorPartida> buscarPorId(Integer id) {
        if (id == null)
            return Optional.empty();
        return jpRepository.findById(id);
    }

    public List<JogadorPartida> listarTodos() {
        return jpRepository.findAll();
    }

    public boolean deletar(Integer id) {
        if (id == null || !jpRepository.existsById(id)) {
            return false;
        }
        jpRepository.deleteById(id);
        return true;
    }
}