package xuzhongwei.gunsecury;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.controllers.BLEController;
import xuzhongwei.gunsecury.profile.GenericBleProfile;
import xuzhongwei.gunsecury.profile.HumidityProfile;
import xuzhongwei.gunsecury.profile.IRTTemperature;
import xuzhongwei.gunsecury.service.BluetoothLeService;

public class DeviceDetailActivity extends AppCompatActivity {
    BLEController mainController;
    private String[] mPlanetTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Activity mActivity;
    private BroadcastReceiver receiver;
    private BluetoothLeService mBluetoothLeService;
    ArrayList<GenericBleProfile> bleProfiles = new ArrayList<GenericBleProfile>();
    List<BluetoothGattService> bleServiceList = new ArrayList<BluetoothGattService>();
    ArrayList<BluetoothGattCharacteristic> characteristicList = new ArrayList<BluetoothGattCharacteristic>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail);
        startBLEService();
        mainController = new BLEController(this);
        initialLayout();
        initialReceiver();

    }


    private void initialLayout(){
        mActivity = this;
        mPlanetTitles = new String[]{"周囲温度", "赤外線温度", "加速度", "湿度","磁気","気圧","ジャイロスコープ","DeviceInformation"};

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectItem(position);
            }
        });

    }


    private void selectItem(int position) {
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
        ChangeContent(position);
    }

    private void ChangeContent(int n){
        String[] ids = {"ambient_temprature_layout","ir_temprature_layout","ir_accelerometer_layout","ir_humidity_layout","ir_magnetometer_layout","ir_barometer_layout","ir_gyroscope_layout","deviceInformationLayout"};


        for(int i=0;i<ids.length;i++){
            ((LinearLayout) findViewById(getResourceId(ids[i],"id",getPackageName()))).setVisibility(View.GONE);
        }

        ((LinearLayout) findViewById(getResourceId(ids[n],"id",getPackageName()))).setVisibility(View.VISIBLE);

    }

    private  int getResourceId(String pVariableName, String pResourcename, String pPackageName)
    {
        try {
            return getResources().getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void startBLEService(){
        mBluetoothLeService = BluetoothLeService.getInstance();
        bleServiceList = mBluetoothLeService.getBLEService();
        for(int s=0;s<bleServiceList.size();s++){
            if(bleServiceList.get(s).getUuid().toString().compareTo(GattInfo.UUID_HUM_SERV.toString()) == 0){
                BluetoothGattService service = bleServiceList.get(s);//not all of the service but the service that is indicated to the HUMIDITY Service
                HumidityProfile humidityProfile = new HumidityProfile(mBluetoothLeService,service);


                humidityProfile.setmOnDataChangedListener(new GenericBleProfile.OnDataChangedListener() {
                    @Override
                    public void onDataChanged(String data) {
                        ((TextView) mActivity.findViewById(R.id.humidityValue)).setText(data);
                    }
                });

                bleProfiles.add(humidityProfile);
            }

            if(bleServiceList.get(s).getUuid().toString().compareTo(GattInfo.UUID_IRT_SERV.toString()) == 0){
                BluetoothGattService service = bleServiceList.get(s);//not all of the service but the service that is indicated to the HUMIDITY Service
                IRTTemperature iRTTemperature = new IRTTemperature(mBluetoothLeService,service);

                iRTTemperature.setmOnDataChangedListener(new GenericBleProfile.OnDataChangedListener() {
                    @Override
                    public void onDataChanged(String data) {
                        ((TextView) mActivity.findViewById(R.id.irTempratureValue)).setText(data);
                    }
                });

                bleProfiles.add(iRTTemperature);
            }
        }



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

    }


    private void initialReceiver(){

        receiver  = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {



                if(intent.getAction().equals(BluetoothLeService.ACTION_DATA_NOTIFY)){
                    byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                    String uuidStr = intent.getStringExtra(BluetoothLeService.EXTRA_UUID);

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
    }




}
