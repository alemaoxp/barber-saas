import 'package:flutter/material.dart';

class CancelAppointmentDialog extends StatelessWidget {
  const CancelAppointmentDialog({
    super.key,
    required this.onConfirm,
  });

  final VoidCallback onConfirm;

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

  static const Color dangerRed =
  Color(0xFFE53945);

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
              const Offset(
                0,
                12,
              ),
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
                    'Cancelar agendamento',

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
                    Navigator.of(
                      context,
                    ).pop();
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
            // ÍCONE
            // ==================================================

            Container(
              width: 76,
              height: 76,

              decoration:
              BoxDecoration(
                color:
                blueBackground,

                shape:
                BoxShape.circle,

                border:
                Border.all(
                  color: primaryBlue
                      .withValues(
                    alpha: 0.20,
                  ),

                  width: 1,
                ),
              ),

              child: const Icon(
                Icons
                    .calendar_month_outlined,

                color:
                primaryBlue,

                size: 42,
              ),
            ),

            const SizedBox(
              height: 25,
            ),

            // ==================================================
            // PERGUNTA
            // ==================================================

            const Text(
              'Tem certeza que deseja\n'
                  'cancelar este agendamento?',

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
            // AVISO
            // ==================================================

            const Text(
              'Essa ação não poderá\n'
                  'ser desfeita.',

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
            // SIM, CANCELAR
            // ==================================================

            SizedBox(
              width:
              double.infinity,

              height: 56,

              child:
              ElevatedButton(
                onPressed:
                onConfirm,

                style:
                ElevatedButton.styleFrom(
                  backgroundColor:
                  dangerRed,

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
                  'SIM, CANCELAR',

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

            const SizedBox(
              height: 12,
            ),

            // ==================================================
            // VOLTAR
            // ==================================================

            SizedBox(
              width:
              double.infinity,

              height: 56,

              child:
              OutlinedButton(
                onPressed: () {
                  Navigator.of(
                    context,
                  ).pop();
                },

                style:
                OutlinedButton.styleFrom(
                  foregroundColor:
                  Colors.white,

                  backgroundColor:
                  Colors.transparent,

                  side:
                  BorderSide(
                    color: Colors.white
                        .withValues(
                      alpha: 0.35,
                    ),

                    width: 1.2,
                  ),

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
                Text(
                  'VOLTAR',

                  style:
                  TextStyle(
                    color: Colors.white
                        .withValues(
                      alpha: 0.90,
                    ),

                    fontSize: 15,

                    fontWeight:
                    FontWeight.w600,
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