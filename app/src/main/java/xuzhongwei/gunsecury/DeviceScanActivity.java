package xuzhongwei.gunsecury;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import xuzhongwei.gunsecury.controllers.BLEController;
import xuzhongwei.gunsecury.model.BLEDeviceDAO;
import xuzhongwei.gunsecury.util.Adapter.DeviceScanResultAdapter;

public class DeviceScanActivity extends AppCompatActivity {
    private ProgressBar mProgressBar = null;
    private Activity mActivity;
    private BLEController mBLEController;
    private ListView bleScanListView;
    private DeviceScanResultAdapter mDeviceScanResultAdapter;
    private ArrayList<BLEDeviceDAO> deviceList = new ArrayList<BLEDeviceDAO>();

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

        bleScanListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                connnect();
            }
        });
    }


    public void startScan(View view){


        mBLEController.setmOnBleDeviceListener(new BLEController.OnBleDeviceListener() {
            @Override
            public void onDeviceDiscoverd(BLEDeviceDAO device) {
                addIntoDeviceList(device);
            }

            @Override
            public void onDeviceDiscoveryStopped() {
                showBLEDevice();
            }
        });


        mBLEController.startScan();
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

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(mActivity.getResources().getString(R.string.scaning_complete_string));
                            goToDeviceDetail();
                        }
                    });

                }
            },3000);


        showToast(getResources().getString(R.string.scaning_string));
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

}
