package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.JogadorPartida;
import br.com.cana.gestorcana_api.service.JogadorPartidaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/jogador-partidas")
public class JogadorPartidaController {

    private final JogadorPartidaService service;

    public JogadorPartidaController(JogadorPartidaService service) {
        this.service = service;
    }

    @GetMapping
    public List<JogadorPartida> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<JogadorPartida> buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/partida/{partidaId:\\d+}")
    public ResponseEntity<List<JogadorPartida>> listarPorPartida(@PathVariable Integer partidaId) {
        return ResponseEntity.ok(service.listarPorPartida(partidaId));
    }

    @PostMapping
    public ResponseEntity<JogadorPartida> salvar(@RequestBody JogadorPartida jogadorPartida) {
        JogadorPartida salvo = service.salvar(jogadorPartida);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<JogadorPartida> atualizar(@PathVariable Integer id, @RequestBody JogadorPartida jogadorPartida) {
        jogadorPartida.setId(id);
        JogadorPartida atualizado = service.salvar(jogadorPartida);
        return ResponseEntity.ok(atualizado);
    }

    @PostMapping("/{id:\\d+}/gol")
    public ResponseEntity<JogadorPartida> adicionarGol(@PathVariable Integer id) {
        Optional<JogadorPartida> optJp = service.buscarPorId(id);
        if (optJp.isPresent()) {
            JogadorPartida atualizado = service.adicionarGol(optJp.get());
            return ResponseEntity.ok(atualizado);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id:\\d+}/cartao-amarelo")
    public ResponseEntity<String> aplicarCartaoAmarelo(@PathVariable Integer id) {
        Optional<JogadorPartida> optJp = service.buscarPorId(id);
        if (optJp.isPresent()) {
            String mensagem = service.aplicarCartaoAmarelo(optJp.get());
            return ResponseEntity.ok(mensagem);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/substituicao")
    public ResponseEntity<String> registrarSubstituicao(@RequestParam Integer saiId, @RequestParam Integer entraId) {
        boolean ok = service.registrarSubstituicao(saiId, entraId);
        if (ok) {
            return ResponseEntity.ok("Substituição realizada com sucesso!");
        }
        return ResponseEntity.badRequest().body("Erro ao realizar substituição. Verifique os IDs informados.");
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        boolean deletou = service.deletar(id);
        if (deletou) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}