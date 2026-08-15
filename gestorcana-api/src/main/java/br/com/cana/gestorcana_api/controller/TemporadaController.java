package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.Temporada;
import br.com.cana.gestorcana_api.service.TemporadaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/temporadas")
public class TemporadaController {

    private final TemporadaService service;

    public TemporadaController(TemporadaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Temporada> listar() {
        return service.listarTodas();
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Temporada> buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ano/{ano:\\d+}")
    public ResponseEntity<Temporada> buscarPorAno(@PathVariable Integer ano) {
        return service.buscarPorAno(ano)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> salvar(@RequestBody Temporada temporada) {
        String resultado = service.salvar(temporada);
        if ("OK".equalsIgnoreCase(resultado)) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Temporada criada com sucesso!");
        }
        return ResponseEntity.badRequest().body(resultado);
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<String> atualizar(@PathVariable Integer id, @RequestBody Temporada temporada) {
        temporada.setId(id);
        String resultado = service.salvar(temporada);
        if ("OK".equalsIgnoreCase(resultado)) {
            return ResponseEntity.ok("Temporada atualizada com sucesso!");
        }
        return ResponseEntity.badRequest().body(resultado);
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