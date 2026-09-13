import 'package:flutter/material.dart';

import '../../../services/barber_api.dart';
import '../home/admin_home_page.dart';
import 'admin_auth.dart';
import 'admin_login_page.dart';

class AdminGate extends StatefulWidget {
  AdminGate({super.key, AdminAuthStore? store, this.api})
      : store = store ?? AdminAuthStore();

  final AdminAuthStore store;
  final BarberApi? api;

  @override
  State<AdminGate> createState() => _AdminGateState();
}

class _AdminGateState extends State<AdminGate> {
  late Future<_AdminSessionState> _session;

  @override
  void initState() {
    super.initState();
    _session = _restore();
  }

  Future<_AdminSessionState> _restore() async {
    final token = await widget.store.token();
    if (token == null || token.isEmpty) return _AdminSessionState.signedOut;
    try {
      await _api().currentAdmin();
      return _AdminSessionState.signedIn;
    } on BarberApiException catch (error) {
      if (error.statusCode == 401) {
        await widget.store.clear();
        return _AdminSessionState.signedOut;
      }
      return _AdminSessionState.error;
    } catch (_) {
      return _AdminSessionState.error;
    }
  }

  Future<void> _goToLogin() async {
    if (!mounted) return;
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute<void>(
        builder: (_) => AdminLoginPage(store: widget.store),
      ),
      (_) => false,
    );
  }

  BarberApi _api() => widget.api ?? adminApi(widget.store, _goToLogin);

  void _retry() {
    setState(() {
      _session = _restore();
    });
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<_AdminSessionState>(
      future: _session,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const Scaffold(
            body: Center(child: CircularProgressIndicator()),
          );
        }
        if (snapshot.data == _AdminSessionState.signedIn) {
          return AdminHomePage(api: _api());
        }
        if (snapshot.data == _AdminSessionState.error) {
          return Scaffold(
            body: Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Text(
                      'Não foi possível confirmar sua sessão.',
                      textAlign: TextAlign.center,
                      style: TextStyle(fontWeight: FontWeight.w700),
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      onPressed: _retry,
                      child: const Text('Tentar novamente'),
                    ),
                  ],
                ),
              ),
            ),
          );
        }
        return AdminLoginPage(store: widget.store);
      },
    );
  }
}

enum _AdminSessionState { signedIn, signedOut, error }
