# Gestor de Partidas CANA ⚽📋

O **Gestor CANA** é uma solução desktop completa desenvolvida para transformar e profissionalizar a gestão de rachas e ligas de futebol comunitário. Nascido de uma necessidade real, o software integra o controle de atletas, sustentabilidade financeira, automação de partidas em tempo real e consolidação de estatísticas da temporada, eliminando o uso de papéis e planilhas complexas.

---

## 🚀 Os 4 Pilares da Experiência (Como Funciona)

O sistema foi desenhado para ser operado de forma rápida e intuitiva, dividindo-se em quatro módulos integrados:

### 1. Cadastro de Atletas
A base de dados do sistema centraliza todas as informações essenciais dos jogadores:
* **Ficha Completa:** Registro de nome, apelido (nome de guerra para a súmula), dados de contato e data de admissão.
* **Mapeamento Tático:** Vínculo da posição nativa do jogador (Goleiro, Lateral, Zagueiro, Meia, Atacante), permitindo que o sistema entenda a característica de cada um para os módulos seguintes.

### 2. Controle de Contribuição
Módulo administrativo focado na saúde financeira e na justiça do racha comunitário:
* **Controle de Adimplência:** Registro e verificação de pagamentos das mensalidades ou taxas de manutenção do campo.
* **Bloqueio Automatizado:** Caso um jogador possua pendências financeiras, o sistema aplica uma regra de negócio blindada que impede que ele seja sorteado ou entre em campo como substituto, liberando-o apenas após a regularização.

### 3. Gerenciamento de Partidas (Painel Live)
O coração operacional do software, desenhado para o uso do mesário na beira do campo:
* **Sorteio Tático Automatizado:** Divide os presentes em equipes equilibradas (Azul e Vermelho) respeitando as posições originais e isolando jogadores suspensos na arbitragem.
* **Súmula em Tempo Real:** Registro instantâneo de eventos com cliques rápidos (Gols ⚽, Gols Contra ⚽(C), Cartões Amarelos 🟨 e Vermelhos 🟥).
* **Histórico de Substituições Infinitas:** Se o Jogador A for substituído pelo Jogador B, a linha do tempo grava a cronologia no formato `Jogador A / Jogador B` e `⚽ / 🟨`, vinculando as estatísticas corretamente a quem as fez.
* **Integração com WhatsApp:** Ao encerrar o jogo, o sistema tira um print do placar e copia automaticamente para a Área de Transferência (Ctrl+C). O organizador só precisa abrir o grupo do WhatsApp e apertar **Ctrl+V** para compartilhar o resultado.

### 4. Estatísticas da Temporada
Consolidação inteligente de dados para o acompanhamento do campeonato ao longo do ano:
* **Métricas Individuais:** Tabela geral que exibe e ordena automaticamente a artilharia (gols), cartões amarelos e cartões vermelhos de cada atleta.
* **Frequência de Presença:** O sistema calcula a porcentagem de participação de cada jogador com base no total de partidas realizadas no ano (Partidas Participadas × 100 / Total de Partidas), gerando relatórios precisos para premiações de fim de temporada.

---

## 🛠️ Tecnologias e Infraestrutura

* **Linguagem:** Java (Interface gráfica construída em Swing para rodar de forma leve e rápida).
* **Arquitetura:** Padrão MVC (Model-View-Controller) para garantir separação estrita entre a interface e as regras de negócio.
* **Banco de Dados:** SQLite (Banco de dados local e embarcado, operando 100% offline na beira do campo, sem depender de internet).

---

## 📦 Como Executar o Software

O sistema é distribuído através de uma pasta compactada. Para rodar, siga os passos:

1. Extraia a pasta compactada recebida no computador.
2. Certifique-se de manter o arquivo do banco de dados (`cana.db`) **na mesma pasta** do arquivo executável (`.jar`).
3. Dê um duplo clique no arquivo **`GestorCANA.jar`** para iniciar o sistema.

*Nota: É necessário ter o Java (JRE) 11 ou superior instalado na máquina.*

---

## 👤 Autor

* **Guilherme Henrique Alves**
* Projeto Integrador - Análise e Desenvolvimento de Sistemas (ADS) - Faculdade Anhanguera Educacional.
* Desenvolvido para gerar impacto real na gestão esportiva comunitária.
