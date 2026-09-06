import 'package:flutter/material.dart';
import 'package:barber_saas_mobile/widgets/cancel_appointment_dialog.dart';
import 'package:barber_saas_mobile/widgets/appointment_cancelled_dialog.dart';

import '../../models/appointment_data.dart';
import '../../services/appointment_store.dart';

class AppointmentsPage extends StatefulWidget {
  const AppointmentsPage({
    super.key,
  });

  @override
  State<AppointmentsPage> createState() =>
      _AppointmentsPageState();
}

class _AppointmentsPageState
    extends State<AppointmentsPage>
    with SingleTickerProviderStateMixin {
  late final TabController _tabController;

  final AppointmentStore _store =
      AppointmentStore.instance;

  final ValueNotifier<int> _storeNotifier =
  ValueNotifier<int>(0);

  @override
  void initState() {
    super.initState();

    _tabController = TabController(
      length: 2,
      vsync: this,
    );
  }

  @override
  void dispose() {
    _tabController.dispose();
    _storeNotifier.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor:
      const Color(0xFF03070A),

      body: SafeArea(
        child: Column(
          children: [
            _buildHeader(),

            _buildTabs(),

            Expanded(
              child: ValueListenableBuilder<int>(
                valueListenable:
                _storeNotifier,

                builder: (
                    context,
                    _,
                    __,
                    ) {
                  final appointments =
                      _store.appointments;

                  return TabBarView(
                    controller:
                    _tabController,

                    children: [
                      _buildUpcoming(
                        appointments,
                      ),

                      _buildHistory(),
                    ],
                  );
                },
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

              padding:
              EdgeInsets.zero,

              constraints:
              const BoxConstraints(),
            ),
          ),

          const Expanded(
            child: Center(
              child: Text(
                'Meus agendamentos',

                style: TextStyle(
                  fontSize: 18,

                  fontWeight:
                  FontWeight.w600,

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
            color:
            Colors.white.withValues(
              alpha: 0.10,
            ),
          ),
        ),
      ),

      child: TabBar(
        controller:
        _tabController,

        indicatorColor:
        Colors.white,

        indicatorWeight: 2,

        dividerColor:
        Colors.transparent,

        labelColor:
        Colors.white,

        unselectedLabelColor:
        Colors.white.withValues(
          alpha: 0.48,
        ),

        labelStyle:
        const TextStyle(
          fontSize: 16,

          fontWeight:
          FontWeight.w600,
        ),

        unselectedLabelStyle:
        const TextStyle(
          fontSize: 16,

          fontWeight:
          FontWeight.w400,
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
        icon:
        Icons.calendar_month_outlined,

        title:
        'Nenhum agendamento',

        subtitle:
        'Seus próximos horários aparecerão aqui.',
      );
    }

    return ListView.builder(
      padding:
      const EdgeInsets.fromLTRB(
        20,
        20,
        20,
        30,
      ),

      physics:
      const BouncingScrollPhysics(),

      itemCount:
      appointments.length,

      itemBuilder: (
          context,
          index,
          ) {
        return Padding(
          padding:
          const EdgeInsets.only(
            bottom: 14,
          ),

          child:
          _buildAppointmentCard(
            appointments[index],
          ),
        );
      },
    );
  }

  // ============================================================
  // HISTÓRICO
  // ============================================================

  Widget _buildHistory() {
    return _buildEmptyState(
      icon:
      Icons.history_rounded,

      title:
      'Nenhum agendamento anterior',

      subtitle:
      'Quando você concluir horários, eles aparecerão aqui.',
    );
  }

  // ============================================================
  // CARD
  // ============================================================

  Widget _buildAppointmentCard(
      AppointmentData appointment,
      ) {
    final dateInfo =
    _parseDate(
      appointment.date,
    );

    final services =
    appointment.services.isEmpty
        ? 'Serviço'
        : appointment.services.join(
      ' + ',
    );

    return Container(
      width: double.infinity,

      padding:
      const EdgeInsets.fromLTRB(
        18,
        18,
        14,
        16,
      ),

      decoration:
      BoxDecoration(
        color:
        Colors.white.withValues(
          alpha: 0.045,
        ),

        borderRadius:
        BorderRadius.circular(
          20,
        ),

        border: Border.all(
          color:
          Colors.white.withValues(
            alpha: 0.13,
          ),
        ),

        boxShadow: [
          BoxShadow(
            color:
            Colors.black.withValues(
              alpha: 0.30,
            ),

            blurRadius: 22,

            offset:
            const Offset(0, 10),
          ),
        ],
      ),

      child: Column(
        children: [
          Row(
            crossAxisAlignment:
            CrossAxisAlignment.start,

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
                  crossAxisAlignment:
                  CrossAxisAlignment.start,

                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            dateInfo.weekday,

                            style:
                            TextStyle(
                              fontSize: 14,

                              color: Colors
                                  .white
                                  .withValues(
                                alpha: 0.70,
                              ),
                            ),
                          ),
                        ),

                        Text(
                          appointment.time,

                          style:
                          const TextStyle(
                            fontSize: 16,

                            fontWeight:
                            FontWeight.w600,

                            color:
                            Colors.white,
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

                      overflow:
                      TextOverflow.ellipsis,

                      style:
                      const TextStyle(
                        fontSize: 16,

                        fontWeight:
                        FontWeight.w600,

                        color:
                        Colors.white,
                      ),
                    ),

                    const SizedBox(
                      height: 7,
                    ),

                    Text(
                      'Jhow Cortes Barbearia',

                      style:
                      TextStyle(
                        fontSize: 14,

                        color: Colors.white
                            .withValues(
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
                child:
                OutlinedButton(
                  onPressed: () =>
                      _cancelAppointment(
                        appointment,
                      ),

                  style:
                  OutlinedButton.styleFrom(
                    minimumSize:
                    const Size(
                      double.infinity,
                      52,
                    ),

                    foregroundColor:
                    Colors.white,

                    side:
                    BorderSide(
                      color: Colors
                          .white
                          .withValues(
                        alpha: 0.55,
                      ),
                    ),

                    shape:
                    RoundedRectangleBorder(
                      borderRadius:
                      BorderRadius
                          .circular(
                        11,
                      ),
                    ),
                  ),

                  child:
                  const Text(
                    'CANCELAR',

                    style:
                    TextStyle(
                      fontSize: 14,

                      fontWeight:
                      FontWeight.w600,
                    ),
                  ),
                ),
              ),

              const SizedBox(
                width: 18,
              ),

              Icon(
                Icons
                    .chevron_right_rounded,

                size: 29,

                color: Colors.white
                    .withValues(
                  alpha: 0.80,
                ),
              ),
            ],
          ),
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

      decoration:
      BoxDecoration(
        color:
        Colors.black.withValues(
          alpha: 0.34,
        ),

        borderRadius:
        BorderRadius.circular(
          11,
        ),

        border: Border.all(
          color:
          Colors.white.withValues(
            alpha: 0.13,
          ),
        ),
      ),

      child: Column(
        mainAxisAlignment:
        MainAxisAlignment.center,

        children: [
          Text(
            day,

            style:
            const TextStyle(
              fontSize: 34,

              height: 1,

              fontWeight:
              FontWeight.w400,

              color:
              Colors.white,
            ),
          ),

          const SizedBox(
            height: 8,
          ),

          Text(
            month,

            style:
            TextStyle(
              fontSize: 15,

              color: Colors.white
                  .withValues(
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
        padding:
        const EdgeInsets.all(
          30,
        ),

        child: Column(
          mainAxisAlignment:
          MainAxisAlignment.center,

          children: [
            Icon(
              icon,

              size: 46,

              color: Colors.white
                  .withValues(
                alpha: 0.35,
              ),
            ),

            const SizedBox(
              height: 18,
            ),

            Text(
              title,

              textAlign:
              TextAlign.center,

              style:
              const TextStyle(
                fontSize: 18,

                fontWeight:
                FontWeight.w600,

                color:
                Colors.white,
              ),
            ),

            const SizedBox(
              height: 8,
            ),

            Text(
              subtitle,

              textAlign:
              TextAlign.center,

              style:
              TextStyle(
                fontSize: 14,

                height: 1.4,

                color: Colors.white
                    .withValues(
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
            // Fecha o primeiro pop-up.
            Navigator.of(
              dialogContext,
            ).pop();

            // Remove o agendamento.
            _store.remove(
              appointment,
            );

            // Atualiza a lista.
            _storeNotifier.value++;

            // Abre o segundo pop-up.
            showDialog(
              context: context,
              barrierDismissible: false,
              builder: (successContext) {
                return AppointmentCancelledDialog(
                  onBackToHome: () {
                    Navigator.of(
                      successContext,
                    ).pop();

                    Navigator.of(
                      context,
                    ).popUntil(
                          (route) => route.isFirst,
                    );
                  },
                );
              },
            );
          },
        );
      },
    );
  }

  // ============================================================
  // DATA
  // ============================================================

  _DateInfo _parseDate(
      String value,
      ) {
    final match =
    RegExp(
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

    final day =
    match.group(1)!;

    final monthName =
    match.group(2)!;

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

    final month =
        months[monthName] ??
            monthName.toUpperCase();

    return _DateInfo(
      day: day,
      month: month,
      weekday:
      _weekdayFromDate(
        value,
      ),
    );
  }

  String _weekdayFromDate(
      String value,
      ) {
    final match =
    RegExp(
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

    final month =
    monthNumbers[
    match.group(2)!];

    if (month == null) {
      return '';
    }

    final date =
    DateTime(
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

    return weekdays[
    date.weekday - 1];
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