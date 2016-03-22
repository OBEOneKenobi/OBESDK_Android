package machina.com.obe;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.FloatMath;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Henry Serrano on 3/18/16.
 * Copyright (c) 2016
 */
public class OBE {
    //TODO: Right and Center missing the right characteristic
    private static String QuaternionCharacteristic = "0003cbb2-0000-1000-8000-00805f9b0131";
    private static String HapticCharacteristic = "0003cbb1-0000-1000-8000-00805f9b0131";

    private static int QuaternionLeft = 0;
    private static int QuaternionRight = 1;
    private static int QuaternionCenter = 2;
    private static int MPUDataSize = 20;
    private static int HapticDataSize = 7;

    private Context context;
    BluetoothAdapter mBtAdapter;
    private Set<BluetoothDevice> pairedDevices;
    BluetoothGatt mBluetoothGatt;
    private OBEListener listener;
    //OBECallback callback;

    public double axLeft, ayLeft, azLeft;
    public double gxLeft, gyLeft, gzLeft;
    public double mxLeft, myLeft, mzLeft;
    public double axRight, ayRight, azRight;
    public double gxRight, gyRight, gzRight;
    public double mxRight, myRight, mzRight;
    public double axCenter, ayCenter, azCenter;
    public double gxCenter, gyCenter, gzCenter;
    public double mxCenter, myCenter, mzCenter;
    //public int Buttons;
    public double Motor1, Motor2, Motor3, Motor4;

    public OBEQuaternion quaternionLeft, quaternionRight, quaternionCenter;
    public OBEEulerAngles eulerAnglesLeft, eulerAnglesRight, eulerAnglesCenter;
    public OBEButtons buttons;

    BluetoothGattCharacteristic write_characteristic;

    //0 - not started
    //1 - connecting
    //2 - connected
    //3 - disconnected
    //4 - no jacket found
    //5 - other error
    /*
		0 NOT_STARTED,
		1 CONNECTING,
		2 CONNECTED,
		3 DISCCONECTED,
		4 NOT_FOUND,
		5 OTHER_ERROR,
		6 CALIBRATING,
		7 CALIBRATED,
		8 CONNECTED_NOT_CALIBRATED,
		9 STARTED_CALIBRATED //ERROR IT WAS ALREADY CALIBRATED WHEN WE STARTED
		*/
    int state = 0;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setOBEListener(OBEListener listen){
        listener = listen;
    }

    public void startBluetooth(){
        state = 1;
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if(quaternionLeft == null){
            quaternionLeft = new OBEQuaternion();
        }
        if(quaternionRight == null){
            quaternionRight = new OBEQuaternion();
        }
        if(quaternionCenter == null){
            quaternionCenter = new OBEQuaternion();
        }

        Log.i("Discovery", "About to Start");
        this.doDiscovery();
    }

    public int getStatus()
    {
        return state;
    }

    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        //intentAction = ACTION_GATT_CONNECTED;
                        //mConnectionState = STATE_CONNECTED;
                        //broadcastUpdate(intentAction);
                        //Log.d("MAIN", "Connected to GATT server."); Log.d("MAIN", "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices();
                        state = 2;
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        //intentAction = ACTION_GATT_DISCONNECTED;
                        //mConnectionState = STATE_DISCONNECTED;
                        //Log.d("MAIN", "Disconnected from GATT server.");
                        state = 3;
                        //broadcastUpdate(intentAction);
                    }
                }

        /*public UUID toUuid(long paramLong){
            return new UUID(0x1000 | paramLong << 32, -9223371485494954757L);
        }
        public String toUuid128(long paramLong){
            return toUuid(paramLong).toString();
        }*/

                private void displayGattServices(List<BluetoothGattService> gattServices) {
                    if (gattServices == null) return;
                    String uuid = null;

                    // Loops through available GATT Services.
                    for (BluetoothGattService gattService : gattServices) {
                        HashMap<String, String> currentServiceData =
                                new HashMap<String, String>();
                        //Log.d("MAIN", "Service" );
                        uuid = gattService.getUuid().toString();
                        List<BluetoothGattCharacteristic> gattCharacteristics =
                                gattService.getCharacteristics();
                        // Loops through available Characteristics.
                        for (BluetoothGattCharacteristic gattCharacteristic :
                                gattCharacteristics) {

                            uuid = gattCharacteristic.getUuid().toString();
                            //Log.d("MAIN", "Characterisitic" ); //Log.d("MAIN", uuid );

                            // Check if characteristic is writable
                            if (((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                                    (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
                                // writing characteristic functions
                                //Log.d("MAIN", "Writable" );
                                write_characteristic = gattCharacteristic;
                            }

                            // OBE-Controls Characteristic
                            // e7add780-b042-4876-aae1-112855353cc1
                            //if (uuid.startsWith("0003cbb2-0000-1000-8000-00805f9b0131")) {
                            if(uuid.startsWith(QuaternionCharacteristic) )/*|| uuid.startsWith(QuaternionCharacteristic_Right)
        					|| uuid.startsWith(QuaternionCharacteristic_Center))*/
                            {
                                //Log.d("MAIN", "Activating Notifications" );
                                // ENABLE INDICATION
        				/*BluetoothGattCharacteristic characteristic =gattCharacteristic;
                    	mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                    	BluetoothGattDescriptor localBluetoothGattDescriptor = characteristic.getDescriptor(UUID.fromString(toUuid128(10498L)));
                    	localBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                    	mBluetoothGatt.writeDescriptor(localBluetoothGattDescriptor);*/

                                // ENABLE NOTIFICATION
                                BluetoothGattCharacteristic characteristic = gattCharacteristic;
                                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                                BluetoothGattDescriptor localBluetoothGattDescriptor =
                                        characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));

                                if(localBluetoothGattDescriptor != null) {
                                    localBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    mBluetoothGatt.writeDescriptor(localBluetoothGattDescriptor);
                                    //Log.i("Notification status", "Enabled");
                                } else {
                                    //Log.i("Notification status", "Not enabled");
                                }
                            }
                            //OBE-Haptics Characteristic
                            else if(uuid.startsWith(HapticCharacteristic)){
                                write_characteristic = gattCharacteristic;
                            }
                        }
                        //Log.d("MAIN", "Characterisitic" );
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                        //Log.d("MAIN", "onServicesDiscovered received!" );
                        displayGattServices(gatt.getServices());
                    } else {
                        //Log.d("MAIN", "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                        //Log.d("MAIN", "Characteristic has been read!" );
                    }
                    else{
                        //Log.d("MAIN", "Characteristic has been read but with error!" );
                    }
                }

                @Override
                public void onDescriptorRead (BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status){
                    //Log.d("MAIN", "Descriptor has been read!" );
                }

                @Override
                public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("MAIN", "Callback: Characteristic has been writen.");
                    }
                    else{
                        Log.d("MAIN", "Callback: Error writing characteristic: "+ status);
                        state = 5;
                    }
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
                    // if callback is null, do nothing
        	/*if(callback == null){
    			return;
    		}*/

                    byte[] arrayOfByte = characteristic.getValue();
                    if(arrayOfByte.length == MPUDataSize){
                        //completePacket = arrayOfByte;

                        int aux = arrayOfByte[MPUDataSize - 2];
                        bufferToFloat(arrayOfByte, aux);
                        if(aux == QuaternionRight){
                            int auxButtons = arrayOfByte[19] & 0xFF;

                        }

                        // Convert bytes to floats
        		/*float w = ByteBuffer.wrap(arrayOfByte, 0, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        		float x = ByteBuffer.wrap(arrayOfByte, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        		float y = ByteBuffer.wrap(arrayOfByte, 8, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();
        		float z = ByteBuffer.wrap(arrayOfByte, 12, 4).order(ByteOrder.LITTLE_ENDIAN).getFloat();

        		String uuid = characteristic.getUuid().toString();

        		// Notify callback that a Quaternion has been updated
        		if(uuid.equals(QuaternionCharacteristic_Left)){
        			callback.onQuaternionUpdated(w, x, y, z, Quaternion_Left);
        		}*/
                    }

            /*String str = "a";
            byte[] strBytes = str.getBytes();
            write_characteristic.setValue(strBytes);
            write_characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            gatt.writeCharacteristic(write_characteristic);*/
                }


                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("MAIN", "Callback: Wrote GATT Descriptor successfully.");
                    }
                    else{
                        Log.d("MAIN", "Callback: Error writing GATT Descriptor: "+ status);
                        state = 5;
                    }
                    //mBluetoothGatt.readCharacteristic(correct_characteristic);
                };
            };

    void doDiscovery() {
        Log.d("MAIN", "doDiscovery()");

        pairedDevices = mBtAdapter.getBondedDevices();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.d("MAIN", device.getName() + " -> " + device.getAddress());
                if (device.getName().startsWith("OBE")){
                    Log.d("MAIN", "OBE FOUND");
                    mBluetoothGatt = device.connectGatt(this.context, false, mGattCallback);
                    return;
                }

            }
        }else {
            Log.d("MAIN", "No devices found");
        }

        state = 4;

        //Log.d("MAIN", "Scanning for devices...");

        // Turn on sub-title for new devices
        // findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        // If we're already discovering, stop it
        /*if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();*/
    }

    // Update OBE_Haptics characteristic
    public void updateMotors(){
        if(write_characteristic != null){

            byte[] data = new byte[HapticDataSize];

            data[0] = (byte)0x7E;
            data[1] = doubleToByte(Motor1);
            data[2] = doubleToByte(Motor2);
            data[3] = doubleToByte(Motor3);
            data[4] = doubleToByte(Motor4);
            data[5] = (byte)0xFF;
            data[6] = 0x00;

            write_characteristic.setValue(data);
            write_characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mBluetoothGatt.writeCharacteristic(write_characteristic);
        }
    }

    private void bufferToFloat(byte[] buffer, int identifier){
        switch(identifier){
            case 0://QuaternionLeft
                axLeft = twoBytesToDouble(buffer[0], buffer[1]);
                ayLeft = twoBytesToDouble(buffer[2], buffer[3]);
                azLeft = twoBytesToDouble(buffer[4], buffer[5]);
                gxLeft = twoBytesToDouble(buffer[6], buffer[7]);
                gyLeft = twoBytesToDouble(buffer[8], buffer[9]);
                gzLeft = twoBytesToDouble(buffer[10], buffer[11]);
                mxLeft = twoBytesToDouble(buffer[12], buffer[13]);
                myLeft = twoBytesToDouble(buffer[14], buffer[15]);
                mzLeft = twoBytesToDouble(buffer[16], buffer[17]);
                break;
            case 1://QuaternionRight
                axRight = twoBytesToDouble(buffer[0], buffer[1]);
                ayRight = twoBytesToDouble(buffer[2], buffer[3]);
                azRight = twoBytesToDouble(buffer[4], buffer[5]);
                gxRight = twoBytesToDouble(buffer[6], buffer[7]);
                gyRight = twoBytesToDouble(buffer[8], buffer[9]);
                gzRight = twoBytesToDouble(buffer[10], buffer[11]);
                mxRight = twoBytesToDouble(buffer[12], buffer[13]);
                myRight = twoBytesToDouble(buffer[14], buffer[15]);
                mzRight = twoBytesToDouble(buffer[16], buffer[17]);
                break;
            case 2://QuaternionRight
                axCenter = twoBytesToDouble(buffer[0], buffer[1]);
                ayCenter = twoBytesToDouble(buffer[2], buffer[3]);
                azCenter = twoBytesToDouble(buffer[4], buffer[5]);
                gxCenter = twoBytesToDouble(buffer[6], buffer[7]);
                gyCenter = twoBytesToDouble(buffer[8], buffer[9]);
                gzCenter = twoBytesToDouble(buffer[10], buffer[11]);
                mxCenter = twoBytesToDouble(buffer[12], buffer[13]);
                myCenter = twoBytesToDouble(buffer[14], buffer[15]);
                mzCenter = twoBytesToDouble(buffer[16], buffer[17]);
                break;
        }
    }

    private double twoBytesToDouble(byte var1, byte var2){
        double auxF = 0.0f;
        short auxS = (short)(((var1 << 8) | var2) & 0xFFFF);
        auxF = auxS;
        auxF /= 32768.0f;

        return auxF;
    }

    private byte doubleToByte(double var){
        byte auxB = 0;

        auxB = (byte)(((int)(var * 255.0)) & 0xFF);

        return auxB;
    }

    private double calculatePitch(double ax, double ay, double az){
        double localPitch = 0.0f, squareResult = 0.0f;

        squareResult = Math.sqrt(ay * ay + az * az);
        localPitch = Math.atan2(-ax, squareResult); // pitch in radians
        //localPitch = localPitch * 57.2957f; // pitch in degrees

        return localPitch;
    }

    private double calculateRoll(double ay, double az){
        double localRoll = 0.0f;

        localRoll = Math.atan2(ay, az); // roll in radians
        //localRoll = localRoll * 57.2957f; // roll in degrees

        return localRoll;
    }

    private double calculateYaw(double roll, double pitch, double mx, double my, double mz){
        double localYaw = 0.0f, upper = 0.0f, lower = 0.0f, sinRoll = 0.0f, cosRoll = 0.0f,
                sinPitch = 0.0f, cosPitch = 0.0f;

        sinRoll = Math.sin(roll);
        cosRoll = Math.cos(roll); // / 57.2957f
        sinPitch = Math.sin(pitch);
        cosPitch = Math.cos(pitch);

        upper = mz * sinRoll - my * cosRoll;
        lower = mx * cosPitch + my * sinPitch * sinRoll +
                mz * sinPitch * cosRoll;
        localYaw = Math.atan2(upper, lower); // yaw in radians
        //localYaw = localYaw * 57.2957f; // yaw in angles

        return localYaw;
    }

    private void anglesToQuaternion(double roll, double pitch, double yaw, int identifier){
        double sinHalfYaw = Math.sin(yaw / 2.0f);
        double cosHalfYaw = Math.cos(yaw / 2.0f);
        double sinHalfPitch = Math.sin(pitch / 2.0f);
        double cosHalfPitch = Math.cos(pitch / 2.0f);
        double sinHalfRoll = Math.sin(roll / 2.0f);
        double cosHalfRoll = Math.cos(roll / 2.0f);

        double x = -cosHalfRoll * sinHalfPitch * sinHalfYaw + cosHalfPitch * cosHalfYaw * sinHalfRoll;
        double y = cosHalfRoll * cosHalfYaw * sinHalfPitch + sinHalfRoll * cosHalfPitch * sinHalfYaw;
        double z = cosHalfRoll * cosHalfPitch * sinHalfYaw - sinHalfRoll * cosHalfYaw * sinHalfPitch;
        double w = cosHalfRoll * cosHalfPitch * cosHalfYaw + sinHalfRoll * sinHalfPitch * sinHalfYaw;

        if(identifier == QuaternionLeft){
            quaternionLeft.w = w; quaternionLeft.x = x;
            quaternionLeft.y = y; quaternionLeft.z = z;
        }else if(identifier == QuaternionRight){
            quaternionRight.w = w; quaternionRight.x = x;
            quaternionRight.y = y; quaternionRight.z = z;
        }else if(identifier == QuaternionCenter){
            quaternionCenter.w = w; quaternionCenter.x = x;
            quaternionCenter.y = y; quaternionCenter.z = z;
        }
    }
}
