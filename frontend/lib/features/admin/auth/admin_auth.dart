import 'package:shared_preferences/shared_preferences.dart';

import '../../../services/barber_api.dart';

const _adminTokenKey = 'admin_auth_token';

class AdminAuthStore {
  Future<String?> token() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_adminTokenKey);
  }

  Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_adminTokenKey, token);
  }

  Future<void> clear() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_adminTokenKey);
  }
}

class AdminUser {
  const AdminUser({
    required this.id,
    required this.name,
    required this.email,
    required this.barbershopId,
    required this.barbershopName,
  });

  final String id;
  final String name;
  final String email;
  final String barbershopId;
  final String barbershopName;

  factory AdminUser.fromJson(Map<String, dynamic> json) {
    return AdminUser(
      id: json['id'] as String,
      name: json['name'] as String,
      email: json['email'] as String,
      barbershopId: json['barbershopId'] as String,
      barbershopName: json['barbershopName'] as String,
    );
  }
}

class AdminLoginResult {
  const AdminLoginResult({required this.token, required this.user});

  final String token;
  final AdminUser user;

  factory AdminLoginResult.fromJson(Map<String, dynamic> json) {
    return AdminLoginResult(
      token: json['token'] as String,
      user: AdminUser.fromJson(json['user'] as Map<String, dynamic>),
    );
  }
}

BarberApi adminApi(AdminAuthStore store, Future<void> Function()? onExpired) {
  return BarberApi(
    tokenProvider: store.token,
    onUnauthorized: () async {
      await store.clear();
      await onExpired?.call();
    },
  );
}
