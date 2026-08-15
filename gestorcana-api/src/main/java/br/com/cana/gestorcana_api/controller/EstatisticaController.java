package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.service.PartidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estatisticas")
public class EstatisticaController {

    @Autowired
    private PartidaService partidaService;

    @GetMapping
    public List<Object[]> obterEstatisticas(@RequestParam(required = false, defaultValue = "2026") int temporada) {
        return partidaService.carregarDadosTelaEstatisticas(temporada);
    }
}