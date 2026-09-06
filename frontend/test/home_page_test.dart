import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:barber_saas_mobile/features/home/home_page.dart';

void main() {
  testWidgets(
    'exibe os elementos principais da Home',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: HomePage(),
        ),
      );

      expect(find.text('Escolha quando\nquer vir'), findsOneWidget);
      expect(
        find.text('Veja os melhores horários para você.'),
        findsOneWidget,
      );
      expect(find.text('Horários disponíveis'), findsOneWidget);
      expect(find.text('Continuar'), findsOneWidget);

      expect(find.text('09:00'), findsOneWidget);
      expect(find.text('10:30'), findsOneWidget);
    },
  );

  testWidgets(
    'permite navegar pelos próximos dias',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: HomePage(),
        ),
      );

      expect(find.text('Hoje'), findsOneWidget);
      expect(find.text('Ter'), findsWidgets);
      expect(find.text('Qua'), findsWidgets);
      expect(find.text('Qui'), findsWidgets);

      await tester.drag(
        find.byKey(const Key('days_selector')),
        const Offset(-300, 0),
      );

      await tester.pumpAndSettle();

      expect(find.text('Sex'), findsWidgets);
    },
  );

  testWidgets(
    'permite navegar pelos horários disponíveis',
        (WidgetTester tester) async {
      await tester.pumpWidget(
        const MaterialApp(
          home: HomePage(),
        ),
      );

      expect(find.text('09:00'), findsOneWidget);
      expect(find.text('10:30'), findsOneWidget);
      final timesSelector = find.byKey(
        const Key('times_selector'),
      );

      expect(timesSelector, findsOneWidget);

      await tester.drag(
        timesSelector,
        const Offset(0, -500),
      );
      await tester.pumpAndSettle();

      expect(find.text('18:30'), findsOneWidget);
    },
  );
}
