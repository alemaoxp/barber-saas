import 'package:flutter/material.dart';

abstract final class AppRadius {
  static const double sm = 10;
  static const double md = 16;
  static const double lg = 24;

  static BorderRadius get card => BorderRadius.circular(md);
  static BorderRadius get hero => BorderRadius.circular(lg);
  static BorderRadius get button => BorderRadius.circular(md);
  static BorderRadius get input => BorderRadius.circular(md);
  static BorderRadius get chip => BorderRadius.circular(sm);
  static BorderRadius get sheet => const BorderRadius.vertical(
        top: Radius.circular(lg),
      );
  static BorderRadius get dialog => BorderRadius.circular(lg);
}
