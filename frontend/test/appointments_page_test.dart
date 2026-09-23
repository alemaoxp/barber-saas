import 'dart:convert';

import 'package:barber_saas_mobile/features/appointments/appointments_page.dart';
import 'package:barber_saas_mobile/features/home/home_page.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  testWidgets('mostra sino ativo conforme o interesse retornado pelo backend',
      (tester) async {
    SharedPreferences.setMockInitialValues(
        {'customer_phone': '(11) 99999-9999'});
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/public/appointments') {
          return http.Response(
              jsonEncode([
                {
                  'id': 'appointment-1',
                  'customerId': 'customer-1',
                  'serviceIds': [],
                  'totalPrice': 40,
                  'appointmentDateTime': '2099-09-10T10:30:00',
                  'status': 'SCHEDULED',
                  'cancelToken': 'cancel-1',
                }
              ]),
              200);
        }
        if (request.url.path == '/api/public/barbers/$barberId/services') {
          return http.Response('[]', 200);
        }
        if (request.url.path.endsWith('/active')) {
          return http.Response(
              jsonEncode({
                'id': 'interest-1',
                'customerId': 'customer-1',
                'appointmentId': 'appointment-1',
                'status': 'ACTIVE',
              }),
              200);
        }
        if (request.url.path.endsWith('/opportunities')) {
          return http.Response('[]', 200);
        }
        return http.Response('not found', 404);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AppointmentsPage(api: api)));
    await tester.pumpAndSettle();

    expect(find.byTooltip('Desativar antecipação'), findsOneWidget);
  });

  testWidgets('abre confirmação da vaga recebida sem aceitar automaticamente',
      (tester) async {
    SharedPreferences.setMockInitialValues(
        {'customer_phone': '(11) 99999-9999'});
    var acceptCalls = 0;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/public/appointments') {
          return http.Response(
              jsonEncode([
                {
                  'id': 'appointment-1',
                  'customerId': 'customer-1',
                  'serviceIds': [],
                  'totalPrice': 40,
                  'appointmentDateTime': '2099-09-10T10:30:00',
                  'status': 'SCHEDULED',
                  'cancelToken': 'cancel-1',
                }
              ]),
              200);
        }
        if (request.url.path == '/api/public/barbers/$barberId/services') {
          return http.Response('[]', 200);
        }
        if (request.url.path.endsWith('/active')) {
          return http.Response(
              jsonEncode({
                'id': 'interest-1',
                'customerId': 'customer-1',
                'appointmentId': 'appointment-1',
                'status': 'ACTIVE',
              }),
              200);
        }
        if (request.url.path.endsWith('/opportunities')) {
          return http.Response(
              jsonEncode([
                {
                  'availableSlotId': 'slot-1',
                  'availableDateTime': '2099-09-09T10:00:00',
                }
              ]),
              200);
        }
        if (request.url.path.endsWith('/accept')) {
          acceptCalls++;
          return http.Response('{}', 200);
        }
        return http.Response('not found', 404);
      }),
    );

    await tester.pumpWidget(MaterialApp(
      home: AppointmentsPage(
          api: api,
          selectedAvailableSlotId: 'slot-1',
          selectedAvailabilityInterestId: 'interest-1'),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Vaga anterior disponível'), findsNWidgets(2));
    expect(find.text('ACEITAR NOVO HORÁRIO'), findsOneWidget);
    expect(find.text('MANTER MEU HORÁRIO ATUAL'), findsOneWidget);
    expect(acceptCalls, 0);
  });

  testWidgets('volta para Home ao abrir Meus Agendamentos diretamente',
      (tester) async {
    SharedPreferences.setMockInitialValues({});
    final api =
        BarberApi(client: MockClient((_) async => http.Response('[]', 200)));

    await tester.pumpWidget(MaterialApp(home: AppointmentsPage(api: api)));
    await tester.pumpAndSettle();
    await tester.tap(find.byIcon(Icons.arrow_back_ios_new_rounded));
    await tester.pumpAndSettle();

    expect(find.byType(HomePage), findsOneWidget);
  });

  testWidgets('volta somente para a rota anterior no fluxo normal',
      (tester) async {
    SharedPreferences.setMockInitialValues({});
    final api =
        BarberApi(client: MockClient((_) async => http.Response('[]', 200)));

    await tester.pumpWidget(MaterialApp(home: Builder(builder: (context) {
      return Scaffold(
        body: TextButton(
          onPressed: () => Navigator.of(context).push(MaterialPageRoute(
            builder: (_) => AppointmentsPage(api: api),
          )),
          child: const Text('ABRIR'),
        ),
      );
    })));
    await tester.tap(find.text('ABRIR'));
    await tester.pumpAndSettle();
    await tester.tap(find.byIcon(Icons.arrow_back_ios_new_rounded));
    await tester.pumpAndSettle();

    expect(find.text('ABRIR'), findsOneWidget);
    expect(find.byType(AppointmentsPage), findsNothing);
  });

  testWidgets('deep link abre confirmação e volta para Home', (tester) async {
    SharedPreferences.setMockInitialValues(
        {'customer_phone': '(11) 99999-9999'});
    final api = BarberApi(client: MockClient((request) async {
      if (request.url.path == '/api/public/appointments') {
        return http.Response(
            jsonEncode([
              {
                'id': 'appointment-1',
                'customerId': 'customer-1',
                'serviceIds': [],
                'totalPrice': 40,
                'appointmentDateTime': '2099-09-10T10:30:00',
                'status': 'SCHEDULED',
                'cancelToken': 'cancel-1',
              }
            ]),
            200);
      }
      if (request.url.path.endsWith('/active')) {
        return http.Response(
            jsonEncode({
              'id': 'interest-1',
              'customerId': 'customer-1',
              'appointmentId': 'appointment-1',
              'status': 'ACTIVE',
            }),
            200);
      }
      if (request.url.path.endsWith('/opportunities')) {
        return http.Response(
            jsonEncode([
              {
                'availableSlotId': 'slot-1',
                'availableDateTime': '2099-09-09T10:00:00',
              }
            ]),
            200);
      }
      return http.Response('[]', 200);
    }));

    await tester.pumpWidget(MaterialApp(
      home: AppointmentsPage(
          api: api,
          selectedAvailableSlotId: 'slot-1',
          selectedAvailabilityInterestId: 'interest-1'),
    ));
    await tester.pumpAndSettle();
    expect(find.text('ACEITAR NOVO HORÁRIO'), findsOneWidget);

    await tester.tap(find.text('MANTER MEU HORÁRIO ATUAL'));
    await tester.pumpAndSettle();
    await tester.tap(find.byIcon(Icons.arrow_back_ios_new_rounded));
    await tester.pumpAndSettle();

    expect(find.byType(HomePage), findsOneWidget);
  });

  testWidgets('deep link selects the matching interest when two use one slot',
      (tester) async {
    SharedPreferences.setMockInitialValues(
        {'customer_phone': '(11) 99999-9999'});
    final api = BarberApi(client: MockClient((request) async {
      if (request.url.path == '/api/public/appointments') {
        return http.Response(
            jsonEncode([
              {
                'id': 'appointment-a',
                'customerId': 'customer-1',
                'serviceIds': [],
                'totalPrice': 40,
                'appointmentDateTime': '2099-09-10T10:30:00',
                'status': 'SCHEDULED',
                'cancelToken': 'cancel-a',
              },
              {
                'id': 'appointment-b',
                'customerId': 'customer-1',
                'serviceIds': [],
                'totalPrice': 40,
                'appointmentDateTime': '2099-09-11T10:30:00',
                'status': 'SCHEDULED',
                'cancelToken': 'cancel-b',
              },
            ]),
            200);
      }
      if (request.url.path == '/api/public/barbers/$barberId/services') {
        return http.Response('[]', 200);
      }
      if (request.url.path.endsWith('/active')) {
        final appointmentId = request.url.queryParameters['appointmentId'];
        return http.Response(
            jsonEncode({
              'id': appointmentId == 'appointment-a'
                  ? 'interest-a'
                  : 'interest-b',
              'customerId': 'customer-1',
              'appointmentId': appointmentId,
              'status': 'ACTIVE',
            }),
            200);
      }
      if (request.url.path.endsWith('/opportunities')) {
        return http.Response(
            jsonEncode([
              {
                'availableSlotId': 'slot-1',
                'availableDateTime': '2099-09-09T10:00:00',
              }
            ]),
            200);
      }
      return http.Response('[]', 200);
    }));

    await tester.pumpWidget(MaterialApp(
      home: AppointmentsPage(
        api: api,
        selectedAvailableSlotId: 'slot-1',
        selectedAvailabilityInterestId: 'interest-b',
      ),
    ));
    await tester.pumpAndSettle();

    expect(find.text('Seu horário atual\n11 SET · 10:30'), findsOneWidget);
  });
}
