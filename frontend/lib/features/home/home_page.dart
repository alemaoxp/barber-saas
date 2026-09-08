import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../models/appointment_data.dart';
import '../../services/barber_api.dart';
import '../service/service_page.dart';
import '../appointments/appointments_page.dart';

class HomePage extends StatefulWidget {
  HomePage({super.key, BarberApi? api}) : _api = api ?? BarberApi();

  final BarberApi _api;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  // ============================================================
  // DATA SELECIONADA
  // ============================================================

  int _selectedDay = 0;

  String? _selectedTime;
  List<String> _times = const [];
  bool _loadingTimes = true;
  String? _timesError;

  final ScrollController _timesScrollController =
  ScrollController();

  // ============================================================
  // JANELA DE AGENDAMENTO
  // ============================================================

  static const int _bookingWindowDays = 30;

  List<_DayOption> get _days {
    final today = DateTime.now();

    return List.generate(
      _bookingWindowDays,
          (index) {
        final date = today.add(
          Duration(days: index),
        );

        return _DayOption(
          date: date,
          label: index == 0
              ? 'Hoje'
              : _weekdayLabel(date),
          day: date.day.toString(),
          month: _monthLabel(date),
        );
      },
    );
  }

  String _weekdayLabel(DateTime date) {
    const weekdays = [
      'Seg',
      'Ter',
      'Qua',
      'Qui',
      'Sex',
      'Sáb',
      'Dom',
    ];

    return weekdays[date.weekday - 1];
  }

  String _monthLabel(DateTime date) {
    const months = [
      'Jan',
      'Fev',
      'Mar',
      'Abr',
      'Mai',
      'Jun',
      'Jul',
      'Ago',
      'Set',
      'Out',
      'Nov',
      'Dez',
    ];

    return months[date.month - 1];
  }

  // ============================================================
  // HORÁRIOS
  // ============================================================

  @override
  void initState() {
    super.initState();
    _loadTimes();
  }

  Future<void> _loadTimes() async {
    final date = _days[_selectedDay].date;
    setState(() {
      _loadingTimes = true;
      _timesError = null;
      _selectedTime = null;
    });
    try {
      final times = await widget._api.availableSlots(date);
      if (!mounted) return;
      setState(() => _times = times);
    } on BarberApiException catch (_) {
      if (!mounted) return;
      setState(() => _timesError = 'Não foi possível carregar os horários.');
    } catch (_) {
      if (!mounted) return;
      setState(() => _timesError = 'Não foi possível carregar os horários.');
    } finally {
      if (mounted) setState(() => _loadingTimes = false);
    }
  }

  @override
  void dispose() {
    _timesScrollController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,

      body: SafeArea(
        child: Column(
          children: [
            Expanded(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(
                  24,
                  2,
                  24,
                  0,
                ),

                child: Column(
                  crossAxisAlignment:
                  CrossAxisAlignment.start,

                  children: [
                    _buildTopArea(),

                    const SizedBox(
                      height: 17,
                    ),

                    const Text(
                      'Escolha quando\nquer vir',

                      style: TextStyle(
                        fontSize: 34,
                        height: 1.02,
                        fontWeight:
                        FontWeight.w700,
                        color:
                        Color(0xFF111111),
                        letterSpacing: -1.1,
                      ),
                    ),

                    const SizedBox(
                      height: 8,
                    ),

                    const Text(
                      'Veja os melhores horários para você.',

                      style: TextStyle(
                        fontSize: 17,
                        height: 1.3,
                        color:
                        Color(0xFF666666),
                      ),
                    ),

                    const SizedBox(
                      height: 18,
                    ),

                    _buildDays(),

                    const SizedBox(
                      height: 18,
                    ),

                    const Row(
                      children: [
                        Icon(
                          Icons
                              .access_time_rounded,
                          size: 24,
                          color:
                          Color(0xFF171717),
                        ),

                        SizedBox(
                          width: 9,
                        ),

                        Text(
                          'Horários disponíveis',

                          style: TextStyle(
                            fontSize: 19,
                            fontWeight:
                            FontWeight.w700,
                            color:
                            Color(0xFF171717),
                          ),
                        ),
                      ],
                    ),

                    const SizedBox(
                      height: 10,
                    ),

                    Expanded(
                      child: _buildTimes(),
                    ),
                  ],
                ),
              ),
            ),

            _buildContinueButton(),
          ],
        ),
      ),
    );
  }

  // ============================================================
  // TOPO
  // ============================================================

  Widget _buildTopArea() {
    return SizedBox(
      height: 82,

      child: Row(
        crossAxisAlignment:
        CrossAxisAlignment.center,

        children: [
          SizedBox(
            width: 48,
            height: 82,

            child: Material(
              color: Colors.transparent,

              child: InkWell(
                borderRadius:
                BorderRadius.circular(
                  14,
                ),

                onTap: () {
                  Navigator.of(
                    context,
                  ).maybePop();
                },

                child: const Center(
                  child: Icon(
                    Icons
                        .arrow_back_ios_new_rounded,
                    size: 24,
                    color:
                    Color(0xFF111111),
                  ),
                ),
              ),
            ),
          ),

          Expanded(
            child: Column(
              mainAxisAlignment:
              MainAxisAlignment.center,

              children: [
                Image.asset(
                  'assets/images/branding/logo.png',

                  width: 76,
                  height: 76,

                  fit: BoxFit.contain,
                ),

                Transform.translate(
                  offset:
                  const Offset(0, -2),

                  child: Container(
                    width: 120,
                    height: 1,
                    color:
                    const Color(
                      0xFF222222,
                    ),
                  ),
                ),
              ],
            ),
          ),

          _buildAppointmentsButton(),
        ],
      ),
    );
  }

  // ============================================================
  // MEUS AGENDAMENTOS
  // ============================================================

  Widget _buildAppointmentsButton() {
    return Material(
      color: Colors.transparent,

      child: InkWell(
        borderRadius:
        BorderRadius.circular(
          16,
        ),

        onTap: _openMyAppointments,

        child: Container(
          width: 48,
          height: 48,

          decoration: BoxDecoration(
            color: Colors.white,

            borderRadius:
            BorderRadius.circular(
              16,
            ),

            border: Border.all(
              color:
              Colors.black.withValues(
                alpha: 0.055,
              ),
            ),

            boxShadow: [
              BoxShadow(
                color:
                Colors.black.withValues(
                  alpha: 0.075,
                ),
                blurRadius: 13,
                offset:
                const Offset(0, 4),
              ),

              const BoxShadow(
                color: Colors.white,
                blurRadius: 4,
                offset:
                Offset(-2, -2),
              ),
            ],
          ),

          child: const Icon(
            Icons.calendar_month_outlined,
            size: 23,
            color: Color(0xFF24527A),
          ),
        ),
      ),
    );
  }

  // ============================================================
  // DIAS
  // ============================================================

  Widget _buildDays() {
    return SizedBox(
      height: 92,

      child: Stack(
        clipBehavior: Clip.none,

        children: [
          ListView.separated(
            key:
            const Key(
              'days_selector',
            ),

            scrollDirection:
            Axis.horizontal,

            physics:
            const BouncingScrollPhysics(),

            padding:
            const EdgeInsets.only(
              right: 50,
            ),

            itemCount:
            _days.length,

            separatorBuilder:
                (_, __) {
              return const SizedBox(
                width: 12,
              );
            },

            itemBuilder:
                (context, index) {
              final day =
              _days[index];

              final selected =
                  _selectedDay ==
                      index;

              return GestureDetector(
                onTap: () {
                  if (_selectedDay != index) {
                    setState(() {
                      _selectedDay = index;
                    });
                    _loadTimes();

                    HapticFeedback
                        .selectionClick();
                  }
                },

                child:
                _buildDayCard(
                  day: day,
                  selected:
                  selected,
                ),
              );
            },
          ),

          Positioned(
            right: -2,
            top: 0,
            bottom: 0,

            child: IgnorePointer(
              child: Container(
                width: 46,

                alignment:
                Alignment.center,

                decoration:
                BoxDecoration(
                  gradient:
                  LinearGradient(
                    begin:
                    Alignment
                        .centerLeft,

                    end:
                    Alignment
                        .centerRight,

                    colors: [
                      Colors.white
                          .withValues(
                        alpha: 0,
                      ),

                      Colors.white
                          .withValues(
                        alpha: 0.97,
                      ),
                    ],
                  ),
                ),

                child: Container(
                  width: 36,
                  height: 36,

                  decoration:
                  BoxDecoration(
                    color:
                    Colors.white,

                    shape:
                    BoxShape.circle,

                    boxShadow: [
                      BoxShadow(
                        color: Colors
                            .black
                            .withValues(
                          alpha: 0.075,
                        ),

                        blurRadius: 8,

                        offset:
                        const Offset(
                          0,
                          3,
                        ),
                      ),
                    ],
                  ),

                  child: Icon(
                    Icons
                        .chevron_right_rounded,

                    size: 27,

                    color: Colors
                        .black
                        .withValues(
                      alpha: 0.55,
                    ),
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDayCard({
    required _DayOption day,
    required bool selected,
  }) {
    return AnimatedContainer(
      duration:
      const Duration(
        milliseconds: 180,
      ),

      curve:
      Curves.easeOut,

      width: 76,

      decoration:
      BoxDecoration(
        color: selected
            ? const Color(
          0xFF111111,
        )
            : Colors.white,

        borderRadius:
        BorderRadius.circular(
          16,
        ),

        border: Border.all(
          color: selected
              ? const Color(
            0xFF111111,
          )
              : Colors.black
              .withValues(
            alpha: 0.065,
          ),
        ),

        boxShadow: [
          BoxShadow(
            color: Colors.black
                .withValues(
              alpha: selected
                  ? 0.13
                  : 0.06,
            ),

            blurRadius:
            selected ? 11 : 9,

            offset:
            const Offset(0, 4),
          ),

          if (!selected)
            const BoxShadow(
              color: Colors.white,
              blurRadius: 4,
              offset:
              Offset(-2, -2),
            ),
        ],
      ),

      child: Column(
        mainAxisAlignment:
        MainAxisAlignment.center,

        children: [
          Text(
            day.label,

            style: TextStyle(
              fontSize: 14,
              fontWeight:
              FontWeight.w600,

              color: selected
                  ? Colors.white
                  : const Color(
                0xFF222222,
              ),
            ),
          ),

          const SizedBox(
            height: 4,
          ),

          Text(
            day.day,

            style: TextStyle(
              fontSize: 24,
              fontWeight:
              FontWeight.w700,

              color: selected
                  ? Colors.white
                  : const Color(
                0xFF111111,
              ),
            ),
          ),

          Text(
            day.month,

            style: TextStyle(
              fontSize: 13,

              color: selected
                  ? Colors.white
                  .withValues(
                alpha: 0.85,
              )
                  : const Color(
                0xFF666666,
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ============================================================
  // HORÁRIOS
  // ============================================================

  Widget _buildTimes() {
    if (_loadingTimes) return const Center(child: CircularProgressIndicator());
    if (_timesError != null) {
      return Center(child: TextButton(onPressed: _loadTimes, child: const Text('Não foi possível carregar os horários.\nTentar novamente', textAlign: TextAlign.center)));
    }
    if (_times.isEmpty) {
      return const Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Nenhum horário disponível neste dia'),
            SizedBox(height: 6),
            Text('Escolha outra data para continuar.'),
          ],
        ),
      );
    }
    return Scrollbar(
      controller:
      _timesScrollController,

      thumbVisibility: true,

      radius:
      const Radius.circular(
        10,
      ),

      thickness: 3,

      child: ListView.separated(
        key:
        const Key(
          'times_selector',
        ),

        controller:
        _timesScrollController,

        physics:
        const BouncingScrollPhysics(),

        padding:
        const EdgeInsets.fromLTRB(
          0,
          1,
          5,
          12,
        ),

        itemCount:
        _times.length,

        separatorBuilder:
            (_, __) {
          return const SizedBox(
            height: 10,
          );
        },

        itemBuilder:
            (context, index) {
          final time =
          _times[index];

          final selected =
              _selectedTime ==
                  time;

          return GestureDetector(
            onTap: () {
              if (_selectedTime != time) {
                setState(() {
                  _selectedTime = time;
                });

                HapticFeedback
                    .selectionClick();
              }
            },

            child:
            _buildTimeCard(
              time: time,
              selected:
              selected,
            ),
          );
        },
      ),
    );
  }

  Widget _buildTimeCard({
    required String time,
    required bool selected,
  }) {
    return AnimatedContainer(
      duration:
      const Duration(
        milliseconds: 180,
      ),

      curve:
      Curves.easeOut,

      height: 58,

      padding:
      const EdgeInsets.symmetric(
        horizontal: 20,
      ),

      decoration:
      BoxDecoration(
        color: selected
            ? const Color(
          0xFF111111,
        )
            : Colors.white,

        borderRadius:
        BorderRadius.circular(
          16,
        ),

        border: Border.all(
          color: selected
              ? const Color(
            0xFF111111,
          )
              : Colors.black
              .withValues(
            alpha: 0.055,
          ),
        ),

        boxShadow: [
          BoxShadow(
            color: Colors.black
                .withValues(
              alpha: selected
                  ? 0.14
                  : 0.06,
            ),

            blurRadius:
            selected ? 12 : 10,

            offset:
            const Offset(0, 4),
          ),

          if (!selected)
            const BoxShadow(
              color: Colors.white,
              blurRadius: 4,
              offset:
              Offset(-2, -2),
            ),
        ],
      ),

      child: Row(
        mainAxisAlignment:
        MainAxisAlignment
            .spaceBetween,

        children: [
          Text(
            time,

            style: TextStyle(
              fontSize: 18,
              fontWeight:
              FontWeight.w600,

              color: selected
                  ? Colors.white
                  : const Color(
                0xFF151515,
              ),
            ),
          ),

          AnimatedContainer(
            duration:
            const Duration(
              milliseconds: 180,
            ),

            width: 30,
            height: 30,

            decoration:
            BoxDecoration(
              shape:
              BoxShape.circle,

              color: selected
                  ? Colors.white
                  : Colors.transparent,

              border: Border.all(
                color: selected
                    ? Colors.white
                    : const Color(
                  0xFF222222,
                ),

                width: 2,
              ),

              boxShadow: selected
                  ? [
                BoxShadow(
                  color: Colors
                      .black
                      .withValues(
                    alpha: 0.12,
                  ),

                  blurRadius: 4,

                  offset:
                  const Offset(
                    0,
                    2,
                  ),
                ),
              ]
                  : null,
            ),

            child: selected
                ? const Icon(
              Icons.check_rounded,
              size: 18,
              color:
              Color(0xFF111111),
            )
                : null,
          ),
        ],
      ),
    );
  }

  // ============================================================
  // CONTINUAR
  // ============================================================

  Widget _buildContinueButton() {
    return Container(
      color:
      Colors.white,

      padding:
      const EdgeInsets.fromLTRB(
        24,
        8,
        24,
        22,
      ),

      child: SizedBox(
        width:
        double.infinity,

        height: 58,

        child:
        ElevatedButton(
          onPressed:
          _selectedTime ==
              null
              ? null
              : _continue,

          style:
          ElevatedButton
              .styleFrom(
            backgroundColor:
            const Color(
              0xFF111111,
            ),

            foregroundColor:
            Colors.white,

            disabledBackgroundColor:
            const Color(
              0xFFE4E4E4,
            ),

            disabledForegroundColor:
            Colors.white,

            elevation: 0,

            shape:
            RoundedRectangleBorder(
              borderRadius:
              BorderRadius
                  .circular(
                16,
              ),
            ),
          ),

          child:
          const Text(
            'Continuar',

            style: TextStyle(
              fontSize: 18,
              fontWeight:
              FontWeight.w600,
            ),
          ),
        ),
      ),
    );
  }

  // ============================================================
  // NAVEGAÇÃO
  // ============================================================

  void _continue() {
    if (_selectedTime == null) {
      return;
    }

    final selectedDay =
    _days[_selectedDay];

    final appointment =
    AppointmentData(
      date:
      '${selectedDay.day} de ${selectedDay.month} de ${selectedDay.date.year}',

      time: _selectedTime!,
      appointmentDateTime: DateTime(
        selectedDay.date.year,
        selectedDay.date.month,
        selectedDay.date.day,
        int.parse(_selectedTime!.split(':')[0]),
        int.parse(_selectedTime!.split(':')[1]),
      ),
    );

    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) =>
            ServicePage(
              appointment:
              appointment,
            ),
      ),
    );
  }

  // ============================================================
  // MEUS AGENDAMENTOS
  // ============================================================

  void _openMyAppointments() {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) =>
        const AppointmentsPage(),
      ),
    );
  }
}

class _DayOption {
  final DateTime date;
  final String label;
  final String day;
  final String month;

  const _DayOption({
    required this.date,
    required this.label,
    required this.day,
    required this.month,
  });
}
