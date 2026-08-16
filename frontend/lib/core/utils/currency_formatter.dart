import 'package:intl/intl.dart';

abstract final class CurrencyFormatter {
  static final NumberFormat _formatter = NumberFormat.currency(
    locale: 'pt_BR',
    symbol: 'R\$',
    decimalDigits: 0,
  );

  static String brl(num value) => _formatter.format(value);
}
