import 'dart:convert';

import 'package:barber_saas_mobile/features/admin/auth/admin_auth.dart';
import 'package:barber_saas_mobile/features/admin/auth/admin_gate.dart';
import 'package:barber_saas_mobile/features/admin/auth/admin_login_page.dart';
import 'package:barber_saas_mobile/features/admin/home/admin_home_page.dart';
import 'package:barber_saas_mobile/features/admin/more/admin_more_page.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' show ClientException;
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  setUp(() {
    SharedPreferences.setMockInitialValues({});
  });

  testWidgets('abre Login sem sessão e screen admin não bypassa autenticação',
      (tester) async {
    await tester.pumpWidget(MaterialApp(home: AdminGate()));
    await tester.pumpAndSettle();

    expect(find.byType(AdminLoginPage), findsOneWidget);
    expect(find.text('Área administrativa'), findsOneWidget);
    expect(find.byType(AdminHomePage), findsNothing);
  });

  testWidgets('login válido persiste token e abre Home', (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/auth/login') {
          return http.Response(jsonEncode(_loginJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );
    final store = AdminAuthStore();

    await tester.pumpWidget(
      MaterialApp(home: AdminLoginPage(store: store, api: api)),
    );
    await tester.enterText(
        find.widgetWithText(TextFormField, 'E-mail'), 'admin@example.com');
    await tester.enterText(
        find.widgetWithText(TextFormField, 'Senha'), 'secret');
    await tester.tap(find.text('Entrar'));
    await tester.pumpAndSettle();

    expect(await store.token(), 'raw-token');
    expect(find.byType(AdminHomePage), findsOneWidget);
  });

  testWidgets('login inválido mostra erro e mantém tela', (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async =>
            http.Response(jsonEncode({'error': 'Credenciais inválidas.'}), 401),
      ),
    );

    await tester.pumpWidget(
      MaterialApp(home: AdminLoginPage(store: AdminAuthStore(), api: api)),
    );
    await tester.enterText(
        find.widgetWithText(TextFormField, 'E-mail'), 'admin@example.com');
    await tester.enterText(
        find.widgetWithText(TextFormField, 'Senha'), 'wrong');
    await tester.tap(find.text('Entrar'));
    await tester.pumpAndSettle();

    expect(find.text('Credenciais inválidas.'), findsOneWidget);
    expect(find.byType(AdminLoginPage), findsOneWidget);
  });

  testWidgets('/auth/me 200 restaura sessão e abre Admin Home', (tester) async {
    SharedPreferences.setMockInitialValues({'admin_auth_token': 'saved'});
    final store = AdminAuthStore();
    final api = BarberApi(
      tokenProvider: store.token,
      client: MockClient((request) async {
        if (request.url.path == '/api/auth/me') {
          return http.Response(jsonEncode(_adminUserJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester
        .pumpWidget(MaterialApp(home: AdminGate(store: store, api: api)));
    await tester.pumpAndSettle();

    expect(await store.token(), 'saved');
    expect(find.byType(AdminHomePage), findsOneWidget);
  });

  testWidgets('401 no AdminGate limpa token e volta ao Login', (tester) async {
    SharedPreferences.setMockInitialValues({'admin_auth_token': 'saved'});
    final store = AdminAuthStore();
    final api = BarberApi(
      tokenProvider: store.token,
      client: MockClient(
        (_) async => http.Response(jsonEncode({'error': 'Unauthorized'}), 401),
      ),
    );

    await tester
        .pumpWidget(MaterialApp(home: AdminGate(store: store, api: api)));
    await tester.pumpAndSettle();

    expect(await store.token(), isNull);
    expect(find.byType(AdminLoginPage), findsOneWidget);
  });

  testWidgets('500 no AdminGate mantém token e permite tentar novamente',
      (tester) async {
    SharedPreferences.setMockInitialValues({'admin_auth_token': 'saved'});
    final store = AdminAuthStore();
    var meCalls = 0;
    final api = BarberApi(
      tokenProvider: store.token,
      client: MockClient((request) async {
        if (request.url.path == '/api/auth/me') {
          meCalls++;
          if (meCalls == 1) {
            return http.Response(jsonEncode({'error': 'Falha'}), 500);
          }
          return http.Response(jsonEncode(_adminUserJson), 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester
        .pumpWidget(MaterialApp(home: AdminGate(store: store, api: api)));
    await tester.pumpAndSettle();

    expect(await store.token(), 'saved');
    expect(find.text('Não foi possível confirmar sua sessão.'), findsOneWidget);

    await tester.tap(find.text('Tentar novamente'));
    await tester.pumpAndSettle();

    expect(meCalls, 2);
    expect(await store.token(), 'saved');
    expect(find.byType(AdminHomePage), findsOneWidget);
  });

  testWidgets('erro de rede no AdminGate mantém token', (tester) async {
    SharedPreferences.setMockInitialValues({'admin_auth_token': 'saved'});
    final store = AdminAuthStore();
    final api = BarberApi(
      tokenProvider: store.token,
      client: MockClient((request) async {
        throw ClientException('offline', request.url);
      }),
    );

    await tester
        .pumpWidget(MaterialApp(home: AdminGate(store: store, api: api)));
    await tester.pumpAndSettle();

    expect(await store.token(), 'saved');
    expect(find.text('Não foi possível confirmar sua sessão.'), findsOneWidget);
  });

  testWidgets('Mais Sair chama logout e limpa token', (tester) async {
    SharedPreferences.setMockInitialValues({'admin_auth_token': 'saved'});
    final store = AdminAuthStore();
    final api = BarberApi(
      tokenProvider: store.token,
      client: MockClient((request) async {
        if (request.url.path == '/api/auth/logout') {
          return http.Response('', 200);
        }
        return http.Response(jsonEncode(_agendaJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminMorePage(api: api)));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Sair'));
    await tester.pumpAndSettle();

    expect(await store.token(), isNull);
    expect(find.byType(AdminLoginPage), findsOneWidget);
  });
}

const _adminUserJson = {
  'id': 'admin-1',
  'name': 'Jhow',
  'email': 'admin@example.com',
  'barbershopId': 'shop-1',
  'barbershopName': 'Jhow Cortes',
};

const _loginJson = {
  'token': 'raw-token',
  'user': _adminUserJson,
};

const _agendaJson = {
  'barberId': barberId,
  'date': '2026-09-09',
  'workingDay': true,
  'slots': [],
};
