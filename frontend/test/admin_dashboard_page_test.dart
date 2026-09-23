import 'dart:convert';

import 'package:barber_saas_mobile/features/admin/dashboard/admin_dashboard_page.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  testWidgets('alterna períodos rápidos e exibe valor e agendamentos',
      (tester) async {
    final requests = <Uri>[];
    final api = BarberApi(client: MockClient((request) async {
      requests.add(request.url);
      return http.Response(jsonEncode(_summaryFor(request.url)), 200);
    }));

    await tester.pumpWidget(MaterialApp(
      home: AdminDashboardPage(api: api, today: DateTime(2026, 9, 13)),
    ));
    await tester.pumpAndSettle();

    expect(find.text('R\$ 90,00'), findsOneWidget);
    expect(find.text('3'), findsOneWidget);
    expect(find.text('Agendamentos'), findsOneWidget);

    await tester.tap(find.text('Hoje'));
    await tester.pumpAndSettle();
    expect(requests.last.queryParameters, {
      'startDate': '2026-09-13',
      'endDate': '2026-09-13',
    });

    await tester.tap(find.text('Ontem'));
    await tester.pumpAndSettle();
    expect(requests.last.queryParameters, {
      'startDate': '2026-09-12',
      'endDate': '2026-09-12',
    });

    await tester.tap(find.text('Este mês'));
    await tester.pumpAndSettle();
    expect(requests.last.queryParameters, {
      'startDate': '2026-09-01',
      'endDate': '2026-09-30',
    });
  });

  testWidgets('período personalizado atualiza o resumo', (tester) async {
    final api = BarberApi(
        client: MockClient((request) async =>
            http.Response(jsonEncode(_summaryFor(request.url)), 200)));
    await tester.pumpWidget(MaterialApp(
      home: AdminDashboardPage(
        api: api,
        today: DateTime(2026, 9, 13),
        pickRange: (_, __) async => DateTimeRange(
          start: DateTime(2026, 9, 5),
          end: DateTime(2026, 9, 12),
        ),
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Selecionar período'));
    await tester.pumpAndSettle();

    expect(find.text('05/09/2026 até 12/09/2026'), findsOneWidget);
  });

  testWidgets(
      'calendário customizado seleciona um intervalo e recarrega o resumo',
      (tester) async {
    final requests = <Uri>[];
    final api = BarberApi(client: MockClient((request) async {
      requests.add(request.url);
      return http.Response(jsonEncode(_summaryFor(request.url)), 200);
    }));
    await tester.pumpWidget(MaterialApp(
      home: AdminDashboardPage(api: api, today: DateTime(2026, 9, 13)),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.text('Selecionar período'));
    await tester.pumpAndSettle();

    expect(find.text('Setembro 2026'), findsOneWidget);
    expect(find.text('Dom'), findsOneWidget);
    await tester.tap(find.byKey(const ValueKey('calendar-day-2026-09-05')));
    await tester.tap(find.byKey(const ValueKey('calendar-day-2026-09-12')));
    await tester.pumpAndSettle();

    expect(requests.last.queryParameters, {
      'startDate': '2026-09-05',
      'endDate': '2026-09-12',
    });
  });

  testWidgets('erro oferece tentativa novamente', (tester) async {
    var calls = 0;
    final api = BarberApi(client: MockClient((_) async {
      calls++;
      return calls == 1
          ? http.Response(jsonEncode({'error': 'Falha'}), 500)
          : http.Response(
              jsonEncode({
                'barberId': barberId,
                'startDate': '2026-09-01',
                'endDate': '2026-09-30',
                'appointmentCount': 3,
                'scheduledValue': 90,
              }),
              200);
    }));
    await tester.pumpWidget(MaterialApp(home: AdminDashboardPage(api: api)));
    await tester.pumpAndSettle();

    expect(find.text('Falha'), findsOneWidget);
    await tester.tap(find.text('Tentar novamente'));
    await tester.pumpAndSettle();
    expect(find.text('Valor agendado'), findsOneWidget);
  });
}

Map<String, dynamic> _summaryFor(Uri uri) => {
      'barberId': barberId,
      'startDate': uri.queryParameters['startDate'],
      'endDate': uri.queryParameters['endDate'],
      'appointmentCount': 3,
      'scheduledValue': 90,
    };
