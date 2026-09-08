import 'package:barber_saas_mobile/models/appointment_data.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  final now = DateTime(2026, 9, 6, 12);

  AppointmentData appointment(DateTime dateTime, String status) => AppointmentData(
        appointmentDateTime: dateTime,
        status: status,
      );

  test('classifica somente agendamento futuro marcado como próximo', () {
    final scheduledFuture = appointment(DateTime(2026, 9, 7, 10), 'SCHEDULED');
    final scheduledPast = appointment(DateTime(2026, 9, 5, 10), 'SCHEDULED');

    expect(scheduledFuture.isUpcomingAt(now), isTrue);
    expect(scheduledPast.isUpcomingAt(now), isFalse);
  });

  test('mantém agendamentos passados ou cancelados no histórico', () {
    expect(appointment(DateTime(2026, 9, 5, 10), 'SCHEDULED').isHistoryAt(now), isTrue);
    expect(appointment(DateTime(2026, 9, 7, 10), 'CANCELED').isHistoryAt(now), isTrue);
  });
}
