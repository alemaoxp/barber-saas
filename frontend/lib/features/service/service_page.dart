import 'package:flutter/material.dart';
import '../data/data_page.dart';
import '../../models/appointment_data.dart';
import '../../models/booking_service.dart';
import '../../services/barber_api.dart';

class ServicePage extends StatefulWidget {
  final AppointmentData appointment;

  const ServicePage({
    super.key,
    required this.appointment,
  });

  @override
  State<ServicePage> createState() => _ServicePageState();
}

class _ServicePageState extends State<ServicePage> {
  // ============================================================
  // SERVIÇOS
  // ============================================================

  final BarberApi _api = BarberApi();
  List<BookingService> _services = const [];
  bool _loading = true;
  String? _error;

  // ============================================================
  // SERVIÇOS SELECIONADOS
  // ============================================================

  final Set<int> _selectedServices = {};

  @override
  void initState() {
    super.initState();
    _loadServices();
  }

  Future<void> _loadServices() async {
    setState(() { _loading = true; _error = null; });
    try {
      final services = await _api.services();
      if (mounted) setState(() => _services = services);
    } on BarberApiException catch (error) {
      if (mounted) setState(() => _error = error.message);
    } catch (_) {
      if (mounted) setState(() => _error = 'Não foi possível carregar os serviços.');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  // ============================================================
  // CÁLCULOS
  // ============================================================

  double get _total {
    double total = 0;

    for (final index in _selectedServices) {
      total += _services[index].price;
    }

    return total;
  }

  int get _selectedCount {
    return _selectedServices.length;
  }

  // ============================================================
  // BUILD
  // ============================================================

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: SafeArea(
        child: Column(
          children: [
            // ==================================================
            // CONTEÚDO PRINCIPAL
            // ==================================================

            Expanded(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(
                  24,
                  2,
                  24,
                  0,
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // ------------------------------------------
                    // TOPO
                    // ------------------------------------------

                    _buildTopArea(),

                    const SizedBox(height: 14),

                    // ------------------------------------------
                    // TÍTULO
                    // ------------------------------------------

                    const Text(
                      'Escolha o serviço',
                      style: TextStyle(
                        fontSize: 34,
                        height: 1.02,
                        fontWeight: FontWeight.w700,
                        color: Color(0xFF111111),
                        letterSpacing: -1.1,
                      ),
                    ),

                    const SizedBox(height: 8),

                    // ------------------------------------------
                    // SUBTÍTULO
                    // ------------------------------------------

                    const Text(
                      'Selecione um ou mais serviços para agendar.',
                      style: TextStyle(
                        fontSize: 17,
                        height: 1.25,
                        color: Color(0xFF666666),
                      ),
                    ),

                    const SizedBox(height: 16),

                    // ------------------------------------------
                    // LISTA DE SERVIÇOS
                    //
                    // SOMENTE ESTA ÁREA ROLA.
                    // ------------------------------------------

                    Expanded(
                      child: _buildServices(),
                    ),
                  ],
                ),
              ),
            ),

            // ==================================================
            // RESUMO + BOTÃO
            // ==================================================

            _buildBottomArea(),
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
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          // --------------------------------------------------
          // VOLTAR
          // --------------------------------------------------

          SizedBox(
            width: 48,
            height: 82,
            child: Material(
              color: Colors.transparent,
              child: InkWell(
                borderRadius: BorderRadius.circular(14),
                onTap: () {
                  Navigator.of(context).maybePop();
                },
                child: const Center(
                  child: Icon(
                    Icons.arrow_back_ios_new_rounded,
                    size: 24,
                    color: Color(0xFF111111),
                  ),
                ),
              ),
            ),
          ),

          // --------------------------------------------------
          // LOGO
          // --------------------------------------------------

          Expanded(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Image.asset(
                  'assets/images/branding/logo.png',
                  width: 76,
                  height: 76,
                  fit: BoxFit.contain,
                ),

                Transform.translate(
                  offset: const Offset(0, -2),
                  child: Container(
                    width: 120,
                    height: 1,
                    color: const Color(0xFF222222),
                  ),
                ),
              ],
            ),
          ),

          // --------------------------------------------------
          // ESPAÇO DIREITO
          // --------------------------------------------------

          const SizedBox(
            width: 48,
            height: 48,
          ),
        ],
      ),
    );
  }

  // ============================================================
  // LISTA DE SERVIÇOS
  // ============================================================

  Widget _buildServices() {
    if (_loading) return const Center(child: CircularProgressIndicator());
    if (_error != null) return Center(child: TextButton(onPressed: _loadServices, child: Text('Tentar novamente\n$_error', textAlign: TextAlign.center)));
    if (_services.isEmpty) return const Center(child: Text('Nenhum serviço disponível no momento.'));
    return Scrollbar(
      radius: const Radius.circular(10),
      thickness: 3,
      child: ListView.separated(
        physics: const BouncingScrollPhysics(),

        padding: const EdgeInsets.fromLTRB(
          0,
          1,
          5,
          12,
        ),

        itemCount: _services.length,

        separatorBuilder: (_, __) {
          return const SizedBox(height: 10);
        },

        itemBuilder: (context, index) {
          final service = _services[index];

          final selected =
          _selectedServices.contains(index);

          return GestureDetector(
            onTap: () {
              _toggleService(index);
            },
            child: _buildServiceCard(
              service: service,
              selected: selected,
            ),
          );
        },
      ),
    );
  }

  // ============================================================
  // CARD DO SERVIÇO
  // ============================================================

  Widget _buildServiceCard({
    required BookingService service,
    required bool selected,
  }) {
    return AnimatedContainer(
      duration: const Duration(milliseconds: 180),
      curve: Curves.easeOut,

      // Altura suficiente para TODOS os elementos internos.
      height: 88,

      padding: const EdgeInsets.symmetric(
        horizontal: 12,
        vertical: 8,
      ),

      decoration: BoxDecoration(
        color: selected
            ? const Color(0xFFF7F7F7)
            : Colors.white,

        borderRadius: BorderRadius.circular(18),

        border: Border.all(
          color: selected
              ? const Color(0xFF111111)
              : Colors.black.withValues(alpha: 0.07),
          width: selected ? 1.3 : 1,
        ),

        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(
              alpha: selected ? 0.08 : 0.05,
            ),
            blurRadius: selected ? 11 : 8,
            offset: const Offset(0, 4),
          ),

          if (!selected)
            const BoxShadow(
              color: Colors.white,
              blurRadius: 4,
              offset: Offset(-2, -2),
            ),
        ],
      ),

      child: Row(
        children: [
          // ==================================================
          // ÍCONE DO SERVIÇO
          // ==================================================

          Container(
            width: 52,
            height: 52,

            decoration: BoxDecoration(
              color: Colors.white,

              borderRadius: BorderRadius.circular(15),

              border: Border.all(
                color: Colors.black.withValues(
                  alpha: 0.05,
                ),
              ),

              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(
                    alpha: 0.04,
                  ),
                  blurRadius: 7,
                  offset: const Offset(0, 3),
                ),
              ],
            ),

            child: Icon(
              Icons.content_cut_rounded,
              size: 25,
              color: const Color(0xFF444444),
            ),
          ),

          const SizedBox(width: 12),

          // ==================================================
          // INFORMAÇÕES
          // ==================================================

          Expanded(
            child: SizedBox(
              height: 64,

              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.start,

                children: [
                  // ------------------------------------------
                  // NOME
                  // ------------------------------------------

                  Text(
                    service.name,

                    maxLines: 1,

                    overflow: TextOverflow.ellipsis,

                    style: const TextStyle(
                      fontSize: 16,
                      height: 1.15,
                      fontWeight: FontWeight.w700,
                      color: Color(0xFF111111),
                    ),
                  ),

                  const SizedBox(height: 3),

                  // ------------------------------------------
                  // DESCRIÇÃO
                  // ------------------------------------------

                  Text(
                    service.description,

                    maxLines: 1,

                    overflow: TextOverflow.ellipsis,

                    style: const TextStyle(
                      fontSize: 12,
                      height: 1.15,
                      color: Color(0xFF777777),
                    ),
                  ),

                  const SizedBox(height: 3),

                  // ------------------------------------------
                  // PREÇO
                  // ------------------------------------------

                  Text(
                    _formatPrice(service.price),

                    maxLines: 1,

                    style: const TextStyle(
                      fontSize: 14,
                      height: 1.15,
                      fontWeight: FontWeight.w700,
                      color: Color(0xFF111111),
                    ),
                  ),
                ],
              ),
            ),
          ),

          const SizedBox(width: 8),

          // ==================================================
          // BOLINHA DE SELEÇÃO
          //
          // MENOR AGORA: 24 x 24
          // ==================================================

          AnimatedContainer(
            duration: const Duration(milliseconds: 180),

            width: 24,
            height: 24,

            decoration: BoxDecoration(
              shape: BoxShape.circle,

              color: selected
                  ? const Color(0xFF111111)
                  : Colors.transparent,

              border: Border.all(
                color: selected
                    ? const Color(0xFF111111)
                    : const Color(0xFF333333),

                width: 1.8,
              ),
            ),

            child: selected
                ? const Icon(
              Icons.check_rounded,
              size: 14,
              color: Colors.white,
            )
                : null,
          ),
        ],
      ),
    );
  }

  // ============================================================
  // ÁREA INFERIOR
  // ============================================================

  Widget _buildBottomArea() {
    final hasSelection =
        _selectedServices.isNotEmpty;

    return Container(
      color: Colors.white,

      padding: const EdgeInsets.fromLTRB(
        24,
        8,
        24,
        22,
      ),

      child: Column(
        children: [
          // ==================================================
          // RESUMO
          // ==================================================

          AnimatedContainer(
            duration: const Duration(milliseconds: 180),

            width: double.infinity,

            padding: const EdgeInsets.symmetric(
              horizontal: 14,
              vertical: 10,
            ),

            decoration: BoxDecoration(
              color: const Color(0xFFF9F9F9),

              borderRadius: BorderRadius.circular(16),

              border: Border.all(
                color: Colors.black.withValues(
                  alpha: 0.055,
                ),
              ),

              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(
                    alpha: 0.04,
                  ),
                  blurRadius: 9,
                  offset: const Offset(0, 3),
                ),
              ],
            ),

            child: Row(
              children: [
                // ------------------------------------------
                // ÍCONE
                // ------------------------------------------

                Container(
                  width: 40,
                  height: 40,

                  decoration: BoxDecoration(
                    color: Colors.white,
                    borderRadius: BorderRadius.circular(12),
                  ),

                  child: const Icon(
                    Icons.shopping_bag_outlined,
                    size: 21,
                    color: Color(0xFF222222),
                  ),
                ),

                const SizedBox(width: 11),

                // ------------------------------------------
                // QUANTIDADE
                // ------------------------------------------

                Expanded(
                  child: Column(
                    crossAxisAlignment:
                    CrossAxisAlignment.start,

                    children: [
                      Text(
                        _selectedCount == 0
                            ? 'Nenhum serviço'
                            : '$_selectedCount ${_selectedCount == 1 ? 'serviço' : 'serviços'} selecionado${_selectedCount == 1 ? '' : 's'}',

                        maxLines: 1,

                        overflow: TextOverflow.ellipsis,

                        style: const TextStyle(
                          fontSize: 14,
                          height: 1.15,
                          fontWeight: FontWeight.w700,
                          color: Color(0xFF111111),
                        ),
                      ),

                      const SizedBox(height: 2),

                      Text(
                        hasSelection
                            ? 'Serviços adicionados'
                            : 'Selecione um ou mais serviços',

                        maxLines: 1,

                        overflow: TextOverflow.ellipsis,

                        style: const TextStyle(
                          fontSize: 11,
                          height: 1.15,
                          color: Color(0xFF777777),
                        ),
                      ),
                    ],
                  ),
                ),

                // ------------------------------------------
                // DIVISÓRIA
                // ------------------------------------------

                Container(
                  width: 1,
                  height: 34,
                  color: Colors.black.withValues(
                    alpha: 0.08,
                  ),
                ),

                const SizedBox(width: 14),

                // ------------------------------------------
                // TOTAL
                // ------------------------------------------

                Column(
                  crossAxisAlignment:
                  CrossAxisAlignment.start,

                  children: [
                    const Text(
                      'Total',

                      style: TextStyle(
                        fontSize: 11,
                        height: 1.1,
                        color: Color(0xFF777777),
                      ),
                    ),

                    const SizedBox(height: 2),

                    Text(
                      _formatPrice(_total),

                      style: const TextStyle(
                        fontSize: 18,
                        height: 1.1,
                        fontWeight: FontWeight.w700,
                        color: Color(0xFF111111),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),

          const SizedBox(height: 10),

          // ==================================================
          // CONTINUAR
          // ==================================================

          SizedBox(
            width: double.infinity,
            height: 58,

            child: ElevatedButton(
              onPressed: hasSelection
                  ? _continue
                  : null,

              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF111111),

                foregroundColor: Colors.white,

                disabledBackgroundColor:
                const Color(0xFFE4E4E4),

                disabledForegroundColor:
                const Color(0xFFAAAAAA),

                elevation: 0,

                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(16),
                ),
              ),

              child: const Text(
                'Continuar',

                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // ============================================================
  // SELECIONAR / DESSELECIONAR
  // ============================================================

  void _toggleService(int index) {
    setState(() {
      if (_selectedServices.contains(index)) {
        _selectedServices.remove(index);
      } else {
        _selectedServices.add(index);
      }
    });
  }

  // ============================================================
  // CONTINUAR
  // ============================================================

  void _continue() {
    if (_selectedServices.isEmpty) {
      return;
    }

    final selectedServices = _selectedServices
        .map(
          (index) => _services[index].name,
    )
        .toList();

    widget.appointment.services =
        selectedServices;

    widget.appointment.serviceIds = _selectedServices.map((index) => _services[index].id).toList();

    widget.appointment.total =
        _total;

    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => DataPage(
          appointment: widget.appointment,
        ),
      ),
    );
  }

  // ============================================================
  // FORMATAÇÃO DE PREÇO
  // ============================================================

  String _formatPrice(double value) {
    return 'R\$ ${value.toStringAsFixed(2).replaceAll('.', ',')}';
  }
}

// ============================================================
// MODELO DO SERVIÇO
// ============================================================
