import 'dart:async';
import 'dart:convert';

import 'package:barber_saas_mobile/features/admin/daily_agenda/daily_agenda_page.dart';
import 'package:barber_saas_mobile/features/admin/more/admin_schedule_blocks_page.dart';
import 'package:barber_saas_mobile/features/admin/more/admin_settings_page.dart';
import 'package:barber_saas_mobile/features/admin/more/admin_more_page.dart';
import 'package:barber_saas_mobile/features/admin/more/admin_working_hours_page.dart';
import 'package:barber_saas_mobile/features/admin/services/admin_services_page.dart';
import 'package:barber_saas_mobile/services/barber_api.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  testWidgets('tocar na aba Mais abre opções administrativas', (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/v1/barbers/$barberId/weekly-schedule') {
          return http.Response(jsonEncode(_weeklyScheduleJson), 200);
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
    await tester.tap(find.text('Mais').last);
    await tester.pumpAndSettle();

    expect(find.byType(AdminMorePage), findsOneWidget);
    expect(find.text('Horários de funcionamento'), findsOneWidget);
    expect(find.text('Expediente semanal e intervalo'), findsOneWidget);
    expect(find.text('Início'), findsOneWidget);
    expect(find.text('Serviços'), findsOneWidget);
    expect(find.text('Serviços e preços'), findsOneWidget);
    expect(find.text('Bloqueios da agenda'), findsOneWidget);
    expect(find.text('Folgas, férias e indisponibilidades'), findsOneWidget);
    expect(find.text('Configurações'), findsOneWidget);
    expect(find.text('Preferências do Admin'), findsOneWidget);
    expect(find.text('Sair'), findsOneWidget);
  });

  testWidgets('item Serviços abre página administrativa existente',
      (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.url.path == '/api/v1/barbers/$barberId/services') {
          return http.Response('[]', 200);
        }
        return http.Response(jsonEncode(_weeklyScheduleJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminMorePage(api: api)));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Serviços').first);
    await tester.pumpAndSettle();

    expect(find.byType(AdminServicesPage), findsOneWidget);
    expect(find.text('Gerencie os serviços da barbearia'), findsOneWidget);

    await tester.tap(find.byTooltip('Voltar para Mais'));
    await tester.pumpAndSettle();

    expect(find.byType(AdminMorePage), findsOneWidget);
  });

  testWidgets('abre configurações sem erro', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: AdminMorePage()));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Configurações'));
    await tester.pumpAndSettle();

    expect(find.byType(AdminSettingsPage), findsOneWidget);
    expect(
      find.text('Ainda não há opções configuráveis disponíveis.'),
      findsOneWidget,
    );
  });

  testWidgets('abre bloqueios, lista, cria e exclui', (tester) async {
    final calls = <String>[];
    late http.Request postRequest;
    final api = BarberApi(
      client: MockClient((request) async {
        calls.add('${request.method} ${request.url.path}');
        if (request.method == 'POST') {
          postRequest = request;
          return http.Response(jsonEncode(_createdBlockJson), 201);
        }
        if (request.method == 'DELETE') return http.Response('', 204);
        return http.Response(jsonEncode([_blockJson]), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminMorePage(api: api)));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Bloqueios da agenda'));
    await tester.pumpAndSettle();

    expect(find.byType(AdminScheduleBlocksPage), findsOneWidget);
    expect(find.text('Dentista'), findsOneWidget);
    expect(find.text('09/09/2026 13:00 - 14:00'), findsOneWidget);

    await tester.tap(find.text('Novo bloqueio'));
    await tester.pumpAndSettle();
    await tester.enterText(
        find.byKey(const Key('schedule-block-date')), '10/09/2026');
    await tester.enterText(
        find.byKey(const Key('schedule-block-start')), '15:00');
    await tester.enterText(
        find.byKey(const Key('schedule-block-end')), '16:00');
    await tester.enterText(
        find.byKey(const Key('schedule-block-reason')), 'Banco');
    await tester.tap(find.text('Salvar bloqueio'));
    await tester.pumpAndSettle();

    expect(postRequest.url.path, '/api/v1/barbers/$barberId/schedule-blocks');
    expect(jsonDecode(postRequest.body), {
      'startDateTime': '2026-09-10T15:00:00',
      'endDateTime': '2026-09-10T16:00:00',
      'reason': 'Banco',
    });

    await tester.drag(find.text('Dentista'), const Offset(-500, 0));
    await tester.pumpAndSettle();

    expect(calls,
        contains('DELETE /api/v1/barbers/$barberId/schedule-blocks/block-1'));
  });

  testWidgets('bloqueios trata erro', (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode({'error': 'Falha'}), 500),
      ),
    );

    await tester
        .pumpWidget(MaterialApp(home: AdminScheduleBlocksPage(api: api)));
    await tester.pumpAndSettle();

    expect(
        find.text('Não foi possível carregar os bloqueios.'), findsOneWidget);
    expect(find.text('Falha'), findsOneWidget);
  });

  testWidgets('abre horários de funcionamento e renderiza agenda semanal',
      (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_weeklyScheduleJson), 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminMorePage(api: api)));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Horários de funcionamento'));
    await tester.pumpAndSettle();

    expect(find.byType(AdminWorkingHoursPage), findsOneWidget);
    expect(find.text('Segunda-feira'), findsOneWidget);
    expect(find.text('Início'), findsOneWidget);
    expect(find.text('09:00'), findsOneWidget);
    expect(find.text('Fim'), findsOneWidget);
    expect(find.text('18:00'), findsOneWidget);
    expect(find.text('Intervalo'), findsOneWidget);
    expect(find.text('12:00'), findsOneWidget);
    expect(find.text('13:00'), findsOneWidget);
    expect(find.text('Domingo'), findsOneWidget);
    expect(find.text('Salvar alterações'), findsOneWidget);
  });

  testWidgets('liga e desliga dia sem exigir horários quando OFF',
      (tester) async {
    final api = BarberApi(
      client: MockClient(
        (_) async => http.Response(jsonEncode(_weeklyScheduleJson), 200),
      ),
    );

    await tester.pumpWidget(MaterialApp(home: AdminWorkingHoursPage(api: api)));
    await tester.pumpAndSettle();

    expect(find.text('Domingo'), findsOneWidget);
    expect(find.byKey(const Key('working-day-SUNDAY')), findsOneWidget);
    expect(find.text('Fechado'), findsNothing);

    await tester.tap(find.byKey(const Key('working-day-SUNDAY')));
    await tester.pumpAndSettle();

    expect(find.text('19:30'), findsOneWidget);
    expect(find.text('14:00'), findsWidgets);

    await tester.tap(find.byKey(const Key('working-day-SUNDAY')));
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.text('Salvar alterações'));
    await tester.tap(find.text('Salvar alterações'));
    await tester.pump();

    expect(
        find.text('Informe todos os horários dos dias ativos.'), findsNothing);
  });

  testWidgets('valida horário inicial antes do final', (tester) async {
    var putCalled = false;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'PUT') putCalled = true;
        return http.Response(jsonEncode(_invalidWeeklyScheduleJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminWorkingHoursPage(api: api)));
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.text('Salvar alterações'));
    await tester.tap(find.text('Salvar alterações'));
    await tester.pumpAndSettle();

    expect(
      find.text('O horário inicial deve ser anterior ao horário final.'),
      findsOneWidget,
    );
    expect(putCalled, isFalse);
  });

  testWidgets('altera início, fim e intervalo antes de salvar', (tester) async {
    late http.Request putRequest;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'PUT') {
          putRequest = request;
          return http.Response(jsonEncode(_savedWeeklyScheduleJson), 200);
        }
        return http.Response(jsonEncode(_weeklyScheduleJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminWorkingHoursPage(api: api)));
    await tester.pumpAndSettle();

    await _pickTime(tester, const Key('time-field-start-MONDAY'), '10:00');
    await _pickTime(tester, const Key('time-field-end-MONDAY'), '17:00');
    await _pickTime(
      tester,
      const Key('time-field-break-start-MONDAY'),
      '11:00',
    );
    await _pickTime(
      tester,
      const Key('time-field-break-end-MONDAY'),
      '12:00',
    );
    await tester.ensureVisible(find.text('Salvar alterações'));
    await tester.tap(find.text('Salvar alterações'));
    await tester.pumpAndSettle();

    final weeklySchedule =
        (jsonDecode(putRequest.body) as Map)['weeklySchedule'] as List;
    final monday = weeklySchedule.first as Map;
    expect(monday['startTime'], '10:00:00');
    expect(monday['endTime'], '17:00:00');
    expect(monday['breakStartTime'], '11:00:00');
    expect(monday['breakEndTime'], '12:00:00');
  });

  testWidgets('salvar envia PUT, mostra loading e sucesso', (tester) async {
    final putResponse = Completer<http.Response>();
    late http.Request putRequest;
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'PUT') {
          putRequest = request;
          return putResponse.future;
        }
        return http.Response(jsonEncode(_weeklyScheduleJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminWorkingHoursPage(api: api)));
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.text('Salvar alterações'));
    await tester.tap(find.text('Salvar alterações'));
    await tester.pump();

    expect(find.text('Salvando...'), findsOneWidget);

    putResponse
        .complete(http.Response(jsonEncode(_savedWeeklyScheduleJson), 200));
    await tester.pumpAndSettle();

    expect(putRequest.url.path, '/api/v1/barbers/$barberId/weekly-schedule');
    expect((jsonDecode(putRequest.body) as Map)['weeklySchedule'], isA<List>());
    expect(find.text('Horários salvos.'), findsOneWidget);
    expect(find.text('09:30'), findsOneWidget);
    expect(find.text('20:00'), findsOneWidget);
  });

  testWidgets('erro ao salvar mantém tela aberta e exibe mensagem',
      (tester) async {
    final api = BarberApi(
      client: MockClient((request) async {
        if (request.method == 'PUT') {
          return http.Response(
            jsonEncode({
              'error': 'O intervalo deve estar dentro do horário de expediente.'
            }),
            400,
          );
        }
        return http.Response(jsonEncode(_weeklyScheduleJson), 200);
      }),
    );

    await tester.pumpWidget(MaterialApp(home: AdminWorkingHoursPage(api: api)));
    await tester.pumpAndSettle();
    await tester.ensureVisible(find.text('Salvar alterações'));
    await tester.tap(find.text('Salvar alterações'));
    await tester.pumpAndSettle();

    expect(find.byType(AdminWorkingHoursPage), findsOneWidget);
    expect(
      find.text('O intervalo deve estar dentro do horário de expediente.'),
      findsOneWidget,
    );
  });
}

Future<void> _pickTime(WidgetTester tester, Key fieldKey, String time) async {
  await tester.ensureVisible(find.byKey(fieldKey));
  await tester.tap(find.byKey(fieldKey));
  await tester.pumpAndSettle();
  await tester.tap(find.text(time).last);
  await tester.pumpAndSettle();
}

const _weeklyScheduleJson = {
  'weeklySchedule': [
    {
      'dayOfWeek': 'MONDAY',
      'workingDay': true,
      'startTime': '09:00:00',
      'endTime': '18:00:00',
      'breakStartTime': '12:00:00',
      'breakEndTime': '13:00:00',
    },
    {
      'dayOfWeek': 'SUNDAY',
      'workingDay': false,
      'startTime': null,
      'endTime': null,
      'breakStartTime': null,
      'breakEndTime': null,
    },
  ],
};

const _invalidWeeklyScheduleJson = {
  'weeklySchedule': [
    {
      'dayOfWeek': 'MONDAY',
      'workingDay': true,
      'startTime': '18:00:00',
      'endTime': '09:00:00',
      'breakStartTime': '12:00:00',
      'breakEndTime': '13:00:00',
    },
  ],
};

const _savedWeeklyScheduleJson = {
  'weeklySchedule': [
    {
      'dayOfWeek': 'MONDAY',
      'workingDay': true,
      'startTime': '09:30:00',
      'endTime': '20:00:00',
      'breakStartTime': '12:00:00',
      'breakEndTime': '14:00:00',
    },
    {
      'dayOfWeek': 'SUNDAY',
      'workingDay': false,
      'startTime': null,
      'endTime': null,
      'breakStartTime': null,
      'breakEndTime': null,
    },
  ],
};

const _agendaJson = {
  'barberId': barberId,
  'date': '2026-09-09',
  'workingDay': true,
  'slots': [],
};

const _blockJson = {
  'id': 'block-1',
  'startDateTime': '2026-09-09T13:00:00',
  'endDateTime': '2026-09-09T14:00:00',
  'reason': 'Dentista',
  'createdAt': '2026-09-01T10:00:00',
};

const _createdBlockJson = {
  'id': 'block-2',
  'startDateTime': '2026-09-10T15:00:00',
  'endDateTime': '2026-09-10T16:00:00',
  'reason': 'Banco',
  'createdAt': '2026-09-01T10:00:00',
};
