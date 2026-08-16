import 'package:flutter/material.dart';

import '../constants/app_spacing.dart';

class PremiumCard extends StatelessWidget {
  const PremiumCard({
    required this.child,
    super.key,
    this.padding = const EdgeInsets.all(AppSpacing.md),
    this.width = double.infinity,
  });

  final Widget child;
  final EdgeInsetsGeometry padding;
  final double? width;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: width,
      child: Card(
        child: Padding(
          padding: padding,
          child: child,
        ),
      ),
    );
  }
}
