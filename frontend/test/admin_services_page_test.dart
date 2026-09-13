import 'dart:async';
import 'dart:convert';

import 'package:barber_saas_mobile/features/admin/more/admin_more_page.dart';
import 'package:barber_saas_mobile/features/admin/services/admin_services_page.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  testWidgets('Mais abre Serviços com volta para Mais', (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return http.Response('[]', 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminMorePage(api: api)));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Serviços'));
    await tester.pumpAndSettle();

    expect(find.byType(AdminServicesPage), findsOneWidget);
    expect(find.text('Gerencie os serviços da barbearia'), findsOneWidget);
    expect(find.byTooltip('Voltar para Mais'), findsOneWidget);

    await tester.tap(find.byTooltip('Voltar para Mais'));
    await tester.pumpAndSettle();

    expect(find.byType(AdminMorePage), findsOneWidget);
    expect(find.byIcon(Icons.arrow_back), findsNothing);
  });

  testWidgets('mostra estado vazio', (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response('[]', 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminServicesPage(api: api)));
    await tester.pumpAndSettle();

    expect(find.text('Nenhum serviço cadastrado.'), findsOneWidget);
  });

  testWidgets('renderiza serviços retornados pela API', (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_servicesJson), 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminServicesPage(api: api)));
    await tester.pumpAndSettle();

    expect(find.text('Corte'), findsOneWidget);
    expect(find.text('R\$ 40,00'), findsOneWidget);
    expect(find.text('Barba'), findsOneWidget);
    expect(find.text('R\$ 30,00'), findsOneWidget);
  });

  testWidgets('abre Novo serviço e valida campos', (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response('[]', 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminServicesPage(api: api)));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Novo serviço'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Salvar serviço'));
    await tester.pumpAndSettle();

    expect(find.text('Informe o nome do serviço.'), findsOneWidget);
    expect(find.text('Informe o preço.'), findsOneWidget);

    await tester.enterText(
        find.widgetWithText(TextFormField, 'Nome do serviço'), 'Corte');
    await tester.enterText(find.widgetWithText(TextFormField, 'Preço'), '0');
    await tester.tap(find.text('Salvar serviço'));
    await tester.pumpAndSettle();

    expect(find.text('Informe um preço maior que zero.'), findsOneWidget);
  });

  testWidgets('criação bem-sucedida atualiza a lista', (tester) async {
    var servicesRequestCount = 0;
    late http.Request postRequest;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'POST') {
          postRequest = request;
          return http.Response(jsonEncode(_createdServiceJson), 201);
        }
        servicesRequestCount++;
        return http.Response(
          jsonEncode(servicesRequestCount == 1 ? [] : [_createdServiceJson]),
          200,
        );
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminServicesPage(api: api)));
    await tester.pumpAndSettle();
    expect(find.text('Nenhum serviço cadastrado.'), findsOneWidget);

    await tester.tap(find.text('Novo serviço'));
    await tester.pumpAndSettle();
    await tester.enterText(
        find.widgetWithText(TextFormField, 'Nome do serviço'), 'Sobrancelha');
    await tester.enterText(
        find.widgetWithText(TextFormField, 'Preço'), '25,50');
    await tester.tap(find.text('Salvar serviço'));
    await tester.pumpAndSettle();

    expect(postRequest.url.path, '/api/v1/barbers/$barberId/services');
    expect(jsonDecode(postRequest.body), {
      'name': 'Sobrancelha',
      'description': '',
      'durationMinutes': 30,
      'price': 25.5,
      'active': true,
    });
    expect(find.text('Sobrancelha'), findsOneWidget);
    expect(find.text('R\$ 25,50'), findsOneWidget);
  });

  testWidgets('tocar no serviço abre detalhes e edita com payload mínimo',
      (tester) async {
    var servicesRequestCount = 0;
    late http.Request putRequest;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'PUT') {
          putRequest = request;
          return http.Response(jsonEncode(_updatedServiceJson), 200);
        }
        servicesRequestCount++;
        return http.Response(
          jsonEncode(servicesRequestCount == 1
              ? _servicesJson
              : [_updatedServiceJson, _servicesJson[1]]),
          200,
        );
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminServicesPage(api: api)));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Corte'));
    await tester.pumpAndSettle();

    expect(find.text('Corte'), findsWidgets);
    expect(find.text('R\$ 40,00'), findsWidgets);
    await tester.tap(find.text('Editar'));
    await tester.pumpAndSettle();

    expect(
        find.widgetWithText(TextFormField, 'Nome do serviço'), findsOneWidget);
    expect(find.widgetWithText(TextFormField, 'Preço'), findsOneWidget);
    expect(find.text('Editar serviço'), findsOneWidget);

    await tester.enterText(
        find.widgetWithText(TextFormField, 'Nome do serviço'), 'Corte Premium');
    await tester.enterText(
        find.widgetWithText(TextFormField, 'Preço'), '55,50');
    await tester.tap(find.text('Salvar alterações'));
    await tester.pumpAndSettle();

    expect(putRequest.method, 'PUT');
    expect(
      putRequest.url.path,
      '/api/v1/barbers/$barberId/services/service-1',
    );
    expect(jsonDecode(putRequest.body), {
      'name': 'Corte Premium',
      'price': 55.5,
    });
    expect(find.text('Corte Premium'), findsOneWidget);
    expect(find.text('R\$ 55,50'), findsOneWidget);
  });

  testWidgets('edição de serviço mostra loading, bloqueia duplo envio e erro',
      (tester) async {
    final putResponse = Completer<http.Response>();
    var putCalls = 0;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'PUT') {
          putCalls++;
          return putResponse.future;
        }
        return http.Response(jsonEncode(_servicesJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminServicesPage(api: api)));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Corte'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Editar'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Salvar alterações'));
    await tester.pump();

    expect(find.text('Salvando...'), findsOneWidget);
    await tester.tap(find.text('Salvando...'));
    await tester.pump();
    expect(putCalls, 1);

    putResponse.complete(
      http.Response(jsonEncode({'error': 'Nome já existe.'}), 409),
    );
    await tester.pumpAndSettle();

    expect(find.text('Nome já existe.'), findsOneWidget);
    expect(find.text('Editar serviço'), findsOneWidget);
  });

  testWidgets('swipe para esquerda configura fundo de exclusão do serviço',
      (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_servicesJson), 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminServicesPage(api: api)));
    await tester.pumpAndSettle();

    final dismissible = tester.widget<Dismissible>(
      find.ancestor(
        of: find.text('Corte'),
        matching: find.byType(Dismissible),
      ),
    );

    expect(dismissible.direction, DismissDirection.endToStart);
    expect(dismissible.secondaryBackground, isNotNull);
    expect(find.byIcon(Icons.delete_outline_rounded), findsNothing);
    expect(find.text('Excluir serviço?'), findsNothing);
  });

  testWidgets('swipe completo chama API e remove da lista', (tester) async {
    var servicesRequestCount = 0;
    late http.Request deleteRequest;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'DELETE') {
          deleteRequest = request;
          return http.Response('', 204);
        }
        servicesRequestCount++;
        return http.Response(
          jsonEncode(
              servicesRequestCount == 1 ? _servicesJson : [_servicesJson[1]]),
          200,
        );
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminServicesPage(api: api)));
    await tester.pumpAndSettle();
    await _swipeFirstService(tester);
    await tester.pumpAndSettle();

    expect(
        deleteRequest.url.path, '/api/v1/barbers/$barberId/services/service-1');
    expect(find.text('Excluir serviço?'), findsNothing);
    expect(find.text('Corte'), findsNothing);
    expect(find.text('Barba'), findsOneWidget);
  });

  testWidgets('erro ao excluir serviço restaura item e mostra mensagem',
      (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'DELETE') {
          return http.Response(
            jsonEncode({'error': 'Serviço possui agendamentos.'}),
            409,
          );
        }
        return http.Response(jsonEncode(_servicesJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminServicesPage(api: api)));
    await tester.pumpAndSettle();
    await _swipeFirstService(tester);
    await tester.pumpAndSettle();

    expect(find.text('Excluir serviço?'), findsNothing);
    expect(find.text('Serviço possui agendamentos.'), findsOneWidget);
    expect(find.text('Corte'), findsOneWidget);
  });
}

Future<void> _swipeFirstService(WidgetTester tester) async {
  await tester.drag(
    find.ancestor(
      of: find.text('Corte'),
      matching: find.byType(Dismissible),
    ),
    const Offset(-500, 0),
  );
}

const _agendaJson = {
  'barberId': barberId,
  'date': '2026-09-09',
  'workingDay': true,
  'slots': [
    {
      'dateTime': '2026-09-09T09:30:00',
      'status': 'FREE',
    },
  ],
};

const _servicesJson = [
  {
    'id': 'service-1',
    'name': 'Corte',
    'description': 'Corte masculino',
    'durationMinutes': 30,
    'price': 40,
    'active': true,
  },
  {
    'id': 'service-2',
    'name': 'Barba',
    'description': 'Barba completa',
    'durationMinutes': 30,
    'price': 30,
    'active': true,
  },
];

const _createdServiceJson = {
  'id': 'service-3',
  'name': 'Sobrancelha',
  'description': '',
  'durationMinutes': 30,
  'price': 25.5,
  'active': true,
};

const _updatedServiceJson = {
  'id': 'service-1',
  'name': 'Corte Premium',
  'description': 'Corte masculino',
  'durationMinutes': 30,
  'price': 55.5,
  'active': true,
};
