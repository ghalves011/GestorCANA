package br.com.cana.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.cana.model.Jogador;
import br.com.cana.util.ConnectionFactory;

public class JogadorDAO {

    // Método para SALVAR um novo jogador no banco de dados
    public boolean inserir(Jogador jogador) {
        String sql = "INSERT INTO Jogador (nome, dataNasc, rg, cpf, telefone, telefoneEmergencia, "
                + "id_endereco, posicao, peDominante, altura, peso, timeAnterior, "
                + "tempoExperiencia, nivel, padrinho_id, grauRelacaoPadrinho, numCamisa, "
                + "estaSuspenso, estaAutorizado, mensalidadeEmDia, golsIniciais, "
                + "cAmarelosIniciais, cVermelhosIniciais, apelido, dataAdmissao) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Usando try-with-resources para garantir que a conexão e o statement sejam fechados
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            // Preenchendo os pontos de interrogação com os dados do Objeto Jogador
            stmt.setString(1, jogador.getNome());
            stmt.setString(2, jogador.getDataNascimento() != null ? jogador.getDataNascimento().toString() : null);
            stmt.setString(3, jogador.getRg());
            stmt.setString(4, jogador.getCpf());
            stmt.setString(5, jogador.getTelefone());
            stmt.setString(6, jogador.getTelefoneEmergencia());
            stmt.setInt(7, jogador.getEndereco().getId());
            stmt.setString(8, jogador.getPosicao());
            stmt.setString(9, jogador.getPeDominante());
            stmt.setDouble(10, jogador.getAltura());
            stmt.setDouble(11, jogador.getPeso());
            stmt.setString(12, jogador.getTimeAnterior());
            stmt.setInt(13, jogador.getTempoExperiencia());
            stmt.setInt(14, jogador.getNivel());

            // Regra do Padrinho (pode ser null)
            if (jogador.getPadrinhoId() != null) {
                stmt.setInt(15, jogador.getPadrinhoId());
            } else {
                stmt.setNull(15, java.sql.Types.INTEGER);
            }

            stmt.setString(16, jogador.getGrauRelacaoPadrinho());
            stmt.setInt(17, jogador.getNumCamisa());
            stmt.setBoolean(18, jogador.isEstaSuspenso());
            stmt.setBoolean(19, jogador.isEstaAutorizado());
            stmt.setBoolean(20, jogador.isMensalidadeEmDia());
            stmt.setInt(21, jogador.getGolsIniciais());
            stmt.setInt(22, jogador.getcAmarelosIniciais());
            stmt.setInt(23, jogador.getcVermelhosIniciais());
            stmt.setString(24, jogador.getApelido());
            stmt.setString(25, br.com.cana.util.DateUtil.paraBanco(jogador.getDataAdmissao()));
            // Executa a gravação no banco
            // 1. Primeiro, executamos o update e guardamos quantas linhas foram afetadas (deve ser 1 se deu certo)
            int linhasAfetadas = stmt.executeUpdate();

            // 2. Se deu certo, tentamos pegar o ID gerado automaticamente para atualizar o objeto Jogador com esse ID (sincronização)
            if (linhasAfetadas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        jogador.setId(rs.getInt(1));
                    }
                }
            }

            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao salvar jogador: " + e.getMessage());
            return false;
        }

    }

    /**
     * Busca todos os jogadores cadastrados no banco de dados.
     * 
     * @return Uma lista de objetos Jogador.
     */
    public List<Jogador> listarTodos() {
        List<Jogador> lista = new ArrayList<>();
        String sql = "SELECT * FROM Jogador ORDER BY nome ASC"; // Busca e já traz em ordem alfabética

        // Usando try-with-resources para fechar tudo automaticamente
        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            ContribuicaoDAO contribuicaoDAO = new ContribuicaoDAO();

            // Enquanto houver uma próxima linha no resultado do banco
            while (rs.next()) {
                // Cria um objeto Jogador e preenche com os dados do ResultSet
                Jogador j = mapearResultSet(rs); // Método auxiliar para evitar repetição de código
                j.setMensalidadeEmDia(!contribuicaoDAO.hasPendencias(j.getId(), j.getDataAdmissao()));
                lista.add(j);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erro ao listar jogadores: " + e.getMessage());
        }

        return lista;
    }

    // 1. ATUALIZAR (CRUD)
    public boolean atualizar(Jogador j) {
        String sql = "UPDATE Jogador SET nome = ?, apelido = ?, dataNasc = ?, rg = ?, cpf = ?, "
                + "telefone = ?, telefoneEmergencia = ?, id_endereco = ?, posicao = ?, "
                + "peDominante = ?, altura = ?, peso = ?, timeAnterior = ?, "
                + "tempoExperiencia = ?, nivel = ?, padrinho_id = ?, grauRelacaoPadrinho = ?, "
                + "numCamisa = ?, estaSuspenso = ?, estaAutorizado = ?, mensalidadeEmDia = ?, dataAdmissao = ? "
                + "WHERE id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setString(1, j.getNome());
            stmt.setString(2, j.getApelido());
            stmt.setString(3, j.getDataNascimento() != null ? j.getDataNascimento().toString() : null);
            stmt.setString(4, j.getRg());
            stmt.setString(5, j.getCpf());
            stmt.setString(6, j.getTelefone());
            stmt.setString(7, j.getTelefoneEmergencia());
            stmt.setInt(8, j.getEndereco().getId());
            stmt.setString(9, j.getPosicao());
            stmt.setString(10, j.getPeDominante());
            stmt.setDouble(11, j.getAltura());
            stmt.setDouble(12, j.getPeso());
            stmt.setString(13, j.getTimeAnterior());
            stmt.setInt(14, j.getTempoExperiencia());
            stmt.setInt(15, j.getNivel());

            if (j.getPadrinhoId() != null && j.getPadrinhoId() > 0) {
                stmt.setInt(16, j.getPadrinhoId());
            } else {
                stmt.setNull(16, java.sql.Types.INTEGER);
            }

            stmt.setString(17, j.getGrauRelacaoPadrinho());
            stmt.setInt(18, j.getNumCamisa());
            stmt.setBoolean(19, j.isEstaSuspenso());
            stmt.setBoolean(20, j.isEstaAutorizado());
            stmt.setBoolean(21, j.isMensalidadeEmDia());
            stmt.setString(22, br.com.cana.util.DateUtil.paraBanco(j.getDataAdmissao()));
            stmt.setInt(23, j.getId()); // O ID do WHERE é o último

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao atualizar jogador: " + e.getMessage());
            return false;
        }
    }

    // 2. EXCLUIR (CRUD)
    public boolean excluir(int id) {
        String sql = "DELETE FROM Jogador WHERE id = ?";

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();

            if (linhasAfetadas > 0) {
                System.out.println("✅ Jogador removido com sucesso!");
                return true;
            }
            return false;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao excluir jogador: " + e.getMessage());
            return false;
        }
    }

    // 3. GESTÃO DE PADRINHOS (Regra: Não pode ser ele mesmo e deve estar Ativo)
    public List<Jogador> listarPadrinhosPossiveis(int idAtual) {
        List<Jogador> lista = new ArrayList<>();
        // "id != ?" para evitar que ele seja padrinho de si mesmo
        String sql = "SELECT id, nome FROM Jogador WHERE id != ? AND status = 'Ativo' ORDER BY nome ASC";

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {

            stmt.setInt(1, idAtual);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Jogador j = new Jogador();
                j.setId(rs.getInt("id"));
                j.setNome(rs.getString("nome"));
                lista.add(j);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao listar padrinhos: " + e.getMessage());
        }
        return lista;
    }

    // 4. FILTROS DE STATUS (Ativos, Suspensos, Bloqueados)
    public List<Jogador> buscarPorStatus(String status) {
        List<Jogador> lista = new ArrayList<>();
        String sql;

        if (status.equalsIgnoreCase("Suspenso")) {
            sql = "SELECT * FROM Jogador WHERE estaSuspenso = 1 ORDER BY nome ASC";
        } else if (status.equalsIgnoreCase("Ativo")) {
            // A nossa correção anterior mantida: tira o estaSuspenso para o cara aparecer e ir pro banco
            sql = "SELECT * FROM Jogador WHERE estaAutorizado = 1 ORDER BY nome ASC";
        } else if (status.equalsIgnoreCase("Pendente")) {
            sql = "SELECT * FROM Jogador WHERE estaAutorizado = 0 ORDER BY nome ASC";
        } else {
            sql = "SELECT * FROM Jogador ORDER BY nome ASC";
        }

        try (Connection conexao = ConnectionFactory.getConnection();
                PreparedStatement stmt = conexao.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            
            // 👇 1. Injeta o DAO do financeiro aqui para fazer a checagem real
            br.com.cana.dao.ContribuicaoDAO contribuicaoDAO = new br.com.cana.dao.ContribuicaoDAO();

            while (rs.next()) {
                Jogador j = mapearResultSet(rs);
                
                // 👇 2. A MÁGICA: Recalcula se o cara tá devendo HOJE, ignorando dados velhos da tabela Jogador!
                j.setMensalidadeEmDia(!contribuicaoDAO.hasPendencias(j.getId(), j.getDataAdmissao()));
                
                lista.add(j);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao filtrar por status: " + e.getMessage());
        }
        return lista;
    }

    // Método auxiliar para transformar o ResultSet em um objeto Jogador (DRY)
    private Jogador mapearResultSet(ResultSet rs) throws SQLException {
        Jogador j = new Jogador();
        j.setId(rs.getInt("id"));
        j.setNome(rs.getString("nome"));
        j.setApelido(rs.getString("apelido"));
        j.setRg(rs.getString("rg"));
        j.setNivel(rs.getInt("nivel"));
        j.setPosicao(rs.getString("posicao"));
        j.setNumCamisa(rs.getInt("numCamisa"));
        j.setTelefone(rs.getString("telefone"));
        j.setTelefoneEmergencia(rs.getString("telefoneEmergencia"));
        j.setCpf(rs.getString("cpf"));

        int idEndereco = rs.getInt("id_endereco");
        if (idEndereco > 0) {
            EnderecoDAO endDAO = new EnderecoDAO();
            j.setEndereco(endDAO.buscarPorId(idEndereco));
        }
        j.setPeDominante(rs.getString("peDominante"));
        j.setAltura(rs.getDouble("altura"));
        j.setPeso(rs.getDouble("peso"));
        j.setTimeAnterior(rs.getString("timeAnterior"));
        j.setTempoExperiencia(rs.getInt("tempoExperiencia"));
        j.setPadrinhoId(rs.getInt("padrinho_id"));
        j.setGrauRelacaoPadrinho(rs.getString("grauRelacaoPadrinho"));

        j.setGrauRelacaoPadrinho(rs.getString("grauRelacaoPadrinho"));

        // Lê diretamente o status que já foi gravado e processado pelo Service no banco!
        j.setEstaSuspenso(rs.getBoolean("estaSuspenso"));
        j.setEstaAutorizado(rs.getBoolean("estaAutorizado"));
        j.setMensalidadeEmDia(rs.getBoolean("mensalidadeEmDia"));
        j.setGolsIniciais(rs.getInt("golsIniciais"));
        j.setcAmarelosIniciais(rs.getInt("cAmarelosIniciais"));
        j.setcVermelhosIniciais(rs.getInt("cVermelhosIniciais"));
        j.setDataAdmissao(br.com.cana.util.DateUtil.ler(rs.getString("dataAdmissao")));

        String dataStr = rs.getString("dataNasc");
        if (dataStr != null && !dataStr.isEmpty()) {
            j.setDataNascimento(java.time.LocalDate.parse(dataStr));
        }
        return j;
    }

    // 5. MÉTODO EXTRA: Atualizar o status do jogador (Ativo, Suspenso, Pendente)
    public boolean atualizarStatus(int jogadorId, String novoStatus) {
        // 1. String SQL para atualizar os campos de status (estaSuspenso e estaAutorizado) com base no novoStatus
        String sql = "UPDATE Jogador SET estaSuspenso = ?, estaAutorizado = ? WHERE id = ?";

        // 2. Definimos valores padrão (Segurança)
        int suspenso = 0; // 0 = falso no SQLite
        int autorizado = 1; // 1 = verdadeiro no SQLite

        // 3. Regra de Negócio: Traduzindo a String para as flags
        if (novoStatus.equalsIgnoreCase("Suspenso")) {
            suspenso = 1;
            autorizado = 1; // Ele continua autorizado, mas está suspenso
        } else if (novoStatus.equalsIgnoreCase("Pendente")) {
            suspenso = 0;
            autorizado = 0; // Não está autorizado a jogar ainda
        } else {
            // "Ativo" ou qualquer outro caso
            suspenso = 0;
            autorizado = 1;
        }

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 4. Setamos os parâmetros na ordem da String SQL
            stmt.setInt(1, suspenso);
            stmt.setInt(2, autorizado);
            stmt.setInt(3, jogadorId); // O ID é sempre o último no UPDATE com WHERE

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erro ao mudar status do jogador: " + e.getMessage());
            return false;
        }
    }

    // 6. MÉTODO EXTRA: Verificar se o número da camisa já está em uso por outro
    // jogador (exceto ele mesmo)
    public boolean isNumeroCamisaEmUso(int numero, int idAtual) {
        // Verifica se existe alguém com esse número, mas que NÃO seja o próprio jogador
        // (id != ?)
        String sql = "SELECT COUNT(*) FROM Jogador WHERE numCamisa = ? AND id != ?";
        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, numero);
            stmt.setInt(2, idAtual);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Busca por ID, trazendo o jogador específico (útil para detalhes, edição,
    // etc.)
    public Jogador buscarPorId(int id) {
        String sql = "SELECT * FROM Jogador WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Reaproveitamos o método auxiliar para mapear o ResultSet em um objeto Jogador
                return mapearResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar jogador por ID: " + e.getMessage());
        }
        return null;
    }

    // Busca por nome ou apelido, ignorando maiúsculas/minúsculas, e só traz se
    // estiver autorizado
    public Jogador buscarPorNomeOuApelido(String nomeOuApelido) {
        // Busca ignorando maiúsculas/minúsculas se bate com o nome completo ou o
        // apelido de um Ativo
        String sql = "SELECT * FROM Jogador WHERE (LOWER(nome) = LOWER(?) OR LOWER(apelido) = LOWER(?)) AND estaAutorizado = 1 LIMIT 1";

        try (Connection conn = ConnectionFactory.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeOuApelido.trim());
            stmt.setString(2, nomeOuApelido.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ContribuicaoDAO contribuicaoDAO = new ContribuicaoDAO();
                    Jogador j = mapearResultSet(rs);
                    // Sincroniza dinamicamente o status financeiro consultando as pendências
                    j.setMensalidadeEmDia(!contribuicaoDAO.hasPendencias(j.getId(), j.getDataAdmissao()));
                    return j;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar jogador por nome/apelido: " + e.getMessage());
        }
        return null;
    }
}