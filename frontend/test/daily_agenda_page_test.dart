import 'dart:async';
import 'dart:convert';

import 'package:barber_saas_mobile/features/admin/daily_agenda/daily_agenda_page.dart';
import 'package:barber_saas_mobile/features/admin/daily_agenda/widgets/new_appointment_bottom_sheet.dart';
import 'package:barber_saas_mobile/features/admin/daily_agenda/widgets/service_selection_bottom_sheet.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  testWidgets('renderiza FREE, OCCUPIED, BLOCKED e navegação admin', (
    tester,
  ) async {
    String? statusRequestPath;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'PATCH') {
          statusRequestPath = request.url.path;
          expect(jsonDecode(request.body), {'status': 'NO_SHOW'});
          return http.Response(jsonEncode(_noShowAppointmentJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester.pumpWidget(
      MaterialApp(
        home: DailyAgendaPage(api: api, initialDate: DateTime(2026, 9, 9)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Jhow Cortes'), findsOneWidget);
    expect(find.text('Agenda'), findsNWidgets(2));
    final agendaBottomItem = find.ancestor(
      of: find.text('Agenda').last,
      matching: find.byType(InkWell),
    );
    expect(agendaBottomItem, findsOneWidget);
    expect(
      find.descendant(of: agendaBottomItem, matching: find.byType(Icon)),
      findsOneWidget,
    );
    expect(find.byIcon(Icons.calendar_today_rounded), findsOneWidget);
    expect(find.text('Manhã'), findsOneWidget);
    expect(find.text('Tarde'), findsOneWidget);
    expect(find.text('Horário livre'), findsOneWidget);
    expect(find.text('Gabriel Silva'), findsOneWidget);
    final occupiedCard = find.ancestor(
      of: find.text('Gabriel Silva'),
      matching: find.byType(InkWell),
    );
    expect(occupiedCard, findsOneWidget);
    expect(
      find.descendant(of: occupiedCard, matching: find.byType(CircleAvatar)),
      findsNothing,
    );
    expect(
      find.descendant(of: occupiedCard, matching: find.text('GS')),
      findsNothing,
    );
    expect(find.text('Corte + Barba'), findsOneWidget);
    expect(find.text('R\$ 80,00'), findsNothing);
    expect(find.text('Pendente'), findsNothing);
    expect(find.text('09:30'), findsOneWidget);
    expect(find.text('10:00'), findsOneWidget);
    expect(find.text('14:30'), findsOneWidget);
    for (final icon in _clockIcons) {
      expect(find.byIcon(icon), findsNothing);
    }
    expect(find.text('Bloqueado'), findsOneWidget);
    expect(find.text('Dentista'), findsOneWidget);
    expect(find.text('Início'), findsOneWidget);
    expect(find.text('Serviços'), findsNothing);
    expect(find.text('Clientes'), findsOneWidget);
    expect(find.text('Mais'), findsOneWidget);
    expect(
      find.byKey(Key('admin_day_${_dateParam(DateTime.now())}')),
      findsOneWidget,
    );
    expect(
      find.byKey(
        Key(
          'admin_day_${_dateParam(DateTime.now().subtract(const Duration(days: 1)))}',
        ),
      ),
      findsNothing,
    );
    expect(find.byTooltip('Novo agendamento'), findsOneWidget);

    await tester.tap(find.text('Gabriel Silva'));
    await tester.pumpAndSettle();
    expect(find.text('Concluir atendimento'), findsNothing);
    expect(find.text('Marcar como não compareceu'), findsOneWidget);

    await tester.tap(find.text('Marcar como não compareceu'));
    await tester.pumpAndSettle();
    expect(find.text('Confirmar falta?'), findsOneWidget);
    await tester.tap(find.widgetWithText(ElevatedButton, 'Não compareceu'));
    await tester.pumpAndSettle();
    expect(
      statusRequestPath,
      '/api/v1/barbers/$barberId/appointments/appointment-1/status',
    );
  });

  testWidgets('tocar no mais de FREE abre bottom sheet Novo agendamento', (
    tester,
  ) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_agendaJson), 200),
      ),
    );

    await tester.pumpWidget(
      MaterialApp(
        home: DailyAgendaPage(api: api, initialDate: DateTime(2026, 9, 9)),
      ),
    );
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

  testWidgets('tocar Selecionar cliente abre seleção e mostra loading', (
    tester,
  ) async {
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

  testWidgets('clientes retornados aparecem com nome e telefone', (
    tester,
  ) async {
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

  testWidgets('tocar cliente retorna seleção ao Novo agendamento', (
    tester,
  ) async {
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

  testWidgets('Continuar permanece desabilitado após selecionar só cliente', (
    tester,
  ) async {
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

    await tester.enterText(
      find.byKey(const Key('admin_customer_search')),
      'ga',
    );
    await tester.pump(const Duration(milliseconds: 200));
    await tester.enterText(
      find.byKey(const Key('admin_customer_search')),
      'gabriel',
    );
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

  testWidgets('tocar Selecionar serviços abre seleção e mostra loading', (
    tester,
  ) async {
    final servicesResponse = Completer<http.Response>();
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return servicesResponse.future;
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openServiceSelection(tester, api);

    expect(find.byType(ServiceSelectionBottomSheet), findsOneWidget);
    expect(find.text('Selecionar serviços'), findsWidgets);
    expect(find.byType(CircularProgressIndicator), findsOneWidget);

    servicesResponse.complete(http.Response('[]', 200));
    await tester.pumpAndSettle();
  });

  testWidgets(
    'tocar Selecionar serviços após selecionar cliente abre seleção',
    (tester) async {
      final api = BarberApi(
        client: MockClient((request) async {
          if (request.url.path.contains('/customers')) {
            return http.Response(jsonEncode(_customersJson), 200);
          }
          if (request.url.path == '/api/v1/barbers/$barberId/services') {
            return http.Response(jsonEncode(_servicesJson), 200);
          }
          return http.Response(jsonEncode(_agendaJson), 200);
        }),
      );

      await _openCustomerSelection(tester, api);
      await tester.pumpAndSettle();
      await tester.tap(find.text('Gabriel Santana'));
      await tester.pumpAndSettle();
      await tester.tap(find.text('Selecionar serviços'));
      await tester.pump();

      expect(find.byType(ServiceSelectionBottomSheet), findsOneWidget);
      await tester.pumpAndSettle();
      expect(find.text('Corte'), findsOneWidget);
    },
  );

  testWidgets('serviços retornados aparecem com nome e preço', (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return http.Response(jsonEncode(_servicesJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openServiceSelection(tester, api);
    await tester.pumpAndSettle();

    expect(find.text('Corte'), findsOneWidget);
    expect(find.text('Barba'), findsOneWidget);
    expect(find.text('R\$ 40,00'), findsOneWidget);
    expect(find.text('R\$ 30,00'), findsOneWidget);
  });

  testWidgets('permite selecionar mais de um serviço e confirmar', (
    tester,
  ) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return http.Response(jsonEncode(_servicesJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openServiceSelection(tester, api);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Corte'));
    await tester.pump();
    await tester.tap(find.text('Barba'));
    await tester.pump();

    expect(find.text('2 selecionados'), findsOneWidget);
    expect(find.text('R\$ 70,00'), findsOneWidget);

    await tester.tap(find.widgetWithText(ElevatedButton, 'Confirmar'));
    await tester.pumpAndSettle();

    expect(
      find.descendant(
        of: find.byType(NewAppointmentBottomSheet),
        matching: find.text('Corte + Barba'),
      ),
      findsOneWidget,
    );
    expect(find.text('R\$ 70,00'), findsOneWidget);
  });

  testWidgets('serviços vazio exibe estado adequado', (tester) async {
    final emptyApi = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return http.Response('[]', 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openServiceSelection(tester, emptyApi);
    await tester.pumpAndSettle();
    expect(find.text('Nenhum serviço encontrado.'), findsOneWidget);
  });

  testWidgets('erro ao buscar serviços é exibido', (tester) async {
    final errorApi = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return http.Response(
            jsonEncode({'error': 'Serviços indisponíveis.'}),
            500,
          );
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openServiceSelection(tester, errorApi);
    await tester.pumpAndSettle();
    expect(find.text('Não foi possível carregar serviços.'), findsOneWidget);
    expect(find.text('Serviços indisponíveis.'), findsOneWidget);
  });

  testWidgets('Continuar habilita apenas com cliente e serviço selecionados', (
    tester,
  ) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          return http.Response(jsonEncode(_customersJson), 200);
        }
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return http.Response(jsonEncode(_servicesJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _openCustomerSelection(tester, api);
    await tester.pumpAndSettle();
    await tester.tap(find.text('Gabriel Santana'));
    await tester.pumpAndSettle();

    var button = tester.widget<ElevatedButton>(
      find.widgetWithText(ElevatedButton, 'Continuar'),
    );
    expect(button.onPressed, isNull);

    await tester.tap(find.text('Selecionar serviços'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Corte'));
    await tester.pump();
    await tester.tap(find.widgetWithText(ElevatedButton, 'Confirmar'));
    await tester.pumpAndSettle();

    button = tester.widget<ElevatedButton>(
      find.widgetWithText(ElevatedButton, 'Continuar'),
    );
    expect(button.onPressed, isNotNull);
  });

  testWidgets('Continuar cria agendamento, fecha modal e recarrega agenda', (
    tester,
  ) async {
    var dailyAgendaCalls = 0;
    late Map<String, dynamic> payload;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          return http.Response(jsonEncode(_customersJson), 200);
        }
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return http.Response(jsonEncode(_servicesJson), 200);
        }
        if (request.method == 'POST' &&
            request.url.path == '/api/v1/barbers/$barberId/appointments') {
          payload = jsonDecode(request.body) as Map<String, dynamic>;
          return http.Response(jsonEncode(_createdAppointmentJson), 201);
        }
        dailyAgendaCalls++;
        return http.Response(
          jsonEncode(
            dailyAgendaCalls == 1 ? _agendaJson : _agendaAfterCreateJson,
          ),
          200,
        );
      }),
    );

    await _selectCustomerAndServices(tester, api, selectSecondService: true);
    await tester.tap(find.widgetWithText(ElevatedButton, 'Continuar'));
    await tester.pumpAndSettle();

    expect(payload, {
      'customerId': 'customer-1',
      'serviceIds': ['service-1', 'service-2'],
      'appointmentDateTime': '2026-09-09T09:30:00',
    });
    expect(find.byType(NewAppointmentBottomSheet), findsNothing);
    expect(dailyAgendaCalls, 2);
    expect(find.text('Horário livre'), findsNothing);
    expect(find.text('Gabriel Santana'), findsOneWidget);
    expect(find.text('Corte + Barba'), findsOneWidget);
    expect(find.text('R\$ 70,00'), findsNothing);
    expect(find.text('Pendente'), findsNothing);
    for (final icon in _clockIcons) {
      expect(find.byIcon(icon), findsNothing);
    }
  });

  testWidgets('Continuar mostra loading e impede múltiplos envios', (
    tester,
  ) async {
    var postCalls = 0;
    final postResponse = Completer<http.Response>();
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          return http.Response(jsonEncode(_customersJson), 200);
        }
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return http.Response(jsonEncode(_servicesJson), 200);
        }
        if (request.method == 'POST' &&
            request.url.path == '/api/v1/barbers/$barberId/appointments') {
          postCalls++;
          return postResponse.future;
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _selectCustomerAndServices(tester, api);
    await tester.tap(find.widgetWithText(ElevatedButton, 'Continuar'));
    await tester.pump();
    await tester.tap(find.widgetWithText(ElevatedButton, 'Criando...'));
    await tester.pump();

    expect(postCalls, 1);
    expect(find.text('Criando...'), findsOneWidget);

    postResponse.complete(
      http.Response(jsonEncode(_createdAppointmentJson), 201),
    );
    await tester.pumpAndSettle();
  });

  testWidgets('erro na criação mantém modal aberto e informa usuário', (
    tester,
  ) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path.contains('/customers')) {
          return http.Response(jsonEncode(_customersJson), 200);
        }
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return http.Response(jsonEncode(_servicesJson), 200);
        }
        if (request.method == 'POST' &&
            request.url.path == '/api/v1/barbers/$barberId/appointments') {
          return http.Response(
            jsonEncode({'error': 'Horário indisponível.'}),
            400,
          );
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await _selectCustomerAndServices(tester, api);
    await tester.tap(find.widgetWithText(ElevatedButton, 'Continuar'));
    await tester.pumpAndSettle();

    expect(find.byType(NewAppointmentBottomSheet), findsOneWidget);
    expect(find.text('Horário indisponível.'), findsOneWidget);
    expect(find.widgetWithText(ElevatedButton, 'Continuar'), findsOneWidget);
  });

  testWidgets('OCCUPIED e BLOCKED não oferecem ação de criação', (
    tester,
  ) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_agendaWithoutFreeJson), 200),
      ),
    );

    await tester.pumpWidget(
      MaterialApp(
        home: DailyAgendaPage(api: api, initialDate: DateTime(2026, 9, 9)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Gabriel Silva'), findsOneWidget);
    expect(find.text('Bloqueado'), findsOneWidget);
    expect(find.byTooltip('Novo agendamento'), findsNothing);
  });

  testWidgets('mostra mensagem integrada para dia sem expediente', (
    tester,
  ) async {
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

    await tester.pumpWidget(
      MaterialApp(
        home: DailyAgendaPage(api: api, initialDate: DateTime(2026, 9, 9)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Não há expediente neste dia.'), findsOneWidget);
  });

  testWidgets('workingDay true com slots vazio mostra agenda vazia', (
    tester,
  ) async {
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

    await tester.pumpWidget(
      MaterialApp(
        home: DailyAgendaPage(api: api, initialDate: DateTime(2026, 9, 9)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.text('Agenda vazia neste dia.'), findsOneWidget);
    expect(find.text('Não há expediente neste dia.'), findsNothing);
  });

  testWidgets('troca de data consulta novamente o daily-agenda', (
    tester,
  ) async {
    final paths = <String>[];
    final api = BarberApi(
      client: MockClient((request) async {
        paths.add(request.url.toString());
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester.pumpWidget(
      MaterialApp(
        home: DailyAgendaPage(api: api, initialDate: DateTime.now()),
      ),
    );
    await tester.pumpAndSettle();

    await tester.tap(
      find.text(
        DateTime.now()
            .add(const Duration(days: 1))
            .day
            .toString()
            .padLeft(2, '0'),
      ),
    );
    await tester.pumpAndSettle();

    expect(paths.length, 2);
    expect(paths.last, contains('/api/v1/barbers/$barberId/daily-agenda'));
  });
}

Future<void> _openCustomerSelection(WidgetTester tester, BarberApi api) async {
  await tester.pumpWidget(
    MaterialApp(
      home: DailyAgendaPage(api: api, initialDate: DateTime(2026, 9, 9)),
    ),
  );
  await tester.pumpAndSettle();
  await tester.tap(find.byTooltip('Novo agendamento'));
  await tester.pumpAndSettle();
  await tester.tap(find.text('Selecionar cliente'));
  await tester.pump();
}

Future<void> _openServiceSelection(WidgetTester tester, BarberApi api) async {
  await tester.pumpWidget(
    MaterialApp(
      home: DailyAgendaPage(api: api, initialDate: DateTime(2026, 9, 9)),
    ),
  );
  await tester.pumpAndSettle();
  await tester.tap(find.byTooltip('Novo agendamento'));
  await tester.pumpAndSettle();
  await tester.tap(find.text('Selecionar serviços'));
  await tester.pump();
}

Future<void> _selectCustomerAndServices(
  WidgetTester tester,
  BarberApi api, {
  bool selectSecondService = false,
}) async {
  await _openCustomerSelection(tester, api);
  await tester.pumpAndSettle();
  await tester.tap(find.text('Gabriel Santana'));
  await tester.pumpAndSettle();
  await tester.tap(find.text('Selecionar serviços'));
  await tester.pumpAndSettle();
  await tester.tap(find.text('Corte'));
  await tester.pump();
  if (selectSecondService) {
    await tester.tap(find.text('Barba'));
    await tester.pump();
  }
  await tester.tap(find.widgetWithText(ElevatedButton, 'Confirmar'));
  await tester.pumpAndSettle();
}

String _dateParam(DateTime value) =>
    '${value.year.toString().padLeft(4, '0')}-${value.month.toString().padLeft(2, '0')}-${value.day.toString().padLeft(2, '0')}';

const _agendaJson = {
  'barberId': barberId,
  'date': '2026-09-09',
  'workingDay': true,
  'slots': [
    {'dateTime': '2026-09-09T09:30:00', 'status': 'FREE'},
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

const _agendaAfterCreateJson = {
  'barberId': barberId,
  'date': '2026-09-09',
  'workingDay': true,
  'slots': [
    {
      'dateTime': '2026-09-09T09:30:00',
      'status': 'OCCUPIED',
      'appointmentId': 'appointment-created',
      'appointmentStatus': 'SCHEDULED',
      'customer': {'id': 'customer-1', 'name': 'Gabriel Santana'},
      'services': [
        {'id': 'service-1', 'name': 'Corte'},
        {'id': 'service-2', 'name': 'Barba'},
      ],
      'totalPrice': 70,
    },
  ],
};

const _customersJson = [
  {'id': 'customer-1', 'name': 'Gabriel Santana', 'phone': '(13) 99999-9999'},
];

const _servicesJson = [
  {
    'id': 'service-1',
    'name': 'Corte',
    'description': 'Corte masculino',
    'price': 40,
  },
  {
    'id': 'service-2',
    'name': 'Barba',
    'description': 'Barba completa',
    'price': 30,
  },
];

const _createdAppointmentJson = {
  'id': 'appointment-created',
  'customerId': 'customer-1',
  'serviceIds': ['service-1', 'service-2'],
  'totalPrice': 70,
  'appointmentDateTime': '2026-09-09T09:30:00',
  'status': 'SCHEDULED',
  'createdAt': '2026-09-10T10:00:00',
};

const _noShowAppointmentJson = {
  'id': 'appointment-1',
  'customerId': 'customer-1',
  'serviceIds': ['service-1', 'service-2'],
  'totalPrice': 80,
  'appointmentDateTime': '2026-09-09T10:00:00',
  'status': 'NO_SHOW',
  'createdAt': '2026-09-09T08:00:00',
};

const _clockIcons = [
  Icons.schedule,
  Icons.schedule_rounded,
  Icons.access_time,
  Icons.access_time_rounded,
  Icons.watch_later,
  Icons.watch_later_rounded,
  Icons.timer,
  Icons.timer_rounded,
];
