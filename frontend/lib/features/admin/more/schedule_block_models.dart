class ScheduleBlock {
  const ScheduleBlock({
    required this.id,
    required this.startDateTime,
    required this.endDateTime,
    this.reason,
  });

  final String id;
  final DateTime startDateTime;
  final DateTime endDateTime;
  final String? reason;

  factory ScheduleBlock.fromJson(Map<String, dynamic> json) {
    return ScheduleBlock(
      id: json['id'] as String,
      startDateTime: DateTime.parse(json['startDateTime'] as String),
      endDateTime: DateTime.parse(json['endDateTime'] as String),
      reason: json['reason'] as String?,
    );
  }
}
