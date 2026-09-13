import 'dart:convert';

import 'package:barber_saas_mobile/features/admin/daily_agenda/daily_agenda_page.dart';
import 'package:barber_saas_mobile/features/admin/home/admin_home_page.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  testWidgets('carrega resumo real do dia e abre agenda completa',
      (tester) async {
    final paths = <String>[];
    final api = BarberApi(
      client: MockClient((request) async {
        paths.add(request.url.path);
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester.pumpWidget(
      MaterialApp(
        home: AdminHomePage(api: api, today: DateTime(2099, 9, 12)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Hoje'), findsOneWidget);
    expect(find.text('2'), findsOneWidget);
    expect(find.text('agendamentos hoje'), findsOneWidget);
    expect(find.text('R\$ 40,00'), findsOneWidget);
    expect(find.text('Valor agendado hoje'), findsOneWidget);
    expect(find.text('restantes'), findsNothing);
    expect(find.text('Agendado'), findsNothing);
    expect(find.text('10:00'), findsOneWidget);
    expect(find.text('Gabriel'), findsOneWidget);
    expect(find.text('Barba'), findsOneWidget);
    expect(find.text('Carlos'), findsNothing);

    await tester.drag(find.byType(Scrollable).first, const Offset(0, -500));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Ver agenda completa'));
    await tester.pumpAndSettle();

    expect(find.byType(DailyAgendaPage), findsOneWidget);
    expect(find.text('Agenda'), findsWidgets);
    expect(paths, everyElement('/api/v1/barbers/$barberId/daily-agenda'));
  });

  testWidgets('mostra loading, erro e estado vazio', (tester) async {
    final response = Future<http.Response>.delayed(
      const Duration(milliseconds: 10),
      () => http.Response(jsonEncode({'error': 'Falha'}), 500),
    );
    final api = BarberApi(client: MockClient((_) => response));

    await tester.pumpWidget(MaterialApp(home: AdminHomePage(api: api)));
    expect(find.byType(CircularProgressIndicator), findsOneWidget);
    await tester.pumpAndSettle();

    expect(find.text('Não foi possível carregar o início.'), findsOneWidget);
    expect(find.text('Falha'), findsOneWidget);

    final emptyApi = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_emptyAgendaJson), 200),
      ),
    );
    await tester.pumpWidget(
      MaterialApp(home: AdminHomePage(key: UniqueKey(), api: emptyApi)),
    );
    await tester.pumpAndSettle();

    await tester.ensureVisible(find.text('Nenhum próximo atendimento.'));
    expect(find.text('Nenhum próximo atendimento.'), findsOneWidget);
    expect(find.text('Nenhum outro horário hoje.'), findsOneWidget);
  });
}

const _agendaJson = {
  'barberId': barberId,
  'date': '2099-09-12',
  'workingDay': true,
  'slots': [
    {
      'dateTime': '2099-09-12T09:30:00',
      'status': 'OCCUPIED',
      'appointmentId': 'appointment-1',
      'appointmentStatus': 'NO_SHOW',
      'customer': {'id': 'customer-1', 'name': 'João'},
      'services': [
        {'id': 'service-1', 'name': 'Corte'}
      ],
      'totalPrice': 40,
    },
    {
      'dateTime': '2099-09-12T10:00:00',
      'status': 'OCCUPIED',
      'appointmentId': 'appointment-2',
      'appointmentStatus': 'SCHEDULED',
      'customer': {'id': 'customer-2', 'name': 'Gabriel'},
      'services': [
        {'id': 'service-2', 'name': 'Barba'}
      ],
      'totalPrice': 40,
    },
    {
      'dateTime': '2099-09-12T11:00:00',
      'status': 'OCCUPIED',
      'appointmentId': 'appointment-3',
      'appointmentStatus': 'CANCELED',
      'customer': {'id': 'customer-3', 'name': 'Carlos'},
      'services': [
        {'id': 'service-3', 'name': 'Sobrancelha'}
      ],
      'totalPrice': 25,
    },
  ],
};

const _emptyAgendaJson = {
  'barberId': barberId,
  'date': '2099-09-12',
  'workingDay': true,
  'slots': [],
};
