import 'package:flutter/material.dart';

import '../constants/app_spacing.dart';

class ResponsivePage extends StatelessWidget {
  const ResponsivePage({
    required this.child,
    super.key,
    this.maxWidth = AppSpacing.pageMaxWidth,
  });

  final Widget child;
  final double maxWidth;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: ConstrainedBox(
        constraints: BoxConstraints(maxWidth: maxWidth),
        child: child,
      ),
    );
  }
}
