class AdminCustomerSummary {
  const AdminCustomerSummary({
    required this.id,
    required this.name,
    required this.phone,
  });

  final String id;
  final String name;
  final String phone;

  factory AdminCustomerSummary.fromJson(Map<String, dynamic> json) {
    return AdminCustomerSummary(
      id: json['id'] as String,
      name: json['name'] as String,
      phone: json['phone'] as String,
    );
  }
}
