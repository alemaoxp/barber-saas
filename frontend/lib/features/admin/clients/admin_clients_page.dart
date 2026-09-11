import 'package:flutter/material.dart';

import '../../../features/admin/daily_agenda/models/admin_customer_summary.dart';
import '../../../formatters/brazilian_phone_input_formatter.dart';
import '../../../services/barber_api.dart';
import '../services/admin_services_page.dart';

class AdminClientsPage extends StatefulWidget {
  const AdminClientsPage({super.key, this.api});

  final BarberApi? api;

  @override
  State<AdminClientsPage> createState() => _AdminClientsPageState();
}

class _AdminClientsPageState extends State<AdminClientsPage> {
  static const _primary = Color(0xFF0D2742);
  static const _background = Color(0xFFF6F8FA);
  static const _muted = Color(0xFF6C7886);
  static const _line = Color(0xFFE3E8EE);

  late final BarberApi _api;
  late Future<List<AdminCustomerSummary>> _clients;

  @override
  void initState() {
    super.initState();
    _api = widget.api ?? BarberApi();
    _clients = _api.adminCustomers();
  }

  void _reload() {
    setState(() {
      _clients = _api.adminCustomers();
    });
  }

  Future<void> _openNewClient() async {
    final created = await showModalBottomSheet<bool>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.white,
      barrierColor: Colors.black.withValues(alpha: 0.32),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (_) => _NewClientBottomSheet(api: _api),
    );
    if (created == true) _reload();
  }

  Future<void> _deleteClient(AdminCustomerSummary client) async {
    try {
      await _api.deleteAdminCustomer(client.id);
    } catch (error) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            error is BarberApiException
                ? error.message
                : 'Não foi possível excluir o cliente.',
          ),
        ),
      );
    } finally {
      if (mounted) _reload();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _background,
      bottomNavigationBar: _AdminNavigation(api: _api),
      body: SafeArea(
        child: Column(
          children: [
            const _Header(),
            Expanded(
              child: FutureBuilder<List<AdminCustomerSummary>>(
                future: _clients,
                builder: (context, snapshot) {
                  if (snapshot.connectionState != ConnectionState.done) {
                    return const Center(child: CircularProgressIndicator());
                  }
                  if (snapshot.hasError) {
                    return _MessageState(
                      title: 'Não foi possível carregar os clientes.',
                      subtitle: snapshot.error is BarberApiException
                          ? (snapshot.error as BarberApiException).message
                          : 'Tente novamente em instantes.',
                      action: TextButton(
                        onPressed: _reload,
                        child: const Text('Tentar novamente'),
                      ),
                    );
                  }
                  final clients = snapshot.requireData;
                  if (clients.isEmpty) {
                    return const _MessageState(
                      title: 'Nenhum cliente cadastrado.',
                    );
                  }
                  return ListView.separated(
                    padding: const EdgeInsets.fromLTRB(22, 22, 22, 96),
                    itemCount: clients.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 8),
                    itemBuilder: (context, index) {
                      final client = clients[index];
                      return Dismissible(
                        key: ValueKey('admin-client-${client.id}'),
                        direction: DismissDirection.endToStart,
                        background: const SizedBox.shrink(),
                        secondaryBackground: const _DeleteSwipeBackground(),
                        onDismissed: (_) => _deleteClient(client),
                        child: _ClientTile(client: client),
                      );
                    },
                  );
                },
              ),
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _openNewClient,
        backgroundColor: _primary,
        foregroundColor: Colors.white,
        icon: const Icon(Icons.add_rounded),
        label: const Text('Novo cliente'),
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(22, 14, 22, 20),
      color: Colors.white,
      child: const Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Center(
            child: Column(
              children: [
                Text(
                  'Jhow Cortes',
                  style: TextStyle(
                    color: _AdminClientsPageState._primary,
                    fontSize: 17,
                    height: 1,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                SizedBox(height: 4),
                Text(
                  'BARBEARIA',
                  style: TextStyle(
                    color: _AdminClientsPageState._muted,
                    fontSize: 7,
                    fontWeight: FontWeight.w800,
                    letterSpacing: 3,
                  ),
                ),
              ],
            ),
          ),
          SizedBox(height: 24),
          Text(
            'Clientes',
            style: TextStyle(
              color: _AdminClientsPageState._primary,
              fontSize: 31,
              height: 1,
              fontWeight: FontWeight.w800,
            ),
          ),
          SizedBox(height: 8),
          Text(
            'Gerencie seus clientes',
            style: TextStyle(
              color: Color(0xFF465260),
              fontSize: 15,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

class _ClientTile extends StatelessWidget {
  const _ClientTile({required this.client});

  final AdminCustomerSummary client;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: _AdminClientsPageState._line),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            client.name,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: const TextStyle(
              color: _AdminClientsPageState._primary,
              fontSize: 16,
              fontWeight: FontWeight.w900,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            client.phone,
            style: const TextStyle(
              color: _AdminClientsPageState._muted,
              fontSize: 14,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}

class _DeleteSwipeBackground extends StatelessWidget {
  const _DeleteSwipeBackground();

  @override
  Widget build(BuildContext context) {
    return Container(
      alignment: Alignment.centerRight,
      padding: const EdgeInsets.only(right: 18),
      decoration: BoxDecoration(
        color: Colors.redAccent,
        borderRadius: BorderRadius.circular(12),
      ),
      child: const Text(
        'Excluir',
        style: TextStyle(color: Colors.white, fontWeight: FontWeight.w900),
      ),
    );
  }
}

class _NewClientBottomSheet extends StatefulWidget {
  const _NewClientBottomSheet({required this.api});

  final BarberApi api;

  @override
  State<_NewClientBottomSheet> createState() => _NewClientBottomSheetState();
}

class _NewClientBottomSheetState extends State<_NewClientBottomSheet> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _phoneController = TextEditingController();
  bool _saving = false;
  String? _error;

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    super.dispose();
  }

  Future<void> _save() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() {
      _saving = true;
      _error = null;
    });
    try {
      await widget.api.createAdminCustomer(
        name: _nameController.text.trim(),
        phone: _phoneController.text.trim(),
      );
      if (!mounted) return;
      Navigator.of(context).pop(true);
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _error = error is BarberApiException
            ? error.message
            : 'Não foi possível salvar o cliente.';
        _saving = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      top: false,
      child: Padding(
        padding: EdgeInsets.fromLTRB(
          22,
          0,
          22,
          18 + MediaQuery.viewInsetsOf(context).bottom,
        ),
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
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
              const Text(
                'Novo cliente',
                style: TextStyle(
                  color: _AdminClientsPageState._primary,
                  fontSize: 24,
                  height: 1,
                  fontWeight: FontWeight.w900,
                ),
              ),
              const SizedBox(height: 20),
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(labelText: 'Nome'),
                textInputAction: TextInputAction.next,
                validator: (value) => value == null || value.trim().isEmpty
                    ? 'Informe o nome.'
                    : null,
              ),
              const SizedBox(height: 14),
              TextFormField(
                controller: _phoneController,
                decoration: const InputDecoration(labelText: 'Telefone'),
                keyboardType: TextInputType.phone,
                inputFormatters: [BrazilianPhoneInputFormatter()],
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Informe o telefone.';
                  }
                  return BrazilianPhoneInputFormatter.isValid(value)
                      ? null
                      : 'Informe um telefone válido.';
                },
              ),
              if (_error != null) ...[
                const SizedBox(height: 12),
                Text(
                  _error!,
                  style: const TextStyle(
                    color: Colors.redAccent,
                    fontWeight: FontWeight.w700,
                  ),
                ),
              ],
              const SizedBox(height: 24),
              SizedBox(
                width: double.infinity,
                height: 54,
                child: ElevatedButton(
                  onPressed: _saving ? null : _save,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: _AdminClientsPageState._primary,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  child: Text(_saving ? 'Salvando...' : 'Salvar cliente'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _MessageState extends StatelessWidget {
  const _MessageState({required this.title, this.subtitle, this.action});

  final String title;
  final String? subtitle;
  final Widget? action;

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
                color: _AdminClientsPageState._primary,
                fontSize: 17,
                fontWeight: FontWeight.w800,
              ),
            ),
            if (subtitle != null) ...[
              const SizedBox(height: 8),
              Text(
                subtitle!,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  color: _AdminClientsPageState._muted,
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ],
            if (action != null) ...[const SizedBox(height: 12), action!],
          ],
        ),
      ),
    );
  }
}

class _AdminNavigation extends StatelessWidget {
  const _AdminNavigation({required this.api});

  final BarberApi api;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: const BoxDecoration(
        color: Colors.white,
        border: Border(top: BorderSide(color: Color(0xFFE7ECF2))),
      ),
      child: SafeArea(
        top: false,
        minimum: const EdgeInsets.only(bottom: 4),
        child: SizedBox(
          height: 58,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _AgendaNavItem(
                label: 'Agenda',
                onTap: () => Navigator.of(context).maybePop(),
              ),
              _NavItem(
                icon: Icons.content_cut_rounded,
                label: 'Serviços',
                onTap: () {
                  Navigator.of(context).pushReplacement(
                    MaterialPageRoute<void>(
                      builder: (_) => AdminServicesPage(api: api),
                    ),
                  );
                },
              ),
              const _NavItem(
                icon: Icons.people_outline_rounded,
                label: 'Clientes',
                active: true,
              ),
              const _NavItem(icon: Icons.more_horiz_rounded, label: 'Mais'),
            ],
          ),
        ),
      ),
    );
  }
}

class _AgendaNavItem extends StatelessWidget {
  const _AgendaNavItem({required this.label, this.onTap});

  final String label;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: SizedBox(
        width: 68,
        height: 58,
        child: Center(
          child: Text(
            label,
            maxLines: 1,
            style: const TextStyle(
              color: _AdminClientsPageState._muted,
              fontSize: 11,
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  const _NavItem({
    required this.icon,
    required this.label,
    this.active = false,
    this.onTap,
  });

  final IconData icon;
  final String label;
  final bool active;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final color = active
        ? _AdminClientsPageState._primary
        : _AdminClientsPageState._muted;
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: SizedBox(
        width: 68,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              width: 30,
              height: 26,
              decoration: BoxDecoration(
                color: active ? const Color(0xFFE4F6FF) : Colors.transparent,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(icon, size: 20, color: color),
            ),
            const SizedBox(height: 2),
            Text(
              label,
              maxLines: 1,
              style: TextStyle(
                color: color,
                fontSize: 11,
                fontWeight: active ? FontWeight.w800 : FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
