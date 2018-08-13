package xuzhongwei.gunsecury.controllers;

import android.Manifest;
import android.app.Activity;
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
import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.UUID;

import xuzhongwei.gunsecury.MyPermissionManager;
import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.model.BLEDeviceDAO;
//BC:6A:29:AE:CD:4E DEVICE

public class BLEController {
    private Activity mActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    private Handler mHandler = new Handler();
    private ScanCallback mLeScanCallback;
    private BluetoothGatt mBluetoothGatt;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 100000;
    private boolean isSendConnect = false;
    private OnBleDeviceListener mOnBleDeviceListener;


    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING = "ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_DISCONNECTING = "ACTION_GATT_DISCONNECTING";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_READ = "ACTION_DATA_READ";
    public final static String ACTION_DATA_NOTIFY = "ACTION_DATA_NOTIFY";
    public final static String ACTION_DATA_WRITE = "ACTION_DATA_WRITE";
    public final static String EXTRA_DATA = "EXTRA_DATA";
    public final static String EXTRA_UUID = "EXTRA_UUID";
    public final static String EXTRA_STATUS = "EXTRA_STATUS";
    public final static String EXTRA_ADDRESS = "EXTRA_ADDRESS";


    public BLEController(Activity activity) {
        mActivity = activity;
        checkPermission();
        initialBlueTooth();
    }


    public void setmOnBleDeviceListener(OnBleDeviceListener mOnBleDeviceListener) {
        this.mOnBleDeviceListener = mOnBleDeviceListener;
    }

    private void checkPermission(){
        String[] permissions = new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.MODIFY_PHONE_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        MyPermissionManager.checkPermission(mActivity,permissions);
    }

    private void initialBlueTooth(){
        // Initializes Bluetooth adapter.
        mBluetoothManager =
                (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
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

                if(mOnBleDeviceListener != null){
                    BLEDeviceDAO bleDevice = new BLEDeviceDAO(name,address);
                    mOnBleDeviceListener.onDeviceDiscoverd(bleDevice);
                }

//                if(name.equals("SensorTag")){
//                    Log.d("BLESCAN",result.getDevice().getAddress());
//                    Log.d("BLESCAN",result.getDevice().toString());
//                    //BC:6A:29:AE:CD:4E
//                    stopScan();
//                    if(!isSendConnect){
//                        connectDevice(result.getDevice());
//                        isSendConnect = true;
//                    }
//                }

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


        public void startScan() {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopScan();
                        if(mOnBleDeviceListener != null){
                            mOnBleDeviceListener.onDeviceDiscoveryStopped();
                        }
                    }
                }, SCAN_PERIOD);
                mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
        }

        public void stopScan(){
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
        }


    private void connectDevice(android.bluetooth.BluetoothDevice device){
        mBluetoothGatt = device.connectGatt(mActivity.getApplicationContext(),false, new BluetoothGattCallback() {
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
                    BluetoothGattService service = gatt.getService(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
                }
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


    public interface OnBleDeviceListener{
        void onDeviceDiscoverd(BLEDeviceDAO bleDevice);
        void onDeviceDiscoveryStopped();
    }


}
