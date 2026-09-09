import 'package:barber_saas_mobile/features/admin/daily_agenda/daily_agenda_models.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('parseia FREE, OCCUPIED, BLOCKED e workingDay false', () {
    final agenda = DailyAgendaResponse.fromJson({
      'barberId': 'barber-1',
      'date': '2026-09-09',
      'workingDay': false,
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
    });

    expect(agenda.barberId, 'barber-1');
    expect(agenda.workingDay, isFalse);
    expect(agenda.slots[0].status, DailyAgendaSlotStatus.free);
    expect(agenda.slots[1].status, DailyAgendaSlotStatus.occupied);
    expect(agenda.slots[1].customer?.name, 'Gabriel Silva');
    expect(agenda.slots[1].services.map((service) => service.name),
        ['Corte', 'Barba']);
    expect(agenda.slots[1].totalPrice, 80);
    expect(agenda.slots[2].status, DailyAgendaSlotStatus.blocked);
    expect(agenda.slots[2].block?.reason, 'Dentista');
  });

  test('exige workingDay no contrato', () {
    expect(
      () => DailyAgendaResponse.fromJson({
        'barberId': 'barber-1',
        'date': '2026-09-09',
        'slots': [],
      }),
      throwsA(isA<TypeError>()),
    );
  });
}
