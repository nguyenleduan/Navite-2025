import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
class PhoneNumberService {
  static const platform = MethodChannel('Chancel');

  static Future<String?> getPhoneNumber() async {
    try {

      final status = await Permission.phone.request();
      if (!status.isGranted) {
        print("Quyền không được cấp");
        return null;
      }

      final String? number = await platform.invokeMethod('getPhoneNumber');
      return number;
    } on PlatformException catch (e) {
      print("Failed to get phone number: '${e.message}'.");
      return null;
    }
  }
}