import 'package:flutter/material.dart';

class SplashLogo extends StatelessWidget {
  const SplashLogo({
    super.key,
    this.size = 150,
  });

  final double size;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: size,
      height: size,
      child: Image.asset(
        'assets/images/branding/logo.png',
        fit: BoxFit.contain,
      ),
    );
  }
}
