import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/appointment_data.dart';
import '../models/availability_interest.dart';
import '../models/booking_service.dart';
import '../features/admin/daily_agenda/daily_agenda_models.dart';
import '../features/admin/daily_agenda/models/admin_customer_summary.dart';
import '../features/admin/auth/admin_auth.dart';
import '../features/admin/more/weekly_schedule_models.dart';
import '../features/admin/more/schedule_block_models.dart';
import 'push_test_client.dart';

const apiBaseUrl = 'http://localhost:8080';
const barberId = '3700633c-35f1-4ab9-af18-c60f8eb23b45';

class BarberApiException implements Exception {
  final String message;
  final int? statusCode;
  const BarberApiException(this.message, {this.statusCode});
}

class BarberApi {
  BarberApi({
    http.Client? client,
    Future<String?> Function()? tokenProvider,
    Future<void> Function()? onUnauthorized,
    Future<Map<String, dynamic>> Function(String publicKey)?
        createPushSubscription,
  })  : _client = client ?? http.Client(),
        _tokenProvider = tokenProvider,
        _onUnauthorized = onUnauthorized,
        _createPushSubscription =
            createPushSubscription ?? createPushTestSubscription;

  final http.Client _client;
  final Future<String?> Function()? _tokenProvider;
  final Future<void> Function()? _onUnauthorized;
  final Future<Map<String, dynamic>> Function(String publicKey)
      _createPushSubscription;

  Future<List<BookingService>> services() async {
    final response = await _client
        .get(Uri.parse('$apiBaseUrl/api/public/barbers/$barberId/services'));
    return _list(response, BookingService.fromJson);
  }

  Future<List<BookingService>> adminServices() async {
    final response = await _adminGet(
        Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/services'));
    return _list(response, BookingService.fromJson);
  }

  Future<BookingService> createAdminService({
    required String name,
    required double price,
  }) async {
    final response = await _adminPost(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/services'),
      body: jsonEncode({
        'name': name,
        'description': '',
        'durationMinutes': 30,
        'price': price,
        'active': true,
      }),
    );
    _ensureSuccess(response, expectedStatus: 201);
    return BookingService.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<BookingService> updateAdminService({
    required String serviceId,
    required String name,
    required double price,
  }) async {
    final response = await _adminPut(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/services/$serviceId'),
      body: jsonEncode({
        'name': name,
        'price': price,
      }),
    );
    _ensureSuccess(response);
    return BookingService.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<List<String>> availableSlots(DateTime date) async {
    final day = _dateOnly(date);
    final response = await _client.get(Uri.parse(
        '$apiBaseUrl/api/public/barbers/$barberId/available-slots?date=$day'));
    if (response.statusCode == 400 && _isNoWorkingDay(response.body)) {
      return const [];
    }
    _ensureSuccess(response);
    return List<String>.from(jsonDecode(response.body) as List)
        .map((time) => time.substring(0, 5))
        .toList();
  }

  Future<DailyAgendaResponse> dailyAgenda(DateTime date) async {
    final day = _dateOnly(date);
    final response = await _adminGet(Uri.parse(
        '$apiBaseUrl/api/v1/barbers/$barberId/daily-agenda?date=$day'));
    _ensureSuccess(response);
    return DailyAgendaResponse.fromJson(
        jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<WeeklyScheduleResponse> weeklySchedule() async {
    final response = await _adminGet(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/weekly-schedule'),
    );
    _ensureSuccess(response);
    return WeeklyScheduleResponse.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<WeeklyScheduleResponse> updateWeeklySchedule(
    List<WeeklyScheduleDay> weeklySchedule,
  ) async {
    final response = await _adminPut(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/weekly-schedule'),
      body: jsonEncode({
        'weeklySchedule': weeklySchedule.map((day) => day.toJson()).toList(),
      }),
    );
    _ensureSuccess(response);
    return WeeklyScheduleResponse.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<List<ScheduleBlock>> scheduleBlocks() async {
    final response = await _adminGet(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/schedule-blocks'),
    );
    return _list(response, ScheduleBlock.fromJson);
  }

  Future<ScheduleBlock> createScheduleBlock({
    required DateTime startDateTime,
    required DateTime endDateTime,
    String? reason,
  }) async {
    final response = await _adminPost(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/schedule-blocks'),
      body: jsonEncode({
        'startDateTime': _isoLocal(startDateTime),
        'endDateTime': _isoLocal(endDateTime),
        'reason': reason?.isEmpty == true ? null : reason,
      }),
    );
    _ensureSuccess(response, expectedStatus: 201);
    return ScheduleBlock.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<void> deleteScheduleBlock(String scheduleBlockId) async {
    final response = await _adminDelete(
      Uri.parse(
        '$apiBaseUrl/api/v1/barbers/$barberId/schedule-blocks/$scheduleBlockId',
      ),
    );
    _ensureSuccess(response, expectedStatus: 204);
  }

  Future<List<AdminCustomerSummary>> adminCustomers({String? query}) async {
    final trimmed = query?.trim();
    final uri = Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/customers')
        .replace(
            queryParameters:
                trimmed == null || trimmed.isEmpty ? null : {'query': trimmed});
    final response = await _adminGet(uri);
    return _list(response, AdminCustomerSummary.fromJson);
  }

  Future<AdminCustomerSummary> createAdminCustomer({
    required String name,
    required String phone,
  }) async {
    final response = await _adminPost(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/customers'),
      body: jsonEncode({
        'name': name,
        'phone': phone,
        'email': null,
        'birthDate': null,
        'notes': null,
        'active': true,
      }),
    );
    _ensureSuccess(response, expectedStatus: 201);
    return AdminCustomerSummary.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<AdminCustomerSummary> updateAdminCustomer({
    required String customerId,
    required String name,
    required String phone,
  }) async {
    final response = await _adminPut(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/customers/$customerId'),
      body: jsonEncode({
        'name': name,
        'phone': phone,
      }),
    );
    _ensureSuccess(response);
    return AdminCustomerSummary.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<void> deleteAdminCustomer(String customerId) async {
    final response = await _adminDelete(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/customers/$customerId'),
    );
    _ensureSuccess(response, expectedStatus: 204);
  }

  Future<AppointmentData> createAppointment(AppointmentData appointment) async {
    final response = await _client.post(
      Uri.parse('$apiBaseUrl/api/public/appointments?barberId=$barberId'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'customerName': appointment.name,
        'customerPhone': appointment.phone,
        'serviceIds': appointment.serviceIds,
        'appointmentDateTime': _isoLocal(appointment.appointmentDateTime!),
        if (appointment.notes?.isNotEmpty == true) 'notes': appointment.notes,
      }),
    );
    _ensureSuccess(response, expectedStatus: 201);
    return AppointmentData.fromJson(
        jsonDecode(response.body) as Map<String, dynamic>)
      ..name = appointment.name
      ..phone = appointment.phone
      ..services = appointment.services
      ..whatsappNotifications = appointment.whatsappNotifications;
  }

  Future<AppointmentData> createAdminAppointment({
    required String customerId,
    required List<String> serviceIds,
    required DateTime appointmentDateTime,
  }) async {
    final response = await _adminPost(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/appointments'),
      body: jsonEncode({
        'customerId': customerId,
        'serviceIds': serviceIds,
        'appointmentDateTime': _isoLocal(appointmentDateTime),
      }),
    );
    _ensureSuccess(response, expectedStatus: 201);
    return AppointmentData.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<AppointmentData> updateAppointmentStatus(
    String appointmentId,
    String status,
  ) async {
    final response = await _adminPatch(
      Uri.parse(
        '$apiBaseUrl/api/v1/barbers/$barberId/appointments/$appointmentId/status',
      ),
      body: jsonEncode({'status': status}),
    );
    _ensureSuccess(response);
    return AppointmentData.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<void> deleteAdminService(String serviceId) async {
    final response = await _adminDelete(
      Uri.parse('$apiBaseUrl/api/v1/barbers/$barberId/services/$serviceId'),
    );
    _ensureSuccess(response, expectedStatus: 204);
  }

  Future<List<AppointmentData>> appointments(String phone) async {
    final response = await _client.get(Uri.parse(
        '$apiBaseUrl/api/public/appointments?barberId=$barberId&phone=${Uri.encodeQueryComponent(phone)}'));
    return _list(response, AppointmentData.fromJson);
  }

  Future<void> cancelAppointment(String cancelToken) async {
    final response = await _client
        .delete(Uri.parse('$apiBaseUrl/api/public/appointments/$cancelToken'));
    _ensureSuccess(response, expectedStatus: 204);
  }

  Future<void> enableTestPush(String customerId) async {
    final keyResponse =
        await _client.get(Uri.parse('$apiBaseUrl/api/dev/push/public-key'));
    _ensureSuccess(keyResponse);
    final publicKey = (jsonDecode(keyResponse.body)
        as Map<String, dynamic>)['publicKey'] as String;
    final subscription = await _createPushSubscription(publicKey);
    final keys = subscription['keys'] as Map<String, dynamic>;
    final response = await _client.post(
      Uri.parse('$apiBaseUrl/api/dev/push/subscription'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({
        'endpoint': subscription['endpoint'],
        'p256dh': keys['p256dh'],
        'auth': keys['auth'],
        'customerId': customerId,
      }),
    );
    _ensureSuccess(response, expectedStatus: 204);
  }

  Future<AvailabilityInterest> createAvailabilityInterest(
      String customerId, String appointmentId) async {
    final response = await _client.post(
      Uri.parse(
          '$apiBaseUrl/api/v1/customers/$customerId/availability-interests'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'appointmentId': appointmentId}),
    );
    _ensureSuccess(response, expectedStatus: 201);
    return AvailabilityInterest.fromJson(
        jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<AvailabilityInterest?> activeAvailabilityInterest(
      String customerId, String appointmentId) async {
    final response = await _client.get(Uri.parse(
        '$apiBaseUrl/api/v1/customers/$customerId/availability-interests/active?appointmentId=$appointmentId'));
    if (response.statusCode == 204) return null;
    _ensureSuccess(response);
    return AvailabilityInterest.fromJson(
        jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<void> cancelAvailabilityInterest(
      String customerId, String interestId) async {
    final response = await _client.delete(Uri.parse(
        '$apiBaseUrl/api/v1/customers/$customerId/availability-interests/$interestId'));
    _ensureSuccess(response, expectedStatus: 204);
  }

  Future<List<AvailabilityOpportunity>> availabilityOpportunities(
      String customerId, String interestId) async {
    final response = await _client.get(Uri.parse(
        '$apiBaseUrl/api/v1/customers/$customerId/availability-interests/$interestId/opportunities'));
    return _list(response, AvailabilityOpportunity.fromJson);
  }

  Future<AvailabilityInterest> acceptAvailabilityOpportunity(
      String customerId, String interestId, String availableSlotId) async {
    final response = await _client.post(
      Uri.parse(
          '$apiBaseUrl/api/v1/customers/$customerId/availability-interests/$interestId/accept'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'availableSlotId': availableSlotId}),
    );
    _ensureSuccess(response);
    return AvailabilityInterest.fromJson(
        jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<AdminLoginResult> loginAdmin({
    required String email,
    required String password,
  }) async {
    final response = await _client.post(
      Uri.parse('$apiBaseUrl/api/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'password': password}),
    );
    _ensureSuccess(response);
    return AdminLoginResult.fromJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<AdminUser> currentAdmin() async {
    final response = await _adminGet(Uri.parse('$apiBaseUrl/api/auth/me'));
    _ensureSuccess(response);
    return AdminUser.fromJson(
        jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<void> logoutAdmin() async {
    final response = await _adminPost(Uri.parse('$apiBaseUrl/api/auth/logout'));
    if (response.statusCode == 401) {
      await _onUnauthorized?.call();
      return;
    }
    _ensureSuccess(response);
  }

  List<T> _list<T>(
      http.Response response, T Function(Map<String, dynamic>) fromJson) {
    _ensureSuccess(response);
    return (jsonDecode(response.body) as List)
        .map((item) => fromJson(item as Map<String, dynamic>))
        .toList();
  }

  void _ensureSuccess(http.Response response, {int? expectedStatus}) {
    if (response.statusCode == (expectedStatus ?? 200)) return;
    final body = jsonDecode(response.body.isEmpty ? '{}' : response.body);
    throw BarberApiException(
        body is Map && body['message'] is String
            ? body['message'] as String
            : body is Map && body['error'] is String
                ? body['error'] as String
                : 'Não foi possível concluir a operação.',
        statusCode: response.statusCode);
  }

  Future<http.Response> _adminGet(Uri uri) async =>
      _adminSend((headers) => _client.get(uri, headers: headers));

  Future<http.Response> _adminPost(Uri uri, {Object? body}) async =>
      _adminSend((headers) => _client.post(uri, headers: headers, body: body));

  Future<http.Response> _adminPut(Uri uri, {Object? body}) async =>
      _adminSend((headers) => _client.put(uri, headers: headers, body: body));

  Future<http.Response> _adminPatch(Uri uri, {Object? body}) async =>
      _adminSend((headers) => _client.patch(uri, headers: headers, body: body));

  Future<http.Response> _adminDelete(Uri uri) async =>
      _adminSend((headers) => _client.delete(uri, headers: headers));

  Future<http.Response> _adminSend(
      Future<http.Response> Function(Map<String, String> headers) send) async {
    final headers = {'Content-Type': 'application/json'};
    final token = await _tokenProvider?.call();
    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }
    final response = await send(headers);
    if (response.statusCode == 401) {
      await _onUnauthorized?.call();
    }
    return response;
  }

  bool _isNoWorkingDay(String body) {
    final json = jsonDecode(body);
    return json is Map && json['error'] == 'Barbeiro não atende neste dia.';
  }

  String _isoLocal(DateTime value) =>
      '${_dateOnly(value)}T${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}:${value.second.toString().padLeft(2, '0')}';

  String _dateOnly(DateTime value) =>
      '${value.year.toString().padLeft(4, '0')}-${value.month.toString().padLeft(2, '0')}-${value.day.toString().padLeft(2, '0')}';
}
