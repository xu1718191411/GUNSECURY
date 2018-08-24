package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import xuzhongwei.gunsecury.util.Adapter.Point3D;
import xuzhongwei.gunsecury.util.Adapter.sensor.Sensor;

public class AcceleroteProfile extends GenericBleProfile {
    public AcceleroteProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService) {
        super(bluetoothLeService, bluetoothGattService);

        List<BluetoothGattCharacteristic> charalist = bluetoothGattService.getCharacteristics();
        for(BluetoothGattCharacteristic c:charalist){

            if(c.getUuid().toString().equals(GattInfo.UUID_ACC_DATA.toString())){
                this.normalData = c;
            }

            if(c.getUuid().toString().equals(GattInfo.UUID_ACC_CONF.toString())){
                this.configData = c;
            }

            if(c.getUuid().toString().equals(GattInfo.UUID_ACC_PERI.toString())){
                this.periodData = c;
            }
        }
    }


    public void updateData(byte[] value){

        Point3D v = Sensor.ACCELEROMETER.convert(value);
        double a = v.x;
        String res = a + "";
        if(mOnDataChangedListener != null){
            mOnDataChangedListener.onDataChanged(res);
        }

    }
}
