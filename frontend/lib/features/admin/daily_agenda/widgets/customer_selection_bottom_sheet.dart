import 'dart:async';

import 'package:flutter/material.dart';

import '../../../../services/barber_api.dart';
import '../models/admin_customer_summary.dart';

class CustomerSelectionBottomSheet extends StatefulWidget {
  const CustomerSelectionBottomSheet({
    super.key,
    required this.api,
  });

  final BarberApi api;

  @override
  State<CustomerSelectionBottomSheet> createState() =>
      _CustomerSelectionBottomSheetState();
}

class _CustomerSelectionBottomSheetState
    extends State<CustomerSelectionBottomSheet> {
  static const _primary = Color(0xFF0D2742);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);
  static const _background = Color(0xFFF6F8FA);

  final _searchController = TextEditingController();
  Timer? _debounce;
  List<AdminCustomerSummary> _customers = const [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadCustomers();
  }

  @override
  void dispose() {
    _debounce?.cancel();
    _searchController.dispose();
    super.dispose();
  }

  void _onSearchChanged(String value) {
    _debounce?.cancel();
    _debounce = Timer(
      const Duration(milliseconds: 350),
      () => _loadCustomers(query: value),
    );
  }

  Future<void> _loadCustomers({String? query}) async {
    setState(() {
      _loading = true;
      _error = null;
    });

    try {
      final customers = await widget.api.adminCustomers(query: query);
      if (!mounted) return;
      setState(() {
        _customers = customers;
        _loading = false;
      });
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _error = error is BarberApiException
            ? error.message
            : 'Não foi possível carregar clientes.';
        _loading = false;
      });
    }
  }

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
                'Selecionar cliente',
                style: TextStyle(
                  color: _primary,
                  fontSize: 24,
                  height: 1,
                  fontWeight: FontWeight.w900,
                ),
              ),
            ),
            const SizedBox(height: 18),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 22),
              child: TextField(
                key: const Key('admin_customer_search'),
                controller: _searchController,
                onChanged: _onSearchChanged,
                textInputAction: TextInputAction.search,
                decoration: InputDecoration(
                  hintText: 'Buscar por nome ou telefone',
                  prefixIcon: const Icon(Icons.search_rounded),
                  filled: true,
                  fillColor: _background,
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 14,
                    vertical: 14,
                  ),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: const BorderSide(color: _line),
                  ),
                  enabledBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: const BorderSide(color: _line),
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: const BorderSide(color: _primary),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 12),
            Expanded(child: _content()),
          ],
        ),
      ),
    );
  }

  Widget _content() {
    if (_loading && _customers.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }
    if (_error != null) {
      return _StateMessage(
        title: 'Não foi possível carregar clientes.',
        subtitle: _error,
      );
    }
    if (_customers.isEmpty) {
      return const _StateMessage(title: 'Nenhum cliente encontrado.');
    }

    return Stack(
      children: [
        ListView.separated(
          padding: const EdgeInsets.fromLTRB(22, 0, 22, 22),
          itemCount: _customers.length,
          separatorBuilder: (_, __) => const SizedBox(height: 8),
          itemBuilder: (context, index) {
            final customer = _customers[index];
            return _CustomerTile(
              customer: customer,
              onTap: () => Navigator.of(context).pop(customer),
            );
          },
        ),
        if (_loading)
          const Positioned(
            top: 0,
            left: 0,
            right: 0,
            child: LinearProgressIndicator(minHeight: 2),
          ),
      ],
    );
  }
}

class _CustomerTile extends StatelessWidget {
  const _CustomerTile({
    required this.customer,
    required this.onTap,
  });

  final AdminCustomerSummary customer;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.white,
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onTap,
        child: Container(
          constraints: const BoxConstraints(minHeight: 66),
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
          decoration: BoxDecoration(
            border: Border.all(color: _CustomerSelectionBottomSheetState._line),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              CircleAvatar(
                radius: 20,
                backgroundColor:
                    _CustomerSelectionBottomSheetState._primary,
                child: Text(
                  _initials(customer.name),
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 13,
                    fontWeight: FontWeight.w900,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      customer.name,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: _CustomerSelectionBottomSheetState._primary,
                        fontSize: 15,
                        fontWeight: FontWeight.w900,
                      ),
                    ),
                    const SizedBox(height: 3),
                    Text(
                      customer.phone,
                      style: const TextStyle(
                        color: _CustomerSelectionBottomSheetState._muted,
                        fontSize: 13,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
              const Icon(
                Icons.chevron_right_rounded,
                color: _CustomerSelectionBottomSheetState._primary,
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _initials(String name) {
    final parts = name
        .trim()
        .split(RegExp(r'\s+'))
        .where((part) => part.isNotEmpty)
        .toList();
    if (parts.isEmpty) return '?';
    return parts.take(2).map((part) => part[0].toUpperCase()).join();
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
                color: _CustomerSelectionBottomSheetState._primary,
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
                  color: _CustomerSelectionBottomSheetState._muted,
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
