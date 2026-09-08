import 'dart:convert';

import 'package:barber_saas_mobile/models/appointment_data.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  test('envia o customerId carregado junto da subscription Web Push', () async {
    var calls = 0;
    final api = BarberApi(
      client: MockClient((request) async {
        calls++;
        if (calls == 1) {
          expect(request.method, 'GET');
          expect(request.url.path, '/api/dev/push/public-key');
          return http.Response(
              jsonEncode({'publicKey': 'vapid-public-key'}), 200);
        }
        expect(request.method, 'POST');
        expect(request.url.path, '/api/dev/push/subscription');
        expect(jsonDecode(request.body), {
          'endpoint': 'https://push.example.test/subscription',
          'p256dh': 'p256dh-key',
          'auth': 'auth-key',
          'customerId': '4817ecd7-1341-4830-a7e5-b351d4da3fe0',
        });
        return http.Response('', 204);
      }),
      createPushSubscription: (_) async => {
        'endpoint': 'https://push.example.test/subscription',
        'keys': {'p256dh': 'p256dh-key', 'auth': 'auth-key'},
      },
    );

    await api.enableTestPush('4817ecd7-1341-4830-a7e5-b351d4da3fe0');
    expect(calls, 2);
  });

  test('cria o agendamento com IDs e horário ISO reais', () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response(
          jsonEncode({
            'id': 'appointment-1',
            'customerId': 'customer-1',
            'serviceIds': ['service-1'],
            'totalPrice': 40.0,
            'appointmentDateTime': '2026-09-09T10:30:00',
            'status': 'SCHEDULED',
            'createdAt': '2026-09-06T10:00:00',
            'cancelToken': 'token-1',
          }),
          201,
        );
      }),
    );

    final appointment = await api.createAppointment(
      AppointmentData(
        name: 'Ana',
        phone: '(11) 99999-9999',
        serviceIds: ['service-1'],
        appointmentDateTime: DateTime(2026, 9, 9, 10, 30),
      ),
    );

    expect(request.method, 'POST');
    expect(request.url.queryParameters['barberId'], barberId);
    expect(
        jsonDecode(request.body)['appointmentDateTime'], '2026-09-09T10:30:00');
    expect(appointment.cancelToken, 'token-1');
    expect(appointment.total, 40.0);
  });

  test('normaliza horários públicos sem alterar a grade', () async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response('["09:00:00", "10:30:00"]', 200),
      ),
    );

    expect(
      await api.availableSlots(DateTime(2026, 9, 9)),
      ['09:00', '10:30'],
    );
  });

  test('trata o retorno de dia sem expediente como lista vazia', () async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(
          jsonEncode(
              {'status': 400, 'error': 'Barbeiro não atende neste dia.'}),
          400,
        ),
      ),
    );

    expect(await api.availableSlots(DateTime(2026, 9, 1)), isEmpty);
  });

  test('cria e consulta interesse ativo real do agendamento', () async {
    var calls = 0;
    final api = BarberApi(
      client: MockClient((request) async {
        calls++;
        expect(request.url.path,
            '/api/v1/customers/customer-1/availability-interests${calls == 1 ? '' : '/active'}');
        if (calls == 1) {
          expect(request.method, 'POST');
          expect(jsonDecode(request.body), {'appointmentId': 'appointment-1'});
          return http.Response(jsonEncode(_interestJson), 201);
        }
        expect(request.method, 'GET');
        expect(request.url.queryParameters['appointmentId'], 'appointment-1');
        return http.Response(jsonEncode(_interestJson), 200);
      }),
    );

    expect(
        (await api.createAvailabilityInterest('customer-1', 'appointment-1'))
            .id,
        'interest-1');
    expect(
        (await api.activeAvailabilityInterest('customer-1', 'appointment-1'))
            ?.status,
        'ACTIVE');
  });

  test('cancela interesse e lista oportunidades reais', () async {
    var calls = 0;
    final api = BarberApi(
      client: MockClient((request) async {
        calls++;
        expect(request.url.path,
            '/api/v1/customers/customer-1/availability-interests/interest-1${calls == 2 ? '/opportunities' : ''}');
        if (calls == 1) {
          expect(request.method, 'DELETE');
          return http.Response('', 204);
        }
        expect(request.method, 'GET');
        return http.Response(jsonEncode([_opportunityJson]), 200);
      }),
    );

    await api.cancelAvailabilityInterest('customer-1', 'interest-1');
    expect(
        (await api.availabilityOpportunities('customer-1', 'interest-1'))
            .single
            .availableSlotId,
        'slot-1');
  });

  test('aceita oportunidade e informa quando a vaga já foi ocupada', () async {
    final api = BarberApi(
      client: MockClient((request) async {
        expect(request.method, 'POST');
        expect(jsonDecode(request.body), {'availableSlotId': 'slot-1'});
        return http.Response(
            jsonEncode({'error': 'Vaga de antecipação indisponível.'}), 400);
      }),
    );

    await expectLater(
      api.acceptAvailabilityOpportunity('customer-1', 'interest-1', 'slot-1'),
      throwsA(isA<BarberApiException>().having((error) => error.message,
          'message', 'Vaga de antecipação indisponível.')),
    );
  });
}

const _interestJson = {
  'id': 'interest-1',
  'customerId': 'customer-1',
  'appointmentId': 'appointment-1',
  'createdAt': '2026-09-06T10:00:00',
  'status': 'ACTIVE',
};

const _opportunityJson = {
  'availableSlotId': 'slot-1',
  'availableDateTime': '2026-09-09T09:30:00',
};
