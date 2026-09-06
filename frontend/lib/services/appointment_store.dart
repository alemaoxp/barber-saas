import '../models/appointment_data.dart';

class AppointmentStore {
  AppointmentStore._();

  static final AppointmentStore instance =
  AppointmentStore._();

  final List<AppointmentData> _appointments = [];

  List<AppointmentData> get appointments =>
      List.unmodifiable(_appointments);

  void addIfNotExists(
      AppointmentData appointment,
      ) {
    final exists = _appointments.any(
          (item) =>
      item.date == appointment.date &&
          item.time == appointment.time &&
          item.name == appointment.name &&
          item.services.join('|') ==
              appointment.services.join('|'),
    );

    if (!exists) {
      _appointments.insert(
        0,
        appointment,
      );
    }
  }

  void remove(
      AppointmentData appointment,
      ) {
    _appointments.remove(
      appointment,
    );
  }

  void clear() {
    _appointments.clear();
  }
}