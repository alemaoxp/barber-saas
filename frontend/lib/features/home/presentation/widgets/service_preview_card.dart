import 'package:flutter/material.dart';

import '../../../../core/constants/app_radius.dart';
import '../../../../core/constants/app_spacing.dart';
import '../../../../core/utils/currency_formatter.dart';
import '../../../../core/widgets/premium_card.dart';

class ServicePreviewCard extends StatelessWidget {
  const ServicePreviewCard({
    required this.icon,
    required this.name,
    required this.price,
    super.key,
  });

  final IconData icon;
  final String name;
  final num price;

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final textTheme = Theme.of(context).textTheme;

    return PremiumCard(
      width: 132,
      padding: const EdgeInsets.all(AppSpacing.md),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: AppSpacing.xxl,
            height: AppSpacing.xxl,
            decoration: BoxDecoration(
              color: colorScheme.primary.withAlpha(31),
              borderRadius: BorderRadius.circular(AppRadius.sm),
            ),
            child: Icon(
              icon,
              color: colorScheme.primary,
            ),
          ),
          const SizedBox(height: AppSpacing.md),
          Text(
            name,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: textTheme.titleSmall,
          ),
          const SizedBox(height: AppSpacing.xs),
          Text(
            CurrencyFormatter.brl(price),
            style: textTheme.bodySmall,
          ),
        ],
      ),
    );
  }
}
