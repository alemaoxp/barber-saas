class WeeklyScheduleResponse {
  const WeeklyScheduleResponse({required this.weeklySchedule});

  final List<WeeklyScheduleDay> weeklySchedule;

  factory WeeklyScheduleResponse.fromJson(Map<String, dynamic> json) {
    return WeeklyScheduleResponse(
      weeklySchedule: (json['weeklySchedule'] as List? ?? const [])
          .map((item) =>
              WeeklyScheduleDay.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class WeeklyScheduleDay {
  const WeeklyScheduleDay({
    required this.dayOfWeek,
    required this.workingDay,
    this.startTime,
    this.endTime,
    this.breakStartTime,
    this.breakEndTime,
  });

  final String dayOfWeek;
  final bool workingDay;
  final String? startTime;
  final String? endTime;
  final String? breakStartTime;
  final String? breakEndTime;

  factory WeeklyScheduleDay.fromJson(Map<String, dynamic> json) {
    return WeeklyScheduleDay(
      dayOfWeek: json['dayOfWeek'] as String? ?? '',
      workingDay: json['workingDay'] as bool? ?? false,
      startTime: json['startTime'] as String?,
      endTime: json['endTime'] as String?,
      breakStartTime: json['breakStartTime'] as String?,
      breakEndTime: json['breakEndTime'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'dayOfWeek': dayOfWeek,
      'workingDay': workingDay,
      'startTime': workingDay ? startTime : null,
      'endTime': workingDay ? endTime : null,
      'breakStartTime': workingDay ? breakStartTime : null,
      'breakEndTime': workingDay ? breakEndTime : null,
    };
  }

  WeeklyScheduleDay copyWith({
    bool? workingDay,
    String? startTime,
    String? endTime,
    String? breakStartTime,
    String? breakEndTime,
  }) {
    return WeeklyScheduleDay(
      dayOfWeek: dayOfWeek,
      workingDay: workingDay ?? this.workingDay,
      startTime: startTime ?? this.startTime,
      endTime: endTime ?? this.endTime,
      breakStartTime: breakStartTime ?? this.breakStartTime,
      breakEndTime: breakEndTime ?? this.breakEndTime,
    );
  }
}
