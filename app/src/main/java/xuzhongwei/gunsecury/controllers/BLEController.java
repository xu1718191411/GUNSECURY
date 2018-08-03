package xuzhongwei.gunsecury.controllers;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.List;

import xuzhongwei.gunsecury.MyPermissionManager;
import xuzhongwei.gunsecury.service.BluetoothLeService;
//BC:6A:29:AE:CD:4E DEVICE

public class BLEController {
    private Activity mActivity;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    private boolean mScanning = true;
    private Handler mHandler = new Handler();
    private ScanCallback mLeScanCallback;
    private BluetoothGatt mBluetoothGatt;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 100000;
    private boolean isSendConnect = false;


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
        scanLeDevice(mScanning);
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
                if(name == null) return;
                if(name.equals("SensorTag")){
                    Log.d("BLESCAN",result.getDevice().getAddress());
                    Log.d("BLESCAN",result.getDevice().toString());
                    //BC:6A:29:AE:CD:4E
                    scanLeDevice(false);
                    if(!isSendConnect){
                        connectDevice(result.getDevice());
                        isSendConnect = true;
                    }
                }
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


    private void scanLeDevice(final boolean enable) {

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
        }

    }


    private void connectDevice(android.bluetooth.BluetoothDevice device){
        BluetoothLeService.getInstance().connectDevice(device);
    }





}
