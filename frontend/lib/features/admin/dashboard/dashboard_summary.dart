class DashboardSummary {
  const DashboardSummary({
    required this.barberId,
    required this.startDate,
    required this.endDate,
    required this.appointmentCount,
    required this.scheduledValue,
  });

  final String barberId;
  final String startDate;
  final String endDate;
  final int appointmentCount;
  final double scheduledValue;

  factory DashboardSummary.fromJson(Map<String, dynamic> json) =>
      DashboardSummary(
        barberId: json['barberId'] as String,
        startDate: json['startDate'] as String,
        endDate: json['endDate'] as String,
        appointmentCount: (json['appointmentCount'] as num).toInt(),
        scheduledValue: (json['scheduledValue'] as num).toDouble(),
      );
}
