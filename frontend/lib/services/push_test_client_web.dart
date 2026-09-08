import 'dart:async';
import 'dart:convert';
import 'dart:js_interop';

@JS('barberPushTest.subscribe')
external JSPromise<JSString> _subscribe(JSString publicKey);

Future<Map<String, dynamic>> createPushTestSubscription(String publicKey) async {
  final json = (await _subscribe(publicKey.toJS).toDart).toDart;
  return jsonDecode(json) as Map<String, dynamic>;
}
