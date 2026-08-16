import 'package:flutter/material.dart';

class SplashContent extends StatefulWidget {
  const SplashContent({
    super.key,
    required this.onAnimationComplete,
  });

  final VoidCallback onAnimationComplete;

  @override
  State<SplashContent> createState() => _SplashContentState();
}

class _SplashContentState extends State<SplashContent> {
  double _logoOpacity = 0.0;
  double _jhowCortesOpacity = 0.0;
  double _barbeariaOpacity = 0.0;
  double _overallOpacity = 1.0;

  @override
  void initState() {
    super.initState();
    _startAnimationSequence();
  }

  void _startAnimationSequence() {
    // Logo animation: starts at 0ms, duration 1090ms
    Future.delayed(Duration.zero, () {
      if (mounted) {
        setState(() => _logoOpacity = 1.0);
      }
    });

    // Jhow Cortes animation: starts at 1060ms, duration 790ms
    Future.delayed(const Duration(milliseconds: 1060), () {
      if (mounted) {
        setState(() => _jhowCortesOpacity = 1.0);
      }
    });

    // Barbearia animation: starts at 1710ms, duration 900ms
    Future.delayed(const Duration(milliseconds: 1710), () {
      if (mounted) {
        setState(() => _barbeariaOpacity = 1.0);
      }
    });

    // Wait for all animations to complete plus some extra time
    Future.delayed(const Duration(milliseconds: 2610), () {
      if (mounted) {
        _startFadeOut();
      }
    });
  }

  void _startFadeOut() {
    // Fade out the entire splash
    setState(() => _overallOpacity = 0.0);

    // After fade out completes, notify parent
    Future.delayed(const Duration(milliseconds: 800), () {
      if (mounted) {
        widget.onAnimationComplete();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedOpacity(
      opacity: _overallOpacity,
      duration: const Duration(milliseconds: 800),
      curve: Curves.easeInOut,
      child: Container(
        color: const Color(0xFF121212),
        child: LayoutBuilder(
          builder: (context, constraints) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                mainAxisSize: MainAxisSize.min,
                children: [
                  // Jhow Cortes (top text)
                  AnimatedOpacity(
                    opacity: _jhowCortesOpacity,
                    duration: const Duration(milliseconds: 790),
                    curve: Curves.easeIn,
                    child: SizedBox(
                      width: constraints.maxWidth * 0.6,
                      child: Image.asset(
                        'assets/images/branding/jhow_cortes.png',
                        fit: BoxFit.contain,
                      ),
                    ),
                  ),
                  
                  const SizedBox(height: 8),
                  
                  // Logo (center)
                  AnimatedOpacity(
                    opacity: _logoOpacity,
                    duration: const Duration(milliseconds: 1090),
                    curve: Curves.easeIn,
                    child: SizedBox(
                      width: constraints.maxWidth * 0.4,
                      child: Image.asset(
                        'assets/images/branding/logo.png',
                        fit: BoxFit.contain,
                      ),
                    ),
                  ),
                  
                  const SizedBox(height: 8),
                  
                  // Barbearia (bottom text)
                  AnimatedOpacity(
                    opacity: _barbeariaOpacity,
                    duration: const Duration(milliseconds: 900),
                    curve: Curves.easeIn,
                    child: SizedBox(
                      width: constraints.maxWidth * 0.7,
                      child: Image.asset(
                        'assets/images/branding/barbearia.png',
                        fit: BoxFit.contain,
                      ),
                    ),
                  ),
                ],
              ),
            );
          },
        ),
      ),
    );
  }
}
