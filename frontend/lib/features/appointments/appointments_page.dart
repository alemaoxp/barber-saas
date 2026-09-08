import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:barber_saas_mobile/widgets/cancel_appointment_dialog.dart';
import 'package:barber_saas_mobile/widgets/appointment_cancelled_dialog.dart';

import '../../models/appointment_data.dart';
import '../../models/availability_interest.dart';
import '../../services/barber_api.dart';

class AppointmentsPage extends StatefulWidget {
  const AppointmentsPage({
    super.key,
    this.api,
  });

  final BarberApi? api;

  @override
  State<AppointmentsPage> createState() => _AppointmentsPageState();
}

class _AppointmentsPageState extends State<AppointmentsPage>
    with SingleTickerProviderStateMixin {
  late final TabController _tabController;

  late final BarberApi _api;
  List<AppointmentData> _appointments = const [];
  Map<String, AvailabilityInterest> _activeInterests = const {};
  Map<String, List<AvailabilityOpportunity>> _opportunities = const {};
  bool _loading = true;
  String? _error;
  bool _enablingTestPush = false;

  @override
  void initState() {
    super.initState();
    _api = widget.api ?? BarberApi();

    _tabController = TabController(
      length: 2,
      vsync: this,
    );
    _loadAppointments();
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _loadAppointments() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final prefs = await SharedPreferences.getInstance();
      final phone = prefs.getString('customer_phone')?.trim() ?? '';
      if (phone.isEmpty) {
        if (mounted) {
          setState(() {
            _appointments = const [];
            _activeInterests = const {};
            _opportunities = const {};
          });
        }
        return;
      }
      final results = await _api.appointments(phone);
      final services = await _api.services();
      final names = {for (final service in services) service.id: service.name};
      for (final appointment in results) {
        appointment.services =
            appointment.serviceIds.map((id) => names[id] ?? 'Serviço').toList();
      }
      final activeEntries = await Future.wait(
        results
            .where((appointment) =>
                appointment.isUpcomingAt(DateTime.now()) &&
                appointment.id != null &&
                appointment.customerId != null)
            .map((appointment) async {
          final interest = await _api.activeAvailabilityInterest(
              appointment.customerId!, appointment.id!);
          return interest == null ? null : MapEntry(appointment.id!, interest);
        }),
      );
      final activeInterests = {
        for (final entry in activeEntries)
          if (entry != null) entry.key: entry.value,
      };
      final opportunityEntries = await Future.wait(
        activeInterests.values.map((interest) async => MapEntry(
              interest.appointmentId,
              await _api.availabilityOpportunities(
                  interest.customerId, interest.id),
            )),
      );
      if (mounted) {
        setState(() {
          _appointments = results;
          _activeInterests = activeInterests;
          _opportunities = {
            for (final entry in opportunityEntries) entry.key: entry.value
          };
        });
      }
    } on BarberApiException catch (error) {
      if (mounted) {
        setState(() => _error = error.message);
      }
    } catch (_) {
      if (mounted) {
        setState(() => _error = 'Não foi possível carregar seus agendamentos.');
      }
    } finally {
      if (mounted) {
        setState(() => _loading = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final now = DateTime.now();
    return Scaffold(
      backgroundColor: const Color(0xFF03070A),
      body: SafeArea(
        child: Column(
          children: [
            _buildHeader(),
            _buildTabs(),
            Expanded(
              child: _loading
                  ? const Center(
                      child: CircularProgressIndicator(color: Colors.white))
                  : _error != null
                      ? Center(
                          child: TextButton(
                              onPressed: _loadAppointments,
                              child: Text('Tentar novamente\n$_error',
                                  textAlign: TextAlign.center)))
                      : TabBarView(
                          controller: _tabController,
                          children: [
                            _buildUpcoming(
                              _appointments
                                  .where((item) => item.isUpcomingAt(now))
                                  .toList(),
                            ),
                            _buildHistory(_appointments
                                .where((item) => item.isHistoryAt(now))
                                .toList()),
                          ],
                        ),
            ),
          ],
        ),
      ),
    );
  }

  // ============================================================
  // HEADER
  // ============================================================

  Widget _buildHeader() {
    return SizedBox(
      height: 66,
      child: Row(
        children: [
          SizedBox(
            width: 64,
            height: 66,
            child: IconButton(
              onPressed: () {
                Navigator.of(context).popUntil(
                  (route) => route.isFirst,
                );
              },
              icon: const Icon(
                Icons.arrow_back_ios_new_rounded,
                size: 24,
                color: Colors.white,
              ),
              padding: EdgeInsets.zero,
              constraints: const BoxConstraints(),
            ),
          ),
          const Expanded(
            child: Center(
              child: Text(
                'Meus agendamentos',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                  color: Colors.white,
                ),
              ),
            ),
          ),
          const SizedBox(
            width: 64,
          ),
        ],
      ),
    );
  }

  // ============================================================
  // ABAS
  // ============================================================

  Widget _buildTabs() {
    return Container(
      height: 52,
      decoration: BoxDecoration(
        border: Border(
          bottom: BorderSide(
            color: Colors.white.withValues(
              alpha: 0.10,
            ),
          ),
        ),
      ),
      child: TabBar(
        controller: _tabController,
        indicatorColor: Colors.white,
        indicatorWeight: 2,
        dividerColor: Colors.transparent,
        labelColor: Colors.white,
        unselectedLabelColor: Colors.white.withValues(
          alpha: 0.48,
        ),
        labelStyle: const TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w600,
        ),
        unselectedLabelStyle: const TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w400,
        ),
        tabs: const [
          Tab(
            text: 'Próximos',
          ),
          Tab(
            text: 'Histórico',
          ),
        ],
      ),
    );
  }

  // ============================================================
  // PRÓXIMOS
  // ============================================================

  Widget _buildUpcoming(
    List<AppointmentData> appointments,
  ) {
    if (appointments.isEmpty) {
      return _buildEmptyState(
        icon: Icons.calendar_month_outlined,
        title: 'Nenhum agendamento',
        subtitle: 'Seus próximos horários aparecerão aqui.',
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.fromLTRB(
        20,
        20,
        20,
        30,
      ),
      physics: const BouncingScrollPhysics(),
      itemCount: appointments.length + 1,
      itemBuilder: (
        context,
        index,
      ) {
        if (index == 0) {
          return Padding(
            padding: const EdgeInsets.only(bottom: 14),
            child: OutlinedButton.icon(
              onPressed: _enablingTestPush ? null : _enableTestPush,
              icon: const Icon(Icons.notifications_outlined),
              label: Text(_enablingTestPush
                  ? 'ATIVANDO NOTIFICAÇÃO...'
                  : 'ATIVAR NOTIFICAÇÃO DE TESTE'),
            ),
          );
        }
        return Padding(
          padding: const EdgeInsets.only(
            bottom: 14,
          ),
          child: _buildAppointmentCard(
            appointments[index - 1],
            interest: _activeInterests[appointments[index - 1].id],
            opportunities:
                _opportunities[appointments[index - 1].id] ?? const [],
          ),
        );
      },
    );
  }

  Future<void> _enableTestPush() async {
    setState(() => _enablingTestPush = true);
    try {
      final customerId = _notificationCustomerId();
      if (customerId == null) {
        throw StateError('Cliente do agendamento atual não encontrado.');
      }
      await _api.enableTestPush(customerId);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Notificação de teste ativada.')),
        );
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
              content: Text('Não foi possível ativar a notificação de teste.')),
        );
      }
    } finally {
      if (mounted) setState(() => _enablingTestPush = false);
    }
  }

  String? _notificationCustomerId() {
    for (final appointment in _appointments) {
      if (appointment.isUpcomingAt(DateTime.now()) &&
          appointment.customerId != null) {
        return appointment.customerId;
      }
    }
    return null;
  }

  // ============================================================
  // HISTÓRICO
  // ============================================================

  Widget _buildHistory(List<AppointmentData> appointments) {
    if (appointments.isNotEmpty) {
      return ListView.builder(
        padding: const EdgeInsets.fromLTRB(20, 20, 20, 30),
        itemCount: appointments.length,
        itemBuilder: (_, index) => Padding(
            padding: const EdgeInsets.only(bottom: 14),
            child:
                _buildAppointmentCard(appointments[index], cancellable: false)),
      );
    }
    return _buildEmptyState(
      icon: Icons.history_rounded,
      title: 'Nenhum agendamento anterior',
      subtitle: 'Quando você concluir horários, eles aparecerão aqui.',
    );
  }

  // ============================================================
  // CARD
  // ============================================================

  Widget _buildAppointmentCard(AppointmentData appointment,
      {bool cancellable = true,
      AvailabilityInterest? interest,
      List<AvailabilityOpportunity> opportunities = const []}) {
    final dateInfo = _parseDate(
      appointment.date,
    );

    final services = appointment.services.isEmpty
        ? 'Serviço'
        : appointment.services.join(
            ' + ',
          );

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(
        18,
        18,
        14,
        16,
      ),
      decoration: BoxDecoration(
        color: Colors.white.withValues(
          alpha: 0.045,
        ),
        borderRadius: BorderRadius.circular(
          20,
        ),
        border: Border.all(
          color: Colors.white.withValues(
            alpha: 0.13,
          ),
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(
              alpha: 0.30,
            ),
            blurRadius: 22,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildDateBox(
                day: dateInfo.day,
                month: dateInfo.month,
              ),
              const SizedBox(
                width: 18,
              ),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            dateInfo.weekday,
                            style: TextStyle(
                              fontSize: 14,
                              color: Colors.white.withValues(
                                alpha: 0.70,
                              ),
                            ),
                          ),
                        ),
                        Text(
                          appointment.time,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                            color: Colors.white,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(
                      height: 10,
                    ),
                    Text(
                      services,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                        color: Colors.white,
                      ),
                    ),
                    const SizedBox(
                      height: 7,
                    ),
                    Text(
                      'Jhow Cortes Barbearia',
                      style: TextStyle(
                        fontSize: 14,
                        color: Colors.white.withValues(
                          alpha: 0.60,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(
            height: 20,
          ),
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: cancellable && appointment.cancelToken != null
                      ? () => _cancelAppointment(appointment)
                      : null,
                  style: OutlinedButton.styleFrom(
                    minimumSize: const Size(
                      double.infinity,
                      52,
                    ),
                    foregroundColor: Colors.white,
                    side: BorderSide(
                      color: Colors.white.withValues(
                        alpha: 0.55,
                      ),
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(
                        11,
                      ),
                    ),
                  ),
                  child: const Text(
                    'CANCELAR',
                    style: TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ),
              const SizedBox(
                width: 18,
              ),
              Tooltip(
                message: interest == null
                    ? 'Ativar antecipação'
                    : 'Desativar antecipação',
                child: IconButton(
                  onPressed: cancellable &&
                          appointment.id != null &&
                          appointment.customerId != null
                      ? () => _toggleAvailabilityInterest(appointment, interest)
                      : null,
                  icon: Icon(
                    interest == null
                        ? Icons.notifications_none_rounded
                        : Icons.notifications_active_rounded,
                    color: interest == null
                        ? Colors.white.withValues(alpha: 0.80)
                        : const Color(0xFF39FF68),
                  ),
                ),
              ),
            ],
          ),
          if (cancellable && interest != null && opportunities.isNotEmpty) ...[
            const SizedBox(height: 16),
            const Divider(color: Color(0x33FFFFFF)),
            const SizedBox(height: 8),
            const Align(
              alignment: Alignment.centerLeft,
              child: Text('Vaga anterior disponível',
                  style: TextStyle(
                      color: Color(0xFF39FF68), fontWeight: FontWeight.w600)),
            ),
            ...opportunities.map((opportunity) => Align(
                  alignment: Alignment.centerLeft,
                  child: TextButton(
                    onPressed: () => _acceptOpportunity(interest, opportunity),
                    child: Text(
                        'ACEITAR ${_formatOpportunity(opportunity.availableDateTime)}'),
                  ),
                )),
          ],
        ],
      ),
    );
  }

  // ============================================================
  // DATA
  // ============================================================

  Widget _buildDateBox({
    required String day,
    required String month,
  }) {
    return Container(
      width: 72,
      height: 92,
      decoration: BoxDecoration(
        color: Colors.black.withValues(
          alpha: 0.34,
        ),
        borderRadius: BorderRadius.circular(
          11,
        ),
        border: Border.all(
          color: Colors.white.withValues(
            alpha: 0.13,
          ),
        ),
      ),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Text(
            day,
            style: const TextStyle(
              fontSize: 34,
              height: 1,
              fontWeight: FontWeight.w400,
              color: Colors.white,
            ),
          ),
          const SizedBox(
            height: 8,
          ),
          Text(
            month,
            style: TextStyle(
              fontSize: 15,
              color: Colors.white.withValues(
                alpha: 0.82,
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ============================================================
  // EMPTY
  // ============================================================

  Widget _buildEmptyState({
    required IconData icon,
    required String title,
    required String subtitle,
  }) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(
          30,
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 46,
              color: Colors.white.withValues(
                alpha: 0.35,
              ),
            ),
            const SizedBox(
              height: 18,
            ),
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w600,
                color: Colors.white,
              ),
            ),
            const SizedBox(
              height: 8,
            ),
            Text(
              subtitle,
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: 14,
                height: 1.4,
                color: Colors.white.withValues(
                  alpha: 0.50,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ============================================================
  // CANCELAR
  // ============================================================

  Future<void> _cancelAppointment(
    AppointmentData appointment,
  ) async {
    await showDialog(
      context: context,
      barrierDismissible: false,
      builder: (dialogContext) {
        return CancelAppointmentDialog(
          onConfirm: () {
            Navigator.of(dialogContext).pop();
            _confirmCancel(appointment);
          },
        );
      },
    );
  }

  Future<void> _confirmCancel(AppointmentData appointment) async {
    try {
      await _api.cancelAppointment(appointment.cancelToken!);
      await _loadAppointments();
      if (!mounted) return;
      await showDialog(
        context: context,
        barrierDismissible: false,
        builder: (successContext) => AppointmentCancelledDialog(
          onBackToHome: () {
            Navigator.of(successContext).pop();
            Navigator.of(context).popUntil((route) => route.isFirst);
          },
        ),
      );
    } on BarberApiException catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(error.message)));
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('Não foi possível cancelar o agendamento.')));
      }
    }
  }

  Future<void> _toggleAvailabilityInterest(
    AppointmentData appointment,
    AvailabilityInterest? interest,
  ) async {
    try {
      if (interest == null) {
        await _api.createAvailabilityInterest(
            appointment.customerId!, appointment.id!);
      } else {
        await _api.cancelAvailabilityInterest(interest.customerId, interest.id);
      }
      await _loadAppointments();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text(interest == null
              ? 'Avisaremos aqui se surgir um horário antes.'
              : 'Antecipação desativada.'),
        ));
      }
    } on BarberApiException catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(context)
            .showSnackBar(SnackBar(content: Text(error.message)));
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('Não foi possível atualizar a antecipação.')));
      }
    }
  }

  Future<void> _acceptOpportunity(
    AvailabilityInterest interest,
    AvailabilityOpportunity opportunity,
  ) async {
    try {
      await _api.acceptAvailabilityOpportunity(
          interest.customerId, interest.id, opportunity.availableSlotId);
      await _loadAppointments();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Seu horário foi antecipado.')));
      }
    } on BarberApiException catch (error) {
      await _loadAppointments();
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          content: Text(error.message == 'Vaga de antecipação indisponível.'
              ? 'Esta vaga acabou de ser ocupada.'
              : error.message),
        ));
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
            content: Text('Não foi possível antecipar seu horário.')));
      }
    }
  }

  String _formatOpportunity(DateTime value) {
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
      'DEZ'
    ];
    return '${value.day.toString().padLeft(2, '0')} ${months[value.month - 1]} · ${value.hour.toString().padLeft(2, '0')}:${value.minute.toString().padLeft(2, '0')}';
  }

  // ============================================================
  // DATA
  // ============================================================

  _DateInfo _parseDate(
    String value,
  ) {
    final match = RegExp(
      r'(\d{1,2}) de ([A-Za-zÀ-ÿ]+)',
    ).firstMatch(
      value,
    );

    if (match == null) {
      return const _DateInfo(
        day: '--',
        month: '---',
        weekday: '',
      );
    }

    final day = match.group(1)!;

    final monthName = match.group(2)!;

    const months = {
      'Jan': 'JAN',
      'Fev': 'FEV',
      'Mar': 'MAR',
      'Abr': 'ABR',
      'Mai': 'MAI',
      'Jun': 'JUN',
      'Jul': 'JUL',
      'Ago': 'AGO',
      'Set': 'SET',
      'Out': 'OUT',
      'Nov': 'NOV',
      'Dez': 'DEZ',
    };

    final month = months[monthName] ?? monthName.toUpperCase();

    return _DateInfo(
      day: day,
      month: month,
      weekday: _weekdayFromDate(
        value,
      ),
    );
  }

  String _weekdayFromDate(
    String value,
  ) {
    final match = RegExp(
      r'(\d{1,2}) de ([A-Za-zÀ-ÿ]+) de (\d{4})',
    ).firstMatch(
      value,
    );

    if (match == null) {
      return '';
    }

    const monthNumbers = {
      'Jan': 1,
      'Fev': 2,
      'Mar': 3,
      'Abr': 4,
      'Mai': 5,
      'Jun': 6,
      'Jul': 7,
      'Ago': 8,
      'Set': 9,
      'Out': 10,
      'Nov': 11,
      'Dez': 12,
    };

    final month = monthNumbers[match.group(2)!];

    if (month == null) {
      return '';
    }

    final date = DateTime(
      int.parse(
        match.group(3)!,
      ),
      month,
      int.parse(
        match.group(1)!,
      ),
    );

    const weekdays = [
      'Segunda-feira',
      'Terça-feira',
      'Quarta-feira',
      'Quinta-feira',
      'Sexta-feira',
      'Sábado',
      'Domingo',
    ];

    return weekdays[date.weekday - 1];
  }
}

// ============================================================
// DATA INFO
// ============================================================

class _DateInfo {
  final String day;
  final String month;
  final String weekday;

  const _DateInfo({
    required this.day,
    required this.month,
    required this.weekday,
  });
}
