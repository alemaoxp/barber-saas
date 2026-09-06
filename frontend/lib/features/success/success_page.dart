import 'dart:ui';

import '../appointments/appointments_page.dart';
import '../../services/appointment_store.dart';
import '../../models/appointment_data.dart';


import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

class SuccessPage extends StatelessWidget {
  final AppointmentData appointment;

  const SuccessPage({
    super.key,
    required this.appointment,
  });

  @override
  Widget build(BuildContext context) {
    AppointmentStore.instance.addIfNotExists(
      appointment,
    );

    return Scaffold(
      backgroundColor:
      const Color(0xFF020609),

      body: Stack(
        children: [
          _buildBackground(),

          SafeArea(
            child: Column(
              children: [
                Expanded(
                  child: Padding(
                    padding:
                    const EdgeInsets.fromLTRB(
                      24,
                      8,
                      24,
                      8,
                    ),

                    child: Column(
                      children: [
                        _buildHeader(context),

                        const SizedBox(
                          height: 0,
                        ),

                        _buildSuccessIcon(),

                        const SizedBox(
                          height: 12,
                        ),

                        _buildSuccessTitle(),

                        const SizedBox(
                          height: 16,
                        ),

                        Expanded(
                          child: Column(
                            children: [
                              _buildAppointmentCard(),

                              if (appointment
                                  .whatsappNotifications) ...[
                                const SizedBox(
                                  height: 10,
                                ),

                                _buildWhatsAppCard(),
                              ],
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),

                _buildBottomArea(context),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ===========================================================================
  // BACKGROUND
  // ===========================================================================

  Widget _buildBackground() {
    return Stack(
      children: [
        Container(
          decoration:
          const BoxDecoration(
            gradient:
            LinearGradient(
              begin:
              Alignment.topCenter,
              end:
              Alignment.bottomCenter,

              colors: [
                Color(0xFF020508),
                Color(0xFF030A10),
                Color(0xFF020609),
              ],
            ),
          ),
        ),

        Positioned(
          top: -120,
          left: 20,

          child: _buildGlow(
            size: 280,
            color:
            const Color(0xFF007BFF),
            opacity: 0.08,
          ),
        ),

        Positioned(
          top: 180,
          left: 80,

          child: _buildGlow(
            size: 250,
            color:
            const Color(0xFF19FF5A),
            opacity: 0.08,
          ),
        ),

        Positioned(
          bottom: -160,
          right: -100,

          child: _buildGlow(
            size: 320,
            color:
            const Color(0xFF0066FF),
            opacity: 0.05,
          ),
        ),

        Positioned.fill(
          child: BackdropFilter(
            filter:
            ImageFilter.blur(
              sigmaX: 30,
              sigmaY: 30,
            ),

            child: Container(
              color:
              Colors.black.withValues(
                alpha: 0.08,
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildGlow({
    required double size,
    required Color color,
    required double opacity,
  }) {
    return ImageFiltered(
      imageFilter:
      ImageFilter.blur(
        sigmaX: 55,
        sigmaY: 55,
      ),

      child: Container(
        width: size,
        height: size,

        decoration:
        BoxDecoration(
          shape:
          BoxShape.circle,

          color:
          color.withValues(
            alpha: opacity,
          ),
        ),
      ),
    );
  }

  // ===========================================================================
  // HEADER
  // ===========================================================================

  Widget _buildHeader(
      BuildContext context,
      ) {
    return SizedBox(
      width: double.infinity,
      height: 58,

      child: Stack(
        children: [
          Center(
            child: Column(
              children: [
                // LOGO ORIGINAL
              ],
            ),
          ),

          Positioned(
            left: 0,
            top: 4,

            child: IconButton(
              onPressed: () {
                Navigator.of(
                  context,
                ).pop();
              },

              icon: const Icon(
                Icons
                    .arrow_back_ios_new_rounded,

                size: 24,

                color:
                Colors.white,
              ),

              padding:
              EdgeInsets.zero,

              constraints:
              const BoxConstraints(),
            ),
          ),
        ],
      ),
    );
  }

  // ===========================================================================
  // ÍCONE DE SUCESSO
  // ===========================================================================

  Widget _buildSuccessIcon() {
    return Container(
      width: 92,
      height: 92,

      decoration:
      BoxDecoration(
        shape:
        BoxShape.circle,

        color:
        const Color(0xFF03100A),

        border:
        Border.all(
          color:
          const Color(0xFF31FF68),

          width: 2,
        ),

        boxShadow: [
          BoxShadow(
            color:
            const Color(0xFF20FF5A)
                .withValues(
              alpha: 0.42,
            ),

            blurRadius: 30,

            spreadRadius: 3,
          ),

          BoxShadow(
            color:
            const Color(0xFF20FF5A)
                .withValues(
              alpha: 0.16,
            ),

            blurRadius: 60,

            spreadRadius: 10,
          ),
        ],
      ),

      child: const Center(
        child: Icon(
          Icons.check_rounded,

          size: 56,

          color:
          Color(0xFF39FF68),
        ),
      ),
    );
  }

  // ===========================================================================
  // TÍTULO
  // ===========================================================================

  Widget _buildSuccessTitle() {
    return Column(
      children: [
        const Text(
          'Agendamento',

          textAlign:
          TextAlign.center,

          style: TextStyle(
            fontSize: 25,
            height: 1.0,
            fontWeight:
            FontWeight.w700,

            color:
            Colors.white,

            letterSpacing: -0.5,
          ),
        ),

        const SizedBox(
          height: 2,
        ),

        const Text(
          'realizado com sucesso!',

          textAlign:
          TextAlign.center,

          style: TextStyle(
            fontSize: 25,
            height: 1.0,
            fontWeight:
            FontWeight.w700,

            color:
            Color(0xFF32FF62),

            letterSpacing: -0.5,
          ),
        ),

        const SizedBox(
          height: 9,
        ),

        Text(
          'Seu horário está reservado.',

          textAlign:
          TextAlign.center,

          style: TextStyle(
            fontSize: 15,

            color:
            Colors.white.withValues(
              alpha: 0.70,
            ),
          ),
        ),
      ],
    );
  }

  // ===========================================================================
  // CARD DO AGENDAMENTO
  // ===========================================================================

  Widget _buildAppointmentCard() {
    return ClipRRect(
      borderRadius:
      BorderRadius.circular(
        23,
      ),

      child: BackdropFilter(
        filter:
        ImageFilter.blur(
          sigmaX: 18,
          sigmaY: 18,
        ),

        child: Container(
          width: double.infinity,

          padding:
          const EdgeInsets.fromLTRB(
            16,
            13,
            16,
            13,
          ),

          decoration:
          BoxDecoration(
            color:
            Colors.white.withValues(
              alpha: 0.055,
            ),

            borderRadius:
            BorderRadius.circular(
              23,
            ),

            border:
            Border.all(
              color:
              Colors.white.withValues(
                alpha: 0.22,
              ),

              width: 1.1,
            ),

            boxShadow: [
              BoxShadow(
                color:
                Colors.black.withValues(
                  alpha: 0.40,
                ),

                blurRadius: 28,

                offset:
                const Offset(
                  0,
                  12,
                ),
              ),

              BoxShadow(
                color:
                Colors.white.withValues(
                  alpha: 0.03,
                ),

                blurRadius: 8,

                offset:
                const Offset(
                  0,
                  -2,
                ),
              ),
            ],
          ),

          child: Column(
            children: [
              _buildInfoRow(
                icon:
                Icons
                    .calendar_month_outlined,

                label:
                'Data',

                value:
                appointment.date,
              ),

              _buildDarkDivider(),

              _buildInfoRow(
                icon:
                Icons
                    .access_time_rounded,

                label:
                'Horário',

                value:
                appointment.time,
              ),

              _buildDarkDivider(),

              _buildInfoRow(
                icon:
                Icons
                    .content_cut_rounded,

                label:
                appointment
                    .services
                    .length >
                    1
                    ? 'Serviços'
                    : 'Serviço',

                value:
                appointment
                    .services
                    .join(
                  ' + ',
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // ===========================================================================
  // WHATSAPP
  // ===========================================================================

  Widget _buildWhatsAppCard() {
    return ClipRRect(
      borderRadius:
      BorderRadius.circular(
        20,
      ),

      child: BackdropFilter(
        filter:
        ImageFilter.blur(
          sigmaX: 18,
          sigmaY: 18,
        ),

        child: Container(
          width: double.infinity,

          padding:
          const EdgeInsets.symmetric(
            horizontal: 16,
            vertical: 13,
          ),

          decoration:
          BoxDecoration(
            color:
            Colors.white.withValues(
              alpha: 0.055,
            ),

            borderRadius:
            BorderRadius.circular(
              20,
            ),

            border:
            Border.all(
              color:
              Colors.white.withValues(
                alpha: 0.18,
              ),

              width: 1,
            ),

            boxShadow: [
              BoxShadow(
                color:
                Colors.black.withValues(
                  alpha: 0.35,
                ),

                blurRadius: 22,

                offset:
                const Offset(
                  0,
                  10,
                ),
              ),
            ],
          ),

          child: Row(
            children: [
              Container(
                width: 48,
                height: 48,

                decoration:
                BoxDecoration(
                  color:
                  Colors.black.withValues(
                    alpha: 0.35,
                  ),

                  borderRadius:
                  BorderRadius.circular(
                    14,
                  ),

                  border:
                  Border.all(
                    color:
                    Colors.white.withValues(
                      alpha: 0.20,
                    ),
                  ),

                  boxShadow: [
                    BoxShadow(
                      color:
                      Colors.black.withValues(
                        alpha: 0.35,
                      ),

                      blurRadius: 10,

                      offset:
                      const Offset(
                        0,
                        4,
                      ),
                    ),
                  ],
                ),

                child: const Center(
                  child: FaIcon(
                    FontAwesomeIcons
                        .whatsapp,

                    size: 25,

                    color:
                    Color(0xFF25D366),
                  ),
                ),
              ),

              const SizedBox(
                width: 14,
              ),

              Expanded(
                child: Text(
                  'Você receberá um lembrete '
                      'pelo WhatsApp antes do '
                      'horário agendado.',

                  style: TextStyle(
                    fontSize: 14,

                    height: 1.3,

                    color: Colors.white
                        .withValues(
                      alpha: 0.78,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // ===========================================================================
  // INFO ROW
  // ===========================================================================

  Widget _buildInfoRow({
    required IconData icon,
    required String label,
    required String value,
  }) {
    return Row(
      crossAxisAlignment:
      CrossAxisAlignment.center,

      children: [
        Container(
          width: 50,
          height: 50,

          decoration:
          BoxDecoration(
            color:
            Colors.white.withValues(
              alpha: 0.07,
            ),

            borderRadius:
            BorderRadius.circular(
              15,
            ),

            border:
            Border.all(
              color:
              Colors.white.withValues(
                alpha: 0.20,
              ),
            ),

            boxShadow: [
              BoxShadow(
                color:
                Colors.black.withValues(
                  alpha: 0.35,
                ),

                blurRadius: 10,

                offset:
                const Offset(
                  0,
                  5,
                ),
              ),

              BoxShadow(
                color:
                Colors.white.withValues(
                  alpha: 0.04,
                ),

                blurRadius: 6,

                offset:
                const Offset(
                  0,
                  -2,
                ),
              ),
            ],
          ),

          child: Icon(
            icon,

            size: 26,

            color:
            Colors.white,
          ),
        ),

        const SizedBox(
          width: 14,
        ),

        Expanded(
          child: Column(
            crossAxisAlignment:
            CrossAxisAlignment.start,

            children: [
              Text(
                label,

                style: TextStyle(
                  fontSize: 13,

                  color: Colors.white
                      .withValues(
                    alpha: 0.55,
                  ),
                ),
              ),

              const SizedBox(
                height: 3,
              ),

              Text(
                value,

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
        ),
      ],
    );
  }

  // ===========================================================================
  // DIVISOR
  // ===========================================================================

  Widget _buildDarkDivider() {
    return Padding(
      padding:
      const EdgeInsets.symmetric(
        vertical: 11,
      ),

      child: Container(
        height: 1,

        color:
        Colors.white.withValues(
          alpha: 0.10,
        ),
      ),
    );
  }

  // ===========================================================================
  // ÁREA INFERIOR
  // ===========================================================================

  Widget _buildBottomArea(
      BuildContext context,
      ) {
    return Container(
      padding:
      const EdgeInsets.fromLTRB(
        24,
        10,
        24,
        15,
      ),

      decoration:
      BoxDecoration(
        color:
        Colors.black.withValues(
          alpha: 0.22,
        ),

        border:
        Border(
          top:
          BorderSide(
            color:
            Colors.white.withValues(
              alpha: 0.06,
            ),
          ),
        ),
      ),

      child: Column(
        children: [
          _buildHomeButton(
            context,
          ),

          const SizedBox(
            height: 9,
          ),

          GestureDetector(
            onTap: () {
              Navigator.of(
                context,
              ).push(
                MaterialPageRoute(
                  builder: (_) =>
                  const AppointmentsPage(),
                ),
              );
            },

            child: Text(
              'VER MEUS AGENDAMENTOS',

              style: TextStyle(
                fontSize: 13,

                fontWeight:
                FontWeight.w500,

                color:
                Colors.white.withValues(
                  alpha: 0.78,
                ),

                decoration:
                TextDecoration.underline,

                decorationColor:
                Colors.white.withValues(
                  alpha: 0.70,
                ),

                decorationThickness: 1,
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ===========================================================================
  // HOME
  // ===========================================================================

  Widget _buildHomeButton(
      BuildContext context,
      ) {
    return Container(
      width: double.infinity,
      height: 56,

      decoration:
      BoxDecoration(
        borderRadius:
        BorderRadius.circular(
          17,
        ),

        boxShadow: [
          BoxShadow(
            color:
            Colors.white.withValues(
              alpha: 0.20,
            ),

            blurRadius: 22,

            spreadRadius: 1,

            offset:
            const Offset(
              0,
              7,
            ),
          ),
        ],
      ),

      child: ElevatedButton(
        onPressed: () {
          Navigator.of(
            context,
          ).popUntil(
                (route) => route.isFirst,
          );
        },

        style:
        ElevatedButton.styleFrom(
          backgroundColor:
          Colors.white,

          foregroundColor:
          Colors.black,

          elevation: 0,

          shape:
          RoundedRectangleBorder(
            borderRadius:
            BorderRadius.circular(
              17,
            ),
          ),
        ),

        child: const Text(
          'VOLTAR PARA HOME',

          style: TextStyle(
            fontSize: 16,

            fontWeight:
            FontWeight.w700,

            letterSpacing: 0.1,
          ),
        ),
      ),
    );
  }
}