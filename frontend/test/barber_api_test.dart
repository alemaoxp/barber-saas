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

  test('createAdminAppointment envia payload administrativo correto', () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response(
          jsonEncode({
            'id': 'appointment-1',
            'customerId': 'customer-1',
            'serviceIds': ['service-1', 'service-2'],
            'totalPrice': 70,
            'appointmentDateTime': '2026-09-09T09:30:00',
            'status': 'SCHEDULED',
            'createdAt': '2026-09-06T10:00:00',
          }),
          201,
        );
      }),
    );

    final appointment = await api.createAdminAppointment(
      customerId: 'customer-1',
      serviceIds: ['service-1', 'service-2'],
      appointmentDateTime: DateTime(2026, 9, 9, 9, 30),
    );

    expect(request.method, 'POST');
    expect(request.url.path, '/api/v1/barbers/$barberId/appointments');
    expect(jsonDecode(request.body), {
      'customerId': 'customer-1',
      'serviceIds': ['service-1', 'service-2'],
      'appointmentDateTime': '2026-09-09T09:30:00',
    });
    expect(appointment.customerId, 'customer-1');
    expect(appointment.serviceIds, ['service-1', 'service-2']);
    expect(appointment.total, 70);
  });

  test('normaliza horários públicos sem alterar a grade', () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response('["09:00:00", "10:30:00"]', 200);
      }),
    );

    expect(
      await api.availableSlots(DateTime(2026, 9, 9)),
      ['09:00', '10:30'],
    );
    expect(request.url.path, '/api/public/barbers/$barberId/available-slots');
  });

  test('consulta meus agendamentos no contexto do barber', () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response('[]', 200);
      }),
    );

    await api.appointments('(11) 99999-9999');

    expect(request.url.path, '/api/public/appointments');
    expect(request.url.queryParameters['barberId'], barberId);
    expect(request.url.queryParameters['phone'], '(11) 99999-9999');
  });

  test('consulta daily-agenda administrativo com barberId existente e data',
      () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response(
          jsonEncode({
            'barberId': barberId,
            'date': '2026-09-09',
            'workingDay': true,
            'slots': [],
          }),
          200,
        );
      }),
    );

    final agenda = await api.dailyAgenda(DateTime(2026, 9, 9));

    expect(request.method, 'GET');
    expect(request.url.path, '/api/v1/barbers/$barberId/daily-agenda');
    expect(request.url.queryParameters['date'], '2026-09-09');
    expect(agenda.barberId, barberId);
  });

  test('adminServices consulta serviços no contexto administrativo do barber',
      () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response(jsonEncode([_serviceJson]), 200);
      }),
    );

    final services = await api.adminServices();

    expect(request.method, 'GET');
    expect(request.url.path, '/api/v1/barbers/$barberId/services');
    expect(services.single.id, 'service-1');
    expect(services.single.name, 'Corte');
    expect(services.single.price, 40);
  });

  test('adminServices mantém tratamento padrão de erro HTTP', () async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(
          jsonEncode({'error': 'Barbeiro não encontrado.'}),
          404,
        ),
      ),
    );

    await expectLater(
      api.adminServices(),
      throwsA(isA<BarberApiException>().having(
        (error) => error.message,
        'message',
        'Barbeiro não encontrado.',
      )),
    );
  });

  test('createAdminService envia POST administrativo do barber', () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response(jsonEncode(_serviceJson), 201);
      }),
    );

    final service = await api.createAdminService(
      name: 'Corte',
      price: 40,
    );

    expect(request.method, 'POST');
    expect(request.url.path, '/api/v1/barbers/$barberId/services');
    expect(jsonDecode(request.body), {
      'name': 'Corte',
      'description': '',
      'durationMinutes': 30,
      'price': 40.0,
      'active': true,
    });
    expect(service.name, 'Corte');
    expect(service.price, 40);
  });

  test('deleteAdminService chama DELETE administrativo do barber', () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response('', 204);
      }),
    );

    await api.deleteAdminService('service-1');

    expect(request.method, 'DELETE');
    expect(request.url.path, '/api/v1/barbers/$barberId/services/service-1');
  });

  test('adminCustomers sem query chama endpoint correto', () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response('[]', 200);
      }),
    );

    final customers = await api.adminCustomers();

    expect(customers, isEmpty);
    expect(request.method, 'GET');
    expect(request.url.path, '/api/v1/barbers/$barberId/customers');
    expect(request.url.query, isEmpty);
  });

  test('adminCustomers com query envia query corretamente', () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response('[]', 200);
      }),
    );

    await api.adminCustomers(query: ' Gabriel 13 ');

    expect(request.url.path, '/api/v1/barbers/$barberId/customers');
    expect(request.url.queryParameters['query'], 'Gabriel 13');
  });

  test('adminCustomers converte JSON para AdminCustomerSummary', () async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(
          jsonEncode([
            {
              'id': 'customer-1',
              'name': 'Gabriel Santana',
              'phone': '(13) 99999-9999',
            }
          ]),
          200,
        ),
      ),
    );

    final customers = await api.adminCustomers();

    expect(customers.single.id, 'customer-1');
    expect(customers.single.name, 'Gabriel Santana');
    expect(customers.single.phone, '(13) 99999-9999');
  });

  test('adminCustomers mantém tratamento padrão de erro HTTP', () async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(
          jsonEncode({'error': 'Barbeiro não encontrado.'}),
          404,
        ),
      ),
    );

    await expectLater(
      api.adminCustomers(),
      throwsA(isA<BarberApiException>().having(
        (error) => error.message,
        'message',
        'Barbeiro não encontrado.',
      )),
    );
  });

  test('createAdminCustomer envia POST administrativo do barber', () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response(
          jsonEncode({
            'id': 'customer-1',
            'name': 'Gabriel Santana',
            'phone': '(13) 99999-9999',
            'email': null,
            'birthDate': null,
            'notes': null,
            'active': true,
          }),
          201,
        );
      }),
    );

    final customer = await api.createAdminCustomer(
      name: 'Gabriel Santana',
      phone: '(13) 99999-9999',
    );

    expect(request.method, 'POST');
    expect(request.url.path, '/api/v1/barbers/$barberId/customers');
    expect(jsonDecode(request.body), {
      'name': 'Gabriel Santana',
      'phone': '(13) 99999-9999',
      'email': null,
      'birthDate': null,
      'notes': null,
      'active': true,
    });
    expect(customer.id, 'customer-1');
    expect(customer.name, 'Gabriel Santana');
    expect(customer.phone, '(13) 99999-9999');
  });

  test('deleteAdminCustomer chama DELETE administrativo do barber', () async {
    late http.Request request;
    final api = BarberApi(
      client: MockClient((incoming) async {
        request = incoming;
        return http.Response('', 204);
      }),
    );

    await api.deleteAdminCustomer('customer-1');

    expect(request.method, 'DELETE');
    expect(request.url.path, '/api/v1/barbers/$barberId/customers/customer-1');
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

const _serviceJson = {
  'id': 'service-1',
  'name': 'Corte',
  'description': 'Corte masculino',
  'price': 40,
};
