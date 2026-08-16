import 'package:flutter/material.dart';

import '../constants/app_spacing.dart';
import 'responsive_page.dart';

class FeaturePlaceholderPage extends StatelessWidget {
  const FeaturePlaceholderPage({
    required this.title,
    super.key,
  });

  final String title;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: ResponsivePage(
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.md),
            child: Center(
              child: Text(
                title,
                style: Theme.of(context).textTheme.headlineSmall,
              ),
            ),
          ),
        ),
      ),
    );
  }
}
