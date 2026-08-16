import 'package:flutter/material.dart';

import '../constants/app_spacing.dart';

class SecondaryButton extends StatelessWidget {
  const SecondaryButton({
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
      height: AppSpacing.compactControlHeight,
      child: icon == null
          ? OutlinedButton(
              onPressed: onPressed,
              child: Text(label),
            )
          : OutlinedButton.icon(
              onPressed: onPressed,
              icon: Icon(icon, size: 18),
              label: Text(label),
            ),
    );
  }
}
