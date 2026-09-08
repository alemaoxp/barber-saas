import 'package:flutter/services.dart';

class BrazilianPhoneInputFormatter extends TextInputFormatter {
  static final _validPhone = RegExp(r'^\([1-9]\d\) (?:9[1-9]\d{3}|[2-8]\d{3})-\d{4}$');

  static String format(String value) {
    final onlyDigits = value.replaceAll(RegExp(r'\D'), '');
    final digits = onlyDigits.length > 11 ? onlyDigits.substring(0, 11) : onlyDigits;
    if (digits.isEmpty) return '';
    if (digits.length <= 2) return '($digits';
    final local = digits.substring(2);
    if (digits.length <= 6) return '(${digits.substring(0, 2)}) $local';
    final split = digits.length == 11 ? 5 : 4;
    return '(${digits.substring(0, 2)}) ${local.substring(0, split)}-${local.substring(split)}';
  }

  static bool isValid(String value) => _validPhone.hasMatch(value);

  @override
  TextEditingValue formatEditUpdate(TextEditingValue oldValue, TextEditingValue newValue) {
    final text = format(newValue.text);
    return TextEditingValue(text: text, selection: TextSelection.collapsed(offset: text.length));
  }
}
