import 'package:animations/animations.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/booking/presentation/pages/booking_page.dart';
import '../../features/cancel_booking/presentation/pages/cancel_booking_page.dart';
import '../../features/confirmation/presentation/pages/confirmation_page.dart';
import '../../features/home/presentation/pages/home_page.dart';
import '../../features/services/presentation/pages/services_page.dart';
import '../../features/splash/presentation/pages/splash_page.dart';

abstract final class AppRoutes {
  static const splash = '/';
  static const home = '/home';
  static const booking = '/booking';
  static const services = '/services';
  static const confirmation = '/confirmation';
  static const cancelBooking = '/cancel-booking';
}

final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: AppRoutes.splash,
    routes: [
      GoRoute(
        path: AppRoutes.splash,
        name: 'splash',
        pageBuilder: (context, state) => _buildPage(
          state: state,
          child: const SplashPage(),
        ),
      ),
      GoRoute(
        path: AppRoutes.home,
        name: 'home',
        pageBuilder: (context, state) => _buildPage(
          state: state,
          child: const HomePage(),
        ),
      ),
      GoRoute(
        path: AppRoutes.booking,
        name: 'booking',
        pageBuilder: (context, state) => _buildPage(
          state: state,
          child: const BookingPage(),
        ),
      ),
      GoRoute(
        path: AppRoutes.services,
        name: 'services',
        pageBuilder: (context, state) => _buildPage(
          state: state,
          child: const ServicesPage(),
        ),
      ),
      GoRoute(
        path: AppRoutes.confirmation,
        name: 'confirmation',
        pageBuilder: (context, state) => _buildPage(
          state: state,
          child: const ConfirmationPage(),
        ),
      ),
      GoRoute(
        path: AppRoutes.cancelBooking,
        name: 'cancelBooking',
        pageBuilder: (context, state) => _buildPage(
          state: state,
          child: const CancelBookingPage(),
        ),
      ),
    ],
  );
});

CustomTransitionPage<void> _buildPage({
  required GoRouterState state,
  required Widget child,
}) {
  return CustomTransitionPage<void>(
    key: state.pageKey,
    child: child,
    transitionsBuilder: (context, animation, secondaryAnimation, child) {
      return FadeThroughTransition(
        animation: animation,
        secondaryAnimation: secondaryAnimation,
        child: child,
      );
    },
  );
}
