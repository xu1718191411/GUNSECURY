package xuzhongwei.gunsecury.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import xuzhongwei.gunsecury.model.BLEDeviceDAO;

public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";

    public final static String ACTION_GATT_CONNECTING =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTING";

    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";

    public final static String ACTION_GATT_DISCONNECTING =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTING";

    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.randomUUID();

    public static final String  FIND_NEW_BLE_DEVICE = "FIND_NEW_BLE_DEVICE";

    private static BluetoothLeService mService;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private Handler mHandler = new Handler();
    private ScanCallback mLeScanCallback;
    private BluetoothGatt mBluetoothGatt;
    private static final long SCAN_PERIOD = 100000;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initializes Bluetooth adapter.
        initialBlueTooth();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mService = this;
        initialBlueTooth();
        return super.onStartCommand(intent, flags, startId);
    }

    public static BluetoothLeService getInstance(){
        if(mService == null){
            mService = new BluetoothLeService();
        }
        return mService;
    }

    public void connectDevice(final android.bluetooth.BluetoothDevice device){
        mBluetoothGatt = device.connectGatt(this,false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Log.d("BLEDISCOVERChange",status+"");
                Log.d("BLEDISCOVERChange",newState+"");
                if(mBluetoothGatt == null){
                    return;
                }

                BluetoothDevice device = gatt.getDevice();
                String address = device.getAddress();
                int a;
                switch (newState){
                    case BluetoothProfile.STATE_CONNECTED:
                        gatt.discoverServices();
                        break;
                    case BluetoothProfile.STATE_CONNECTING:

                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:

                        break;
                    case BluetoothProfile.STATE_DISCONNECTING:
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                BluetoothDevice devide = gatt.getDevice();
                Log.d("BLEDISCOVERDiscovered",gatt.toString());
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    List<BluetoothGattService> list = gatt.getServices();
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                int a = 1;
                super.onCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.d("BLEDISCOVERREAD",characteristic.toString());
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    UUID uuid = gatt.getServices().get(0).getUuid();
                    int a = 0;
                }
            }
        });
    }


    public void startScan() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);

        if(mBluetoothAdapter == null) return;
        mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
    }

    public void stopScan(){
        mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
    }



    private void initialBlueTooth(){

        mBluetoothManager =
                (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mLeScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if(result == null) return;
                if(result.getDevice() == null) return;
                String name = result.getDevice().getName();
                if(name == null) name="UNKNOWN DEVICE";
                String address = result.getDevice().getAddress();
                if(address == null) return;
                BLEDeviceDAO bleDevice = new BLEDeviceDAO(name,address,result.getDevice());
                broadcastUpdate(bleDevice);

            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    public List<BluetoothGattService> getBLEService(){
        if(mBluetoothGatt == null) return null;
        return  mBluetoothGatt.getServices();
    }


    private void broadcastUpdate(BLEDeviceDAO bleDevice){
        Intent intent = new Intent();
        intent.putExtra(FIND_NEW_BLE_DEVICE,(Parcelable) bleDevice);
        intent.setAction(FIND_NEW_BLE_DEVICE);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }


}
