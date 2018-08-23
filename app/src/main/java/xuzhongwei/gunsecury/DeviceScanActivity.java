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

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.controllers.BLEController;
import xuzhongwei.gunsecury.model.BLEDeviceDAO;
import xuzhongwei.gunsecury.profile.GenericBleProfile;
import xuzhongwei.gunsecury.profile.HumidityProfile;
import xuzhongwei.gunsecury.profile.IRTTemperature;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import xuzhongwei.gunsecury.util.Adapter.DeviceScanResultAdapter;

public class DeviceScanActivity extends AppCompatActivity {
    private ProgressBar mProgressBar = null;
    private Activity mActivity;
    private BLEController mBLEController;
    private ListView bleScanListView;
    private DeviceScanResultAdapter mDeviceScanResultAdapter;
    private ArrayList<BLEDeviceDAO> deviceList = new ArrayList<BLEDeviceDAO>();
    private BluetoothLeService mBluetoothLeService;
    private UIHandler mUIHandler;
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

//        mBluetoothLeService.setmOnBleDeviceListener(new BLEController.OnBleDeviceListener() {
//            @Override
//            public void onDeviceDiscoverd(BLEDeviceDAO device) {
//                addIntoDeviceList(device);
//            }
//
//            @Override
//            public void onDeviceDiscoveryStopped() {
//                showBLEDevice();
//            }
//
//            @Override
//            public void onDeviceServiceDiscoverd(List<BluetoothGattService> list) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        showToast(mActivity.getResources().getString(R.string.scaning_complete_string));
//                        //goToDeviceDetail();
//                    }
//                });
//
//            }
//        });

        mUIHandler = new UIHandler();


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
                }else if(intent.getAction().equals(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)){
                    bleServiceList = mBluetoothLeService.getBLEService();

                    if(bleServiceList.size() > 0){
                        for(int i=0;i<bleServiceList.size();i++){
                            List<BluetoothGattCharacteristic> characteristics = bleServiceList.get(i).getCharacteristics();
                            if(characteristics.size() > 0){
                                for(int j=0;j<characteristics.size();j++){
                                    characteristicList.add(characteristics.get(j));
                                }
                            }
                        }
                    }

                    Message msg = new Message();
                    msg.what = CHARACTERISTICS_FOUND;
                    Bundle bundle = new Bundle();
                    bundle.putInt(CHARACTERISTICS_FOUND_RESULT,characteristicList.size());
                    msg.setData(bundle);
                    mUIHandler.sendMessage(msg);

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            //loop the GattService and retrieve each Service towards HUMIDITY,TEMPERATURE,GRAVITY......
                            for(int s=0;s<bleServiceList.size();s++){
                                    if(bleServiceList.get(s).getUuid().toString().compareTo(GattInfo.UUID_HUM_SERV.toString()) == 0){
                                        BluetoothGattService service = bleServiceList.get(s);//not all of the service but the service that is indicated to the HUMIDITY Service
                                        HumidityProfile humidityProfile = new HumidityProfile(mBluetoothLeService,service);
                                        humidityProfile.configureService();
                                        try{
                                            Thread.sleep(1000);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        bleProfiles.add(humidityProfile);
                                    }

                                    if(bleServiceList.get(s).getUuid().toString().compareTo(GattInfo.UUID_IRT_SERV.toString()) == 0){

                                        BluetoothGattService service = bleServiceList.get(s);//not all of the service but the service that is indicated to the HUMIDITY Service
                                        IRTTemperature iRTTemperature = new IRTTemperature(mBluetoothLeService,service);
                                        iRTTemperature.configureService();
                                        try{
                                            Thread.sleep(1000);
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                        bleProfiles.add(iRTTemperature);
                                    }
                            }

                            for(final GenericBleProfile p:bleProfiles){
                                p.enableService();
                            }


//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    for(final GenericBleProfile p:bleProfiles){
//                                    p.enableService();
//                                    }
//                                }
//                            });


                        }
                    });

                    thread.start();

                }else if(intent.getAction().equals(BluetoothLeService.ACTION_DATA_NOTIFY)){
                    byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);


                    try{
                        Thread.sleep(1000);
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                    for(int i=0;i<characteristicList.size();i++){
                        BluetoothGattCharacteristic bleCharacteristic = characteristicList.get(i);
                        if(bleCharacteristic.getUuid().toString().equals(uuidStr)){
                            for(int j=0;j<bleProfiles.size();j++){
                                if(bleProfiles.get(j).checkNormalData(uuidStr)){
                                    bleProfiles.get(j).updateData(value);
                                }
                            }
                        }

                    }



                }else{

                }

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.FIND_NEW_BLE_DEVICE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_NOTIFY);
        registerReceiver(receiver,intentFilter);


        bleScanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(deviceList == null) return;
                if(deviceList.size() <= i) return;
                mBluetoothLeService.connectDevice(deviceList.get(i).getDevice());
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
        mActivity.startActivity(intent);
    }

    private void showToast(String str){
        Toast toast = Toast.makeText(mActivity,str,Toast.LENGTH_LONG);
        toast.show();
    }



    class UIHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case CHARACTERISTICS_FOUND:
                    int res = msg.getData().getInt(CHARACTERISTICS_FOUND_RESULT);
                    showToast(res+"");
                    PageJumpHandler pageJumpHandler = new PageJumpHandler();
                    pageJumpHandler.sendEmptyMessage(5000);
                    break;
            }
        }
    }

    class PageJumpHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            unregisterReceiver(receiver);
            goToDeviceDetail();
        }
    }

}
