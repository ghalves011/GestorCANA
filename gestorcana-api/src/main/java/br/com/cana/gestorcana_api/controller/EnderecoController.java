package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.Endereco;
import br.com.cana.gestorcana_api.service.EnderecoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

    private final EnderecoService service;

    public EnderecoController(EnderecoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Endereco> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Endereco> buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id:\\d+}/formatado")
    public ResponseEntity<String> buscarEnderecoFormatado(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(end -> ResponseEntity.ok(service.obterEnderecoFormatado(end)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> salvar(@RequestBody Endereco endereco) {
        Endereco salvo = service.salvar(endereco);
        if (salvo != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        }
        return ResponseEntity.badRequest().body("Erro ao salvar o endereço.");
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<?> atualizar(@PathVariable Integer id, @RequestBody Endereco endereco) {
        endereco.setId(id);
        boolean atualizou = service.atualizar(endereco);
        if (atualizou) {
            return ResponseEntity.ok(endereco);
        }
        return ResponseEntity.badRequest().body("Erro ao atualizar o endereço. Verifique o ID enviado.");
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