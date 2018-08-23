package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import xuzhongwei.gunsecury.service.BluetoothLeService;

public class GenericBleProfile {
    protected BluetoothGattCharacteristic normalData;
    protected BluetoothGattCharacteristic configData;
    protected BluetoothGattCharacteristic periodData;
    protected   BluetoothLeService mBluetoothLeService;
    protected BluetoothGattService mBluetoothGattService;
    protected OnDataChangedListener mOnDataChangedListener;

    public void setmOnDataChangedListener(OnDataChangedListener mOnDataChangedListener) {
        this.mOnDataChangedListener = mOnDataChangedListener;
    }

    public GenericBleProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService) {
        this.mBluetoothLeService = bluetoothLeService;
        this.mBluetoothGattService = bluetoothGattService;
    }

    public void enableService(){
        mBluetoothLeService.writeCharacteristic(this.configData, (byte)0x01);
    }


    public void configureService(){
//        mBluetoothLeService.writeCharacteristic(configData,(byte)0x01);
        mBluetoothLeService.setCharacteristicNotification(normalData,true);
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


}
