import 'package:flutter/material.dart';

import 'features/appointments/appointments_page.dart';
import 'features/admin/daily_agenda/daily_agenda_page.dart';
import 'features/home/home_page.dart';
import 'features/splash/splash_page.dart';

final GlobalKey<NavigatorState> navigatorKey = GlobalKey<NavigatorState>();

void main() {
  runApp(const BarberSaasApp());
}

class BarberSaasApp extends StatelessWidget {
  const BarberSaasApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Barber SaaS',
      navigatorKey: navigatorKey,
      home: switch (
      Uri.base.queryParameters['screen'] ??
          const String.fromEnvironment('START_SCREEN')
      ) {
        'appointments' => const AppointmentsPage(),
        'admin' => const DailyAgendaPage(),
        _ => SplashPage(
            onFinished: () {
              navigatorKey.currentState?.pushReplacement(
                MaterialPageRoute(
                  builder: (_) => HomePage(),
                ),
              );
            },
          ),
      },
    );
  }
}
