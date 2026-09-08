class BookingService {
  final String id;
  final String name;
  final String description;
  final double price;

  const BookingService({required this.id, required this.name, required this.description, required this.price});

  factory BookingService.fromJson(Map<String, dynamic> json) => BookingService(
        id: json['id'] as String,
        name: json['name'] as String,
        description: json['description'] as String? ?? '',
        price: (json['price'] as num).toDouble(),
      );
}
