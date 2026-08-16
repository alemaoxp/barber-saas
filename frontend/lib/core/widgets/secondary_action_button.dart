import 'package:flutter/material.dart';

import 'secondary_button.dart';

class SecondaryActionButton extends StatelessWidget {
  const SecondaryActionButton({
    required this.label,
    required this.onPressed,
    super.key,
  });

  final String label;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return SecondaryButton(
      label: label,
      onPressed: onPressed,
    );
  }
}
