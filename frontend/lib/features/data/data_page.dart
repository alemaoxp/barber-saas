import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../models/appointment_data.dart';
import '../confirmation/confirmation_page.dart';

class DataPage extends StatefulWidget {
  final AppointmentData appointment;

  const DataPage({
    super.key,
    required this.appointment,
  });

  @override
  State<DataPage> createState() => _DataPageState();
}

class _DataPageState extends State<DataPage> {
  late final TextEditingController _nameController;
  late final TextEditingController _phoneController;

  bool _whatsappNotifications = true;

  bool _loadingSavedData = true;

  static const String _nameKey =
      'customer_name';

  static const String _phoneKey =
      'customer_phone';

  static const String _whatsappKey =
      'customer_whatsapp_notifications';

  @override
  void initState() {
    super.initState();

    _nameController =
        TextEditingController();

    _phoneController =
        TextEditingController();

    _loadSavedData();
  }

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();

    super.dispose();
  }

  // =========================================================================
  // CARREGAR DADOS SALVOS
  // =========================================================================

  Future<void> _loadSavedData() async {
    try {
      final prefs =
      await SharedPreferences.getInstance();

      final savedName =
      prefs.getString(_nameKey);

      final savedPhone =
      prefs.getString(_phoneKey);

      final savedWhatsapp =
      prefs.getBool(_whatsappKey);

      if (!mounted) {
        return;
      }

      setState(() {
        if (savedName != null &&
            savedName.isNotEmpty) {
          _nameController.text =
              savedName;
        }

        if (savedPhone != null &&
            savedPhone.isNotEmpty) {
          _phoneController.text =
              savedPhone;
        }

        if (savedWhatsapp != null) {
          _whatsappNotifications =
              savedWhatsapp;
        }

        _loadingSavedData = false;
      });
    } catch (_) {
      if (!mounted) {
        return;
      }

      setState(() {
        _loadingSavedData = false;
      });
    }
  }

  // =========================================================================
  // SALVAR DADOS
  // =========================================================================

  Future<void> _saveCustomerData() async {
    try {
      final prefs =
      await SharedPreferences.getInstance();

      await prefs.setString(
        _nameKey,
        _nameController.text.trim(),
      );

      await prefs.setString(
        _phoneKey,
        _phoneController.text.trim(),
      );

      await prefs.setBool(
        _whatsappKey,
        _whatsappNotifications,
      );
    } catch (_) {
      // O agendamento continua normalmente
      // mesmo se o armazenamento local falhar.
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor:
      const Color(0xFF050505),

      body: Stack(
        children: [
          _buildBackground(),

          SafeArea(
            child: Column(
              children: [
                Expanded(
                  child:
                  SingleChildScrollView(
                    physics:
                    const BouncingScrollPhysics(),

                    padding:
                    const EdgeInsets.fromLTRB(
                      24,
                      8,
                      24,
                      20,
                    ),

                    child: Column(
                      crossAxisAlignment:
                      CrossAxisAlignment.start,

                      children: [
                        _buildHeader(),

                        const SizedBox(
                          height: 28,
                        ),

                        _buildTitle(),

                        const SizedBox(
                          height: 24,
                        ),

                        _buildDataCard(),

                        const SizedBox(
                          height: 20,
                        ),

                        _buildWhatsAppOption(),
                      ],
                    ),
                  ),
                ),

                _buildContinueButton(),
              ],
            ),
          ),

          if (_loadingSavedData)
            Positioned.fill(
              child: Container(
                color: Colors.black
                    .withValues(
                  alpha: 0.18,
                ),

                child:
                const Center(
                  child:
                  SizedBox(
                    width: 22,
                    height: 22,
                    child:
                    CircularProgressIndicator(
                      strokeWidth: 2,
                      color:
                      Colors.white,
                    ),
                  ),
                ),
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
                Color(0xFF171717),
                Color(0xFF090909),
                Color(0xFF020202),
              ],
            ),
          ),
        ),

        Positioned(
          top: -100,
          left: -80,

          child: _buildGlow(
            size: 260,
            opacity: 0.10,
          ),
        ),

        Positioned(
          top: 260,
          right: -100,

          child: _buildGlow(
            size: 240,
            opacity: 0.07,
          ),
        ),

        Positioned(
          bottom: -100,
          left: -60,

          child: _buildGlow(
            size: 250,
            opacity: 0.08,
          ),
        ),

        Positioned.fill(
          child: BackdropFilter(
            filter:
            ImageFilter.blur(
              sigmaX: 25,
              sigmaY: 25,
            ),

            child: Container(
              color:
              Colors.black.withValues(
                alpha: 0.12,
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
        sigmaX: 45,
        sigmaY: 45,
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

  Widget _buildHeader() {
    return SizedBox(
      width: double.infinity,
      height: 90,

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

                Container(
                  width: 120,
                  height: 1,

                  color:
                  Colors.white.withValues(
                    alpha: 0.75,
                  ),
                ),
              ],
            ),
          ),

          Positioned(
            left: 0,
            top: 22,

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

                color: Colors.white,
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
      crossAxisAlignment:
      CrossAxisAlignment.start,

      children: [
        const Text(
          'Seus dados',

          style:
          TextStyle(
            fontSize: 34,
            height: 1.05,
            fontWeight:
            FontWeight.w700,
            color: Colors.white,
            letterSpacing: -0.8,
          ),
        ),

        const SizedBox(
          height: 9,
        ),

        Text(
          'Preencha seus dados para finalizar o agendamento.',

          style:
          TextStyle(
            fontSize: 17,
            height: 1.35,

            color:
            Colors.white.withValues(
              alpha: 0.70,
            ),
          ),
        ),
      ],
    );
  }

  // =========================================================================
  // ÁREA DOS DADOS
  // =========================================================================

  Widget _buildDataCard() {
    return Container(
      width: double.infinity,

      padding:
      const EdgeInsets.fromLTRB(
        12,
        16,
        12,
        16,
      ),

      decoration:
      BoxDecoration(
        color:
        Colors.white.withValues(
          alpha: 0.035,
        ),

        borderRadius:
        BorderRadius.circular(26),

        border: Border.all(
          color:
          Colors.white.withValues(
            alpha: 0.08,
          ),
        ),

        boxShadow: [
          BoxShadow(
            color:
            Colors.black.withValues(
              alpha: 0.30,
            ),

            blurRadius: 25,

            offset:
            const Offset(0, 12),
          ),
        ],
      ),

      child: Column(
        children: [
          _buildNameField(),

          const SizedBox(
            height: 18,
          ),

          Container(
            height: 1,

            margin:
            const EdgeInsets.symmetric(
              horizontal: 4,
            ),

            color:
            Colors.white.withValues(
              alpha: 0.07,
            ),
          ),

          const SizedBox(
            height: 18,
          ),

          _buildPhoneField(),
        ],
      ),
    );
  }

  // =========================================================================
  // NOME
  // =========================================================================

  Widget _buildNameField() {
    return Column(
      crossAxisAlignment:
      CrossAxisAlignment.start,

      children: [
        const Padding(
          padding:
          EdgeInsets.only(
            left: 6,
          ),

          child: Text(
            'Nome completo',

            style:
            TextStyle(
              fontSize: 14,
              fontWeight:
              FontWeight.w500,
              color:
              Color(0xFFD0D0D0),
            ),
          ),
        ),

        const SizedBox(
          height: 8,
        ),

        Row(
          children: [
            _buildIconBox(
              child:
              const Icon(
                Icons.person_rounded,

                size: 29,

                color:
                Color(0xFF9E2525),
              ),
            ),

            const SizedBox(
              width: 12,
            ),

            Expanded(
              child:
              _buildTextFieldContainer(
                child:
                TextField(
                  controller:
                  _nameController,

                  textInputAction:
                  TextInputAction.next,

                  style:
                  const TextStyle(
                    fontSize: 17,
                    fontWeight:
                    FontWeight.w600,
                    color:
                    Color(0xFF151515),
                  ),

                  decoration:
                  const InputDecoration(
                    border:
                    InputBorder.none,

                    isDense: true,

                    contentPadding:
                    EdgeInsets.zero,

                    hintText:
                    'Digite seu nome completo',

                    hintStyle:
                    TextStyle(
                      fontSize: 16,
                      fontWeight:
                      FontWeight.w500,
                      color:
                      Color(0xFF777777),
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  // =========================================================================
  // WHATSAPP
  // =========================================================================

  Widget _buildPhoneField() {
    return Column(
      crossAxisAlignment:
      CrossAxisAlignment.start,

      children: [
        const Padding(
          padding:
          EdgeInsets.only(
            left: 6,
          ),

          child: Text(
            'WhatsApp',

            style:
            TextStyle(
              fontSize: 14,
              fontWeight:
              FontWeight.w500,
              color:
              Color(0xFFD0D0D0),
            ),
          ),
        ),

        const SizedBox(
          height: 8,
        ),

        Row(
          children: [
            _buildWhatsAppIconBox(),

            const SizedBox(
              width: 12,
            ),

            Expanded(
              child:
              _buildTextFieldContainer(
                child:
                TextField(
                  controller:
                  _phoneController,

                  keyboardType:
                  TextInputType.phone,

                  style:
                  const TextStyle(
                    fontSize: 17,
                    fontWeight:
                    FontWeight.w600,
                    color:
                    Color(0xFF151515),
                  ),

                  decoration:
                  const InputDecoration(
                    border:
                    InputBorder.none,

                    isDense: true,

                    contentPadding:
                    EdgeInsets.zero,

                    hintText:
                    '(11) 99999-9999',

                    hintStyle:
                    TextStyle(
                      fontSize: 16,
                      fontWeight:
                      FontWeight.w500,
                      color:
                      Color(0xFF777777),
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  // =========================================================================
  // CAIXA DO ÍCONE
  // =========================================================================

  Widget _buildIconBox({
    required Widget child,
  }) {
    return Container(
      width: 58,
      height: 58,

      decoration:
      BoxDecoration(
        color: Colors.white,

        borderRadius:
        BorderRadius.circular(16),

        border: Border.all(
          color:
          const Color(0xFFF4F4F4),
        ),

        boxShadow: [
          BoxShadow(
            color:
            Colors.black.withValues(
              alpha: 0.28,
            ),

            blurRadius: 12,

            offset:
            const Offset(3, 5),
          ),

          const BoxShadow(
            color:
            Color(0xFFFFFFFF),

            blurRadius: 7,

            offset:
            Offset(-3, -3),
          ),
        ],
      ),

      child:
      Center(
        child: child,
      ),
    );
  }

  // =========================================================================
  // WHATSAPP ICON BOX
  // =========================================================================

  Widget _buildWhatsAppIconBox() {
    return Container(
      width: 58,
      height: 58,

      decoration:
      BoxDecoration(
        color: Colors.white,

        borderRadius:
        BorderRadius.circular(16),

        border: Border.all(
          color:
          const Color(0xFFF4F4F4),
        ),

        boxShadow: [
          BoxShadow(
            color:
            Colors.black.withValues(
              alpha: 0.28,
            ),

            blurRadius: 12,

            offset:
            const Offset(3, 5),
          ),

          const BoxShadow(
            color:
            Color(0xFFFFFFFF),

            blurRadius: 7,

            offset:
            Offset(-3, -3),
          ),
        ],
      ),

      child:
      const Center(
        child:
        FaIcon(
          FontAwesomeIcons.whatsapp,

          size: 30,

          color:
          Color(0xFF25D366),
        ),
      ),
    );
  }

  // =========================================================================
  // CAMPO BRANCO
  // =========================================================================

  Widget _buildTextFieldContainer({
    required Widget child,
  }) {
    return Container(
      height: 58,

      padding:
      const EdgeInsets.symmetric(
        horizontal: 16,
      ),

      decoration:
      BoxDecoration(
        color:
        const Color(0xFFF7F7F7),

        borderRadius:
        BorderRadius.circular(16),

        border: Border.all(
          color:
          const Color(0xFFEAEAEA),
        ),

        boxShadow: [
          BoxShadow(
            color:
            Colors.black.withValues(
              alpha: 0.20,
            ),

            blurRadius: 10,

            offset:
            const Offset(3, 4),
          ),

          const BoxShadow(
            color:
            Colors.white,

            blurRadius: 7,

            offset:
            Offset(-3, -3),
          ),
        ],
      ),

      child:
      Center(
        child: child,
      ),
    );
  }

  // =========================================================================
  // LEMBRETES WHATSAPP
  // =========================================================================

  Widget _buildWhatsAppOption() {
    return GestureDetector(
      onTap: () {
        setState(() {
          _whatsappNotifications =
          !_whatsappNotifications;
        });
      },

      child: Row(
        crossAxisAlignment:
        CrossAxisAlignment.center,

        children: [
          Expanded(
            child: Text(
              'Quero receber lembretes e\nconfirmações pelo WhatsApp.',

              style:
              TextStyle(
                fontSize: 15,
                height: 1.35,

                color:
                Colors.white.withValues(
                  alpha: 0.90,
                ),
              ),
            ),
          ),

          const SizedBox(
            width: 12,
          ),

          _buildReminderSwitch(),
        ],
      ),
    );
  }

  // =========================================================================
  // SWITCH
  // =========================================================================

  Widget _buildReminderSwitch() {
    return AnimatedContainer(
      duration:
      const Duration(
        milliseconds: 220,
      ),

      curve:
      Curves.easeOut,

      width: 58,
      height: 32,

      padding:
      const EdgeInsets.all(3),

      decoration:
      BoxDecoration(
        color: _whatsappNotifications
            ? const Color(0xFF25D366)
            : const Color(0xFFE9E9E9),

        borderRadius:
        BorderRadius.circular(30),

        boxShadow: [
          BoxShadow(
            color:
            Colors.black.withValues(
              alpha: 0.35,
            ),

            blurRadius: 8,

            offset:
            const Offset(2, 4),
          ),

          BoxShadow(
            color:
            Colors.white.withValues(
              alpha: 0.10,
            ),

            blurRadius: 5,

            offset:
            const Offset(-2, -2),
          ),
        ],
      ),

      child:
      AnimatedAlign(
        duration:
        const Duration(
          milliseconds: 220,
        ),

        curve:
        Curves.easeOut,

        alignment:
        _whatsappNotifications
            ? Alignment.centerRight
            : Alignment.centerLeft,

        child:
        Container(
          width: 26,
          height: 26,

          decoration:
          BoxDecoration(
            shape:
            BoxShape.circle,

            color:
            Colors.white,

            boxShadow: [
              BoxShadow(
                color:
                Colors.black.withValues(
                  alpha: 0.18,
                ),

                blurRadius: 6,

                offset:
                const Offset(1, 2),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // =========================================================================
  // BOTÃO AGENDAR
  // =========================================================================

  Widget _buildContinueButton() {
    return Container(
      padding:
      const EdgeInsets.fromLTRB(
        24,
        12,
        24,
        18,
      ),

      decoration:
      BoxDecoration(
        color:
        Colors.black.withValues(
          alpha: 0.40,
        ),

        border:
        Border(
          top: BorderSide(
            color:
            Colors.white.withValues(
              alpha: 0.07,
            ),
          ),
        ),
      ),

      child:
      SizedBox(
        width:
        double.infinity,

        height: 60,

        child:
        ElevatedButton(
          onPressed:
          _continue,

          style:
          ElevatedButton.styleFrom(
            backgroundColor:
            Colors.white,

            foregroundColor:
            const Color(0xFF111111),

            elevation: 0,

            shape:
            RoundedRectangleBorder(
              borderRadius:
              BorderRadius.circular(
                18,
              ),
            ),

            shadowColor:
            Colors.white,
          ),

          child:
          const Text(
            'Agendar',

            style:
            TextStyle(
              fontSize: 18,
              fontWeight:
              FontWeight.w700,
            ),
          ),
        ),
      ),
    );
  }

  // =========================================================================
  // AGENDAR
  // =========================================================================

  Future<void> _continue() async {
    FocusScope.of(context).unfocus();

    final name =
    _nameController.text.trim();

    final phone =
    _phoneController.text.trim();

    if (name.isEmpty ||
        phone.isEmpty) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(
        const SnackBar(
          content: Text(
            'Preencha seu nome e WhatsApp.',
          ),

          behavior:
          SnackBarBehavior.floating,
        ),
      );

      return;
    }

    // Salva os dados para os próximos agendamentos.
    await _saveCustomerData();

    // Atualiza o agendamento atual.
    widget.appointment.name =
        name;

    widget.appointment.phone =
        phone;

    widget.appointment
        .whatsappNotifications =
        _whatsappNotifications;

    if (!mounted) {
      return;
    }

    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) =>
            ConfirmationPage(
              appointment:
              widget.appointment,
            ),
      ),
    );
  }
}