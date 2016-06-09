# OBESDK_Android

This repository contains all the source code related to OBE's SDK for iOS.

**Installation**

In order to add OBE's SDK into your project, you must download the repository and import the OBE SDK Project into your project.

**Use**

You can initialise the OBE SDK like this: (refer to the demo project)

	// make your class implement the 'OBEListener' interface
	OBE obe = new OBE(getApplicationContext(), this);
	
To start scanning for an OBE Jacket. Use the following code:

	obe.startBluetooth();
	
In order to read data from an OBE jacket. The following piece of code must be implemented inside your class:

	@Override
    public void onOBEConnected() {
        Toast.makeText(getApplicationContext(), "OBE Connected", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQuaternionsUpdated(OBEQuaternion left, OBEQuaternion right, OBEQuaternion center) {

    }

    @Override
    public void onButtonsUpdated() {

    }

    @Override
    public void onBatteryChanged() {
        String battery = String.valueOf(obe.batteryLevel);
        Toast.makeText(getApplicationContext(), "Battery: " + battery, Toast.LENGTH_SHORT).show();
    }
	
There are several addressable properties, such as:

* Motor1 (Float - Left hand Motor)
* Motor2 (Float - Right hand Motor)
* Motor3 (Float - Logo Motor)
* Motor4 (Float - Cerebrum Motor)
* QuaternionLeft (Quaternion - Left Hand)
* QuaternionRight (Quaternion - Right Hand)
* QuaternionCenter (Quaternion - Cerebrum)
* LeftButton1 (Boolean - Left Button on Left Hand)
* LeftButton2 (Boolean - Right Button on Left Hand)
* LeftButton3 (Boolean - Up Button on Left Hand)
* LeftButton4 (Boolean - Down Button on Left Hand)
* RightButton1 (Boolean - Left Button on Right Hand)
* RightButton2 (Boolean - Right Button on Right Hand)
* RightButton3 (Boolean - Up Button on Right Hand)
* RightButton4 (Boolean - Down Button on Right Hand)
* LogoButton (Boolean - Logo button)