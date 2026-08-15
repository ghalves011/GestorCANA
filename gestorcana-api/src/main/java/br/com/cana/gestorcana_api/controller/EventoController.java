package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.Evento;
import br.com.cana.gestorcana_api.service.EventoService;
import br.com.cana.gestorcana_api.service.PartidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;

    // 🌟 O PartidaService foi injetado aqui no topo!
    @Autowired
    private PartidaService partidaService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @PostMapping
    public ResponseEntity<String> registrarLance(@RequestBody Evento evento) {
        String resultado = eventoService.registrarLance(evento);
        if ("OK".equalsIgnoreCase(resultado)) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Evento registrado com sucesso!");
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    @GetMapping("/partida/{partidaId:\\d+}")
    public ResponseEntity<List<Evento>> listarPorPartida(@PathVariable Integer partidaId) {
        List<Evento> eventos = eventoService.listarPorPartida(partidaId);
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/jogador/{jogadorId:\\d+}")
    public ResponseEntity<List<Evento>> listarPorJogador(@PathVariable Integer jogadorId) {
        List<Evento> eventos = eventoService.listarPorJogador(jogadorId);
        return ResponseEntity.ok(eventos);
    }

    // --- BLOCO DE MÉTODOS DE APOIO DA TELA AO VIVO ---

    @PostMapping("/adicionar")
    public ResponseEntity<String> adicionarEvento(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(partidaService.adicionarEventoAoJogador(payload.get("eventos"), payload.get("token")));
    }

    @PostMapping("/remover")
    public ResponseEntity<String> removerEvento(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(partidaService.removerEventoDoJogador(payload.get("eventos"), payload.get("token")));
    }

    @PostMapping("/contar-tokens")
    public ResponseEntity<Integer> contarTokens(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(partidaService.contarTokensNoBlocoAtivo(payload.get("eventos"), payload.get("token")));
    }

    @PostMapping("/contar-amarelos-ativo")
    public ResponseEntity<Integer> contarAmarelos(@RequestBody String eventos) {
        // Limpa as aspas extras do JSON antes de contar
        String evLimpo = eventos != null ? eventos.replace("\"", "") : "";
        return ResponseEntity.ok(partidaService.contarAmarelosDoJogadorAtivo(evLimpo));
    }
}