import 'package:flutter/material.dart';

import '../../services/barber_api.dart';
import 'clients/admin_clients_page.dart';
import 'daily_agenda/daily_agenda_page.dart';
import 'home/admin_home_page.dart';
import 'more/admin_more_page.dart';

enum AdminTab { home, agenda, clients, more }

class AdminNavigation extends StatelessWidget {
  const AdminNavigation({
    super.key,
    required this.activeTab,
    required this.api,
  });

  static const primary = Color(0xFF0D2742);
  static const muted = Color(0xFF6C7886);

  final AdminTab activeTab;
  final BarberApi api;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        border: Border(top: BorderSide(color: Color(0xFFE7ECF2))),
      ),
      child: SafeArea(
        top: false,
        minimum: const EdgeInsets.only(bottom: 4),
        child: SizedBox(
          height: 58,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _NavItem(
                icon: Icons.home_outlined,
                label: 'Início',
                active: activeTab == AdminTab.home,
                onTap: () => _open(context, AdminTab.home),
              ),
              _NavItem(
                icon: Icons.calendar_today_rounded,
                label: 'Agenda',
                active: activeTab == AdminTab.agenda,
                onTap: () => _open(context, AdminTab.agenda),
              ),
              _NavItem(
                icon: Icons.people_outline_rounded,
                label: 'Clientes',
                active: activeTab == AdminTab.clients,
                onTap: () => _open(context, AdminTab.clients),
              ),
              _NavItem(
                icon: Icons.more_horiz_rounded,
                label: 'Mais',
                active: activeTab == AdminTab.more,
                onTap: () => _open(context, AdminTab.more),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _open(BuildContext context, AdminTab tab) {
    if (tab == activeTab) return;
    Navigator.of(context).pushReplacement(
      MaterialPageRoute<void>(
        builder: (_) => switch (tab) {
          AdminTab.home => AdminHomePage(api: api),
          AdminTab.agenda => DailyAgendaPage(api: api),
          AdminTab.clients => AdminClientsPage(api: api),
          AdminTab.more => AdminMorePage(api: api),
        },
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  const _NavItem({
    required this.icon,
    required this.label,
    required this.active,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final bool active;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final color = active ? AdminNavigation.primary : AdminNavigation.muted;
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: SizedBox(
        width: 68,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 30,
              height: 26,
              decoration: BoxDecoration(
                color: active ? const Color(0xFFE4F6FF) : Colors.transparent,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(icon, size: 20, color: color),
            ),
            const SizedBox(height: 2),
            Text(
              label,
              maxLines: 1,
              style: TextStyle(
                color: color,
                fontSize: 11,
                fontWeight: active ? FontWeight.w800 : FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
