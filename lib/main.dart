import 'package:flutter/material.dart';

import 'get_phone_service.dart';
import 'notification_helper.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:firebase_core/firebase_core.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // TRY THIS: Try running your application with "flutter run". You'll see
        // the application has a purple toolbar. Then, without quitting the app,
        // try changing the seedColor in the colorScheme below to Colors.green
        // and then invoke "hot reload" (save your changes or press the "hot
        // reload" button in a Flutter-supported IDE, or press "r" if you used
        // the command line to start the app).
        //
        // Notice that the counter didn't reset back to zero; the application
        // state is not lost during the reload. To reset the state, use hot
        // restart instead.
        //
        // This works for code too, not just values: Most code changes can be
        // tested with just a hot reload.
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}


class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  @override
  void initState() {
    super.initState();
    FirebaseMessaging.instance.getToken().then((token) {
      setState(() {
         print('FCM: $token');
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        color: Colors.white,
        alignment: Alignment.center,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
           InkWell(
             onTap: (){
                NotificationHelper.checkPermission();
             },
            child: Container(
              width: 300,
              color: Colors.grey,
              height: 50,
              alignment: Alignment.center,
              child: Text('Permission'),
            ),
           ),
            SizedBox(
               height: 30,
            ),
            InkWell(
              onTap: (){
                NotificationHelper.scheduleDailyNotification();
              },
              child: Container(
                width: 300,
                color: Colors.grey,
                height: 50,
                alignment: Alignment.center,
                child: Text('SEND'),
              ),
            ),
            SizedBox(
               height: 30,
            ),
            InkWell(
              onTap: (){
                NotificationHelper.showNotification();
              },
              child: Container(
                width: 300,
                color: Colors.grey,
                height: 50,
                alignment: Alignment.center,
                child: Text('Show'),
              ),
            ),
            SizedBox(
              height: 30,
            ),
          ],
        ),
      ),
    );
  }
}
