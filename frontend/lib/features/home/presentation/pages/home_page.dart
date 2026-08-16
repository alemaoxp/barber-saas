import 'package:flutter/material.dart';

import '../widgets/home_header.dart';
import '../widgets/hero_section.dart';
import '../widgets/continue_button.dart';
import '../widgets/benefits_section.dart';

class HomePage extends StatelessWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF121212),
      body: Center(
        child: ConstrainedBox(
          constraints: const BoxConstraints(
            maxWidth: 375, // Mobile width
          ),
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Header
                const HomeHeader(),
                const SizedBox(height: 24),
                // Hero section
                const HeroSection(),
                const SizedBox(height: 32),
                // Placeholder for schedule card (SizedBox with same height as reference)
                Container(
                  height: 180,
                  margin: const EdgeInsets.symmetric(horizontal: 16),
                  decoration: BoxDecoration(
                    color: Colors.transparent,
                    borderRadius: BorderRadius.circular(16),
                    border: Border.all(
                      color: const Color(0xFF2A2A2A),
                      width: 1,
                    ),
                  ),
                  child: const Center(
                    child: Text(
                      'Selecione a data e o horário',
                      style: TextStyle(
                        color: Color(0xFFBDBDBD),
                        fontSize: 14,
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 32),
                // Continue button
                const ContinueButton(),
                const SizedBox(height: 32),
                // Benefits section
                const BenefitsSection(),
                const SizedBox(height: 40),
              ],
            ),
          ),
        ),
      ),
    );
  }
}