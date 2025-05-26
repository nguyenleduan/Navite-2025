import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';

class NotificationHelper {
  static const platform = MethodChannel('Chancel');

  static Future<void> checkPermission() async {
    try {
      final status = await Permission.notification.request();
      if (!status.isGranted) {
        print("Quyền không được cấp");
        return null;
      }
      final bool canSchedule = await platform.invokeMethod('checkPermission');
      if (!canSchedule) {
        print('1111111');
      }
      print('22222222');
    } on PlatformException catch (e) {
      print("Failed to schedule notification: ${e.message}");
    }
  }
  static Future<void> scheduleDailyNotification() async {
    try {
      final status = await Permission.notification.request();
      if (!status.isGranted) {
        print("Quyền không được cấp");
        return null;
      }
        await platform.invokeMethod('scheduleDailyNotification',{
        'h' : DateTime.now().hour,
        'm' : DateTime.now().minute +1
      });
      print('scheduleDailyNotification DONE');
    } on PlatformException catch (e) {
      print("Failed to schedule notification: ${e.message}");
    }
  }
  static Future<void> showNotification() async {
    try {
      final status = await Permission.notification.request();
      if (!status.isGranted) {
        print("Quyền không được cấp");
        return null;
      }
      await platform.invokeMethod('showNotification', {
        'title': 'Chào các bạn',
        'body': 'Welcome to App',
      });
    } catch (e) {
      print("Test noti error: $e");
    }
  }
}