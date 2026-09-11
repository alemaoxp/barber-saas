import 'package:flutter/material.dart';

import '../clients/admin_clients_page.dart';
import '../../../services/barber_api.dart';
import '../services/admin_services_page.dart';
import 'daily_agenda_models.dart';
import 'widgets/new_appointment_bottom_sheet.dart';

String _weekdayName(DateTime date) {
  const names = [
    'Segunda-feira',
    'Terça-feira',
    'Quarta-feira',
    'Quinta-feira',
    'Sexta-feira',
    'Sábado',
    'Domingo',
  ];
  return names[date.weekday - 1];
}

String _weekdayShort(DateTime date) {
  const names = ['Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb', 'Dom'];
  return names[date.weekday - 1];
}

String _monthName(DateTime date) {
  const names = [
    'janeiro',
    'fevereiro',
    'março',
    'abril',
    'maio',
    'junho',
    'julho',
    'agosto',
    'setembro',
    'outubro',
    'novembro',
    'dezembro',
  ];
  return names[date.month - 1];
}

class DailyAgendaPage extends StatefulWidget {
  const DailyAgendaPage({super.key, this.api, this.initialDate});

  final BarberApi? api;
  final DateTime? initialDate;

  @override
  State<DailyAgendaPage> createState() => _DailyAgendaPageState();
}

class _DailyAgendaPageState extends State<DailyAgendaPage> {
  static const _primary = Color(0xFF0D2742);
  static const _background = Color(0xFFF6F8FA);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  late final BarberApi _api;
  late DateTime _selectedDate;
  late Future<DailyAgendaResponse> _agenda;

  @override
  void initState() {
    super.initState();
    _api = widget.api ?? BarberApi();
    _selectedDate = _dateOnly(widget.initialDate ?? DateTime.now());
    _agenda = _api.dailyAgenda(_selectedDate);
  }

  void _selectDate(DateTime date) {
    setState(() {
      _selectedDate = _dateOnly(date);
      _agenda = _api.dailyAgenda(_selectedDate);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _background,
      bottomNavigationBar: _AdminNavigation(
        onClientsTap: () {
          Navigator.of(context).push(
            MaterialPageRoute<void>(
              builder: (_) => AdminClientsPage(api: _api),
            ),
          );
        },
        onServicesTap: () {
          Navigator.of(context).push(
            MaterialPageRoute<void>(
              builder: (_) => AdminServicesPage(api: _api),
            ),
          );
        },
      ),
      body: SafeArea(
        child: Column(
          children: [
            _Header(selectedDate: _selectedDate, onDateSelected: _selectDate),
            Expanded(
              child: FutureBuilder<DailyAgendaResponse>(
                future: _agenda,
                builder: (context, snapshot) {
                  if (snapshot.connectionState != ConnectionState.done) {
                    return const Center(child: CircularProgressIndicator());
                  }
                  if (snapshot.hasError) {
                    return _MessageState(
                      title: 'Não foi possível carregar a agenda.',
                      subtitle: snapshot.error is BarberApiException
                          ? (snapshot.error as BarberApiException).message
                          : 'Tente novamente em instantes.',
                      action: TextButton(
                        onPressed: () => _selectDate(_selectedDate),
                        child: const Text('Tentar novamente'),
                      ),
                    );
                  }

                  final agenda = snapshot.requireData;
                  if (!agenda.workingDay) {
                    return const _MessageState(
                      title: 'Não há expediente neste dia.',
                    );
                  }
                  if (agenda.slots.isEmpty) {
                    return const _MessageState(
                      title: 'Agenda vazia neste dia.',
                    );
                  }

                  return _AgendaList(
                    slots: agenda.slots,
                    onCreateAppointment: _openNewAppointment,
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  DateTime _dateOnly(DateTime value) =>
      DateTime(value.year, value.month, value.day);

  void _openNewAppointment(DailyAgendaSlot slot) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      barrierColor: Colors.black.withValues(alpha: 0.32),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => NewAppointmentBottomSheet(
        slot: slot,
        api: _api,
        onCreated: () {
          Navigator.of(context).pop();
          _selectDate(_selectedDate);
        },
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.selectedDate, required this.onDateSelected});

  static const int _bookingWindowDays = 30;

  final DateTime selectedDate;
  final ValueChanged<DateTime> onDateSelected;

  @override
  Widget build(BuildContext context) {
    final today = _dateOnly(DateTime.now());
    final titleDate =
        '${_weekdayName(selectedDate)}, ${selectedDate.day} de ${_monthName(selectedDate)}';

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(22, 14, 22, 18),
      color: Colors.white,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            height: 42,
            child: Stack(
              alignment: Alignment.center,
              children: [
                Align(
                  alignment: Alignment.centerLeft,
                  child: IconButton(
                    onPressed: () {},
                    icon: const Icon(Icons.menu_rounded),
                    color: _DailyAgendaPageState._primary,
                    tooltip: 'Menu',
                  ),
                ),
                const Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      'Jhow Cortes',
                      style: TextStyle(
                        color: _DailyAgendaPageState._primary,
                        fontSize: 17,
                        height: 1,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    SizedBox(height: 4),
                    Text(
                      'BARBEARIA',
                      style: TextStyle(
                        color: _DailyAgendaPageState._muted,
                        fontSize: 7,
                        fontWeight: FontWeight.w800,
                        letterSpacing: 3,
                      ),
                    ),
                  ],
                ),
                Align(
                  alignment: Alignment.centerRight,
                  child: IconButton(
                    onPressed: () {},
                    icon: const Icon(Icons.notifications_rounded),
                    color: _DailyAgendaPageState._primary,
                    tooltip: 'Notificações',
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 18),
          Row(
            children: [
              const Expanded(
                child: Text(
                  'Agenda',
                  style: TextStyle(
                    color: _DailyAgendaPageState._primary,
                    fontSize: 31,
                    height: 1,
                    fontWeight: FontWeight.w800,
                  ),
                ),
              ),
              IconButton(
                onPressed: _sameDay(selectedDate, today)
                    ? null
                    : () => onDateSelected(
                          selectedDate.subtract(const Duration(days: 1)),
                        ),
                icon: const Icon(Icons.chevron_left_rounded),
                color: const Color(0xFFC3CBD4),
                tooltip: 'Dia anterior',
              ),
              IconButton(
                onPressed: () =>
                    onDateSelected(selectedDate.add(const Duration(days: 1))),
                icon: const Icon(Icons.chevron_right_rounded),
                color: _DailyAgendaPageState._primary,
                tooltip: 'Próximo dia',
              ),
            ],
          ),
          Text(
            titleDate,
            style: const TextStyle(
              color: Color(0xFF465260),
              fontSize: 15,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 18),
          SizedBox(
            height: 70,
            child: ListView.separated(
              key: const Key('admin_days_selector'),
              scrollDirection: Axis.horizontal,
              itemCount: _bookingWindowDays,
              padding: const EdgeInsets.only(right: 2),
              separatorBuilder: (_, __) => const SizedBox(width: 7),
              itemBuilder: (context, index) {
                final day = today.add(Duration(days: index));
                final selected = _sameDay(day, selectedDate);
                return _DayButton(
                  key: Key('admin_day_${_dateParam(day)}'),
                  date: day,
                  selected: selected,
                  onTap: () => onDateSelected(day),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  bool _sameDay(DateTime first, DateTime second) =>
      first.year == second.year &&
      first.month == second.month &&
      first.day == second.day;

  DateTime _dateOnly(DateTime value) =>
      DateTime(value.year, value.month, value.day);

  String _dateParam(DateTime value) =>
      '${value.year.toString().padLeft(4, '0')}-${value.month.toString().padLeft(2, '0')}-${value.day.toString().padLeft(2, '0')}';
}

class _DayButton extends StatelessWidget {
  const _DayButton({
    super.key,
    required this.date,
    required this.selected,
    required this.onTap,
  });

  final DateTime date;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(10),
      onTap: onTap,
      child: Container(
        width: 53,
        decoration: BoxDecoration(
          color: selected ? _DailyAgendaPageState._primary : Colors.white,
          border: Border.all(
            color: selected
                ? _DailyAgendaPageState._primary
                : _DailyAgendaPageState._line,
          ),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              _weekdayShort(date),
              style: TextStyle(
                color: selected ? Colors.white70 : const Color(0xFF657181),
                fontWeight: FontWeight.w700,
                fontSize: 11,
              ),
            ),
            const SizedBox(height: 5),
            Text(
              date.day.toString().padLeft(2, '0'),
              style: TextStyle(
                color: selected ? Colors.white : const Color(0xFF17202A),
                fontWeight: FontWeight.w800,
                fontSize: 18,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _AgendaList extends StatelessWidget {
  const _AgendaList({required this.slots, required this.onCreateAppointment});

  final List<DailyAgendaSlot> slots;
  final ValueChanged<DailyAgendaSlot> onCreateAppointment;

  @override
  Widget build(BuildContext context) {
    final morning =
        slots.where((slot) => slot.dateTime.hour < 12).toList(growable: false);
    final afternoon =
        slots.where((slot) => slot.dateTime.hour >= 12).toList(growable: false);

    return ListView(
      padding: const EdgeInsets.fromLTRB(22, 22, 22, 18),
      children: [
        if (morning.isNotEmpty) ...[
          const _SectionTitle('Manhã'),
          ...morning.map(
            (slot) => _AgendaSlotTile(
              slot: slot,
              onCreateAppointment: onCreateAppointment,
            ),
          ),
          const SizedBox(height: 14),
        ],
        if (afternoon.isNotEmpty) ...[
          const _SectionTitle('Tarde'),
          ...afternoon.map(
            (slot) => _AgendaSlotTile(
              slot: slot,
              onCreateAppointment: onCreateAppointment,
            ),
          ),
        ],
      ],
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.title);

  final String title;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(0, 0, 0, 8),
      child: Text(
        title,
        style: const TextStyle(
          color: _DailyAgendaPageState._primary,
          fontSize: 16,
          fontWeight: FontWeight.w800,
        ),
      ),
    );
  }
}

class _AgendaSlotTile extends StatelessWidget {
  const _AgendaSlotTile({
    required this.slot,
    required this.onCreateAppointment,
  });

  final DailyAgendaSlot slot;
  final ValueChanged<DailyAgendaSlot> onCreateAppointment;

  @override
  Widget build(BuildContext context) {
    return switch (slot.status) {
      DailyAgendaSlotStatus.free => _FreeSlot(
          slot: slot,
          onCreateAppointment: onCreateAppointment,
        ),
      DailyAgendaSlotStatus.occupied => _OccupiedSlot(slot: slot),
      DailyAgendaSlotStatus.blocked => _BlockedSlot(slot: slot),
    };
  }
}

class _FreeSlot extends StatelessWidget {
  const _FreeSlot({required this.slot, required this.onCreateAppointment});

  final DailyAgendaSlot slot;
  final ValueChanged<DailyAgendaSlot> onCreateAppointment;

  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: const BoxDecoration(
        border: Border(bottom: BorderSide(color: Color(0xFFE9EEF3))),
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 10),
        child: Row(
          children: [
            _TimeText(slot.dateTime),
            const SizedBox(width: 16),
            const Expanded(
              child: Text(
                'Horário livre',
                style: TextStyle(
                  color: _DailyAgendaPageState._muted,
                  fontSize: 15,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
            SizedBox.square(
              dimension: 32,
              child: IconButton(
                padding: EdgeInsets.zero,
                visualDensity: VisualDensity.compact,
                onPressed: () => onCreateAppointment(slot),
                icon: const Icon(Icons.add_rounded, size: 20),
                color: _DailyAgendaPageState._primary,
                tooltip: 'Novo agendamento',
                style: IconButton.styleFrom(
                  backgroundColor: Colors.white,
                  side: const BorderSide(color: _DailyAgendaPageState._line),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _OccupiedSlot extends StatelessWidget {
  const _OccupiedSlot({required this.slot});

  final DailyAgendaSlot slot;

  @override
  Widget build(BuildContext context) {
    final customerName = slot.customer?.name ?? 'Cliente';
    final services = slot.services.map((service) => service.name).join(' + ');
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          _TimeText(slot.dateTime),
          const SizedBox(width: 16),
          Expanded(
            child: Material(
              color: Colors.white,
              elevation: 0.5,
              shadowColor: Colors.black.withValues(alpha: 0.06),
              borderRadius: BorderRadius.circular(10),
              child: InkWell(
                borderRadius: BorderRadius.circular(10),
                onTap: () {},
                child: Container(
                  padding: const EdgeInsets.fromLTRB(10, 9, 10, 9),
                  decoration: BoxDecoration(
                    border: Border.all(color: _DailyAgendaPageState._line),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              customerName,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                              style: const TextStyle(
                                color: Color(0xFF152033),
                                fontSize: 13,
                                fontWeight: FontWeight.w900,
                              ),
                            ),
                            if (services.isNotEmpty) ...[
                              const SizedBox(height: 3),
                              Text(
                                services,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  color: Color(0xFF465260),
                                  fontSize: 12,
                                  fontWeight: FontWeight.w600,
                                ),
                              ),
                            ],
                          ],
                        ),
                      ),
                      const SizedBox(width: 8),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 9,
                              vertical: 4,
                            ),
                            decoration: BoxDecoration(
                              color: const Color(0xFFDDF3E5),
                              borderRadius: BorderRadius.circular(999),
                            ),
                            child: Text(
                              _statusLabel(slot.appointmentStatus),
                              style: const TextStyle(
                                color: Color(0xFF16773A),
                                fontSize: 10,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                          ),
                          const SizedBox(height: 5),
                          Text(
                            _money(slot.totalPrice),
                            style: const TextStyle(
                              color: _DailyAgendaPageState._primary,
                              fontSize: 12,
                              fontWeight: FontWeight.w900,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(width: 7),
                      const Icon(
                        Icons.chevron_right_rounded,
                        color: _DailyAgendaPageState._primary,
                        size: 20,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _money(num? value) {
    if (value == null) return 'R\$ 0,00';
    return 'R\$ ${value.toStringAsFixed(2).replaceAll('.', ',')}';
  }

  String _statusLabel(String? status) {
    return switch (status) {
      'SCHEDULED' => 'Agendado',
      'COMPLETED' => 'Concluído',
      _ => status ?? 'Agendado',
    };
  }
}

class _BlockedSlot extends StatelessWidget {
  const _BlockedSlot({required this.slot});

  final DailyAgendaSlot slot;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          _TimeText(slot.dateTime),
          const SizedBox(width: 16),
          Expanded(
            child: Container(
              padding: const EdgeInsets.fromLTRB(10, 9, 10, 9),
              decoration: BoxDecoration(
                color: const Color(0xFFFFEEF1),
                borderRadius: BorderRadius.circular(10),
                border: Border.all(color: const Color(0xFFF8D7DE)),
              ),
              child: Row(
                children: [
                  Container(
                    width: 36,
                    height: 36,
                    decoration: const BoxDecoration(
                      color: Color(0xFFFFDDE3),
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(
                      Icons.lock_rounded,
                      size: 18,
                      color: Color(0xFFC9172D),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Bloqueado',
                          style: TextStyle(
                            color: Color(0xFF9B0E1E),
                            fontSize: 13,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                        if (slot.block?.reason?.isNotEmpty == true) ...[
                          const SizedBox(height: 3),
                          Text(
                            slot.block!.reason!,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: const TextStyle(
                              color: Color(0xFF57343A),
                              fontSize: 12,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ],
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _TimeText extends StatelessWidget {
  const _TimeText(this.dateTime);

  final DateTime dateTime;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 48,
      child: Text(
        '${dateTime.hour.toString().padLeft(2, '0')}:${dateTime.minute.toString().padLeft(2, '0')}',
        maxLines: 1,
        softWrap: false,
        overflow: TextOverflow.visible,
        textAlign: TextAlign.left,
        style: const TextStyle(
          color: _DailyAgendaPageState._primary,
          fontSize: 14,
          fontWeight: FontWeight.w800,
        ),
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
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: Color(0xFF182331),
                fontSize: 18,
                fontWeight: FontWeight.w800,
              ),
            ),
            if (subtitle != null) ...[
              const SizedBox(height: 8),
              Text(
                subtitle!,
                textAlign: TextAlign.center,
                style: const TextStyle(color: Color(0xFF657181)),
              ),
            ],
            if (action != null) ...[const SizedBox(height: 12), action!],
          ],
        ),
      ),
    );
  }
}

class _AdminNavigation extends StatelessWidget {
  const _AdminNavigation({
    required this.onClientsTap,
    required this.onServicesTap,
  });

  final VoidCallback onClientsTap;
  final VoidCallback onServicesTap;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        border: Border(top: BorderSide(color: Color(0xFFE7ECF2))),
      ),
      child: SafeArea(
        top: false,
        minimum: EdgeInsets.only(bottom: 4),
        child: SizedBox(
          height: 58,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              const _AgendaNavItem(label: 'Agenda'),
              _NavItem(
                icon: Icons.content_cut_rounded,
                label: 'Serviços',
                onTap: onServicesTap,
              ),
              _NavItem(
                icon: Icons.people_outline_rounded,
                label: 'Clientes',
                onTap: onClientsTap,
              ),
              _NavItem(icon: Icons.more_horiz_rounded, label: 'Mais'),
            ],
          ),
        ),
      ),
    );
  }
}

class _AgendaNavItem extends StatelessWidget {
  const _AgendaNavItem({required this.label});

  final String label;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      borderRadius: BorderRadius.circular(12),
      child: SizedBox(
        width: 68,
        height: 58,
        child: Center(
          child: Text(
            label,
            maxLines: 1,
            style: const TextStyle(
              color: _DailyAgendaPageState._primary,
              fontSize: 11,
              fontWeight: FontWeight.w800,
            ),
          ),
        ),
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  const _NavItem({
    required this.icon,
    required this.label,
    this.onTap,
  });

  final IconData icon;
  final String label;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    const color = _DailyAgendaPageState._muted;
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
                color: Colors.transparent,
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
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
