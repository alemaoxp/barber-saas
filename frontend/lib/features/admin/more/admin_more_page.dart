import 'package:flutter/material.dart';

import '../../../services/barber_api.dart';
import '../admin_navigation.dart';
import '../services/admin_services_page.dart';
import 'admin_schedule_blocks_page.dart';
import 'admin_settings_page.dart';
import 'admin_working_hours_page.dart';

class AdminMorePage extends StatelessWidget {
  const AdminMorePage({super.key, this.api});

  static const _primary = Color(0xFF0D2742);
  static const _background = Color(0xFFF6F8FA);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  final BarberApi? api;

  @override
  Widget build(BuildContext context) {
    final resolvedApi = api ?? BarberApi();
    return Scaffold(
      backgroundColor: _background,
      bottomNavigationBar:
          AdminNavigation(activeTab: AdminTab.more, api: resolvedApi),
      appBar: AppBar(
        backgroundColor: Colors.white,
        foregroundColor: _primary,
        elevation: 0,
        title: const Text('Mais'),
      ),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.fromLTRB(22, 18, 22, 22),
          children: [
            _MoreTile(
              icon: Icons.schedule_rounded,
              title: 'Horários de funcionamento',
              subtitle: 'Expediente semanal e intervalo',
              onTap: () {
                Navigator.of(context).push(
                  MaterialPageRoute<void>(
                    builder: (_) => AdminWorkingHoursPage(api: resolvedApi),
                  ),
                );
              },
            ),
            const SizedBox(height: 8),
            _MoreTile(
              icon: Icons.content_cut_rounded,
              title: 'Serviços',
              subtitle: 'Serviços e preços',
              onTap: () {
                Navigator.of(context).push(
                  MaterialPageRoute<void>(
                    builder: (_) => AdminServicesPage(
                      api: resolvedApi,
                      showBackToMore: true,
                    ),
                  ),
                );
              },
            ),
            const SizedBox(height: 8),
            _MoreTile(
              icon: Icons.event_busy_rounded,
              title: 'Bloqueios da agenda',
              subtitle: 'Folgas, férias e indisponibilidades',
              onTap: () {
                Navigator.of(context).push(
                  MaterialPageRoute<void>(
                    builder: (_) => AdminScheduleBlocksPage(api: resolvedApi),
                  ),
                );
              },
            ),
            const SizedBox(height: 8),
            _MoreTile(
              icon: Icons.settings_outlined,
              title: 'Configurações',
              subtitle: 'Preferências do Admin',
              onTap: () {
                Navigator.of(context).push(
                  MaterialPageRoute<void>(
                    builder: (_) => const AdminSettingsPage(),
                  ),
                );
              },
            ),
            const SizedBox(height: 8),
            const _MoreTile(
              icon: Icons.logout_rounded,
              title: 'Sair',
              subtitle: 'Disponível quando houver autenticação',
            ),
          ],
        ),
      ),
    );
  }
}

class _MoreTile extends StatelessWidget {
  const _MoreTile({
    required this.icon,
    required this.title,
    this.subtitle,
    this.onTap,
  });

  final IconData icon;
  final String title;
  final String? subtitle;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.white,
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            border: Border.all(color: AdminMorePage._line),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              Icon(icon, color: AdminMorePage._primary),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: const TextStyle(
                        color: AdminMorePage._primary,
                        fontSize: 16,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                    if (subtitle != null) ...[
                      const SizedBox(height: 3),
                      Text(
                        subtitle!,
                        style: const TextStyle(
                          color: AdminMorePage._muted,
                          fontSize: 13,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ],
                ),
              ),
              if (onTap != null)
                const Icon(Icons.chevron_right_rounded,
                    color: AdminMorePage._muted),
            ],
          ),
        ),
      ),
    );
  }
}
