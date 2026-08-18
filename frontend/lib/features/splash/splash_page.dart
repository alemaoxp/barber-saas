import 'dart:async';

import 'package:flutter/material.dart';

class SplashPage extends StatefulWidget {
  const SplashPage({
    super.key,
    required this.onFinished,
  });

  final VoidCallback onFinished;

  @override
  State<SplashPage> createState() => _SplashPageState();
}

class _SplashPageState extends State<SplashPage>
    with TickerProviderStateMixin {
  late final AnimationController _animationController;

  late final Animation<double> _jhowCortesAnimation;
  late final Animation<double> _logoAnimation;
  late final Animation<double> _barbeariaAnimation;

  Timer? _navigationTimer;

  @override
  void initState() {
    super.initState();

    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 2610),
    );

    // Jhow Cortes:
    // começa praticamente imediatamente.
    _jhowCortesAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(
      CurvedAnimation(
        parent: _animationController,
        curve: const Interval(
          0.40,
          0.71,
          curve: Curves.easeIn,
        ),
      ),
    );

    // Logo do barbeiro:
    // começa depois do Jhow Cortes.
    _logoAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(
      CurvedAnimation(
        parent: _animationController,
        curve: const Interval(
          0.0,
          0.42,
          curve: Curves.easeIn,
        ),
      ),
    );

    // Texto "Barbearia":
    // entra por último.
    _barbeariaAnimation = Tween<double>(
      begin: 0.0,
      end: 1.0,
    ).animate(
      CurvedAnimation(
        parent: _animationController,
        curve: const Interval(
          0.66,
          1.0,
          curve: Curves.easeIn,
        ),
      ),
    );

    _animationController.forward();

    _navigationTimer = Timer(
      const Duration(seconds: 4),
      _goToHome,
    );
  }

  void _goToHome() {
    if (!mounted) return;

    widget.onFinished();
  }

  @override
  void dispose() {
    _navigationTimer?.cancel();
    _animationController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0F1115),
      body: SafeArea(
        child: Stack(
          children: [
            // JHOW CORTES
            Align(
              alignment: const Alignment(0, -0.45),
              child: FadeTransition(
                opacity: _jhowCortesAnimation,
                child: Image.asset(
                  'assets/images/branding/jhow_cortes.png',
                  width: 400,
                  height: 350,
                  fit: BoxFit.cover,
                ),
              ),
            ),

            // DESENHO DO BARBEIRO
            Align(
              alignment: const Alignment(0, 0.01),
              child: FadeTransition(
                opacity: _logoAnimation,
                child: Image.asset(
                  'assets/images/branding/logo.png',
                  width: 300,
                  height: 220.9,
                  fit: BoxFit.cover,
                ),
              ),
            ),

            // BARBEARIA
            Align(
              alignment: const Alignment(0, 0.51),
              child: FadeTransition(
                opacity: _barbeariaAnimation,
                child: Image.asset(
                  'assets/images/branding/barbearia.png',
                  width: 293.96,
                  height: 200,
                  fit: BoxFit.cover,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}