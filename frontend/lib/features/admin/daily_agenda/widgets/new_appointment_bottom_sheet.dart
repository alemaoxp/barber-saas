import 'package:flutter/material.dart';

import '../../../../services/barber_api.dart';
import '../daily_agenda_models.dart';
import '../models/admin_customer_summary.dart';
import 'customer_selection_bottom_sheet.dart';

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

String _dateLabel(DateTime date) =>
    '${_weekdayName(date)}, ${date.day.toString().padLeft(2, '0')} de ${_monthName(date)}';

String _timeLabel(DateTime date) =>
    '${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';

class NewAppointmentBottomSheet extends StatefulWidget {
  const NewAppointmentBottomSheet({
    super.key,
    required this.slot,
    required this.api,
  });

  final DailyAgendaSlot slot;
  final BarberApi api;

  static const _primary = Color(0xFF0D2742);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  @override
  State<NewAppointmentBottomSheet> createState() =>
      _NewAppointmentBottomSheetState();
}

class _NewAppointmentBottomSheetState extends State<NewAppointmentBottomSheet> {
  AdminCustomerSummary? _selectedCustomer;

  Future<void> _selectCustomer() async {
    final customer = await showModalBottomSheet<AdminCustomerSummary>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      barrierColor: Colors.black.withValues(alpha: 0.32),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => CustomerSelectionBottomSheet(api: widget.api),
    );
    if (!mounted || customer == null) return;
    setState(() => _selectedCustomer = customer);
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      top: false,
      child: Padding(
        padding: EdgeInsets.only(
          left: 22,
          right: 22,
          bottom: 18 + MediaQuery.viewInsetsOf(context).bottom,
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
              'Novo agendamento',
              style: TextStyle(
                color: NewAppointmentBottomSheet._primary,
                fontSize: 24,
                height: 1,
                fontWeight: FontWeight.w900,
              ),
            ),
            const SizedBox(height: 10),
            Text(
              _dateLabel(widget.slot.dateTime),
              style: const TextStyle(
                color: Color(0xFF465260),
                fontSize: 15,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 18),
            Text(
              _timeLabel(widget.slot.dateTime),
              style: const TextStyle(
                color: NewAppointmentBottomSheet._primary,
                fontSize: 42,
                height: 1,
                fontWeight: FontWeight.w900,
              ),
            ),
            const SizedBox(height: 26),
            _SheetSection(
              title: 'Cliente',
              actionLabel: _selectedCustomer?.name ?? 'Selecionar cliente',
              subtitle: _selectedCustomer?.phone,
              onTap: _selectCustomer,
            ),
            const SizedBox(height: 18),
            const _SheetSection(
              title: 'Serviços',
              actionLabel: 'Selecionar serviços',
              onTap: null,
            ),
            const SizedBox(height: 28),
            SizedBox(
              width: double.infinity,
              height: 54,
              child: ElevatedButton(
                onPressed: null,
                style: ElevatedButton.styleFrom(
                  disabledBackgroundColor:
                      NewAppointmentBottomSheet._primary.withValues(alpha: 0.36),
                  disabledForegroundColor: Colors.white.withValues(alpha: 0.8),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                child: const Text(
                  'Continuar',
                  style: TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w800,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _SheetSection extends StatelessWidget {
  const _SheetSection({
    required this.title,
    required this.actionLabel,
    required this.onTap,
    this.subtitle,
  });

  final String title;
  final String actionLabel;
  final String? subtitle;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(
            color: NewAppointmentBottomSheet._primary,
            fontSize: 16,
            fontWeight: FontWeight.w900,
          ),
        ),
        const SizedBox(height: 10),
        Material(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
          child: InkWell(
            borderRadius: BorderRadius.circular(12),
            onTap: onTap,
            child: Container(
              constraints: const BoxConstraints(minHeight: 54),
              padding: const EdgeInsets.symmetric(horizontal: 14),
              decoration: BoxDecoration(
                border: Border.all(color: NewAppointmentBottomSheet._line),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                children: [
                  Expanded(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 10),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Text(
                            actionLabel,
                            style: TextStyle(
                              color: subtitle == null
                                  ? NewAppointmentBottomSheet._muted
                                  : NewAppointmentBottomSheet._primary,
                              fontSize: 15,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                          if (subtitle != null) ...[
                            const SizedBox(height: 3),
                            Text(
                              subtitle!,
                              style: const TextStyle(
                                color: NewAppointmentBottomSheet._muted,
                                fontSize: 13,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          ],
                        ],
                      ),
                    ),
                  ),
                  const Icon(
                    Icons.chevron_right_rounded,
                    color: NewAppointmentBottomSheet._primary,
                  ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }
}
