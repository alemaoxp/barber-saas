import 'dart:async';
import 'dart:convert';

import 'package:barber_saas_mobile/features/admin/daily_agenda/daily_agenda_page.dart';
import 'package:barber_saas_mobile/features/admin/daily_agenda/widgets/new_appointment_bottom_sheet.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  testWidgets('renderiza FREE, OCCUPIED, BLOCKED e navegação admin',
      (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_agendaJson), 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(
      home: DailyAgendaPage(
        api: api,
        initialDate: DateTime(2026, 9, 9),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Jhow Cortes'), findsOneWidget);
    expect(find.text('Agenda'), findsNWidgets(2));
    expect(find.text('Manhã'), findsOneWidget);
    expect(find.text('Tarde'), findsOneWidget);
    expect(find.text('Horário livre'), findsOneWidget);
    expect(find.text('Gabriel Silva'), findsOneWidget);
    expect(find.text('Corte + Barba'), findsOneWidget);
    expect(find.text('R\$ 80,00'), findsOneWidget);
    expect(find.text('Agendado'), findsOneWidget);
    expect(find.text('Bloqueado'), findsOneWidget);
    expect(find.text('Dentista'), findsOneWidget);
    expect(find.text('Serviços'), findsOneWidget);
    expect(find.text('Clientes'), findsOneWidget);
    expect(find.text('Mais'), findsOneWidget);
    expect(find.byKey(Key('admin_day_${_dateParam(DateTime.now())}')),
        findsOneWidget);
    expect(
        find.byKey(Key('admin_day_${_dateParam(
          DateTime.now().subtract(const Duration(days: 1)),
        )}')),
        findsNothing);
    expect(find.byTooltip('Novo agendamento'), findsOneWidget);
  });

  testWidgets('tocar no mais de FREE abre bottom sheet Novo agendamento',
      (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_agendaJson), 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(
      home: DailyAgendaPage(
        api: api,
        initialDate: DateTime(2026, 9, 9),
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.byTooltip('Novo agendamento'));
    await tester.pumpAndSettle();

    expect(find.text('Novo agendamento'), findsOneWidget);
    expect(find.text('Quarta-feira, 09 de setembro'), findsOneWidget);
    expect(
      find.descendant(
        of: find.byType(NewAppointmentBottomSheet),
        matching: find.text('09:30'),
      ),
      findsOneWidget,
    );
    expect(find.text('Selecionar cliente'), findsOneWidget);
    expect(find.text('Selecionar serviços'), findsOneWidget);

    final button = tester.widget<ElevatedButton>(
      find.widgetWithText(ElevatedButton, 'Continuar'),
    );
    expect(button.onPressed, isNull);
  });

  testWidgets('tocar Selecionar cliente abre seleção e mostra loading',
      (tester) async {
    final customersResponse = Completer<http.Response>();
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          return customersResponse.future;
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openCustomerSelection(tester, api);

    expect(find.text('Selecionar cliente'), findsWidgets);
    expect(find.text('Buscar por nome ou telefone'), findsOneWidget);
    expect(find.byType(CircularProgressIndicator), findsOneWidget);

    customersResponse.complete(http.Response('[]', 200));
    await tester.pumpAndSettle();
  });

  testWidgets('clientes retornados aparecem com nome e telefone',
      (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          return http.Response(jsonEncode(_customersJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openCustomerSelection(tester, api);
    await tester.pumpAndSettle();

    expect(find.text('Gabriel Santana'), findsOneWidget);
    expect(find.text('(13) 99999-9999'), findsOneWidget);
  });

  testWidgets('lista vazia mostra estado vazio', (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          return http.Response('[]', 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openCustomerSelection(tester, api);
    await tester.pumpAndSettle();

    expect(find.text('Nenhum cliente encontrado.'), findsOneWidget);
  });

  testWidgets('tocar cliente retorna seleção ao Novo agendamento',
      (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          return http.Response(jsonEncode(_customersJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openCustomerSelection(tester, api);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Gabriel Santana'));
    await tester.pumpAndSettle();

    expect(
      find.descendant(
        of: find.byType(NewAppointmentBottomSheet),
        matching: find.text('Gabriel Santana'),
      ),
      findsOneWidget,
    );
    expect(find.text('(13) 99999-9999'), findsOneWidget);
  });

  testWidgets('Continuar permanece desabilitado após selecionar só cliente',
      (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          return http.Response(jsonEncode(_customersJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openCustomerSelection(tester, api);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Gabriel Santana'));
    await tester.pumpAndSettle();

    final button = tester.widget<ElevatedButton>(
      find.widgetWithText(ElevatedButton, 'Continuar'),
    );
    expect(button.onPressed, isNull);
  });

  testWidgets('busca envia query com debounce', (tester) async {
    final queries = <String?>[];
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          queries.add(request.url.queryParameters['query']);
          return http.Response('[]', 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openCustomerSelection(tester, api);
    await tester.pumpAndSettle();

    await tester.enterText(find.byKey(const Key('admin_customer_search')), 'ga');
    await tester.pump(const Duration(milliseconds: 200));
    await tester.enterText(
        find.byKey(const Key('admin_customer_search')), 'gabriel');
    await tester.pump(const Duration(milliseconds: 400));
    await tester.pumpAndSettle();

    expect(queries, [null, 'gabriel']);
  });

  testWidgets('erro ao buscar clientes é exibido', (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          return http.Response(
            jsonEncode({'error': 'Barbeiro não encontrado.'}),
            404,
          );
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openCustomerSelection(tester, api);
    await tester.pumpAndSettle();

    expect(find.text('Não foi possível carregar clientes.'), findsOneWidget);
    expect(find.text('Barbeiro não encontrado.'), findsOneWidget);
  });

  testWidgets('OCCUPIED e BLOCKED não oferecem ação de criação',
      (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_agendaWithoutFreeJson), 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(
      home: DailyAgendaPage(
        api: api,
        initialDate: DateTime(2026, 9, 9),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Gabriel Silva'), findsOneWidget);
    expect(find.text('Bloqueado'), findsOneWidget);
    expect(find.byTooltip('Novo agendamento'), findsNothing);
  });

  testWidgets('mostra mensagem integrada para dia sem expediente',
      (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(
          jsonEncode({
            'barberId': barberId,
            'date': '2026-09-09',
            'workingDay': false,
            'slots': [],
          }),
          200,
        ),
      ),
    );

    await tester.pumpWidget(MaterialApp(
      home: DailyAgendaPage(
        api: api,
        initialDate: DateTime(2026, 9, 9),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Não há expediente neste dia.'), findsOneWidget);
  });

  testWidgets('workingDay true com slots vazio mostra agenda vazia',
      (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(
          jsonEncode({
            'barberId': barberId,
            'date': '2026-09-09',
            'workingDay': true,
            'slots': [],
          }),
          200,
        ),
      ),
    );

    await tester.pumpWidget(MaterialApp(
      home: DailyAgendaPage(
        api: api,
        initialDate: DateTime(2026, 9, 9),
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Agenda vazia neste dia.'), findsOneWidget);
    expect(find.text('Não há expediente neste dia.'), findsNothing);
  });

  testWidgets('troca de data consulta novamente o daily-agenda',
      (tester) async {
    final paths = <String>[];
    final api = BarberApi(
      client: MockClient((request) async {
        paths.add(request.url.toString());
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(
      home: DailyAgendaPage(
        api: api,
        initialDate: DateTime.now(),
      ),
    ));
    await tester.pumpAndSettle();

    await tester.tap(find.text(DateTime.now()
        .add(const Duration(days: 1))
        .day
        .toString()
        .padLeft(2, '0')));
    await tester.pumpAndSettle();

    expect(paths.length, 2);
    expect(paths.last, contains('/api/v1/barbers/$barberId/daily-agenda'));
  });
}

Future<void> _openCustomerSelection(WidgetTester tester, BarberApi api) async {
  await tester.pumpWidget(MaterialApp(
    home: DailyAgendaPage(
      api: api,
      initialDate: DateTime(2026, 9, 9),
    ),
  ));
  await tester.pumpAndSettle();
  await tester.tap(find.byTooltip('Novo agendamento'));
  await tester.pumpAndSettle();
  await tester.tap(find.text('Selecionar cliente'));
  await tester.pump();
}

String _dateParam(DateTime value) =>
    '${value.year.toString().padLeft(4, '0')}-${value.month.toString().padLeft(2, '0')}-${value.day.toString().padLeft(2, '0')}';

const _agendaJson = {
  'barberId': barberId,
  'date': '2026-09-09',
  'workingDay': true,
  'slots': [
    {
      'dateTime': '2026-09-09T09:30:00',
      'status': 'FREE',
    },
    {
      'dateTime': '2026-09-09T10:00:00',
      'status': 'OCCUPIED',
      'appointmentId': 'appointment-1',
      'appointmentStatus': 'SCHEDULED',
      'customer': {'id': 'customer-1', 'name': 'Gabriel Silva'},
      'services': [
        {'id': 'service-1', 'name': 'Corte'},
        {'id': 'service-2', 'name': 'Barba'},
      ],
      'totalPrice': 80,
    },
    {
      'dateTime': '2026-09-09T14:30:00',
      'status': 'BLOCKED',
      'block': {
        'id': 'block-1',
        'startDateTime': '2026-09-09T14:30:00',
        'endDateTime': '2026-09-09T15:00:00',
        'reason': 'Dentista',
      },
    },
  ],
};

const _agendaWithoutFreeJson = {
  'barberId': barberId,
  'date': '2026-09-09',
  'workingDay': true,
  'slots': [
    {
      'dateTime': '2026-09-09T10:00:00',
      'status': 'OCCUPIED',
      'appointmentId': 'appointment-1',
      'appointmentStatus': 'SCHEDULED',
      'customer': {'id': 'customer-1', 'name': 'Gabriel Silva'},
      'services': [
        {'id': 'service-1', 'name': 'Corte'},
      ],
      'totalPrice': 40,
    },
    {
      'dateTime': '2026-09-09T14:30:00',
      'status': 'BLOCKED',
      'block': {
        'id': 'block-1',
        'startDateTime': '2026-09-09T14:30:00',
        'endDateTime': '2026-09-09T15:00:00',
        'reason': 'Dentista',
      },
    },
  ],
};

const _customersJson = [
  {
    'id': 'customer-1',
    'name': 'Gabriel Santana',
    'phone': '(13) 99999-9999',
  },
];
