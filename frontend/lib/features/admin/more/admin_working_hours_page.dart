import 'package:flutter/material.dart';

import '../../../services/barber_api.dart';
import 'weekly_schedule_models.dart';

class AdminWorkingHoursPage extends StatefulWidget {
  const AdminWorkingHoursPage({super.key, this.api});

  final BarberApi? api;

  @override
  State<AdminWorkingHoursPage> createState() => _AdminWorkingHoursPageState();
}

class _AdminWorkingHoursPageState extends State<AdminWorkingHoursPage> {
  static const _primary = Color(0xFF0D2742);
  static const _background = Color(0xFFF6F8FA);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  late final BarberApi _api;
  late Future<WeeklyScheduleResponse> _schedule;
  List<WeeklyScheduleDay> _days = const [];
  bool _loaded = false;
  bool _saving = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _api = widget.api ?? BarberApi();
    _schedule = _api.weeklySchedule();
  }

  void _reload() {
    setState(() {
      _loaded = false;
      _error = null;
      _schedule = _api.weeklySchedule();
    });
  }

  Future<void> _save() async {
    final validation = _validate();
    if (validation != null) {
      setState(() => _error = validation);
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      final response = await _api.updateWeeklySchedule(_days);
      if (!mounted) return;
      setState(() {
        _days = response.weeklySchedule;
        _saving = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Horários salvos.')),
      );
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _saving = false;
        _error = error is BarberApiException
            ? error.message
            : 'Não foi possível salvar os horários.';
      });
    }
  }

  void _setDay(int index, WeeklyScheduleDay day) {
    setState(() {
      final next = [..._days];
      next[index] = day;
      _days = next;
      _error = null;
    });
  }

  String? _validate() {
    for (final day in _days) {
      if (!day.workingDay) continue;
      if (day.startTime == null ||
          day.endTime == null ||
          day.breakStartTime == null ||
          day.breakEndTime == null) {
        return 'Informe todos os horários dos dias ativos.';
      }
      final start = _minutes(day.startTime!);
      final end = _minutes(day.endTime!);
      final breakStart = _minutes(day.breakStartTime!);
      final breakEnd = _minutes(day.breakEndTime!);
      if (start >= end) {
        return 'O horário inicial deve ser anterior ao horário final.';
      }
      if (breakStart >= breakEnd) {
        return 'O início do intervalo deve ser anterior ao fim do intervalo.';
      }
      if (breakStart < start || breakEnd > end) {
        return 'O intervalo deve estar dentro do horário de expediente.';
      }
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _background,
      appBar: AppBar(
        backgroundColor: Colors.white,
        foregroundColor: _primary,
        elevation: 0,
        title: const Text('Horários de funcionamento'),
      ),
      body: FutureBuilder<WeeklyScheduleResponse>(
        future: _schedule,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return _MessageState(
              title: 'Não foi possível carregar os horários.',
              subtitle: snapshot.error is BarberApiException
                  ? (snapshot.error as BarberApiException).message
                  : 'Tente novamente em instantes.',
              action: TextButton(
                onPressed: _reload,
                child: const Text('Tentar novamente'),
              ),
            );
          }
          if (!_loaded) {
            _days = snapshot.requireData.weeklySchedule;
            _loaded = true;
          }
          return SafeArea(
            child: ListView.separated(
              padding: const EdgeInsets.fromLTRB(22, 18, 22, 22),
              itemCount: _days.length + 1,
              separatorBuilder: (_, __) => const SizedBox(height: 8),
              itemBuilder: (context, index) {
                if (index == _days.length) {
                  return _SaveArea(
                    saving: _saving,
                    error: _error,
                    onSave: _save,
                  );
                }
                return _WorkingDayEditor(
                  day: _days[index],
                  onChanged: (day) => _setDay(index, day),
                );
              },
            ),
          );
        },
      ),
    );
  }
}

class _WorkingDayEditor extends StatelessWidget {
  const _WorkingDayEditor({required this.day, required this.onChanged});

  final WeeklyScheduleDay day;
  final ValueChanged<WeeklyScheduleDay> onChanged;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: _AdminWorkingHoursPageState._line),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        children: [
          Row(
            children: [
              Expanded(
                child: Text(
                  _dayLabel(day.dayOfWeek),
                  style: const TextStyle(
                    color: _AdminWorkingHoursPageState._primary,
                    fontSize: 16,
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
              Text(
                day.workingDay ? 'ON' : 'OFF',
                style: const TextStyle(
                  color: _AdminWorkingHoursPageState._muted,
                  fontSize: 12,
                  fontWeight: FontWeight.w900,
                ),
              ),
              const SizedBox(width: 8),
              Switch(
                key: Key('working-day-${day.dayOfWeek}'),
                value: day.workingDay,
                activeThumbColor: _AdminWorkingHoursPageState._primary,
                onChanged: (value) => onChanged(
                  value ? _enabledDay(day) : _disabledDay(day),
                ),
              ),
            ],
          ),
          if (day.workingDay) ...[
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: _TimeField(
                    fieldKey: 'start-${day.dayOfWeek}',
                    label: 'Início',
                    value: day.startTime,
                    onChanged: (value) => onChanged(day.copyWith(
                      startTime: value,
                    )),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _TimeField(
                    fieldKey: 'end-${day.dayOfWeek}',
                    label: 'Fim',
                    value: day.endTime,
                    onChanged: (value) => onChanged(day.copyWith(
                      endTime: value,
                    )),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: _TimeField(
                    fieldKey: 'break-start-${day.dayOfWeek}',
                    label: 'Intervalo',
                    value: day.breakStartTime,
                    onChanged: (value) => onChanged(day.copyWith(
                      breakStartTime: value,
                    )),
                  ),
                ),
                const Padding(
                  padding: EdgeInsets.fromLTRB(10, 28, 10, 0),
                  child: Text(
                    'até',
                    style: TextStyle(
                      color: _AdminWorkingHoursPageState._muted,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                Expanded(
                  child: _TimeField(
                    fieldKey: 'break-end-${day.dayOfWeek}',
                    label: '',
                    value: day.breakEndTime,
                    onChanged: (value) => onChanged(day.copyWith(
                      breakEndTime: value,
                    )),
                  ),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }
}

class _TimeField extends StatelessWidget {
  const _TimeField({
    required this.fieldKey,
    required this.label,
    required this.value,
    required this.onChanged,
  });

  final String fieldKey;
  final String label;
  final String? value;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    return DropdownButtonFormField<String>(
      key: Key('time-field-$fieldKey'),
      initialValue: value,
      decoration: InputDecoration(
        labelText: label.isEmpty ? null : label,
        border: OutlineInputBorder(borderRadius: BorderRadius.circular(10)),
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      ),
      items: _timeOptions
          .map(
            (time) => DropdownMenuItem(
              value: time,
              child: Text(_shortTime(time)),
            ),
          )
          .toList(),
      onChanged: (value) {
        if (value != null) onChanged(value);
      },
    );
  }
}

class _SaveArea extends StatelessWidget {
  const _SaveArea({required this.saving, this.error, required this.onSave});

  final bool saving;
  final String? error;
  final VoidCallback onSave;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          if (error != null) ...[
            Text(
              error!,
              style: const TextStyle(
                color: Colors.redAccent,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: 12),
          ],
          SizedBox(
            height: 52,
            child: ElevatedButton(
              onPressed: saving ? null : onSave,
              style: ElevatedButton.styleFrom(
                backgroundColor: _AdminWorkingHoursPageState._primary,
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              child: Text(saving ? 'Salvando...' : 'Salvar alterações'),
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
                color: _AdminWorkingHoursPageState._primary,
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
                  color: _AdminWorkingHoursPageState._muted,
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

WeeklyScheduleDay _enabledDay(WeeklyScheduleDay day) {
  final defaults = _defaultTimes(day.dayOfWeek);
  return day.copyWith(
    workingDay: true,
    startTime: day.startTime ?? defaults.start,
    endTime: day.endTime ?? defaults.end,
    breakStartTime: day.breakStartTime ?? '12:00:00',
    breakEndTime: day.breakEndTime ?? '14:00:00',
  );
}

WeeklyScheduleDay _disabledDay(WeeklyScheduleDay day) {
  return WeeklyScheduleDay(dayOfWeek: day.dayOfWeek, workingDay: false);
}

({String start, String end}) _defaultTimes(String dayOfWeek) {
  return dayOfWeek == 'MONDAY'
      ? (start: '09:30:00', end: '20:00:00')
      : (start: '09:30:00', end: '19:30:00');
}

String _dayLabel(String value) {
  return switch (value) {
    'MONDAY' => 'Segunda-feira',
    'TUESDAY' => 'Terça-feira',
    'WEDNESDAY' => 'Quarta-feira',
    'THURSDAY' => 'Quinta-feira',
    'FRIDAY' => 'Sexta-feira',
    'SATURDAY' => 'Sábado',
    'SUNDAY' => 'Domingo',
    _ => value,
  };
}

int _minutes(String value) {
  final parts = value.split(':');
  return int.parse(parts[0]) * 60 + int.parse(parts[1]);
}

String _shortTime(String? value) =>
    value == null ? '--:--' : value.substring(0, 5);

final _timeOptions = List.generate(48, (index) {
  final hour = index ~/ 2;
  final minute = index.isEven ? 0 : 30;
  return '${hour.toString().padLeft(2, '0')}:'
      '${minute.toString().padLeft(2, '0')}:00';
});
