import 'dart:convert';

import 'package:barber_saas_mobile/features/admin/daily_agenda/daily_agenda_page.dart';
import 'package:barber_saas_mobile/features/admin/admin_navigation.dart';
import 'package:barber_saas_mobile/features/admin/home/admin_home_page.dart';
import 'package:barber_saas_mobile/features/admin/dashboard/admin_dashboard_page.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  testWidgets(
      'mantém a barra do Admin e anima apenas o conteúdo ao trocar de aba',
      (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.endsWith('/customers')) {
          return http.Response('[]', 200);
        }
        if (request.url.path.endsWith('/dashboard/summary')) {
          return http.Response(jsonEncode(_summaryJson), 200);
        }
        if (request.url.path.endsWith('/next-appointment')) {
          return http.Response(jsonEncode(_todayNextAppointmentJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminHomePage(api: api)));
    await _pumpHome(tester);

    expect(find.byType(AnimatedSwitcher), findsOneWidget);
    expect(find.text('Jhow Cortes'), findsOneWidget);
    expect(find.text('BARBEARIA'), findsOneWidget);
    expect(find.byTooltip('Menu'), findsNothing);
    expect(find.byTooltip('Notificações'), findsNothing);
    final navigation = tester.element(find.byType(AdminNavigation));

    await tester.tap(find.text('Agenda').last);
    await tester.pump();

    expect(find.byType(AnimatedSwitcher), findsOneWidget);
    expect(identical(navigation, tester.element(find.byType(AdminNavigation))),
        isTrue);
    expect(
      tester.widget<AdminNavigation>(find.byType(AdminNavigation)).activeTab,
      AdminTab.agenda,
    );
    expect(find.text('Agenda'), findsWidgets);
    expect(find.byTooltip('Menu'), findsNothing);
    expect(find.byTooltip('Notificações'), findsNothing);
    expect(find.text('Início'), findsOneWidget);
    expect(find.text('Clientes'), findsOneWidget);
    expect(find.text('Mais'), findsOneWidget);
  });

  testWidgets('carrega resumo real do dia e abre agenda completa',
      (tester) async {
    final paths = <String>[];
    final api = BarberApi(
      client: MockClient((request) async {
        paths.add(request.url.path);
        if (request.url.path.endsWith('/dashboard/summary')) {
          return http.Response(jsonEncode(_summaryJson), 200);
        }
        if (request.url.path.endsWith('/next-appointment')) {
          return http.Response(jsonEncode(_todayNextAppointmentJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester.pumpWidget(
      MaterialApp(
        home: AdminHomePage(api: api, today: DateTime(2099, 9, 12)),
      ),
    );
    await _pumpHome(tester);

    expect(find.text('Hoje'), findsOneWidget);
    expect(find.text('2'), findsOneWidget);
    expect(find.text('agendamentos hoje'), findsOneWidget);
    expect(find.text('R\$ 80,00'), findsOneWidget);
    expect(find.text('Valor agendado hoje'), findsOneWidget);
    expect(find.text('Este mês'), findsOneWidget);
    expect(find.text('restantes'), findsNothing);
    expect(find.text('Agendado'), findsNothing);
    expect(find.text('10:00'), findsOneWidget);
    expect(find.text('Gabriel'), findsOneWidget);
    expect(find.text('Barba'), findsOneWidget);
    expect(find.text('Carlos'), findsNothing);

    await tester.drag(find.byType(Scrollable).first, const Offset(0, -500));
    await tester.pump();
    await tester.tap(find.text('Ver agenda completa'));
    await _pumpHome(tester);

    expect(find.byType(DailyAgendaPage), findsOneWidget);
    expect(find.text('Agenda'), findsWidgets);
    expect(paths, contains('/api/v1/barbers/$barberId/daily-agenda'));
    expect(paths, contains('/api/v1/barbers/$barberId/dashboard/summary'));
  });

  testWidgets('resumo de agendamentos fica centralizado e compacto',
      (tester) async {
    await tester.binding.setSurfaceSize(const Size(390, 844));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    final api = BarberApi(client: MockClient((request) async {
      if (request.url.path.endsWith('/next-appointment')) {
        return http.Response(jsonEncode(_todayNextAppointmentJson), 200);
      }
      return http.Response(
          jsonEncode(request.url.path.endsWith('/dashboard/summary')
              ? _summaryJson
              : _agendaJson),
          200);
    }));
    await tester.pumpWidget(MaterialApp(
      home: AdminHomePage(api: api, today: DateTime(2099, 9, 12)),
    ));
    await _pumpHome(tester);

    final number = find.text('2');
    final label = find.text('agendamentos hoje');
    expect(tester.getTopLeft(label).dy,
        greaterThan(tester.getBottomLeft(number).dy));
    expect(tester.getCenter(number).dx, closeTo(tester.getCenter(label).dx, 1));
    final countCard =
        find.ancestor(of: number, matching: find.byType(Container)).first;
    final valueCard = find
        .ancestor(
            of: find.text('Valor agendado hoje'),
            matching: find.byType(Container))
        .first;
    expect(tester.getSize(countCard).height, lessThan(94));
    expect(tester.getRect(countCard).top, tester.getRect(valueCard).top);
    expect(tester.getRect(countCard).bottom, tester.getRect(valueCard).bottom);
    expect(tester.takeException(), isNull);
  });

  testWidgets('abre dashboard pelo resumo mensal', (tester) async {
    final api = BarberApi(client: MockClient((request) async {
      if (request.url.path.endsWith('/dashboard/summary')) {
        return http.Response(jsonEncode(_summaryJson), 200);
      }
      if (request.url.path.endsWith('/next-appointment')) {
        return http.Response(jsonEncode(_todayNextAppointmentJson), 200);
      }
      return http.Response(jsonEncode(_agendaJson), 200);
    }));
    await tester.pumpWidget(MaterialApp(
      home: AdminHomePage(api: api, today: DateTime(2099, 9, 12)),
    ));
    await _pumpHome(tester);

    expect(find.text('Ver dashboard'), findsNothing);
    await tester.ensureVisible(find.text('Este mês'));
    await tester.tap(find.text('Este mês'));
    await tester.pumpAndSettle();

    expect(find.byType(AdminDashboardPage), findsOneWidget);
  });

  testWidgets('mostra loading, erro e estado vazio', (tester) async {
    final response = Future<http.Response>.delayed(
      const Duration(milliseconds: 10),
      () => http.Response(jsonEncode({'error': 'Falha'}), 500),
    );
    final api = BarberApi(client: MockClient((request) {
      if (request.url.path.endsWith('/dashboard/summary')) {
        return Future.value(http.Response(jsonEncode(_summaryJson), 200));
      }
      if (request.url.path.endsWith('/next-appointment')) {
        return Future.value(http.Response('', 204));
      }
      return response;
    }));

    await tester.pumpWidget(MaterialApp(home: AdminHomePage(api: api)));
    expect(find.byType(CircularProgressIndicator), findsOneWidget);
    await tester.pumpAndSettle();

    expect(find.text('Não foi possível carregar o início.'), findsOneWidget);
    expect(find.text('Falha'), findsOneWidget);

    final emptyApi = BarberApi(
      client: MockClient(
        (request) async => request.url.path.endsWith('/dashboard/summary')
            ? http.Response(jsonEncode(_summaryJson), 200)
            : request.url.path.endsWith('/next-appointment')
                ? http.Response('', 204)
                : http.Response(jsonEncode(_emptyAgendaJson), 200),
      ),
    );
    await tester.pumpWidget(
      MaterialApp(home: AdminHomePage(key: UniqueKey(), api: emptyApi)),
    );
    await _pumpHome(tester);

    await tester.ensureVisible(find.text('Nenhum próximo atendimento.'));
    expect(find.text('Nenhum próximo atendimento.'), findsOneWidget);
    expect(find.text('Nenhum outro horário hoje.'), findsOneWidget);
  });

  testWidgets('mantém o próximo atendimento de hoje sem data adicional',
      (tester) async {
    final api = BarberApi(client: MockClient((request) async {
      if (request.url.path.endsWith('/dashboard/summary')) {
        return http.Response(jsonEncode(_summaryJson), 200);
      }
      if (request.url.path.endsWith('/next-appointment')) {
        return http.Response(jsonEncode(_todayNextAppointmentJson), 200);
      }
      return http.Response(jsonEncode(_agendaJson), 200);
    }));

    await tester.pumpWidget(MaterialApp(
      home: AdminHomePage(api: api, today: DateTime(2099, 9, 21)),
    ));
    await _pumpHome(tester);

    expect(find.text('10:00'), findsOneWidget);
    expect(find.text('SEGUNDA-FEIRA · 21 SET'), findsNothing);
  });

  testWidgets(
      'mostra o próximo scheduled futuro com a data e ignora dia sem agenda',
      (tester) async {
    final paths = <String>[];
    final api = BarberApi(client: MockClient((request) async {
      paths.add(request.url.path);
      if (request.url.path.endsWith('/dashboard/summary')) {
        return http.Response(jsonEncode(_summaryJson), 200);
      }
      if (request.url.path.endsWith('/next-appointment')) {
        return http.Response(jsonEncode(_wednesdayNextAppointmentJson), 200);
      }
      return http.Response(jsonEncode(_emptyAgendaJson), 200);
    }));

    await tester.pumpWidget(MaterialApp(
      home: AdminHomePage(api: api, today: DateTime(2099, 9, 21)),
    ));
    await _pumpHome(tester);

    expect(find.text('QUARTA-FEIRA · 23 SET'), findsOneWidget);
    expect(find.text('10:30'), findsOneWidget);
    expect(find.text('Gabriel'), findsOneWidget);
    await tester.drag(find.byType(Scrollable).first, const Offset(0, -500));
    await tester.pump();
    expect(find.text('Nenhum outro horário hoje.'), findsOneWidget);
    expect(paths.where((path) => path.endsWith('/daily-agenda')).length, 1);
    expect(paths.where((path) => path.endsWith('/next-appointment')).length, 1);
  });

  testWidgets('alinha horário e cliente lado a lado no próximo atendimento',
      (tester) async {
    final api = BarberApi(client: MockClient((request) async {
      if (request.url.path.endsWith('/dashboard/summary')) {
        return http.Response(jsonEncode(_summaryJson), 200);
      }
      if (request.url.path.endsWith('/next-appointment')) {
        return http.Response(jsonEncode(_wednesdayNextAppointmentJson), 200);
      }
      return http.Response(jsonEncode(_emptyAgendaJson), 200);
    }));

    await tester.pumpWidget(MaterialApp(
      home: AdminHomePage(api: api, today: DateTime(2099, 9, 21)),
    ));
    await _pumpHome(tester);

    final time = tester.getCenter(find.text('10:30'));
    final customer = tester.getCenter(find.text('Gabriel'));
    final service = tester.getCenter(find.text('Corte'));
    expect((time.dy - customer.dy).abs(), lessThan(16));
    expect(service.dy, greaterThan(customer.dy));
  });

  testWidgets('mostra estado vazio apenas quando não há scheduled futuro',
      (tester) async {
    final api = BarberApi(client: MockClient((request) async {
      if (request.url.path.endsWith('/dashboard/summary')) {
        return http.Response(jsonEncode(_summaryJson), 200);
      }
      if (request.url.path.endsWith('/next-appointment')) {
        return http.Response('', 204);
      }
      return http.Response(jsonEncode(_emptyAgendaJson), 200);
    }));

    await tester.pumpWidget(MaterialApp(
      home: AdminHomePage(api: api, today: DateTime(2099, 9, 21)),
    ));
    await _pumpHome(tester);

    expect(find.text('Nenhum próximo atendimento.'), findsOneWidget);
    expect(find.text('Nenhum outro horário hoje.'), findsOneWidget);
  });
}

Future<void> _pumpHome(WidgetTester tester) async {
  await tester.pump();
  await tester.pump();
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

const _summaryJson = {
  'barberId': barberId,
  'startDate': '2099-09-01',
  'endDate': '2099-09-30',
  'appointmentCount': 3,
  'scheduledValue': 90,
};

const _todayNextAppointmentJson = {
  'dateTime': '2099-09-21T10:00:00',
  'status': 'OCCUPIED',
  'appointmentId': 'appointment-2',
  'appointmentStatus': 'SCHEDULED',
  'customer': {'id': 'customer-2', 'name': 'Gabriel'},
  'services': [
    {'id': 'service-2', 'name': 'Barba'}
  ],
  'totalPrice': 40,
};

const _wednesdayNextAppointmentJson = {
  'dateTime': '2099-09-23T10:30:00',
  'status': 'OCCUPIED',
  'appointmentId': 'appointment-4',
  'appointmentStatus': 'SCHEDULED',
  'customer': {'id': 'customer-2', 'name': 'Gabriel'},
  'services': [
    {'id': 'service-2', 'name': 'Corte'}
  ],
  'totalPrice': 40,
};
