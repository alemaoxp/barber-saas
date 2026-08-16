import 'package:flutter/material.dart';

import '../../../../core/theme/app_colors.dart';

class BenefitsSection extends StatelessWidget {
  const BenefitsSection({super.key});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Section title
          Padding(
            padding: const EdgeInsets.only(bottom: 24),
            child: Text(
              'Por que agendar conosco?',
              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary,
                fontSize: 20,
                height: 1.25,
              ),
            ),
          ),
          // Benefits grid
          const Row(
            children: [
              Expanded(
                child: _BenefitItem(
                  icon: Icons.flash_on,
                  title: 'Rápido e prático',
                  description: 'Agendamento em menos de 2 minutos',
                ),
              ),
              SizedBox(width: 12),
              Expanded(
                child: _BenefitItem(
                  icon: Icons.calendar_today,
                  title: 'Horário em tempo real',
                  description: 'Veja disponibilidade atualizada',
                ),
              ),
              SizedBox(width: 12),
              Expanded(
                child: _BenefitItem(
                  icon: Icons.chat,
                  title: 'Confirmação pelo WhatsApp',
                  description: 'Receba lembretes e confirmações',
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _BenefitItem extends StatelessWidget {
  const _BenefitItem({
    required this.icon,
    required this.title,
    required this.description,
  });

  final IconData icon;
  final String title;
  final String description;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.cardBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Icon
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: AppColors.blue.withOpacity(0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(
              icon,
              color: AppColors.blue,
              size: 20,
            ),
          ),
          const SizedBox(height: 16),
          // Title
          Text(
            title,
            style: Theme.of(context).textTheme.titleSmall?.copyWith(
              fontWeight: FontWeight.w700,
              color: AppColors.textPrimary,
              fontSize: 15,
              height: 1.35,
            ),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
          const SizedBox(height: 4),
          // Description
          Text(
            description,
            style: Theme.of(context).textTheme.bodySmall?.copyWith(
              color: AppColors.textSecondary,
              fontSize: 12,
              height: 1.4,
              fontWeight: FontWeight.w400,
            ),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }
}