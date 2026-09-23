import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../../models/booking_service.dart';
import '../../../../services/barber_api.dart';
import '../daily_agenda_models.dart';
import '../models/admin_customer_summary.dart';
import 'customer_selection_bottom_sheet.dart';
import 'service_selection_bottom_sheet.dart';

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

String _formatPrice(double value) {
  return 'R\$ ${value.toStringAsFixed(2).replaceAll('.', ',')}';
}

class NewAppointmentBottomSheet extends StatefulWidget {
  const NewAppointmentBottomSheet({
    super.key,
    required this.slot,
    required this.api,
    required this.onCreated,
    this.appointmentId,
    this.initialCustomerId,
    this.initialCustomerName,
    this.initialServiceIds = const [],
    this.initialServiceLabel,
  });

  final DailyAgendaSlot slot;
  final BarberApi api;
  final VoidCallback onCreated;
  final String? appointmentId;
  final String? initialCustomerId;
  final String? initialCustomerName;
  final List<String> initialServiceIds;
  final String? initialServiceLabel;

  static const _primary = Color(0xFF0D2742);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  @override
  State<NewAppointmentBottomSheet> createState() =>
      _NewAppointmentBottomSheetState();
}

class _NewAppointmentBottomSheetState extends State<NewAppointmentBottomSheet> {
  AdminCustomerSummary? _selectedCustomer;
  List<BookingService> _selectedServices = const [];
  bool _saving = false;
  String? _error;

  bool get _isEditing => widget.appointmentId != null;
  String? get _customerId => _selectedCustomer?.id ?? widget.initialCustomerId;
  String? get _customerName =>
      _selectedCustomer?.name ?? widget.initialCustomerName;
  List<String> get _serviceIds => _selectedServices.isNotEmpty
      ? _selectedServices.map((service) => service.id).toList()
      : widget.initialServiceIds;
  bool get _canContinue =>
      !_saving && _customerId != null && _serviceIds.isNotEmpty;

  double get _servicesTotal => _selectedServices.fold<double>(
        0,
        (total, service) => total + service.price,
      );

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

  Future<void> _selectServices() async {
    final services = await showModalBottomSheet<List<BookingService>>(
      context: context,
      useRootNavigator: true,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      barrierColor: Colors.black.withValues(alpha: 0.32),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => ServiceSelectionBottomSheet(
        api: widget.api,
        initialServices: _selectedServices.isEmpty
            ? widget.initialServiceIds
                .map((id) => BookingService(
                      id: id,
                      name: '',
                      description: '',
                      price: 0,
                    ))
                .toList()
            : _selectedServices,
      ),
    );
    if (!mounted || services == null) return;
    setState(() => _selectedServices = services);
  }

  Future<void> _saveAppointment() async {
    if (!_canContinue) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      if (_isEditing) {
        await widget.api.updateAdminAppointment(
          appointmentId: widget.appointmentId!,
          customerId: _customerId!,
          serviceIds: _serviceIds,
          appointmentDateTime: widget.slot.dateTime,
        );
      } else {
        await widget.api.createAdminAppointment(
          customerId: _customerId!,
          serviceIds: _serviceIds,
          appointmentDateTime: widget.slot.dateTime,
        );
      }
      if (!mounted) return;
      HapticFeedback.lightImpact();
      widget.onCreated();
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _error = error is BarberApiException
            ? error.message
            : 'Não foi possível salvar o agendamento.';
        _saving = false;
      });
    }
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
            Text(
              _isEditing ? 'Editar agendamento' : 'Novo agendamento',
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
              actionLabel: _customerName ?? 'Selecionar cliente',
              subtitle: _selectedCustomer?.phone,
              onTap: _selectCustomer,
            ),
            const SizedBox(height: 18),
            _SheetSection(
              title: 'Serviços',
              actionLabel: _selectedServices.isEmpty
                  ? widget.initialServiceLabel ?? 'Selecionar serviços'
                  : _selectedServices
                      .map((service) => service.name)
                      .join(' + '),
              subtitle: _selectedServices.isEmpty
                  ? null
                  : _formatPrice(_servicesTotal),
              onTap: _selectServices,
            ),
            const SizedBox(height: 28),
            if (_error != null) ...[
              Text(
                _error!,
                style: const TextStyle(
                  color: Colors.redAccent,
                  fontSize: 13,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 12),
            ],
            SizedBox(
              width: double.infinity,
              height: 54,
              child: ElevatedButton(
                onPressed: _canContinue ? _saveAppointment : null,
                style: ElevatedButton.styleFrom(
                  disabledBackgroundColor: NewAppointmentBottomSheet._primary
                      .withValues(alpha: 0.36),
                  disabledForegroundColor: Colors.white.withValues(alpha: 0.8),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                ),
                child: Text(
                  _saving
                      ? (_isEditing ? 'Salvando...' : 'Criando...')
                      : (_isEditing ? 'Salvar alterações' : 'Continuar'),
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
