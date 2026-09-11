import 'dart:convert';

import 'package:barber_saas_mobile/features/admin/clients/admin_clients_page.dart';
import 'package:barber_saas_mobile/features/admin/daily_agenda/daily_agenda_page.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  testWidgets('tocar na aba Clientes abre AdminClientsPage', (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/v1/barbers/$barberId/customers') {
          return http.Response('[]', 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(
      home: DailyAgendaPage(
        api: api,
        initialDate: DateTime(2026, 9, 9),
      ),
    ));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Clientes').last);
    await tester.pumpAndSettle();

    expect(find.byType(AdminClientsPage), findsOneWidget);
    expect(find.text('Gerencie seus clientes'), findsOneWidget);
  });

  testWidgets('mostra estado vazio', (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response('[]', 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminClientsPage(api: api)));
    await tester.pumpAndSettle();

    expect(find.text('Nenhum cliente cadastrado.'), findsOneWidget);
  });

  testWidgets('renderiza clientes retornados pela API', (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_clientsJson), 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminClientsPage(api: api)));
    await tester.pumpAndSettle();

    expect(find.text('Gabriel Santana'), findsOneWidget);
    expect(find.text('(13) 99999-9999'), findsOneWidget);
    expect(find.text('Carlos Lima'), findsOneWidget);
    expect(find.text('(13) 98888-8888'), findsOneWidget);
  });

  testWidgets('abre Novo cliente e valida campos', (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response('[]', 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminClientsPage(api: api)));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Novo cliente'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Salvar cliente'));
    await tester.pumpAndSettle();

    expect(find.text('Informe o nome.'), findsOneWidget);
    expect(find.text('Informe o telefone.'), findsOneWidget);

    await tester.enterText(find.widgetWithText(TextFormField, 'Nome'), 'Ana');
    await tester.enterText(
        find.widgetWithText(TextFormField, 'Telefone'), '123');
    await tester.tap(find.text('Salvar cliente'));
    await tester.pumpAndSettle();

    expect(find.text('Informe um telefone válido.'), findsOneWidget);
  });

  testWidgets('criação bem-sucedida atualiza a lista', (tester) async {
    var listRequestCount = 0;
    late http.Request postRequest;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'POST') {
          postRequest = request;
          return http.Response(jsonEncode(_createdClientJson), 201);
        }
        listRequestCount++;
        return http.Response(
          jsonEncode(listRequestCount == 1 ? [] : [_createdClientJson]),
          200,
        );
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminClientsPage(api: api)));
    await tester.pumpAndSettle();
    expect(find.text('Nenhum cliente cadastrado.'), findsOneWidget);

    await tester.tap(find.text('Novo cliente'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Nome'),
      'Gabriel Santana',
    );
    await tester.enterText(
      find.widgetWithText(TextFormField, 'Telefone'),
      '13999999999',
    );
    await tester.tap(find.text('Salvar cliente'));
    await tester.pumpAndSettle();

    expect(postRequest.url.path, '/api/v1/barbers/$barberId/customers');
    expect(jsonDecode(postRequest.body), {
      'name': 'Gabriel Santana',
      'phone': '(13) 99999-9999',
      'email': null,
      'birthDate': null,
      'notes': null,
      'active': true,
    });
    expect(find.text('Gabriel Santana'), findsOneWidget);
    expect(find.text('(13) 99999-9999'), findsOneWidget);
  });

  testWidgets('swipe para esquerda configura fundo de exclusão do cliente',
      (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_clientsJson), 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminClientsPage(api: api)));
    await tester.pumpAndSettle();

    final dismissible = tester.widget<Dismissible>(
      find.ancestor(
        of: find.text('Gabriel Santana'),
        matching: find.byType(Dismissible),
      ),
    );

    expect(dismissible.direction, DismissDirection.endToStart);
    expect(dismissible.secondaryBackground, isNotNull);
    expect(find.byIcon(Icons.delete_outline_rounded), findsNothing);
    expect(find.text('Excluir cliente?'), findsNothing);
  });

  testWidgets('card de cliente ocupa largura disponível da lista',
      (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_clientsJson), 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminClientsPage(api: api)));
    await tester.pumpAndSettle();

    final size = tester.getSize(
      find.ancestor(
        of: find.text('Gabriel Santana'),
        matching: find.byType(Dismissible),
      ),
    );

    expect(size.width, 756);
  });

  testWidgets('swipe completo chama API e remove da lista', (tester) async {
    var listRequestCount = 0;
    late http.Request deleteRequest;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'DELETE') {
          deleteRequest = request;
          return http.Response('', 204);
        }
        listRequestCount++;
        return http.Response(
          jsonEncode(listRequestCount == 1 ? _clientsJson : [_clientsJson[1]]),
          200,
        );
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminClientsPage(api: api)));
    await tester.pumpAndSettle();
    await _swipeFirstClient(tester);
    await tester.pumpAndSettle();

    expect(deleteRequest.url.path,
        '/api/v1/barbers/$barberId/customers/customer-1');
    expect(find.text('Excluir cliente?'), findsNothing);
    expect(find.text('Gabriel Santana'), findsNothing);
    expect(find.text('Carlos Lima'), findsOneWidget);
  });

  testWidgets('erro ao excluir cliente restaura item e mostra mensagem',
      (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'DELETE') {
          return http.Response(
            jsonEncode({'error': 'Cliente possui agendamentos.'}),
            409,
          );
        }
        return http.Response(jsonEncode(_clientsJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminClientsPage(api: api)));
    await tester.pumpAndSettle();
    await _swipeFirstClient(tester);
    await tester.pumpAndSettle();

    expect(find.text('Excluir cliente?'), findsNothing);
    expect(find.text('Cliente possui agendamentos.'), findsOneWidget);
    expect(find.text('Gabriel Santana'), findsOneWidget);
  });
}

Future<void> _swipeFirstClient(WidgetTester tester) async {
  await tester.drag(
    find.ancestor(
      of: find.text('Gabriel Santana'),
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

const _clientsJson = [
  {
    'id': 'customer-1',
    'name': 'Gabriel Santana',
    'phone': '(13) 99999-9999',
  },
  {
    'id': 'customer-2',
    'name': 'Carlos Lima',
    'phone': '(13) 98888-8888',
  },
];

const _createdClientJson = {
  'id': 'customer-1',
  'name': 'Gabriel Santana',
  'phone': '(13) 99999-9999',
  'email': null,
  'birthDate': null,
  'notes': null,
  'active': true,
};
