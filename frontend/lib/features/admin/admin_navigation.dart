import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

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
    this.onTabChanged,
  });

  static const primary = Color(0xFF0D2742);
  static const muted = Color(0xFF6C7886);

  final AdminTab activeTab;
  final BarberApi api;
  final ValueChanged<AdminTab>? onTabChanged;

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
    HapticFeedback.selectionClick();
    if (onTabChanged != null) {
      onTabChanged!(tab);
      return;
    }
    Navigator.of(context).pushReplacement(
      MaterialPageRoute<void>(
        builder: (_) => AdminShell(api: api, initialTab: tab),
      ),
    );
  }
}

class AdminShell extends StatefulWidget {
  const AdminShell({
    super.key,
    required this.api,
    this.initialTab = AdminTab.home,
    this.homeToday,
  });

  final BarberApi api;
  final AdminTab initialTab;
  final DateTime? homeToday;

  static bool selectTab(BuildContext context, AdminTab tab) {
    final shell = context.findAncestorStateOfType<_AdminShellState>();
    if (shell == null) return false;
    shell.selectTab(tab);
    return true;
  }

  @override
  State<AdminShell> createState() => _AdminShellState();
}

class _AdminShellState extends State<AdminShell> {
  late AdminTab _activeTab = widget.initialTab;

  void selectTab(AdminTab tab) {
    if (tab == _activeTab) return;
    setState(() => _activeTab = tab);
  }

  @override
  Widget build(BuildContext context) {
    final page = switch (_activeTab) {
      AdminTab.home => AdminHomePage(
          key: const ValueKey(AdminTab.home),
          api: widget.api,
          today: widget.homeToday,
          embedded: true,
        ),
      AdminTab.agenda => DailyAgendaPage(
          key: const ValueKey(AdminTab.agenda),
          api: widget.api,
          embedded: true,
        ),
      AdminTab.clients => AdminClientsPage(
          key: const ValueKey(AdminTab.clients),
          api: widget.api,
          embedded: true,
        ),
      AdminTab.more => AdminMorePage(
          key: const ValueKey(AdminTab.more),
          api: widget.api,
          embedded: true,
        ),
    };
    return Scaffold(
      backgroundColor: const Color(0xFFF6F8FA),
      body: AnimatedSwitcher(
        duration: const Duration(milliseconds: 220),
        transitionBuilder: (child, animation) => SlideTransition(
          position: Tween<Offset>(
            begin: const Offset(0.08, 0),
            end: Offset.zero,
          ).animate(animation),
          child: FadeTransition(opacity: animation, child: child),
        ),
        child: page,
      ),
      bottomNavigationBar: AdminNavigation(
        activeTab: _activeTab,
        api: widget.api,
        onTabChanged: selectTab,
      ),
    );
  }
}

class AdminBrandMark extends StatelessWidget {
  const AdminBrandMark({super.key});

  @override
  Widget build(BuildContext context) {
    return const Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Text(
          'Jhow Cortes',
          style: TextStyle(
            color: AdminNavigation.primary,
            fontSize: 17,
            height: 1,
            fontWeight: FontWeight.w800,
          ),
        ),
        SizedBox(height: 4),
        Text(
          'BARBEARIA',
          style: TextStyle(
            color: AdminNavigation.muted,
            fontSize: 7,
            fontWeight: FontWeight.w800,
            letterSpacing: 3,
          ),
        ),
      ],
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
