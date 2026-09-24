import 'package:flutter_test/flutter_test.dart';

import 'package:barber_saas_mobile/services/barber_api.dart';

void main() {
  test('API URL defaults to the local backend', () {
    expect(apiBaseUrl, 'http://localhost:8080');
  });
}
