class AppointmentData {
  String date;
  String time;
  DateTime? appointmentDateTime;
  List<String> services;
  List<String> serviceIds;
  double total;
  String name;
  String phone;
  bool whatsappNotifications;
  String? id;
  String? customerId;
  String? status;
  String? cancelToken;
  String? notes;
  DateTime? createdAt;

  AppointmentData({
    this.date = '',
    this.time = '',
    this.appointmentDateTime,
    this.services = const [],
    this.serviceIds = const [],
    this.total = 0,
    this.name = '',
    this.phone = '',
    this.whatsappNotifications = true,
    this.id,
    this.customerId,
    this.status,
    this.cancelToken,
    this.notes,
    this.createdAt,
  });

  factory AppointmentData.fromJson(Map<String, dynamic> json) {
    final dateTime = DateTime.parse(json['appointmentDateTime'] as String);
    return AppointmentData(
      id: json['id'] as String?,
      customerId: json['customerId'] as String?,
      serviceIds: List<String>.from(json['serviceIds'] as List? ?? const []),
      total: (json['totalPrice'] as num?)?.toDouble() ?? 0,
      appointmentDateTime: dateTime,
      date: _displayDate(dateTime),
      time: '${dateTime.hour.toString().padLeft(2, '0')}:${dateTime.minute.toString().padLeft(2, '0')}',
      status: json['status'] as String?,
      cancelToken: json['cancelToken'] as String?,
      notes: json['notes'] as String?,
      createdAt: json['createdAt'] == null ? null : DateTime.parse(json['createdAt'] as String),
    );
  }

  bool isUpcomingAt(DateTime now) =>
      status == 'SCHEDULED' && appointmentDateTime != null && appointmentDateTime!.isAfter(now);

  bool isHistoryAt(DateTime now) =>
      status == 'CANCELED' ||
      status == 'COMPLETED' ||
      (appointmentDateTime != null && !appointmentDateTime!.isAfter(now));

  static String _displayDate(DateTime value) {
    const months = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
    return '${value.day} de ${months[value.month - 1]} de ${value.year}';
  }
}
