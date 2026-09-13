class DailyAgendaResponse {
  const DailyAgendaResponse({
    required this.barberId,
    required this.date,
    required this.workingDay,
    required this.slots,
  });

  final String barberId;
  final DateTime date;
  final bool workingDay;
  final List<DailyAgendaSlot> slots;

  factory DailyAgendaResponse.fromJson(Map<String, dynamic> json) {
    return DailyAgendaResponse(
      barberId: json['barberId'] as String,
      date: DateTime.parse(json['date'] as String),
      workingDay: json['workingDay'] as bool,
      slots: (json['slots'] as List? ?? const [])
          .map((item) => DailyAgendaSlot.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class DailyAgendaSlot {
  const DailyAgendaSlot({
    required this.dateTime,
    required this.status,
    this.appointmentId,
    this.appointmentStatus,
    this.customer,
    this.services = const [],
    this.totalPrice,
    this.block,
  });

  final DateTime dateTime;
  final DailyAgendaSlotStatus status;
  final String? appointmentId;
  final String? appointmentStatus;
  final DailyAgendaCustomer? customer;
  final List<DailyAgendaService> services;
  final num? totalPrice;
  final DailyAgendaBlock? block;

  factory DailyAgendaSlot.fromJson(Map<String, dynamic> json) {
    return DailyAgendaSlot(
      dateTime: DateTime.parse(json['dateTime'] as String),
      status: DailyAgendaSlotStatus.fromJson(json['status'] as String),
      appointmentId: json['appointmentId'] as String?,
      appointmentStatus: json['appointmentStatus'] as String?,
      customer: json['customer'] == null
          ? null
          : DailyAgendaCustomer.fromJson(
              json['customer'] as Map<String, dynamic>),
      services: (json['services'] as List? ?? const [])
          .map((item) =>
              DailyAgendaService.fromJson(item as Map<String, dynamic>))
          .toList(),
      totalPrice: json['totalPrice'] as num?,
      block: json['block'] == null
          ? null
          : DailyAgendaBlock.fromJson(json['block'] as Map<String, dynamic>),
    );
  }
}

String appointmentStatusLabel(DailyAgendaSlot slot, DateTime now) {
  switch (slot.appointmentStatus) {
    case 'COMPLETED':
      return 'Concluído';
    case 'CANCELED':
      return 'Cancelado';
    case 'NO_SHOW':
      return 'Não compareceu';
    case 'SCHEDULED':
      final duration = slot.dateTime.weekday == DateTime.monday ? 40 : 30;
      if (now.isBefore(slot.dateTime)) return 'Agendado';
      if (now.isBefore(slot.dateTime.add(Duration(minutes: duration)))) {
        return 'Em atendimento';
      }
      return 'Pendente';
    default:
      return slot.appointmentStatus ?? 'Agendado';
  }
}

enum DailyAgendaSlotStatus {
  free,
  occupied,
  blocked;

  static DailyAgendaSlotStatus fromJson(String value) {
    return switch (value) {
      'FREE' => DailyAgendaSlotStatus.free,
      'OCCUPIED' => DailyAgendaSlotStatus.occupied,
      'BLOCKED' => DailyAgendaSlotStatus.blocked,
      _ => throw FormatException('Status inválido: $value'),
    };
  }
}

class DailyAgendaCustomer {
  const DailyAgendaCustomer({
    required this.id,
    required this.name,
  });

  final String id;
  final String name;

  factory DailyAgendaCustomer.fromJson(Map<String, dynamic> json) {
    return DailyAgendaCustomer(
      id: json['id'] as String,
      name: json['name'] as String,
    );
  }
}

class DailyAgendaService {
  const DailyAgendaService({
    required this.id,
    required this.name,
  });

  final String id;
  final String name;

  factory DailyAgendaService.fromJson(Map<String, dynamic> json) {
    return DailyAgendaService(
      id: json['id'] as String,
      name: json['name'] as String,
    );
  }
}

class DailyAgendaBlock {
  const DailyAgendaBlock({
    required this.id,
    required this.startDateTime,
    required this.endDateTime,
    this.reason,
  });

  final String id;
  final DateTime startDateTime;
  final DateTime endDateTime;
  final String? reason;

  factory DailyAgendaBlock.fromJson(Map<String, dynamic> json) {
    return DailyAgendaBlock(
      id: json['id'] as String,
      startDateTime: DateTime.parse(json['startDateTime'] as String),
      endDateTime: DateTime.parse(json['endDateTime'] as String),
      reason: json['reason'] as String?,
    );
  }
}
