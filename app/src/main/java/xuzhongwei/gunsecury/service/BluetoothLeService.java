package xuzhongwei.gunsecury.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.model.BLEDeviceDAO;

public class BluetoothLeService extends Service {

    private final static String TAG = BluetoothLeService.class.getSimpleName();

    public final static String ACTION_GATT_CONNECTED =
            "ACTION_GATT_CONNECTED";

    public final static String ACTION_GATT_CONNECTING =
            "ACTION_GATT_CONNECTING";

    public final static String ACTION_GATT_DISCONNECTED =
            "ACTION_GATT_DISCONNECTED";

    public final static String ACTION_GATT_DISCONNECTING =
            "ACTION_GATT_DISCONNECTING";

    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED";

    public final static String ACTION_DATA_AVAILABLE =
            "ACTION_DATA_AVAILABLE";

    public final static String ACTION_DATA_NOTIFY =
            "ACTION_DATA_NOTIFY";

    public final static String EXTRA_DATA =
            "EXTRA_DATA";

    public final static String EXTRA_UUID =
            "EXTRA_UUID";

    public final static String EXTRA_STATUS =
            "EXTRA_STATUS";


    public static final String  FIND_NEW_BLE_DEVICE = "FIND_NEW_BLE_DEVICE";

    private static BluetoothLeService mService;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private Handler mHandler = new Handler();
    private ScanCallback mLeScanCallback;
    private BluetoothGatt mBluetoothGatt;
    private static final long SCAN_PERIOD = 100000;
    private LinkedList<BluetoothGattCharacteristic> writeList = new LinkedList<BluetoothGattCharacteristic>();

    private volatile bleRequest curBleRequest = null;


    private volatile LinkedList<bleRequest> procQueue;
    private final Lock lock = new ReentrantLock();
    public enum bleRequestOperation {
        wrBlocking,
        wr,
        rdBlocking,
        rd,
        nsBlocking,
    }

    public enum bleRequestStatus {
        not_queued,
        queued,
        processing,
        timeout,
        done,
        no_such_request,
        failed,
    }

    public class bleRequest {
        public int id;
        public BluetoothGattCharacteristic characteristic;
        public bleRequestOperation operation;
        public volatile bleRequestStatus status;
        public int timeout;
        public int curTimeout;
        public boolean notifyenable;
    }




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
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,gatt.getDevice());
                }
            }


            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                broadcastUpdate(ACTION_DATA_NOTIFY, characteristic, BluetoothGatt.GATT_SUCCESS);
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

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
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


    public BluetoothDevice getDevice(){
        if(mBluetoothGatt != null){
            return mBluetoothGatt.getDevice();
        }
        return null;
    }

    private void initialBlueTooth(){
        procQueue = new LinkedList<bleRequest>();
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
                broadcastUpdate(FIND_NEW_BLE_DEVICE,result.getDevice());
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


        Thread queueThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    executeQueue();
                    try {
                        Thread.sleep(50,0);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        queueThread.start();


    }



    public List<BluetoothGattService> getBLEService(){
        if(mBluetoothGatt == null) return null;
        return  mBluetoothGatt.getServices();
    }

    private void broadcastUpdate(String status ,BluetoothDevice bleDevice){
        Intent intent = new Intent();
        intent.putExtra(status,(Parcelable) bleDevice);
        intent.setAction(status);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic, final int status) {
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_UUID, characteristic.getUuid().toString());
        intent.putExtra(EXTRA_DATA, characteristic.getValue());
        intent.putExtra(EXTRA_STATUS, status);
        sendBroadcast(intent);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte b) {
        byte[] val = new byte[1];
        val[0] = b;
        characteristic.setValue(val);

        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.wrBlocking;
        addRequestToQueue(req);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic){
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic){
        mBluetoothGatt.writeCharacteristic(characteristic);
    }


    public void setCharacteristicNotification(
        BluetoothGattCharacteristic characteristic, boolean enable) {
        bleRequest req = new bleRequest();
        req.status = bleRequestStatus.not_queued;
        req.characteristic = characteristic;
        req.operation = bleRequestOperation.nsBlocking;
        req.notifyenable = enable;
        addRequestToQueue(req);
    }

    private void addRequestToQueue(bleRequest req){
        lock.lock();
        if (procQueue.peekLast() != null) {
            req.id = procQueue.peek().id++;
        }
        else {
            req.id = 0;
        }

        procQueue.add(req);
        lock.unlock();
    }


    private void executeQueue(){
        lock.lock();
        if (curBleRequest != null) {
            Log.d(TAG, "executeQueue, curBleRequest running");
            try {
                Thread.sleep(10, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            lock.unlock();
            return;
        }



        if (procQueue == null) {
            lock.unlock();
            return;
        }
        if (procQueue.size() == 0) {
            lock.unlock();
            return;
        }

        bleRequest procReq = procQueue.removeFirst();
        curBleRequest = procReq;
        switch (procReq.operation) {
            case rd:
                //Read, do non blocking read

                break;
            case rdBlocking:
                //Normal (blocking) read
                mBluetoothGatt.readCharacteristic(procReq.characteristic);
                break;
            case wr:
                mBluetoothGatt.writeCharacteristic(procReq.characteristic);
                //Write, do non blocking write (Ex: OAD)
                break;
            case wrBlocking:
                //Normal (blocking) write

                Log.d(TAG, "executeQueue, wrBlocking running" + procReq.characteristic.getUuid().toString()+"");
                procReq.status = bleRequestStatus.processing;
                mBluetoothGatt.writeCharacteristic(procReq.characteristic);
                procReq.status = bleRequestStatus.done;
                break;
            case nsBlocking:
                Log.d(TAG, "executeQueue, nsBlocking running" +  procReq.characteristic.getUuid().toString()+"");
                Boolean res = mBluetoothGatt.setCharacteristicNotification(procReq.characteristic, procReq.notifyenable);
                if (res) {
                    procReq.status = bleRequestStatus.processing;
                    BluetoothGattDescriptor clientConfig = procReq.characteristic
                            .getDescriptor(GattInfo.CLIENT_CHARACTERISTIC_CONFIG);
                    if (clientConfig != null) {

                        if (procReq.notifyenable) {
                            // Log.i(TAG, "Enable notification: " +
                            // characteristic.getUuid().toString());
                            clientConfig
                                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        } else {
                            // Log.i(TAG, "Disable notification: " +
                            // characteristic.getUuid().toString());
                            clientConfig
                                    .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                        }
                        mBluetoothGatt.writeDescriptor(clientConfig);


                    }

                    procReq.status = bleRequestStatus.done;

                }

                break;
            default:
                break;
        }

        curBleRequest = null;
        lock.unlock();
    }



    public bleRequestStatus pollForStatusofRequest(bleRequest req) {
        lock.lock();
        if (req == curBleRequest) {
            bleRequestStatus stat = curBleRequest.status;
            if (stat == bleRequestStatus.done) {
                curBleRequest = null;
            }
            if (stat == bleRequestStatus.timeout) {
                curBleRequest = null;
            }
            lock.unlock();
            return stat;
        }
        else {
            lock.unlock();
            return bleRequestStatus.no_such_request;
        }
    }

}
