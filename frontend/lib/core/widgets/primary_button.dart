import 'package:flutter/material.dart';

import '../constants/app_spacing.dart';

class PrimaryButton extends StatelessWidget {
  const PrimaryButton({
    required this.label,
    required this.onPressed,
    super.key,
    this.icon,
  });

  final String label;
  final IconData? icon;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: double.infinity,
      height: AppSpacing.controlHeight,
      child: icon == null
          ? FilledButton(
              onPressed: onPressed,
              child: Text(label),
            )
          : FilledButton.icon(
              onPressed: onPressed,
              icon: Icon(icon, size: 20),
              label: Text(label),
            ),
    );
  }
}
