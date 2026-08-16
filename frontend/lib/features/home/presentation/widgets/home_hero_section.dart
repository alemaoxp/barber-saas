import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../../../../core/constants/app_assets.dart';
import '../../../../core/constants/app_radius.dart';
import '../../../../core/constants/app_spacing.dart';
import '../../../../core/constants/app_strings.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/widgets/primary_button.dart';

class HomeHeroSection extends StatelessWidget {
  const HomeHeroSection({
    required this.onSchedulePressed,
    super.key,
  });

  final VoidCallback onSchedulePressed;

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final scaffoldBackground = Theme.of(context).scaffoldBackgroundColor;
    final textTheme = Theme.of(context).textTheme;

    return ClipRRect(
      borderRadius: AppRadius.hero,
      child: AspectRatio(
        aspectRatio: 0.78,
        child: Stack(
          fit: StackFit.expand,
          children: [
            CachedNetworkImage(
              imageUrl: AppAssets.heroImageUrl,
              fit: BoxFit.cover,
              placeholder: (_, __) => ColoredBox(color: colorScheme.surface),
              errorWidget: (_, __, ___) => ColoredBox(
                color: colorScheme.surface,
                child: Icon(
                  Icons.content_cut_rounded,
                  color: colorScheme.primary,
                  size: AppSpacing.compactControlHeight,
                ),
              ),
            ),
            DecoratedBox(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [
                    scaffoldBackground.withAlpha(51),
                    scaffoldBackground.withAlpha(170),
                    AppColors.overlay,
                  ],
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(AppSpacing.lg),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.end,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    AppStrings.homeHeroTitle,
                    style: textTheme.headlineMedium?.copyWith(
                          height: 1.08,
                        ),
                  ),
                  const SizedBox(height: AppSpacing.sm),
                  Text(
                    AppStrings.homeHeroDescription,
                    style: textTheme.bodyMedium,
                  ),
                  const SizedBox(height: AppSpacing.lg),
                  PrimaryButton(
                    label: AppStrings.scheduleButton,
                    icon: Icons.calendar_month_rounded,
                    onPressed: onSchedulePressed,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
