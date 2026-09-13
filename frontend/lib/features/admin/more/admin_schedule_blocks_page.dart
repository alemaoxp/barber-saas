import 'package:flutter/material.dart';

import '../../../services/barber_api.dart';
import 'schedule_block_models.dart';

class AdminScheduleBlocksPage extends StatefulWidget {
  const AdminScheduleBlocksPage({super.key, this.api});

  final BarberApi? api;

  @override
  State<AdminScheduleBlocksPage> createState() =>
      _AdminScheduleBlocksPageState();
}

class _AdminScheduleBlocksPageState extends State<AdminScheduleBlocksPage> {
  static const _primary = Color(0xFF0D2742);
  static const _background = Color(0xFFF6F8FA);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  late final BarberApi _api;
  late Future<List<ScheduleBlock>> _blocks;

  @override
  void initState() {
    super.initState();
    _api = widget.api ?? BarberApi();
    _blocks = _api.scheduleBlocks();
  }

  void _reload() {
    setState(() {
      _blocks = _api.scheduleBlocks();
    });
  }

  Future<void> _openNewBlock() async {
    final created = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      barrierColor: Colors.black.withValues(alpha: 0.32),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => _NewBlockBottomSheet(api: _api),
    );
    if (created == true) _reload();
  }

  Future<void> _deleteBlock(ScheduleBlock block) async {
    try {
      await _api.deleteScheduleBlock(block.id);
      _reload();
    } catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(_errorMessage(error))),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _background,
      appBar: AppBar(
        backgroundColor: Colors.white,
        foregroundColor: _primary,
        elevation: 0,
        title: const Text('Bloqueios da agenda'),
      ),
      body: SafeArea(
        child: FutureBuilder<List<ScheduleBlock>>(
          future: _blocks,
          builder: (context, snapshot) {
            if (snapshot.connectionState != ConnectionState.done) {
              return const Center(child: CircularProgressIndicator());
            }
            if (snapshot.hasError) {
              return _MessageState(
                title: 'Não foi possível carregar os bloqueios.',
                subtitle: _errorMessage(snapshot.error),
                action: TextButton(
                  onPressed: _reload,
                  child: const Text('Tentar novamente'),
                ),
              );
            }
            final blocks = snapshot.requireData;
            if (blocks.isEmpty) {
              return const _MessageState(title: 'Nenhum bloqueio cadastrado.');
            }
            return ListView.separated(
              padding: const EdgeInsets.fromLTRB(22, 18, 22, 96),
              itemCount: blocks.length,
              separatorBuilder: (_, __) => const SizedBox(height: 8),
              itemBuilder: (context, index) {
                final block = blocks[index];
                return Dismissible(
                  key: ValueKey('schedule-block-${block.id}'),
                  direction: DismissDirection.endToStart,
                  background: const SizedBox.shrink(),
                  secondaryBackground: const _DeleteSwipeBackground(),
                  onDismissed: (_) => _deleteBlock(block),
                  child: _BlockTile(block: block),
                );
              },
            );
          },
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _openNewBlock,
        backgroundColor: _primary,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add_rounded),
        label: const Text('Novo bloqueio'),
      ),
    );
  }
}

class _BlockTile extends StatelessWidget {
  const _BlockTile({required this.block});

  final ScheduleBlock block;

  @override
  Widget build(BuildContext context) {
    final reason = block.reason?.trim();
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: _AdminScheduleBlocksPageState._line),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '${_date(block.startDateTime)} ${_time(block.startDateTime)} - ${_time(block.endDateTime)}',
            style: const TextStyle(
              color: _AdminScheduleBlocksPageState._primary,
              fontSize: 15,
              fontWeight: FontWeight.w900,
            ),
          ),
          if (reason != null && reason.isNotEmpty) ...[
            const SizedBox(height: 6),
            Text(
              reason,
              style: const TextStyle(
                color: _AdminScheduleBlocksPageState._muted,
                fontSize: 14,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _NewBlockBottomSheet extends StatefulWidget {
  const _NewBlockBottomSheet({required this.api});

  final BarberApi api;

  @override
  State<_NewBlockBottomSheet> createState() => _NewBlockBottomSheetState();
}

class _NewBlockBottomSheetState extends State<_NewBlockBottomSheet> {
  final _dateController = TextEditingController();
  final _startController = TextEditingController();
  final _endController = TextEditingController();
  final _reasonController = TextEditingController();
  bool _saving = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _dateController.text = _date(DateTime.now());
    _startController.text = '09:00';
    _endController.text = '10:00';
  }

  @override
  void dispose() {
    _dateController.dispose();
    _startController.dispose();
    _endController.dispose();
    _reasonController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    final date = _parseDate(_dateController.text);
    final start = _parseTime(_startController.text);
    final end = _parseTime(_endController.text);
    if (date == null || start == null || end == null) {
      setState(() => _error = 'Informe data e horários válidos.');
      return;
    }
    final startDateTime =
        DateTime(date.year, date.month, date.day, start.hour, start.minute);
    final endDateTime =
        DateTime(date.year, date.month, date.day, end.hour, end.minute);
    if (!endDateTime.isAfter(startDateTime)) {
      setState(() => _error = 'O fim deve ser depois do início.');
      return;
    }
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.api.createScheduleBlock(
        startDateTime: startDateTime,
        endDateTime: endDateTime,
        reason: _reasonController.text.trim(),
      );
      if (!mounted) return;
      Navigator.of(context).pop(true);
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _error = _errorMessage(error);
        _saving = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      top: false,
      child: Padding(
        padding: EdgeInsets.fromLTRB(
          22,
          0,
          22,
          18 + MediaQuery.viewInsetsOf(context).bottom,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 46,
                height: 5,
                margin: const EdgeInsets.only(top: 10, bottom: 22),
                decoration: BoxDecoration(
                  color: const Color(0xFFD8DEE6),
                  borderRadius: BorderRadius.circular(999),
                ),
              ),
            ),
            const Text(
              'Novo bloqueio',
              style: TextStyle(
                color: _AdminScheduleBlocksPageState._primary,
                fontSize: 24,
                height: 1,
                fontWeight: FontWeight.w900,
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              key: const Key('schedule-block-date'),
              controller: _dateController,
              decoration: const InputDecoration(labelText: 'Data (dd/mm/aaaa)'),
              keyboardType: TextInputType.datetime,
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: TextField(
                    key: const Key('schedule-block-start'),
                    controller: _startController,
                    decoration:
                        const InputDecoration(labelText: 'Início (HH:mm)'),
                    keyboardType: TextInputType.datetime,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: TextField(
                    key: const Key('schedule-block-end'),
                    controller: _endController,
                    decoration: const InputDecoration(labelText: 'Fim (HH:mm)'),
                    keyboardType: TextInputType.datetime,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            TextField(
              key: const Key('schedule-block-reason'),
              controller: _reasonController,
              decoration: const InputDecoration(labelText: 'Motivo'),
            ),
            if (_error != null) ...[
              const SizedBox(height: 12),
              Text(
                _error!,
                style: const TextStyle(
                  color: Colors.redAccent,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              height: 54,
              child: ElevatedButton(
                onPressed: _saving ? null : _save,
                style: ElevatedButton.styleFrom(
                  backgroundColor: _AdminScheduleBlocksPageState._primary,
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                child: Text(_saving ? 'Salvando...' : 'Salvar bloqueio'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _DeleteSwipeBackground extends StatelessWidget {
  const _DeleteSwipeBackground();

  @override
  Widget build(BuildContext context) {
    return Container(
      alignment: Alignment.centerRight,
      padding: const EdgeInsets.only(right: 18),
      decoration: BoxDecoration(
        color: Colors.redAccent,
        borderRadius: BorderRadius.circular(8),
      ),
      child: const Text(
        'Excluir',
        style: TextStyle(color: Colors.white, fontWeight: FontWeight.w900),
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
                color: _AdminScheduleBlocksPageState._primary,
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
                  color: _AdminScheduleBlocksPageState._muted,
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

DateTime? _parseDate(String value) {
  final parts = value.split('/');
  if (parts.length != 3) return null;
  final day = int.tryParse(parts[0]);
  final month = int.tryParse(parts[1]);
  final year = int.tryParse(parts[2]);
  if (day == null || month == null || year == null) return null;
  return DateTime(year, month, day);
}

TimeOfDay? _parseTime(String value) {
  final parts = value.split(':');
  if (parts.length != 2) return null;
  final hour = int.tryParse(parts[0]);
  final minute = int.tryParse(parts[1]);
  if (hour == null || minute == null) return null;
  if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;
  return TimeOfDay(hour: hour, minute: minute);
}

String _date(DateTime value) =>
    '${value.day.toString().padLeft(2, '0')}/${value.month.toString().padLeft(2, '0')}/${value.year.toString().padLeft(4, '0')}';

String _time(DateTime value) =>
    '${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}';

String _errorMessage(Object? error) => error is BarberApiException
    ? error.message
    : 'Não foi possível concluir a operação.';
