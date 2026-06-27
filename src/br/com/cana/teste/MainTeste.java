package br.com.cana.teste;

import java.time.LocalDate;
import java.util.List;

import br.com.cana.dao.EventoDAO;
import br.com.cana.dao.JogadorDAO;
import br.com.cana.dao.JogadorPartidaDAO;
import br.com.cana.dao.PartidaDAO;
import br.com.cana.dao.TemporadaDAO;
import br.com.cana.model.Endereco;
import br.com.cana.model.Evento;
import br.com.cana.model.Jogador;
import br.com.cana.model.Partida;
import br.com.cana.model.Temporada;
import br.com.cana.service.JogadorService; // Importante: vamos usar o Service agora
import br.com.cana.util.DatabaseUtil;

public class MainTeste {
    public static void main(String[] args) {
        System.out.println("--- ⚽ TESTE DE BACK-END INTEGRADO: CANA (COM ENDEREÇO) ---");

        // 1. Instanciar as DAOs e Services
        JogadorDAO jogadorDAO = new JogadorDAO();
        JogadorService jogadorService = new JogadorService(); // O maestro do teste
        PartidaDAO partidaDAO = new PartidaDAO();
        EventoDAO eventoDAO = new EventoDAO();
        JogadorPartidaDAO jpDAO = new JogadorPartidaDAO();
        TemporadaDAO temporadaDAO = new TemporadaDAO();

        // 2. Resetar o Banco
        DatabaseUtil.resetarBanco();

        try {
            // --- SEÇÃO 0: TEMPORADA ---
            System.out.println("\n[Teste 0] Criando Temporada...");
            Temporada t2026 = new Temporada();
            t2026.setAno(2026);
            if (temporadaDAO.inserir(t2026)) {
                System.out.println("✅ Temporada " + t2026.getAno() + " salva!");
            }

            // --- SEÇÃO 1: JOGADOR COM ENDEREÇO (A NOVIDADE!) ---
            System.out.println("\n[Teste 1] Salvando Jogador com Endereço completo...");
            
            // Primeiro criamos o objeto Endereço
            Endereco end1 = new Endereco();
            end1.setLogradouro("Avenida Brasil");
            end1.setNumero("123");
            end1.setBairro("Centro");
            end1.setCidade("Santa Bárbara d'Oeste");
            end1.setEstado("SP");
            end1.setCep("13450-000");

            // Agora criamos o Jogador e colocamos o endereço dentro dele
            Jogador j1 = new Jogador();
            j1.setNome("Guilherme Alves");
            j1.setApelido("Guilherme");
            j1.setRg("12.345.678-9");
            j1.setCpf("123.456.789-00");
            j1.setTelefone("(19) 99999-8888");
            j1.setNivel(85);
            j1.setDataNascimento(LocalDate.of(2000, 1, 1));
            j1.setEstaAutorizado(true);
            j1.setEndereco(end1); // Vínculo do objeto

            // Usamos o SERVICE para salvar (ele salvará o endereço e o jogador em cascata)
            String resultado = jogadorService.salvarJogador(j1);
            
            if (resultado.equals("OK")) {
                System.out.println("✅ Sucesso: Jogador e Endereço salvos em cascata!");
                System.out.println("   ID Jogador: " + j1.getId());
                System.out.println("   ID Endereço: " + j1.getEndereco().getId());
            } else {
                System.err.println("❌ Erro no Service: " + resultado);
            }

            // --- SEÇÃO 2: PARTIDA ---
            System.out.println("\n[Teste 2] Criando Partida...");
            Partida p1 = new Partida();
            p1.setDataPartida(LocalDate.now());
            p1.setNomePartida("Partida de Teste");
            p1.setTemporadaId(t2026.getId());
            p1.setGolsTimeAzul(4);
            p1.setGolsTimeVermelho(4);
            
            if (partidaDAO.salvarCompleto(p1)) { 
                System.out.println("✅ Partida salva! ID: " + p1.getId());
            }

            // --- SEÇÃO 3: VÍNCULO (Jogador na Partida) ---
            System.out.println("\n[Teste 3] Vinculando Jogador à Partida...");
            if (jpDAO.vincular(j1.getId(), p1.getId(), "Vermelho", "Titular", "Zagueiro")) {
                System.out.println("✅ Vínculo Jogador-Partida criado!");
            }

            // --- SEÇÃO 4: EVENTO ---
            System.out.println("\n[Teste 4] Registrando GOL...");
            Evento ev = new Evento();
            ev.setTipo("GOL");
            ev.setJogador(j1);
            ev.setPartida(p1);
            ev.setCorTime("Vermelho");
            if (eventoDAO.inserir(ev)) {
                System.out.println("✅ Gol registrado para " + j1.getNome());
            }

            // --- SEÇÃO FINAL: VALIDAÇÃO ---
            System.out.println("\n--- 📈 RELATÓRIO DE CONSISTÊNCIA ---");
            List<Jogador> todos = jogadorDAO.listarTodos();
            
            for (Jogador j : todos) {
                System.out.println("Jogador encontrado: " + j.getNome());
                // Testando se o ID do endereço veio corretamente do banco
                System.out.println(" > FK Endereço: " + (j.getEndereco() != null ? j.getEndereco().getId() : "NULO"));
            }
            
            System.out.println("\n✅ Teste de integração FINALIZADO com sucesso!");

        } catch (Exception e) {
            System.err.println("\n❌ ERRO DURANTE O TESTE:");
            e.printStackTrace();
        }
    }
}