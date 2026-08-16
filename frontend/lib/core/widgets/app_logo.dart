import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';

import '../constants/app_assets.dart';
import '../constants/app_spacing.dart';

class AppLogo extends StatelessWidget {
  const AppLogo({
    super.key,
    this.size = AppSpacing.logoSize,
  });

  final double size;

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Container(
      width: size,
      height: size,
      padding: const EdgeInsets.all(3),
      decoration: BoxDecoration(
        shape: BoxShape.circle,
        color: colorScheme.primary,
      ),
      child: ClipOval(
        child: CachedNetworkImage(
          imageUrl: AppAssets.logoImageUrl,
          fit: BoxFit.cover,
          placeholder: (_, __) => ColoredBox(color: colorScheme.surface),
          errorWidget: (_, __, ___) => Icon(
            Icons.content_cut_rounded,
            color: colorScheme.onPrimary,
          ),
        ),
      ),
    );
  }
}
