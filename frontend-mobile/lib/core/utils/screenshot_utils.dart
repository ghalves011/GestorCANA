import 'dart:io';
import 'dart:typed_data';
import 'dart:ui' as ui;

import 'package:flutter/rendering.dart';
import 'package:flutter/widgets.dart';
import 'package:path_provider/path_provider.dart';
import 'package:share_plus/share_plus.dart';

/// Replaces the desktop's "screenshot panel -> clipboard -> paste into
/// WhatsApp" flow (ImagemUtil.tirarPrintPainel/copiarParaClipboard) with the
/// mobile-native equivalent: capture a RepaintBoundary to PNG bytes, save to
/// a temp file, and hand it to the OS share sheet.
class ScreenshotUtils {
  ScreenshotUtils._();

  static Future<Uint8List> captureBoundary(GlobalKey boundaryKey, {double pixelRatio = 2.0}) async {
    final RenderRepaintBoundary boundary =
        boundaryKey.currentContext!.findRenderObject()! as RenderRepaintBoundary;
    final ui.Image image = await boundary.toImage(pixelRatio: pixelRatio);
    final ByteData? byteData = await image.toByteData(format: ui.ImageByteFormat.png);
    return byteData!.buffer.asUint8List();
  }

  static Future<void> shareBoundaryAsImage(
    GlobalKey boundaryKey, {
    String fileName = 'placar_cana.png',
    String? text,
  }) async {
    final Uint8List bytes = await captureBoundary(boundaryKey);
    final Directory tempDir = await getTemporaryDirectory();
    final File file = File('${tempDir.path}/$fileName');
    await file.writeAsBytes(bytes, flush: true);

    await Share.shareXFiles(
      <XFile>[XFile(file.path, mimeType: 'image/png')],
      text: text,
    );
  }
}
