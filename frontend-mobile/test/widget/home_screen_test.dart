import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gestorcana_mobile/features/home/screens/home_screen.dart';

void main() {
  testWidgets('HomeScreen renders the four main menu actions', (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: MaterialApp(home: HomeScreen()),
      ),
    );

    expect(find.text('CADASTRO'), findsOneWidget);
    expect(find.text('CONTRIBUIÇÃO'), findsOneWidget);
    expect(find.text('PARTIDAS'), findsOneWidget);
    expect(find.text('ESTATÍSTICAS'), findsOneWidget);
  });
}
