import 'package:flutter/material.dart';
import 'package:cached_network_image/cached_network_image.dart';

import '../../../../core/constants/app_assets.dart';
import '../../../../core/theme/app_colors.dart';

class HeroSection extends StatelessWidget {
  const HeroSection({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 280,
      margin: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(16),
      ),
      clipBehavior: Clip.antiAlias,
      child: Stack(
        children: [
          // Background image
          Positioned.fill(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(16),
              child: CachedNetworkImage(
                imageUrl: AppAssets.heroImageUrl,
                fit: BoxFit.cover,
                placeholder: (context, url) => Container(
                  color: AppColors.card,
                ),
                errorWidget: (context, url, error) => Container(
                  color: AppColors.card,
                  child: const Icon(
                    Icons.photo,
                    color: AppColors.textSecondary,
                    size: 48,
                  ),
                ),
              ),
            ),
          ),
          // Dark gradient overlay
          Positioned.fill(
            child: Container(
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(16),
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [
                    Colors.transparent,
                    const Color(0xFF000000).withOpacity(0.7),
                  ],
                  stops: const [0.4, 1.0],
                ),
              ),
            ),
          ),
          // Title content
          Positioned(
            left: 24,
            right: 24,
            bottom: 32,
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                RichText(
                  text: TextSpan(
                    children: [
                      TextSpan(
                        text: 'Seu estilo,\n',
                        style: Theme.of(context).textTheme.displaySmall?.copyWith(
                          color: AppColors.textPrimary,
                          fontWeight: FontWeight.w800,
                          fontSize: 32,
                          height: 1.1,
                        ),
                      ),
                      TextSpan(
                        text: 'do seu jeito.',
                        style: Theme.of(context).textTheme.displaySmall?.copyWith(
                          color: AppColors.blue,
                          fontWeight: FontWeight.w800,
                          fontSize: 32,
                          height: 1.1,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  'Agende seu horário\nem poucos cliques.',
                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: AppColors.textSecondary,
                    fontSize: 16,
                    height: 1.4,
                    fontWeight: FontWeight.w400,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}