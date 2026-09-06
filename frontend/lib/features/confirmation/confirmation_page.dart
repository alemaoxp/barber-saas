import 'dart:ui';

import 'package:flutter/material.dart';

import '../success/success_page.dart';
import '../../models/appointment_data.dart';

class ConfirmationPage extends StatelessWidget {
  final AppointmentData appointment;

  const ConfirmationPage({
    super.key,
    required this.appointment,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,

      body: Stack(
        children: [
          _buildBackground(),

          SafeArea(
            child: Column(
              children: [
                Expanded(
                  child: SingleChildScrollView(
                    physics:
                    const BouncingScrollPhysics(),

                    padding:
                    const EdgeInsets.fromLTRB(
                      24,
                      10,
                      24,
                      30,
                    ),

                    child: Column(
                      children: [
                        _buildHeader(context),

                        const SizedBox(
                          height: 28,
                        ),

                        _buildTitle(),

                        const SizedBox(
                          height: 28,
                        ),

                        _buildConfirmationCard(),
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

  // =========================================================================
  // BACKGROUND
  // =========================================================================

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
                Color(0xFFFFFFFF),
                Color(0xFFF8F9FA),
                Color(0xFFF2F4F5),
              ],
            ),
          ),
        ),

        Positioned(
          top: -100,
          left: -80,
          child: _buildGlow(
            size: 260,
            opacity: 0.28,
          ),
        ),

        Positioned(
          top: 80,
          right: -120,
          child: _buildGlow(
            size: 260,
            opacity: 0.20,
          ),
        ),

        Positioned(
          bottom: -120,
          left: -80,
          child: _buildGlow(
            size: 280,
            opacity: 0.18,
          ),
        ),

        Positioned.fill(
          child: BackdropFilter(
            filter: ImageFilter.blur(
              sigmaX: 35,
              sigmaY: 35,
            ),

            child: Container(
              color:
              Colors.white.withValues(
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
    required double opacity,
  }) {
    return ImageFiltered(
      imageFilter:
      ImageFilter.blur(
        sigmaX: 50,
        sigmaY: 50,
      ),

      child: Container(
        width: size,
        height: size,

        decoration:
        BoxDecoration(
          shape:
          BoxShape.circle,

          color:
          Colors.white.withValues(
            alpha: opacity,
          ),
        ),
      ),
    );
  }

  // =========================================================================
  // HEADER
  // =========================================================================

  Widget _buildHeader(
      BuildContext context,
      ) {
    return SizedBox(
      width: double.infinity,

      child: Stack(
        children: [
          Center(
            child: Column(
              children: [
                Image.asset(
                  'assets/images/branding/logo.png',

                  width: 78,
                  height: 78,

                  fit: BoxFit.contain,
                ),

                const SizedBox(
                  height: 1,
                ),

                Container(
                  width: 120,
                  height: 1,

                  color: Colors.black,
                ),
              ],
            ),
          ),

          Positioned(
            left: 0,
            top: 0,

            child: IconButton(
              onPressed: () {
                Navigator.of(
                  context,
                ).pop();
              },

              icon:
              const Icon(
                Icons
                    .arrow_back_ios_new_rounded,

                size: 24,

                color: Colors.black,
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

  // =========================================================================
  // TÍTULO
  // =========================================================================

  Widget _buildTitle() {
    return Column(
      children: [
        const Text(
          'Confirmação',

          textAlign:
          TextAlign.center,

          style: TextStyle(
            fontSize: 34,
            height: 1.05,
            fontWeight:
            FontWeight.w700,
            color: Colors.black,
            letterSpacing: -0.8,
          ),
        ),

        const SizedBox(
          height: 12,
        ),

        Text(
          'Confira os detalhes do seu\nagendamento',

          textAlign:
          TextAlign.center,

          style: TextStyle(
            fontSize: 17,
            height: 1.4,

            color:
            Colors.black.withValues(
              alpha: 0.62,
            ),
          ),
        ),
      ],
    );
  }

  // =========================================================================
  // CARD PRINCIPAL
  // =========================================================================

  Widget _buildConfirmationCard() {
    final servicesText =
    appointment.services.isEmpty
        ? 'Nenhum serviço'
        : appointment.services.join(' + ');

    return Container(
      width: double.infinity,

      padding:
      const EdgeInsets.fromLTRB(
        20,
        22,
        20,
        22,
      ),

      decoration:
      BoxDecoration(
        color:
        Colors.white.withValues(
          alpha: 0.58,
        ),

        borderRadius:
        BorderRadius.circular(30),

        border: Border.all(
          color:
          Colors.white.withValues(
            alpha: 0.95,
          ),

          width: 1.4,
        ),

        boxShadow: [
          BoxShadow(
            color:
            Colors.black.withValues(
              alpha: 0.10,
            ),

            blurRadius: 35,

            spreadRadius: 2,

            offset:
            const Offset(0, 18),
          ),

          BoxShadow(
            color:
            Colors.white.withValues(
              alpha: 0.90,
            ),

            blurRadius: 18,

            offset:
            const Offset(0, -5),
          ),
        ],
      ),

      child: Column(
        children: [
          // ================================================================
          // DATA
          // ================================================================

          _buildInfoRow(
            icon:
            Icons.calendar_month_outlined,

            label: 'Data',

            value:
            appointment.date,
          ),

          _buildDivider(),

          // ================================================================
          // HORÁRIO
          // ================================================================

          _buildInfoRow(
            icon:
            Icons.access_time_rounded,

            label: 'Horário',

            value:
            appointment.time,
          ),

          _buildDivider(),

          // ================================================================
          // SERVIÇO(S)
          // ================================================================

          _buildInfoRow(
            icon:
            Icons.content_cut_rounded,

            label:
            appointment.services.length > 1
                ? 'Serviços'
                : 'Serviço',

            value:
            servicesText,
          ),

          _buildDivider(),

          // ================================================================
          // NOME
          // ================================================================

          _buildInfoRow(
            icon:
            Icons.person_outline_rounded,

            label: 'Nome',

            value:
            appointment.name,
          ),
        ],
      ),
    );
  }

  // =========================================================================
  // LINHA DE INFORMAÇÃO
  // =========================================================================

  Widget _buildInfoRow({
    required IconData icon,
    required String label,
    required String value,
  }) {
    return Row(
      crossAxisAlignment:
      CrossAxisAlignment.center,

      children: [
        _buildInfoIcon(icon),

        const SizedBox(
          width: 16,
        ),

        Expanded(
          child: Column(
            crossAxisAlignment:
            CrossAxisAlignment.start,

            children: [
              Text(
                label,

                style: TextStyle(
                  fontSize: 14,
                  fontWeight:
                  FontWeight.w400,

                  color:
                  Colors.black.withValues(
                    alpha: 0.58,
                  ),
                ),
              ),

              const SizedBox(
                height: 6,
              ),

              Text(
                value,

                maxLines: 2,

                overflow:
                TextOverflow.ellipsis,

                style:
                const TextStyle(
                  fontSize: 17,
                  fontWeight:
                  FontWeight.w600,

                  color: Colors.black,

                  height: 1.2,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  // =========================================================================
  // ÍCONE
  // =========================================================================

  Widget _buildInfoIcon(
      IconData icon,
      ) {
    return Container(
      width: 58,
      height: 58,

      decoration:
      BoxDecoration(
        color:
        Colors.white.withValues(
          alpha: 0.72,
        ),

        borderRadius:
        BorderRadius.circular(18),

        border: Border.all(
          color:
          Colors.white.withValues(
            alpha: 0.95,
          ),

          width: 1.2,
        ),

        boxShadow: [
          BoxShadow(
            color:
            Colors.black.withValues(
              alpha: 0.10,
            ),

            blurRadius: 16,

            offset:
            const Offset(0, 7),
          ),

          BoxShadow(
            color:
            Colors.white.withValues(
              alpha: 0.95,
            ),

            blurRadius: 10,

            offset:
            const Offset(0, -4),
          ),
        ],
      ),

      child:
      Icon(
        icon,

        size: 29,

        color: Colors.black,
      ),
    );
  }

  // =========================================================================
  // DIVISOR
  // =========================================================================

  Widget _buildDivider() {
    return Padding(
      padding:
      const EdgeInsets.symmetric(
        vertical: 18,
      ),

      child: Container(
        height: 1,

        color:
        Colors.black.withValues(
          alpha: 0.08,
        ),
      ),
    );
  }

  // =========================================================================
  // ÁREA INFERIOR
  // =========================================================================

  Widget _buildBottomArea(
      BuildContext context,
      ) {
    return Container(
      padding:
      const EdgeInsets.fromLTRB(
        24,
        16,
        24,
        20,
      ),

      decoration:
      BoxDecoration(
        color:
        Colors.white.withValues(
          alpha: 0.60,
        ),

        border: Border(
          top: BorderSide(
            color:
            Colors.white.withValues(
              alpha: 0.90,
            ),
          ),
        ),

        boxShadow: [
          BoxShadow(
            color:
            Colors.black.withValues(
              alpha: 0.06,
            ),

            blurRadius: 25,

            offset:
            const Offset(0, -8),
          ),
        ],
      ),

      child:
      _buildConfirmButton(
        context,
      ),
    );
  }

  // =========================================================================
  // BOTÃO
  // =========================================================================

  Widget _buildConfirmButton(
      BuildContext context,
      ) {
    return Container(
      width: double.infinity,

      height: 62,

      decoration:
      BoxDecoration(
        borderRadius:
        BorderRadius.circular(19),

        boxShadow: [
          BoxShadow(
            color:
            Colors.black.withValues(
              alpha: 0.20,
            ),

            blurRadius: 22,

            spreadRadius: 1,

            offset:
            const Offset(0, 9),
          ),

          BoxShadow(
            color:
            Colors.white.withValues(
              alpha: 0.80,
            ),

            blurRadius: 8,

            offset:
            const Offset(0, -2),
          ),
        ],
      ),

      child:
      ElevatedButton(
        onPressed:
            () => _confirmAppointment(
          context,
        ),

        style:
        ElevatedButton.styleFrom(
          backgroundColor:
          const Color(0xFF111111),

          foregroundColor:
          Colors.white,

          elevation: 0,

          shape:
          RoundedRectangleBorder(
            borderRadius:
            BorderRadius.circular(
              19,
            ),

            side: BorderSide(
              color:
              Colors.white.withValues(
                alpha: 0.40,
              ),

              width: 1,
            ),
          ),
        ),

        child:
        const Text(
          'Confirmar agendamento',

          style: TextStyle(
            fontSize: 17,
            fontWeight:
            FontWeight.w600,
          ),
        ),
      ),
    );
  }

  // =========================================================================
  // CONFIRMAR
  // =========================================================================

  void _confirmAppointment(BuildContext context) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => SuccessPage(
          appointment: appointment,
        ),
      ),
    );
  }
}