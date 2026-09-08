import 'dart:convert';

import 'package:barber_saas_mobile/features/appointments/appointments_page.dart';
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
                  'appointmentDateTime': '2026-09-10T10:30:00',
                  'status': 'SCHEDULED',
                  'cancelToken': 'cancel-1',
                }
              ]),
              200);
        }
        if (request.url.path == '/api/public/services') {
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
}
