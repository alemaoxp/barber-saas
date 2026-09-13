import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

import '../../../services/barber_api.dart';
import '../admin_navigation.dart';
import '../daily_agenda/daily_agenda_models.dart';
import '../daily_agenda/daily_agenda_page.dart';

class AdminHomePage extends StatefulWidget {
  const AdminHomePage({super.key, this.api, this.today});

  final BarberApi? api;
  final DateTime? today;

  @override
  State<AdminHomePage> createState() => _AdminHomePageState();
}

class _AdminHomePageState extends State<AdminHomePage> {
  static const _primary = Color(0xFF0D2742);
  static const _background = Color(0xFFF6F8FA);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  late final BarberApi _api;
  late final DateTime _today;
  late Future<DailyAgendaResponse> _agenda;

  @override
  void initState() {
    super.initState();
    _api = widget.api ?? BarberApi();
    _today = DateTime(
      (widget.today ?? DateTime.now()).year,
      (widget.today ?? DateTime.now()).month,
      (widget.today ?? DateTime.now()).day,
    );
    _agenda = _api.dailyAgenda(_today);
  }

  void _reload() {
    setState(() {
      _agenda = _api.dailyAgenda(_today);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _background,
      bottomNavigationBar: AdminNavigation(activeTab: AdminTab.home, api: _api),
      body: SafeArea(
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
            );
          },
        ),
      ),
    );
  }
}

class _HomeContent extends StatelessWidget {
  const _HomeContent({
    required this.api,
    required this.today,
    required this.agenda,
  });

  final BarberApi api;
  final DateTime today;
  final DailyAgendaResponse agenda;

  @override
  Widget build(BuildContext context) {
    final appointments = agenda.slots
        .where((slot) =>
            slot.status == DailyAgendaSlotStatus.occupied &&
            slot.appointmentStatus != 'CANCELED')
        .toList(growable: false);
    final total = appointments
        .where((slot) => slot.appointmentStatus != 'NO_SHOW')
        .fold<double>(
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
    final next = upcoming.isNotEmpty ? upcoming.first : null;
    final following = upcoming.skip(1).take(2).toList(growable: false);

    return ListView(
      padding: const EdgeInsets.fromLTRB(22, 16, 22, 24),
      children: [
        const Text(
          'Jhow Cortes',
          style: TextStyle(
            color: _AdminHomePageState._primary,
            fontSize: 24,
            fontWeight: FontWeight.w900,
          ),
        ),
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
              ),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: _MetricCard(
                value: _money(total),
                label: 'Valor agendado hoje',
              ),
            ),
          ],
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
        next == null ? const _EmptyNext() : _NextAppointment(slot: next),
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
  const _MetricCard({required this.value, required this.label});

  final String value;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 94,
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: _AdminHomePageState._line),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          FittedBox(
            fit: BoxFit.scaleDown,
            alignment: Alignment.centerLeft,
            child: Text(
              value,
              style: const TextStyle(
                color: _AdminHomePageState._primary,
                fontSize: 22,
                fontWeight: FontWeight.w900,
              ),
            ),
          ),
          const SizedBox(height: 6),
          Text(
            label,
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              color: _AdminHomePageState._muted,
              fontSize: 11,
              fontWeight: FontWeight.w700,
            ),
          ),
        ],
      ),
    );
  }
}

class _NextAppointment extends StatelessWidget {
  const _NextAppointment({required this.slot});

  final DailyAgendaSlot slot;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: _AdminHomePageState._line),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            _time(slot.dateTime),
            style: const TextStyle(
              color: _AdminHomePageState._primary,
              fontSize: 28,
              fontWeight: FontWeight.w900,
            ),
          ),
          const SizedBox(height: 6),
          Text(
            slot.customer?.name ?? 'Cliente',
            style: const TextStyle(
              color: _AdminHomePageState._primary,
              fontSize: 17,
              fontWeight: FontWeight.w900,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            slot.services.map((service) => service.name).join(' + '),
            style: const TextStyle(
              color: _AdminHomePageState._muted,
              fontSize: 14,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

class _EmptyNext extends StatelessWidget {
  const _EmptyNext();

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: _AdminHomePageState._line),
        borderRadius: BorderRadius.circular(8),
      ),
      child: const Text(
        'Nenhum próximo atendimento.',
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
