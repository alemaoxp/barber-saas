import 'package:flutter/material.dart';

class AppointmentCancelledDialog extends StatelessWidget {
  const AppointmentCancelledDialog({
    super.key,
    required this.onBackToHome,
  });

  final VoidCallback onBackToHome;

  // ============================================================
  // CORES DO BARBER SAAS
  // ============================================================

  static const Color cardColor =
  Color(0xFF0B1118);

  static const Color borderColor =
  Color(0xFF26313D);

  static const Color primaryBlue =
  Color(0xFF087CFF);

  static const Color blueBackground =
  Color(0xFF071A32);

  static const Color successGreen =
  Color(0xFF32D583);

  static const Color successBackground =
  Color(0xFF061B12);

  static const Color secondaryText =
  Color(0xFF9AA5B5);

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor:
      Colors.transparent,

      insetPadding:
      const EdgeInsets.symmetric(
        horizontal: 24,
      ),

      child: Container(
        width: double.infinity,

        padding:
        const EdgeInsets.fromLTRB(
          24,
          24,
          24,
          22,
        ),

        decoration:
        BoxDecoration(
          color: cardColor,

          borderRadius:
          BorderRadius.circular(
            24,
          ),

          border:
          Border.all(
            color: borderColor,
            width: 1.2,
          ),

          boxShadow: [
            BoxShadow(
              color:
              Colors.black.withValues(
                alpha: 0.45,
              ),

              blurRadius: 30,

              spreadRadius: 4,

              offset:
              const Offset(0, 12),
            ),
          ],
        ),

        child: Column(
          mainAxisSize:
          MainAxisSize.min,

          children: [
            // ==================================================
            // HEADER
            // ==================================================

            Row(
              children: [
                const Expanded(
                  child: Text(
                    'Agendamento cancelado',

                    style:
                    TextStyle(
                      color:
                      Colors.white,

                      fontSize: 21,

                      fontWeight:
                      FontWeight.w600,
                    ),
                  ),
                ),

                GestureDetector(
                  onTap: () {
                    onBackToHome();
                  },

                  child: Container(
                    width: 38,
                    height: 38,

                    decoration:
                    BoxDecoration(
                      color: Colors.white
                          .withValues(
                        alpha: 0.045,
                      ),

                      borderRadius:
                      BorderRadius
                          .circular(
                        11,
                      ),

                      border:
                      Border.all(
                        color: Colors.white
                            .withValues(
                          alpha: 0.08,
                        ),
                      ),
                    ),

                    child: Icon(
                      Icons
                          .close_rounded,

                      color: Colors.white
                          .withValues(
                        alpha: 0.85,
                      ),

                      size: 23,
                    ),
                  ),
                ),
              ],
            ),

            const SizedBox(
              height: 30,
            ),

            // ==================================================
            // ÍCONE DE SUCESSO
            // ==================================================

            Container(
              width: 76,
              height: 76,

              decoration:
              BoxDecoration(
                color:
                successBackground,

                shape:
                BoxShape.circle,

                border:
                Border.all(
                  color:
                  successGreen
                      .withValues(
                    alpha: 0.65,
                  ),

                  width: 1.5,
                ),

                boxShadow: [
                  BoxShadow(
                    color:
                    successGreen
                        .withValues(
                      alpha: 0.12,
                    ),

                    blurRadius: 24,

                    spreadRadius: 2,
                  ),
                ],
              ),

              child: const Icon(
                Icons
                    .check_rounded,

                color:
                successGreen,

                size: 42,
              ),
            ),

            const SizedBox(
              height: 25,
            ),

            // ==================================================
            // TÍTULO
            // ==================================================

            const Text(
              'Agendamento cancelado\n'
                  'com sucesso!',

              textAlign:
              TextAlign.center,

              style:
              TextStyle(
                color:
                Colors.white,

                fontSize: 20,

                height: 1.28,

                fontWeight:
                FontWeight.w600,
              ),
            ),

            const SizedBox(
              height: 16,
            ),

            // ==================================================
            // DESCRIÇÃO
            // ==================================================

            const Text(
              'Seu horário foi cancelado\n'
                  'e não está mais reservado.',

              textAlign:
              TextAlign.center,

              style:
              TextStyle(
                color:
                secondaryText,

                fontSize: 16,

                height: 1.4,

                fontWeight:
                FontWeight.w400,
              ),
            ),

            const SizedBox(
              height: 28,
            ),

            // ==================================================
            // VOLTAR PARA HOME
            // ==================================================

            SizedBox(
              width:
              double.infinity,

              height: 56,

              child:
              ElevatedButton(
                onPressed:
                onBackToHome,

                style:
                ElevatedButton.styleFrom(
                  backgroundColor:
                  primaryBlue,

                  foregroundColor:
                  Colors.white,

                  elevation: 0,

                  shadowColor:
                  Colors.transparent,

                  shape:
                  RoundedRectangleBorder(
                    borderRadius:
                    BorderRadius
                        .circular(
                      13,
                    ),
                  ),
                ),

                child:
                const Text(
                  'VOLTAR PARA HOME',

                  style:
                  TextStyle(
                    fontSize: 15,

                    fontWeight:
                    FontWeight.w700,

                    letterSpacing:
                    0.2,
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