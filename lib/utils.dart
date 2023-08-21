import 'package:flutter/cupertino.dart';

enum DeviceType { phone, tablet }

class Utility {
  static bool isPhone = getDeviceTabletType() == DeviceType.phone;

  static DeviceType getDeviceTabletType() {
    final data = MediaQueryData.fromWindow(WidgetsBinding.instance.window);
    return data.size.shortestSide < 650 ? DeviceType.phone : DeviceType.tablet;
  }
}
