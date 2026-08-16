import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../widgets/splash_content.dart';

class SplashPage extends StatefulWidget {
  const SplashPage({super.key});

  @override
  State<SplashPage> createState() => _SplashPageState();
}

class _SplashPageState extends State<SplashPage> {
  bool _isNavigating = false;

  void _handleAnimationComplete() {
    if (!_isNavigating && mounted) {
      _isNavigating = true;
      // Navigate to home with fade transition
      context.goNamed('home');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF121212),
      body: SplashContent(
        onAnimationComplete: _handleAnimationComplete,
      ),
    );
  }
}
