class AppointmentData {
  String date;
  String time;

  List<String> services;
  double total;

  String name;
  String phone;

  bool whatsappNotifications;

  AppointmentData({
    this.date = '',
    this.time = '',
    this.services = const [],
    this.total = 0,
    this.name = '',
    this.phone = '',
    this.whatsappNotifications = true,
  });
}