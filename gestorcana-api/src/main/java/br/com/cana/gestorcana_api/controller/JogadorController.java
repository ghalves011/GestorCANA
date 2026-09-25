package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.Jogador;
import br.com.cana.gestorcana_api.service.JogadorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jogadores")
public class JogadorController {

    private final JogadorService jogadorService;

    public JogadorController(JogadorService jogadorService) {
        this.jogadorService = jogadorService;
    }

    // ÚNICO MÉTODO LISTAR (Delega para a Service)
    @GetMapping({"", "/"})
    public List<Jogador> listar(@RequestParam(required = false) String status) {
        return jogadorService.filtrarPorStatus(status);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Jogador> buscarPorId(@PathVariable Integer id) {
        Jogador jogador = jogadorService.buscarPorId(id).orElse(null);
        return jogador != null ? ResponseEntity.ok(jogador) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> salvar(@RequestBody Jogador jogador) {
        try {
            // DELEGA A SALVAÇÃO E VALIDAÇÃO PARA A SERVICE!
            String resultado = jogadorService.salvarJogador(jogador);
            
            if ("OK".equals(resultado)) {
                return ResponseEntity.status(HttpStatus.CREATED).body(jogador);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno: " + e.getMessage());
        }
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody Jogador jogador) {
        jogador.setId(id); // Garante o ID na requisição
        String resultado = jogadorService.atualizarJogador(jogador);
        
        if ("OK".equals(resultado)) {
            return ResponseEntity.ok(jogador);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
        }
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<String> deletar(@PathVariable Integer id) {
        String resultado = jogadorService.excluir(id);
        if ("OK".equals(resultado)) {
            return ResponseEntity.ok("Jogador excluído com sucesso.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
        }
    }

    @PostMapping("/buscar-por-nome")
    public ResponseEntity<Jogador> buscarPorNome(@RequestBody String nome) {
        Jogador j = jogadorService.buscarPorNomeOuApelido(nome.replace("\"", "").trim());
        return j != null ? ResponseEntity.ok(j) : ResponseEntity.ok(null);
    }
}