package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import xuzhongwei.gunsecury.service.BluetoothLeService;

public class GenericBleProfile {
    protected BluetoothGattCharacteristic normalData;
    protected BluetoothGattCharacteristic configData;
    protected BluetoothGattCharacteristic periodData;
    protected   BluetoothLeService mBluetoothLeService;
    protected BluetoothGattService mBluetoothGattService;
    protected OnDataChangedListener mOnDataChangedListener;
    protected BluetoothDevice mBTDevice;
    public boolean isEnabled;
    public boolean isConfigured;

    public void setmOnDataChangedListener(OnDataChangedListener mOnDataChangedListener) {
        this.mOnDataChangedListener = mOnDataChangedListener;
    }

    public GenericBleProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService,BluetoothDevice device) {
        this.mBluetoothLeService = bluetoothLeService;
        this.mBluetoothGattService = bluetoothGattService;
        this.mBTDevice = device;
    }

    public void enableService(){

        int error = mBluetoothLeService.writeCharacteristic(this.configData, (byte)0x01);
        if (error != 0) {
            if (this.configData != null)
                printError("Sensor enable failed: ",this.configData,error);
        }
        //this.periodWasUpdated(1000);
        this.isEnabled = true;


    }


    public void configureService(){
//        mBluetoothLeService.writeCharacteristic(configData,(byte)0x01);
        mBluetoothLeService.setCharacteristicNotification(normalData,true);

        int error = this.mBluetoothLeService.setCharacteristicNotification(this.normalData, true);
        if (error != 0) {
            if (this.normalData != null)
                printError("Sensor notification enable failed: ",this.normalData,error);
        }
        this.isConfigured = true;


    }

    public Boolean checkNormalData(String uuid){
        if(uuid == null) return false;
        if(normalData == null) return false;
        if(uuid.equals(normalData.getUuid().toString())) return true;
        return false;
    }

    public void updateData(byte[] data){

    }

    public interface OnDataChangedListener{
        void onDataChanged(String data);
    }

    public void printError (String msg, BluetoothGattCharacteristic c, int error) {
        try {
            Log.d("GenericBluetoothProfile", msg + c.getUuid().toString() + " Error: " + error);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
