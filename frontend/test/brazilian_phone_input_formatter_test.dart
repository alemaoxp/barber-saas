import 'package:barber_saas_mobile/formatters/brazilian_phone_input_formatter.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  final formatter = BrazilianPhoneInputFormatter();

  TextEditingValue format(String value) => formatter.formatEditUpdate(
        const TextEditingValue(),
        TextEditingValue(text: value),
      );

  test('formata celular brasileiro e limita a onze dígitos', () {
    expect(format('11999991234').text, '(11) 99999-1234');
    expect(format('11a9999912345').text, '(11) 99999-1234');
  });

  test('formata telefone fixo e valida somente números completos', () {
    expect(format('1133334444').text, '(11) 3333-4444');
    expect(BrazilianPhoneInputFormatter.isValid('(11) 3333-4444'), isTrue);
    expect(BrazilianPhoneInputFormatter.isValid('(11) 99999-1234'), isTrue);
    expect(BrazilianPhoneInputFormatter.isValid('(11) 9999-1234'), isFalse);
  });
}
