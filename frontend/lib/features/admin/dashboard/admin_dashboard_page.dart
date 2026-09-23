import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:intl/intl.dart';

import '../../../services/barber_api.dart';
import 'dashboard_summary.dart';

typedef DashboardRangePicker = Future<DateTimeRange?> Function(
  BuildContext context,
  DateTimeRange initialRange,
);

class AdminDashboardPage extends StatefulWidget {
  const AdminDashboardPage({
    super.key,
    required this.api,
    this.today,
    this.pickRange,
  });

  final BarberApi api;
  final DateTime? today;
  final DashboardRangePicker? pickRange;

  @override
  State<AdminDashboardPage> createState() => _AdminDashboardPageState();
}

class _AdminDashboardPageState extends State<AdminDashboardPage> {
  static const _primary = Color(0xFF0D2742);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  late DateTime _today;
  late DateTimeRange _range;
  late Future<DashboardSummary> _summary;

  @override
  void initState() {
    super.initState();
    _today = _date(widget.today ?? DateTime.now());
    _range = DateTimeRange(start: _monthStart(_today), end: _monthEnd(_today));
    _load();
  }

  void _load() {
    _summary = widget.api.dashboardSummary(
      startDate: _range.start,
      endDate: _range.end,
    );
  }

  void _setRange(DateTimeRange range) {
    HapticFeedback.selectionClick();
    setState(() {
      _range = range;
      _load();
    });
  }

  Future<void> _selectRange() async {
    final picker = widget.pickRange ?? _defaultPicker;
    final selected = await picker(context, _range);
    if (selected != null) _setRange(selected);
  }

  Future<DateTimeRange?> _defaultPicker(
    BuildContext context,
    DateTimeRange initialRange,
  ) =>
      showDialog<DateTimeRange>(
        context: context,
        builder: (_) => _DashboardRangeDialog(initialRange: initialRange),
      );

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF6F8FA),
      appBar: AppBar(
        backgroundColor: Colors.white,
        foregroundColor: _primary,
        elevation: 0,
        title: const Text('Resumo'),
      ),
      body: SafeArea(
        top: false,
        child: ListView(
          padding: const EdgeInsets.all(22),
          children: [
            const Text('Resumo financeiro',
                style: TextStyle(
                  color: _primary,
                  fontSize: 26,
                  fontWeight: FontWeight.w900,
                )),
            const SizedBox(height: 16),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                _PeriodButton(
                    label: 'Hoje',
                    onTap: () =>
                        _setRange(DateTimeRange(start: _today, end: _today))),
                _PeriodButton(
                    label: 'Ontem',
                    onTap: () {
                      final yesterday =
                          _today.subtract(const Duration(days: 1));
                      _setRange(
                          DateTimeRange(start: yesterday, end: yesterday));
                    }),
                _PeriodButton(
                    label: 'Este mês',
                    onTap: () => _setRange(DateTimeRange(
                        start: _monthStart(_today), end: _monthEnd(_today)))),
              ],
            ),
            const SizedBox(height: 20),
            Text('Período',
                style: TextStyle(color: _muted, fontWeight: FontWeight.w800)),
            const SizedBox(height: 4),
            Text(_rangeText(_range),
                style: const TextStyle(
                    color: _primary,
                    fontSize: 17,
                    fontWeight: FontWeight.w900)),
            const SizedBox(height: 10),
            OutlinedButton.icon(
              onPressed: _selectRange,
              icon: const Icon(Icons.date_range_rounded),
              label: const Text('Selecionar período'),
            ),
            const SizedBox(height: 20),
            FutureBuilder<DashboardSummary>(
              future: _summary,
              builder: (context, snapshot) {
                if (snapshot.connectionState != ConnectionState.done) {
                  return const Center(
                      child: Padding(
                    padding: EdgeInsets.all(32),
                    child: CircularProgressIndicator(),
                  ));
                }
                if (snapshot.hasError) {
                  final message = snapshot.error is BarberApiException
                      ? (snapshot.error as BarberApiException).message
                      : 'Não foi possível carregar o resumo.';
                  return _DashboardMessage(
                      message: message, onRetry: () => setState(_load));
                }
                final summary = snapshot.requireData;
                return Container(
                  padding: const EdgeInsets.all(18),
                  decoration: BoxDecoration(
                    color: Colors.white,
                    border: Border.all(color: _line),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text('Valor agendado',
                          style: TextStyle(
                              color: _muted, fontWeight: FontWeight.w700)),
                      const SizedBox(height: 4),
                      Text(_money(summary.scheduledValue),
                          style: const TextStyle(
                              color: _primary,
                              fontSize: 30,
                              fontWeight: FontWeight.w900)),
                      const SizedBox(height: 18),
                      const Text('Agendamentos',
                          style: TextStyle(
                              color: _muted, fontWeight: FontWeight.w700)),
                      const SizedBox(height: 4),
                      Text('${summary.appointmentCount}',
                          style: const TextStyle(
                              color: _primary,
                              fontSize: 22,
                              fontWeight: FontWeight.w900)),
                    ],
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }
}

class _PeriodButton extends StatelessWidget {
  const _PeriodButton({required this.label, required this.onTap});
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) =>
      OutlinedButton(onPressed: onTap, child: Text(label));
}

class _DashboardRangeDialog extends StatefulWidget {
  const _DashboardRangeDialog({required this.initialRange});

  final DateTimeRange initialRange;

  @override
  State<_DashboardRangeDialog> createState() => _DashboardRangeDialogState();
}

class _DashboardRangeDialogState extends State<_DashboardRangeDialog> {
  static const _surface = Color(0xFF171C22);
  static const _range = Color(0xFF59616A);
  static const _selected = Color(0xFFE9EEF3);
  static const _weekday = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
  static const _months = [
    'Janeiro',
    'Fevereiro',
    'Março',
    'Abril',
    'Maio',
    'Junho',
    'Julho',
    'Agosto',
    'Setembro',
    'Outubro',
    'Novembro',
    'Dezembro',
  ];

  late DateTime _month;
  DateTime? _start;
  DateTime? _end;

  @override
  void initState() {
    super.initState();
    _month = _monthStart(widget.initialRange.start);
    _start = _date(widget.initialRange.start);
    _end = _date(widget.initialRange.end);
  }

  void _select(DateTime day) {
    if (_start == null || _end != null) {
      setState(() {
        _start = day;
        _end = null;
      });
      return;
    }
    if (day.isBefore(_start!)) {
      setState(() => _start = day);
      return;
    }
    Navigator.of(context).pop(DateTimeRange(start: _start!, end: day));
  }

  @override
  Widget build(BuildContext context) => Dialog(
        backgroundColor: Colors.transparent,
        insetPadding: const EdgeInsets.all(20),
        child: Container(
          constraints: const BoxConstraints(maxWidth: 380),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: _surface,
            borderRadius: BorderRadius.circular(20),
            border: Border.all(color: Colors.white12),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                children: [
                  IconButton(
                    onPressed: () => setState(
                        () => _month = DateTime(_month.year, _month.month - 1)),
                    icon: const Icon(Icons.chevron_left_rounded),
                    color: Colors.white,
                    tooltip: 'Mês anterior',
                  ),
                  Expanded(
                    child: Text(
                      '${_months[_month.month - 1]} ${_month.year}',
                      textAlign: TextAlign.center,
                      style: const TextStyle(
                          color: Colors.white, fontWeight: FontWeight.w800),
                    ),
                  ),
                  IconButton(
                    onPressed: () => setState(
                        () => _month = DateTime(_month.year, _month.month + 1)),
                    icon: const Icon(Icons.chevron_right_rounded),
                    color: Colors.white,
                    tooltip: 'Próximo mês',
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: _weekday
                    .map((label) => Expanded(
                          child: Text(label,
                              textAlign: TextAlign.center,
                              style: const TextStyle(
                                  color: Color(0xFF9CA7B2),
                                  fontSize: 12,
                                  fontWeight: FontWeight.w700)),
                        ))
                    .toList(),
              ),
              const SizedBox(height: 6),
              ...List.generate(
                  6,
                  (week) => Row(
                        children: List.generate(7, (weekday) {
                          final first = DateTime(_month.year, _month.month);
                          final day = first.add(Duration(
                              days: week * 7 + weekday - first.weekday % 7));
                          return Expanded(
                              child: _CalendarDay(
                            day: day,
                            currentMonth: day.month == _month.month,
                            start: _start,
                            end: _end,
                            onTap: () => _select(day),
                          ));
                        }),
                      )),
            ],
          ),
        ),
      );
}

class _CalendarDay extends StatelessWidget {
  const _CalendarDay({
    required this.day,
    required this.currentMonth,
    required this.start,
    required this.end,
    required this.onTap,
  });

  final DateTime day;
  final bool currentMonth;
  final DateTime? start;
  final DateTime? end;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final isStart = start != null && _sameDate(day, start!);
    final isEnd = end != null && _sameDate(day, end!);
    final inRange = start != null &&
        end != null &&
        day.isAfter(start!) &&
        day.isBefore(end!);
    final single = isStart && isEnd;
    return Container(
      height: 42,
      color: inRange ? _DashboardRangeDialogState._range : null,
      child: TextButton(
        key: ValueKey('calendar-day-${DateFormat('yyyy-MM-dd').format(day)}'),
        onPressed: onTap,
        style: TextButton.styleFrom(
          foregroundColor:
              currentMonth ? Colors.white : const Color(0xFF68727C),
          padding: EdgeInsets.zero,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.horizontal(
              left: Radius.circular(isStart || single ? 20 : 0),
              right: Radius.circular(isEnd || single ? 20 : 0),
            ),
          ),
          backgroundColor:
              isStart || isEnd ? _DashboardRangeDialogState._selected : null,
        ),
        child: Text('${day.day}',
            style: TextStyle(
              color: isStart || isEnd ? const Color(0xFF171C22) : null,
              fontWeight: isStart || isEnd ? FontWeight.w800 : FontWeight.w500,
            )),
      ),
    );
  }
}

class _DashboardMessage extends StatelessWidget {
  const _DashboardMessage({required this.message, required this.onRetry});
  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) => Center(
          child: Column(
        children: [
          Text(message, textAlign: TextAlign.center),
          TextButton(onPressed: onRetry, child: const Text('Tentar novamente')),
        ],
      ));
}

DateTime _date(DateTime value) => DateTime(value.year, value.month, value.day);
DateTime _monthStart(DateTime value) => DateTime(value.year, value.month);
DateTime _monthEnd(DateTime value) => DateTime(value.year, value.month + 1, 0);
bool _sameDate(DateTime first, DateTime second) =>
    first.year == second.year &&
    first.month == second.month &&
    first.day == second.day;
String _rangeText(DateTimeRange range) =>
    '${DateFormat('dd/MM/yyyy').format(range.start)} até ${DateFormat('dd/MM/yyyy').format(range.end)}';
String _money(double value) =>
    'R\$ ${value.toStringAsFixed(2).replaceAll('.', ',')}';
