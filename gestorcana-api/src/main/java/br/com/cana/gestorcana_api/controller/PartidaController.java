package br.com.cana.gestorcana_api.controller;

import br.com.cana.gestorcana_api.entity.Jogador;
import br.com.cana.gestorcana_api.entity.Partida;
import br.com.cana.gestorcana_api.service.PartidaService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/partidas")
public class PartidaController {

    @Autowired
    private PartidaService service;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, context) -> {
                String str = json.getAsString();
                if (str == null || str.isEmpty())
                    return null;
                if (str.contains("/"))
                    return LocalDate.parse(str, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                return LocalDate.parse(str.split("T")[0]);
            })
            .registerTypeAdapter(LocalDate.class,
                    (com.google.gson.JsonSerializer<LocalDate>) (src, typeOfSrc,
                            context) -> new com.google.gson.JsonPrimitive(src.toString()))
            .create();

    @GetMapping({ "", "/" })
    public List<Partida> listar(@RequestParam(required = false) Integer temporada) {
        if (temporada != null) {
            return service.listarPartidasDaTemporada(temporada);
        }
        return service.listarTodas();
    }

    @GetMapping("/temporada/{temporadaId}")
    public List<Partida> listarPorTemporada(@PathVariable int temporadaId) {
        return service.listarPartidasDaTemporada(temporadaId);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Partida> buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id)
                .map(partida -> {
                    service.preencherElencosTransientes(partida); // Preenche antes de enviar
                    return ResponseEntity.ok(partida);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sortear")
    public ResponseEntity<Partida> sortear(@RequestBody String payloadJson) {
        try {
            JsonObject root = gson.fromJson(payloadJson, JsonObject.class);
            Partida partida = gson.fromJson(root.get("partida"), Partida.class);

            List<Jogador> jogadores = new ArrayList<>();
            if (root.has("jogadores") && root.get("jogadores").isJsonArray()) {
                JsonArray arr = root.getAsJsonArray("jogadores");
                for (JsonElement elem : arr) {
                    jogadores.add(gson.fromJson(elem, Jogador.class));
                }
            }

            service.sortearTimesTatico(partida, jogadores, partida.getFormacaoAzul(), partida.getFormacaoVermelho());
            return ResponseEntity.ok(partida);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/grid-historico")
    public ResponseEntity<List<Object[]>> gridHistorico(@RequestParam String time, @RequestBody Partida partida) {
        // Agora ele devolve EXATAMENTE o visual que foi salvo no banco!
        return ResponseEntity.ok(service.obterGridHistoricoSalvo(time, partida));
    }

    @PostMapping("/tem-eventos")
    public ResponseEntity<Boolean> temEventos(@RequestBody List<String> eventos) {
        return ResponseEntity.ok(service.temEventosNaTela(eventos));
    }

    @PostMapping("/atrasados")
    public ResponseEntity<List<Jogador>> obterAtrasados(@RequestBody Partida partida) {
        return ResponseEntity.ok(service.obterAtrasadosDisponiveisOrdenados(partida));
    }

    @PostMapping("/valida-linha")
    public ResponseEntity<String> validarLinha(@RequestBody Map<String, String> payload) {
        String reserva = payload.get("reserva");
        String erro = service.validarRestricaoParaLinha(reserva);
        return ResponseEntity.ok(erro != null ? erro : "OK");
    }

    @PostMapping({ "", "/" })
    public ResponseEntity<Boolean> salvar(@RequestBody Partida partida) {
        boolean salvou = service.salvarPartida(partida);
        return ResponseEntity.ok(salvou);
    }

    @PostMapping("/finalizar")
    public ResponseEntity<Boolean> finalizar(@RequestBody Map<String, Object> payload) {
        try {
            Partida partida = gson.fromJson(gson.toJson(payload.get("partida")), Partida.class);
            String jsonAzul = gson.toJson(payload.get("gridAzul"));
            String jsonVermelho = gson.toJson(payload.get("gridVermelho"));

            boolean ok = service.finalizarEFecharPartida(partida, jsonAzul, jsonVermelho);
            return ResponseEntity.ok(ok);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("/inverter-mesmo-time")
    public ResponseEntity<Boolean> inverterMesmoTime(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(true);
    }

    @PostMapping("/permutar-adversario")
    public ResponseEntity<Boolean> permutarAdversario(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(true);
    }

    @PostMapping("/tem-goleiro-natural")
    public ResponseEntity<Boolean> temGoleiroNatural(@RequestBody Map<String, Object> payload) {
        return ResponseEntity.ok(false);
    }

    @PostMapping("/atrasados-disponiveis")
    public ResponseEntity<List<Jogador>> obterAtrasadosDisp(@RequestBody String payloadJson) {
        Partida partida = gson.fromJson(payloadJson, Partida.class);
        return ResponseEntity.ok(service.obterAtrasadosDisponiveisOrdenados(partida));
    }

    @PostMapping("/validar-restricao-substituicao")
    public ResponseEntity<String> validarRestricaoSub(@RequestBody String nome) {
        String res = service.validarRestricaoParaSubstituicao(nome.replace("\"", "").trim());
        return ResponseEntity.ok(res == null ? "" : res);
    }

    @PostMapping("/validar-restricao-arbitragem")
    public ResponseEntity<String> validarRestricaoArb(@RequestBody String payloadJson) {
        JsonObject root = gson.fromJson(payloadJson, JsonObject.class);
        Partida p = gson.fromJson(root.get("partida"), Partida.class);
        Jogador j = gson.fromJson(root.get("jogador"), Jogador.class);
        String res = service.validarRestricaoParaArbitragem(p, j);
        return ResponseEntity.ok(res == null ? "" : res);
    }

    @PostMapping("/definir-arbitragem")
    public ResponseEntity<Partida> definirArb(@RequestBody String payloadJson) {
        JsonObject root = gson.fromJson(payloadJson, JsonObject.class);
        Partida p = gson.fromJson(root.get("partida"), Partida.class);
        Jogador j = gson.fromJson(root.get("jogador"), Jogador.class);
        String cargo = root.get("cargo").getAsString();
        boolean acumular = root.get("acumular").getAsBoolean();
        service.definirArbitragemSemDuplicidade(p, j, cargo, acumular);
        return ResponseEntity.ok(p);
    }

    @PostMapping("/remover-arbitragem")
    public ResponseEntity<Partida> removerArb(@RequestBody String payloadJson) {
        JsonObject root = gson.fromJson(payloadJson, JsonObject.class);
        Partida p = gson.fromJson(root.get("partida"), Partida.class);
        String cargo = root.get("cargo").getAsString();
        boolean gerarHistorico = root.get("gerarHistorico").getAsBoolean();
        service.removerDaArbitragem(p, cargo, gerarHistorico);
        return ResponseEntity.ok(p);
    }

    @PostMapping("/processar-substituicao-jogador")
    public ResponseEntity<String[]> procSubst(@RequestBody String payloadJson) {
        JsonObject root = gson.fromJson(payloadJson, JsonObject.class);
        String res[] = service.processarSubstituicaoJogador(
                root.get("nomeSaindo").getAsString(),
                root.get("nomeEntrandoRaw").getAsString(),
                root.get("posicao").getAsString());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/atualizar-substituicao-lista-presenca")
    public ResponseEntity<Partida> atualizarSubstLista(@RequestBody String payloadJson) {
        JsonObject root = gson.fromJson(payloadJson, JsonObject.class);
        Partida p = gson.fromJson(root.get("partida"), Partida.class);
        service.atualizarSubstituicaoNaListaPresenca(p,
                root.get("nomeSaindo").getAsString(),
                root.get("nomeEntrandoLimpo").getAsString(),
                root.get("timeAlvo").getAsString());
        return ResponseEntity.ok(p);
    }
}