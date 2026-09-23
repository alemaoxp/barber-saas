import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../../services/barber_api.dart';
import '../admin_navigation.dart';
import '../daily_agenda/daily_agenda_models.dart';
import '../daily_agenda/daily_agenda_page.dart';
import '../dashboard/admin_dashboard_page.dart';
import '../dashboard/dashboard_summary.dart';

class AdminHomePage extends StatefulWidget {
  const AdminHomePage({super.key, this.api, this.today, this.embedded = false});

  final BarberApi? api;
  final DateTime? today;
  final bool embedded;

  @override
  State<AdminHomePage> createState() => _AdminHomePageState();
}

class _AdminHomePageState extends State<AdminHomePage> {
  static const _primary = Color(0xFF0D2742);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  late final BarberApi _api;
  late final DateTime _today;
  late Future<DailyAgendaResponse> _agenda;
  late Future<DailyAgendaSlot?> _nextAppointment;
  late Future<DashboardSummary> _monthSummary;

  @override
  void initState() {
    super.initState();
    if (!widget.embedded) return;
    _api = widget.api ?? BarberApi();
    _today = DateTime(
      (widget.today ?? DateTime.now()).year,
      (widget.today ?? DateTime.now()).month,
      (widget.today ?? DateTime.now()).day,
    );
    _agenda = _api.dailyAgenda(_today);
    _nextAppointment = _api.nextScheduledAppointment();
    _monthSummary = _api.dashboardSummary(
      startDate: DateTime(_today.year, _today.month),
      endDate: DateTime(_today.year, _today.month + 1, 0),
    );
  }

  void _reload() {
    setState(() {
      _agenda = _api.dailyAgenda(_today);
      _nextAppointment = _api.nextScheduledAppointment();
      _monthSummary = _api.dashboardSummary(
        startDate: DateTime(_today.year, _today.month),
        endDate: DateTime(_today.year, _today.month + 1, 0),
      );
    });
  }

  @override
  Widget build(BuildContext context) {
    if (!widget.embedded) {
      return AdminShell(
          api: widget.api ?? BarberApi(), homeToday: widget.today);
    }
    return SafeArea(
      child: FutureBuilder<DailyAgendaResponse>(
        future: _agenda,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return _MessageState(
              title: 'Não foi possível carregar o início.',
              subtitle: snapshot.error is BarberApiException
                  ? (snapshot.error as BarberApiException).message
                  : 'Tente novamente em instantes.',
              action: TextButton(
                onPressed: _reload,
                child: const Text('Tentar novamente'),
              ),
            );
          }
          return _HomeContent(
            api: _api,
            today: _today,
            agenda: snapshot.requireData,
            nextAppointment: _nextAppointment,
            monthSummary: _monthSummary,
          );
        },
      ),
    );
  }
}

class _HomeContent extends StatelessWidget {
  const _HomeContent({
    required this.api,
    required this.today,
    required this.agenda,
    required this.nextAppointment,
    required this.monthSummary,
  });

  final BarberApi api;
  final DateTime today;
  final DailyAgendaResponse agenda;
  final Future<DailyAgendaSlot?> nextAppointment;
  final Future<DashboardSummary> monthSummary;

  @override
  Widget build(BuildContext context) {
    final appointments = agenda.slots
        .where((slot) =>
            slot.status == DailyAgendaSlotStatus.occupied &&
            slot.appointmentStatus != 'CANCELED')
        .toList(growable: false);
    final total = appointments.fold<double>(
      0,
      (sum, slot) => sum + (slot.totalPrice?.toDouble() ?? 0),
    );
    final actualNow = DateTime.now();
    final now = _sameDay(today, actualNow) ? actualNow : today;
    final upcoming = appointments
        .where((slot) =>
            slot.appointmentStatus == 'SCHEDULED' &&
            slot.dateTime
                .add(Duration(
                  minutes: slot.dateTime.weekday == DateTime.monday ? 40 : 30,
                ))
                .isAfter(now))
        .toList(growable: false)
      ..sort((a, b) => a.dateTime.compareTo(b.dateTime));
    final following = upcoming.skip(1).take(2).toList(growable: false);

    return ListView(
      padding: const EdgeInsets.fromLTRB(22, 16, 22, 24),
      children: [
        const SizedBox(height: 42, child: Center(child: AdminBrandMark())),
        const SizedBox(height: 20),
        const Text(
          'Hoje',
          style: TextStyle(
            color: _AdminHomePageState._primary,
            fontSize: 31,
            height: 1,
            fontWeight: FontWeight.w800,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          DateFormat('dd/MM/yyyy').format(today),
          style: const TextStyle(
            color: Color(0xFF465260),
            fontSize: 15,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 22),
        Row(
          children: [
            Expanded(
              child: _MetricCard(
                value: appointments.length.toString(),
                label: 'agendamentos hoje',
                centered: true,
                height: 76,
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: _MetricCard(
                value: _money(total),
                label: 'Valor agendado hoje',
                height: 76,
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        FutureBuilder<DashboardSummary>(
          future: monthSummary,
          builder: (context, snapshot) {
            final value = snapshot.hasData
                ? _money(snapshot.requireData.scheduledValue)
                : '—';
            return _MetricCard(
              value: value,
              label: 'Este mês',
              icon: Icons.bar_chart_rounded,
              trailing: Icons.chevron_right_rounded,
              onTap: () => Navigator.of(context).push(
                MaterialPageRoute<void>(
                  builder: (_) => AdminDashboardPage(api: api, today: today),
                ),
              ),
            );
          },
        ),
        const SizedBox(height: 18),
        const Text(
          'Próximo atendimento',
          style: TextStyle(
            color: _AdminHomePageState._primary,
            fontSize: 17,
            fontWeight: FontWeight.w900,
          ),
        ),
        const SizedBox(height: 8),
        FutureBuilder<DailyAgendaSlot?>(
          future: nextAppointment,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const SizedBox(
                height: 72,
                child: Center(child: CircularProgressIndicator()),
              );
            }
            if (snapshot.hasError) {
              return const _EmptyNext(
                message: 'Não foi possível carregar o próximo atendimento.',
              );
            }
            final next = snapshot.data;
            return next == null
                ? const _EmptyNext()
                : _NextAppointment(
                    slot: next,
                    showDate: !_sameDay(next.dateTime, today),
                  );
          },
        ),
        const SizedBox(height: 14),
        const Text(
          'Próximos horários',
          style: TextStyle(
            color: _AdminHomePageState._primary,
            fontSize: 17,
            fontWeight: FontWeight.w900,
          ),
        ),
        const SizedBox(height: 8),
        if (following.isEmpty)
          const _MessageState(title: 'Nenhum outro horário hoje.')
        else
          ...following.map((slot) => _UpcomingRow(slot: slot)),
        const SizedBox(height: 22),
        SizedBox(
          height: 48,
          child: ElevatedButton(
            onPressed: () {
              if (AdminShell.selectTab(context, AdminTab.agenda)) return;
              Navigator.of(context).pushReplacement(
                MaterialPageRoute<void>(
                  builder: (_) => DailyAgendaPage(api: api, initialDate: today),
                ),
              );
            },
            style: ElevatedButton.styleFrom(
              backgroundColor: _AdminHomePageState._primary,
              foregroundColor: Colors.white,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
            child: const Text('Ver agenda completa'),
          ),
        ),
      ],
    );
  }
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({
    required this.value,
    required this.label,
    this.centered = false,
    this.height = 94,
    this.icon,
    this.trailing,
    this.onTap,
  });

  final String value;
  final String label;
  final bool centered;
  final double height;
  final IconData? icon;
  final IconData? trailing;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final content = Container(
      height: height,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: _AdminHomePageState._line),
        borderRadius: BorderRadius.circular(8),
        boxShadow: const [
          BoxShadow(
            color: Color(0x120D2742),
            blurRadius: 10,
            offset: Offset(0, 4),
          ),
        ],
      ),
      child: Row(
        children: [
          if (icon != null) ...[
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: const Color(0x140D2742),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(icon, color: _AdminHomePageState._primary),
            ),
            const SizedBox(width: 12),
          ],
          Expanded(
            child: FittedBox(
              fit: BoxFit.scaleDown,
              alignment: centered ? Alignment.center : Alignment.centerLeft,
              child: Column(
                crossAxisAlignment: centered
                    ? CrossAxisAlignment.center
                    : CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    value,
                    style: const TextStyle(
                      color: _AdminHomePageState._primary,
                      fontSize: 22,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    label,
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    textAlign: centered ? TextAlign.center : TextAlign.start,
                    style: const TextStyle(
                      color: _AdminHomePageState._muted,
                      fontSize: 11,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
            ),
          ),
          if (trailing != null)
            Icon(trailing, color: _AdminHomePageState._muted),
        ],
      ),
    );
    if (onTap == null) return content;
    return Material(
      color: Colors.transparent,
      borderRadius: BorderRadius.circular(8),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: content,
      ),
    );
  }
}

class _NextAppointment extends StatelessWidget {
  const _NextAppointment({required this.slot, required this.showDate});

  final DailyAgendaSlot slot;
  final bool showDate;

  @override
  Widget build(BuildContext context) {
    final details = Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          slot.customer?.name ?? 'Cliente',
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(
            color: _AdminHomePageState._primary,
            fontSize: 17,
            fontWeight: FontWeight.w900,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          slot.services.map((service) => service.name).join(' + '),
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(
            color: _AdminHomePageState._muted,
            fontSize: 14,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );

    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: _AdminHomePageState._line),
        borderRadius: BorderRadius.circular(8),
        boxShadow: const [
          BoxShadow(
            color: Color(0x120D2742),
            blurRadius: 10,
            offset: Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (showDate) ...[
            Text(
              _nextAppointmentDate(slot.dateTime),
              style: const TextStyle(
                color: _AdminHomePageState._muted,
                fontSize: 12,
                fontWeight: FontWeight.w800,
              ),
            ),
            const SizedBox(height: 10),
          ],
          LayoutBuilder(
            builder: (context, constraints) {
              final time = Text(
                _time(slot.dateTime),
                style: const TextStyle(
                  color: _AdminHomePageState._primary,
                  fontSize: 28,
                  fontWeight: FontWeight.w900,
                ),
              );
              if (constraints.maxWidth < 260) {
                return Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [time, const SizedBox(height: 10), details],
                );
              }
              return Row(
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  time,
                  Container(
                    width: 1,
                    height: 44,
                    margin: const EdgeInsets.symmetric(horizontal: 16),
                    color: _AdminHomePageState._line,
                  ),
                  Expanded(child: details),
                ],
              );
            },
          ),
        ],
      ),
    );
  }
}

class _EmptyNext extends StatelessWidget {
  const _EmptyNext({this.message = 'Nenhum próximo atendimento.'});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: _AdminHomePageState._line),
        borderRadius: BorderRadius.circular(8),
        boxShadow: const [
          BoxShadow(
            color: Color(0x120D2742),
            blurRadius: 10,
            offset: Offset(0, 4),
          ),
        ],
      ),
      child: Text(
        message,
        style: TextStyle(
          color: _AdminHomePageState._muted,
          fontSize: 14,
          fontWeight: FontWeight.w700,
        ),
      ),
    );
  }
}

class _UpcomingRow extends StatelessWidget {
  const _UpcomingRow({required this.slot});

  final DailyAgendaSlot slot;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 12),
      decoration: const BoxDecoration(
        border: Border(bottom: BorderSide(color: Color(0xFFE9EEF3))),
      ),
      child: Row(
        children: [
          SizedBox(
            width: 54,
            child: Text(
              _time(slot.dateTime),
              style: const TextStyle(
                color: _AdminHomePageState._primary,
                fontSize: 14,
                fontWeight: FontWeight.w900,
              ),
            ),
          ),
          Expanded(
            child: Text(
              slot.customer?.name ?? 'Cliente',
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                color: _AdminHomePageState._primary,
                fontSize: 14,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _MessageState extends StatelessWidget {
  const _MessageState({required this.title, this.subtitle, this.action});

  final String title;
  final String? subtitle;
  final Widget? action;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(28),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: _AdminHomePageState._primary,
                fontSize: 17,
                fontWeight: FontWeight.w800,
              ),
            ),
            if (subtitle != null) ...[
              const SizedBox(height: 8),
              Text(
                subtitle!,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  color: _AdminHomePageState._muted,
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
            if (action != null) ...[const SizedBox(height: 12), action!],
          ],
        ),
      ),
    );
  }
}

String _time(DateTime value) =>
    '${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}';

String _money(double value) =>
    'R\$ ${value.toStringAsFixed(2).replaceAll('.', ',')}';

bool _sameDay(DateTime first, DateTime second) =>
    first.year == second.year &&
    first.month == second.month &&
    first.day == second.day;

String _nextAppointmentDate(DateTime value) {
  const weekdays = [
    'SEGUNDA-FEIRA',
    'TERÇA-FEIRA',
    'QUARTA-FEIRA',
    'QUINTA-FEIRA',
    'SEXTA-FEIRA',
    'SÁBADO',
    'DOMINGO',
  ];
  const months = [
    'JAN',
    'FEV',
    'MAR',
    'ABR',
    'MAI',
    'JUN',
    'JUL',
    'AGO',
    'SET',
    'OUT',
    'NOV',
    'DEZ',
  ];
  return '${weekdays[value.weekday - 1]} · ${value.day} ${months[value.month - 1]}';
}
