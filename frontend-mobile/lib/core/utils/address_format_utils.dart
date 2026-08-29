/// Mirrors EnderecoUtil.formatarCompleto. Takes plain fields (rather than
/// the Endereco model) so core/ doesn't depend on the players feature.
class AddressFormatUtils {
  AddressFormatUtils._();

  static String formatarCompleto({
    String? logradouro,
    String? numero,
    String? complemento,
    String? bairro,
    String? cidade,
    String? estado,
  }) {
    final String rua = (logradouro ?? '').trim();
    final String num = (numero ?? '').trim();
    final String comp = (complemento ?? '').trim();
    final String bai = (bairro ?? '').trim();
    final String cid = (cidade ?? '').trim();
    final String uf = (estado ?? '').trim();

    final StringBuffer buffer = StringBuffer();
    if (rua.isNotEmpty) buffer.write(rua);
    if (num.isNotEmpty) buffer.write(', $num');
    if (comp.isNotEmpty) buffer.write(' ($comp)');
    if (bai.isNotEmpty) buffer.write(buffer.isEmpty ? bai : ' - $bai');
    if (cid.isNotEmpty || uf.isNotEmpty) {
      final String cidadeUf = <String>[if (cid.isNotEmpty) cid, if (uf.isNotEmpty) uf].join('/');
      buffer.write(buffer.isEmpty ? cidadeUf : ', $cidadeUf');
    }
    return buffer.toString();
  }
}
