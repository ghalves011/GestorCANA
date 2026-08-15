package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.service.ContribuicaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contribuicoes")
public class ContribuicaoController {

    private final ContribuicaoService contribuicaoService;

    // Injeta o Service gerenciado pelo Spring
    public ContribuicaoController(ContribuicaoService contribuicaoService) {
        this.contribuicaoService = contribuicaoService;
    }

    @GetMapping
    public List<Object[]> obterMatrizContribuicoes(
            @RequestParam(required = false, defaultValue = "2026") int ano,
            @RequestParam(required = false, defaultValue = "") String busca) {
        return contribuicaoService.obterMatrizContribuicoes(ano, busca);
    }

    @PostMapping
    public ResponseEntity<String> registrarPagamento(@RequestBody Map<String, Object> payload) {
        int jogadorId = ((Number) payload.get("jogadorId")).intValue();
        int mes = ((Number) payload.get("mes")).intValue();
        int ano = ((Number) payload.get("ano")).intValue();
        double valor = payload.containsKey("valor") ? ((Number) payload.get("valor")).doubleValue() : 50.0;

        String resultado = contribuicaoService.registrarPagamento(jogadorId, mes, ano, valor);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/gerar-lote")
    public ResponseEntity<String> gerarLoteAnual(@RequestBody Map<String, Object> payload) {
        int ano = ((Number) payload.get("ano")).intValue();
        double valor = payload.containsKey("valor") ? ((Number) payload.get("valor")).doubleValue() : 50.0;

        String resultado = contribuicaoService.gerarLoteAnual(ano, valor);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/recibo")
    public ResponseEntity<String> obterTextoRecibo(
            @RequestParam int jogadorId,
            @RequestParam int mes,
            @RequestParam int ano) {
        String recibo = contribuicaoService.obterTextoRecibo(jogadorId, mes, ano);
        return ResponseEntity.ok(recibo);
    }

    @DeleteMapping
    public ResponseEntity<String> excluirPagamento(
            @RequestParam int jogadorId,
            @RequestParam int mes,
            @RequestParam int ano) {
        String resultado = contribuicaoService.excluirPagamento(jogadorId, mes, ano);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/pode-jogar/{jogadorId}")
    public ResponseEntity<Boolean> podeJogar(@PathVariable int jogadorId) {
        return ResponseEntity.ok(contribuicaoService.podeJogar(jogadorId));
    }
}