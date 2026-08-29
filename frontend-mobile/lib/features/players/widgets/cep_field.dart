import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../../core/utils/cep_lookup_service.dart';

/// CEP input with a "buscar endereço" action, mirroring the desktop's
/// on-blur ViaCEP auto-fill (CepUtil.buscar), triggered here by the
/// search button and by leaving the field (onEditingComplete).
class CepField extends StatefulWidget {
  const CepField({
    super.key,
    required this.controller,
    required this.onResult,
    this.enabled = true,
  });

  final TextEditingController controller;
  final ValueChanged<CepLookupResult?> onResult;
  final bool enabled;

  @override
  State<CepField> createState() => _CepFieldState();
}

class _CepFieldState extends State<CepField> {
  final CepLookupService _service = CepLookupService();
  bool _loading = false;

  Future<void> _buscar() async {
    if (_loading) return;
    setState(() => _loading = true);
    final CepLookupResult? result = await _service.buscar(widget.controller.text);
    if (mounted) {
      setState(() => _loading = false);
      widget.onResult(result);
      if (result == null && CepLookupService.onlyDigits(widget.controller.text).length == 8) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('CEP não encontrado.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: widget.controller,
      enabled: widget.enabled,
      keyboardType: TextInputType.number,
      inputFormatters: <TextInputFormatter>[FilteringTextInputFormatter.digitsOnly, LengthLimitingTextInputFormatter(8)],
      decoration: InputDecoration(
        labelText: 'CEP',
        suffixIcon: _loading
            ? const Padding(
                padding: EdgeInsets.all(12),
                child: SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2)),
              )
            : IconButton(icon: const Icon(Icons.search), onPressed: widget.enabled ? _buscar : null),
      ),
      onEditingComplete: widget.enabled ? _buscar : null,
    );
  }
}
