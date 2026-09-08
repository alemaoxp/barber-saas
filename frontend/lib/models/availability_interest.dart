class AvailabilityInterest {
  const AvailabilityInterest({
    required this.id,
    required this.customerId,
    required this.appointmentId,
    required this.status,
  });

  final String id;
  final String customerId;
  final String appointmentId;
  final String status;

  factory AvailabilityInterest.fromJson(Map<String, dynamic> json) =>
      AvailabilityInterest(
        id: json['id'] as String,
        customerId: json['customerId'] as String,
        appointmentId: json['appointmentId'] as String,
        status: json['status'] as String,
      );
}

class AvailabilityOpportunity {
  const AvailabilityOpportunity({
    required this.availableSlotId,
    required this.availableDateTime,
  });

  final String availableSlotId;
  final DateTime availableDateTime;

  factory AvailabilityOpportunity.fromJson(Map<String, dynamic> json) =>
      AvailabilityOpportunity(
        availableSlotId: json['availableSlotId'] as String,
        availableDateTime: DateTime.parse(json['availableDateTime'] as String),
      );
}
