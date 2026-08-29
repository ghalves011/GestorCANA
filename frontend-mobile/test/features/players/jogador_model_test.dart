import 'package:flutter_test/flutter_test.dart';
import 'package:gestorcana_mobile/features/players/models/jogador.dart';

void main() {
  group('Jogador.fromJson', () {
    test('parses a full backend-shaped payload', () {
      final Jogador jogador = Jogador.fromJson(<String, dynamic>{
        'id': 42,
        'nome': 'João da Silva',
        'apelido': 'Joãozinho',
        'cpf': '12345678901',
        'rg': '1234567',
        'dataNascimento': '1995-04-20',
        'dataAdmissao': '2024-01-10T00:00:00',
        'telefone': '11987654321',
        'posicao': 'ATACANTE',
        'nivel': 78,
        'estaSuspenso': false,
        'estaAutorizado': true,
        'mensalidadeEmDia': 1,
        'endereco': <String, dynamic>{
          'id': 5,
          'logradouro': 'Rua das Flores',
          'cidade': 'São Paulo',
          'estado': 'sp',
        },
      });

      expect(jogador.id, 42);
      expect(jogador.nome, 'João da Silva');
      expect(jogador.nomeExibir, 'Joãozinho');
      expect(jogador.dataNascimento, DateTime(1995, 4, 20));
      expect(jogador.dataAdmissao, DateTime(2024, 1, 10));
      expect(jogador.nivel, 78);
      expect(jogador.status, 'Ativo');
      // mensalidadeEmDia arrives as 1 (int) here — must be tolerated like
      // the backend's Contribuicao.pago quirk.
      expect(jogador.mensalidadeEmDia, isTrue);
      expect(jogador.endereco.cidade, 'São Paulo');
    });

    test('defaults nivel to 50 and endereco to empty when absent', () {
      final Jogador jogador = Jogador.fromJson(<String, dynamic>{'nome': 'Sem Sobrenome'});
      expect(jogador.nivel, 50);
      expect(jogador.endereco.logradouro, isNull);
      expect(jogador.nomeExibir, 'Sem Sobrenome');
    });

    test('estaSuspenso true flips status to Suspenso', () {
      final Jogador jogador = Jogador.fromJson(<String, dynamic>{'nome': 'X', 'estaSuspenso': true});
      expect(jogador.status, 'Suspenso');
    });
  });

  group('Jogador.toJson round trip', () {
    test('serializes dates back to yyyy-MM-dd', () {
      final Jogador jogador = Jogador.novo()
        ..nome = 'Teste'
        ..dataNascimento = DateTime(2000, 12, 3);
      final Map<String, dynamic> json = jogador.toJson();
      expect(json['dataNascimento'], '2000-12-03');
    });
  });
}
