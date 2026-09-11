import 'package:flutter/material.dart';

import '../../../../models/booking_service.dart';
import '../../../../services/barber_api.dart';

class ServiceSelectionBottomSheet extends StatefulWidget {
  const ServiceSelectionBottomSheet({
    super.key,
    required this.api,
    this.initialServices = const [],
  });

  final BarberApi api;
  final List<BookingService> initialServices;

  @override
  State<ServiceSelectionBottomSheet> createState() =>
      _ServiceSelectionBottomSheetState();
}

class _ServiceSelectionBottomSheetState
    extends State<ServiceSelectionBottomSheet> {
  static const _primary = Color(0xFF0D2742);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  List<BookingService> _services = const [];
  late final Set<String> _selectedIds;
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _selectedIds = widget.initialServices.map((service) => service.id).toSet();
    _loadServices();
  }

  Future<void> _loadServices() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final services = await widget.api.adminServices();
      if (!mounted) return;
      setState(() {
        _services = services;
        _loading = false;
      });
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _error = error is BarberApiException
            ? error.message
            : 'Não foi possível carregar serviços.';
        _loading = false;
      });
    }
  }

  List<BookingService> get _selectedServices => _services
      .where((service) => _selectedIds.contains(service.id))
      .toList(growable: false);

  double get _total => _selectedServices.fold<double>(
        0,
        (total, service) => total + service.price,
      );

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      top: false,
      child: SizedBox(
        height: MediaQuery.sizeOf(context).height * 0.82,
        child: Column(
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
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: 22),
              child: Text(
                'Selecionar serviços',
                style: TextStyle(
                  color: _primary,
                  fontSize: 24,
                  height: 1,
                  fontWeight: FontWeight.w900,
                ),
              ),
            ),
            const SizedBox(height: 18),
            Expanded(child: _content()),
            _Footer(
              total: _total,
              selectedCount: _selectedIds.length,
              onConfirm: _selectedIds.isEmpty
                  ? null
                  : () => Navigator.of(context).pop(_selectedServices),
            ),
          ],
        ),
      ),
    );
  }

  Widget _content() {
    if (_loading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_error != null) {
      return _StateMessage(
        title: 'Não foi possível carregar serviços.',
        subtitle: _error,
      );
    }
    if (_services.isEmpty) {
      return const _StateMessage(title: 'Nenhum serviço encontrado.');
    }

    return ListView.separated(
      padding: const EdgeInsets.fromLTRB(22, 0, 22, 22),
      itemCount: _services.length,
      separatorBuilder: (_, __) => const SizedBox(height: 8),
      itemBuilder: (context, index) {
        final service = _services[index];
        final selected = _selectedIds.contains(service.id);
        return _ServiceTile(
          service: service,
          selected: selected,
          onTap: () {
            setState(() {
              selected
                  ? _selectedIds.remove(service.id)
                  : _selectedIds.add(service.id);
            });
          },
        );
      },
    );
  }
}

class _ServiceTile extends StatelessWidget {
  const _ServiceTile({
    required this.service,
    required this.selected,
    required this.onTap,
  });

  final BookingService service;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: selected
          ? _ServiceSelectionBottomSheetState._primary.withValues(alpha: 0.06)
          : Colors.white,
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onTap,
        child: Container(
          constraints: const BoxConstraints(minHeight: 66),
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
          decoration: BoxDecoration(
            border: Border.all(
              color: selected
                  ? _ServiceSelectionBottomSheetState._primary
                  : _ServiceSelectionBottomSheetState._line,
            ),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      service.name,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: _ServiceSelectionBottomSheetState._primary,
                        fontSize: 15,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      _formatPrice(service.price),
                      style: const TextStyle(
                        color: _ServiceSelectionBottomSheetState._muted,
                        fontSize: 13,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
              ),
              Icon(
                selected
                    ? Icons.check_circle_rounded
                    : Icons.radio_button_unchecked_rounded,
                color: selected
                    ? _ServiceSelectionBottomSheetState._primary
                    : _ServiceSelectionBottomSheetState._muted,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _Footer extends StatelessWidget {
  const _Footer({
    required this.total,
    required this.selectedCount,
    required this.onConfirm,
  });

  final double total;
  final int selectedCount;
  final VoidCallback? onConfirm;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.fromLTRB(22, 14, 22, 18),
      decoration: const BoxDecoration(
        color: Colors.white,
        border: Border(
          top: BorderSide(color: _ServiceSelectionBottomSheetState._line),
        ),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '$selectedCount selecionado${selectedCount == 1 ? '' : 's'}',
                  style: const TextStyle(
                    color: _ServiceSelectionBottomSheetState._muted,
                    fontSize: 13,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 3),
                Text(
                  _formatPrice(total),
                  style: const TextStyle(
                    color: _ServiceSelectionBottomSheetState._primary,
                    fontSize: 18,
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ],
            ),
          ),
          SizedBox(
            height: 48,
            child: ElevatedButton(
              onPressed: onConfirm,
              style: ElevatedButton.styleFrom(
                backgroundColor: _ServiceSelectionBottomSheetState._primary,
                disabledBackgroundColor: _ServiceSelectionBottomSheetState
                    ._primary
                    .withValues(alpha: 0.36),
                foregroundColor: Colors.white,
                disabledForegroundColor: Colors.white.withValues(alpha: 0.8),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              child: const Text(
                'Confirmar',
                style: TextStyle(fontWeight: FontWeight.w800),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _StateMessage extends StatelessWidget {
  const _StateMessage({
    required this.title,
    this.subtitle,
  });

  final String title;
  final String? subtitle;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(28),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              title,
              textAlign: TextAlign.center,
              style: const TextStyle(
                color: _ServiceSelectionBottomSheetState._primary,
                fontSize: 16,
                fontWeight: FontWeight.w900,
              ),
            ),
            if (subtitle != null) ...[
              const SizedBox(height: 8),
              Text(
                subtitle!,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  color: _ServiceSelectionBottomSheetState._muted,
                  fontSize: 13,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

String _formatPrice(double value) {
  return 'R\$ ${value.toStringAsFixed(2).replaceAll('.', ',')}';
}
