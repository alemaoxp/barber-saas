import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';

class ContinueButton extends StatelessWidget {
  const ContinueButton({super.key});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: SizedBox(
        width: double.infinity,
        height: 56,
        child: ElevatedButton(
          onPressed: null, // Disabled state
          style: ElevatedButton.styleFrom(
            backgroundColor: AppColors.disabled,
            foregroundColor: AppColors.textSecondary,
            disabledBackgroundColor: AppColors.disabled,
            disabledForegroundColor: AppColors.textSecondary,
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
          child: Text(
            'CONTINUAR',
            style: Theme.of(context).textTheme.labelLarge?.copyWith(
              fontWeight: FontWeight.w700,
              fontSize: 14,
              letterSpacing: 0.5,
            ),
          ),
        ),
      ),
    );
  }
}