package xuzhongwei.gunsecury;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import xuzhongwei.gunsecury.controllers.BLEController;
import xuzhongwei.gunsecury.model.BLEDeviceDAO;
import xuzhongwei.gunsecury.profile.GenericBleProfile;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import xuzhongwei.gunsecury.util.Adapter.DeviceScanResultAdapter;

public class DeviceScanActivity extends AppCompatActivity {
    private ProgressBar mProgressBar = null;
    private Activity mActivity;
    private BLEController mBLEController;
    private ListView bleScanListView;
    private DeviceScanResultAdapter mDeviceScanResultAdapter;
    private ArrayList<BLEDeviceDAO> deviceList = new ArrayList<BLEDeviceDAO>();
    private ArrayList<BluetoothDevice> mBluetoothDevicesList = new ArrayList<BluetoothDevice>();
    private BluetoothLeService mBluetoothLeService;
    private BluetoothDevice mBluetoothDevice = null;

    private static final int CHARACTERISTICS_FOUND = 1;
    private static final String CHARACTERISTICS_FOUND_RESULT = "CHARACTERISTICS_FOUND_RESULT";
    ArrayList<GenericBleProfile> bleProfiles = new ArrayList<GenericBleProfile>();
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_scan);
        mProgressBar = (ProgressBar) findViewById(R.id.connectingProgress);
        mActivity = this;
        bleScanListView = (ListView) findViewById(R.id.ble_scan_list);
        mDeviceScanResultAdapter = new DeviceScanResultAdapter(getApplicationContext());
        bleScanListView.setAdapter(mDeviceScanResultAdapter);
        mBLEController = new BLEController(this);
        mBluetoothLeService = BluetoothLeService.getInstance();


        receiver  = new BroadcastReceiver() {
            List<BluetoothGattService> bleServiceList = new ArrayList<BluetoothGattService>();
            ArrayList<BluetoothGattCharacteristic> characteristicList = new ArrayList<BluetoothGattCharacteristic>();
            @Override
            public void onReceive(Context context, Intent intent) {


                if(intent.getAction().equals(BluetoothLeService.FIND_NEW_BLE_DEVICE)){
                    Bundle bundle = intent.getExtras();
                    if(bundle == null) return;
                    Parcelable p = bundle.getParcelable(BluetoothLeService.FIND_NEW_BLE_DEVICE);
                    if(p == null) return;
                    BluetoothDevice device = (BluetoothDevice) p;
                    BLEDeviceDAO dao = new BLEDeviceDAO(device.getName(),device.getAddress(),device);
                    addIntoDeviceList(dao);
                    addIntoDeviceList(device);
                }else{
                    String a = intent.getAction();
                    String b = BluetoothLeService.ACTION_GATT_CONNECTED;
                    if(a.equals(b)){
                        PageJumpHandler pageJumpHandler = new PageJumpHandler();
                        pageJumpHandler.sendEmptyMessage(8000);
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.FIND_NEW_BLE_DEVICE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);

        registerReceiver(receiver,intentFilter);


        bleScanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(deviceList == null) return;
                if(deviceList.size() <= i) return;
                mBluetoothLeService.connect(deviceList.get(i).getDevice().toString());
                mBluetoothDevice = mBluetoothDevicesList.get(i);
                connnect();
            }
        });

    }




    public void startScan(View view){
        if(mBluetoothLeService != null){
            mBluetoothLeService.startScan();
        }
    }

    private void stopScan(){
        if(mBluetoothLeService != null){
            mBluetoothLeService.stopScan();
        }
    }

    private void addIntoDeviceList(BLEDeviceDAO device){
        for(int i=0;i<deviceList.size();i++){
            if(deviceList.get(i).getDeviceAddress().equals(device.getDeviceAddress())){
                return;
            }
        }
        deviceList.add(device);
        showBLEDevice();
    }

    private void addIntoDeviceList(BluetoothDevice device){
        for(int i=0;i<mBluetoothDevicesList.size();i++){
            if(mBluetoothDevicesList.get(i).getAddress().equals(device.getAddress())){
                return;
            }
        }
        mBluetoothDevicesList.add(device);
    }

    private void showBLEDevice(){
        mDeviceScanResultAdapter.setmBLEDeviceList(deviceList);
        mDeviceScanResultAdapter.notifyDataSetChanged();
    }

    public void connnect(){
        mProgressBar.setVisibility(View.VISIBLE);
        Timer timer = new Timer();
        showToast(getResources().getString(R.string.scaning_string));
        stopScan();
    }

    private void goToDeviceDetail(){
        mProgressBar.setVisibility(View.GONE);
        Intent intent = new Intent(mActivity,DeviceDetailActivity.class);
        intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE, mBluetoothDevice);
        mActivity.startActivity(intent);
    }


    private void goToMyWorld(){
        mProgressBar.setVisibility(View.GONE);
        Intent intent = new Intent(mActivity,MyWorldActivity.class);
        intent.putExtra(DeviceDetailActivity.EXTRA_DEVICE, mBluetoothDevice);
        mActivity.startActivity(intent);
    }


    private void showToast(String str){
        Toast toast = Toast.makeText(mActivity,str,Toast.LENGTH_LONG);
        toast.show();
    }


    class PageJumpHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showToast("connected");
            unregisterReceiver(receiver);
            goToDeviceDetail();
        }
    }

}
