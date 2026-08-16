import 'package:flutter/material.dart';

import '../../../../core/constants/app_spacing.dart';
import '../../../../core/constants/app_strings.dart';
import '../../../../core/widgets/section_title.dart';
import 'service_preview_card.dart';

class ServicesPreviewSection extends StatelessWidget {
  const ServicesPreviewSection({super.key});

  static const _services = [
    _ServicePreview(
      icon: Icons.content_cut_rounded,
      name: 'Corte',
      price: 40,
    ),
    _ServicePreview(
      icon: Icons.face_retouching_natural_rounded,
      name: 'Barba',
      price: 30,
    ),
    _ServicePreview(
      icon: Icons.brush_rounded,
      name: 'Pigmentação',
      price: 50,
    ),
    _ServicePreview(
      icon: Icons.workspace_premium_rounded,
      name: 'Combo',
      price: 65,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const SectionTitle(title: AppStrings.servicesTitle),
        const SizedBox(height: AppSpacing.md),
        SizedBox(
          height: 142,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            physics: const BouncingScrollPhysics(),
            itemBuilder: (context, index) {
              final service = _services[index];
              return ServicePreviewCard(
                icon: service.icon,
                name: service.name,
                price: service.price,
              );
            },
            separatorBuilder: (_, __) => const SizedBox(width: AppSpacing.md),
            itemCount: _services.length,
          ),
        ),
      ],
    );
  }
}

class _ServicePreview {
  const _ServicePreview({
    required this.icon,
    required this.name,
    required this.price,
  });

  final IconData icon;
  final String name;
  final num price;
}
