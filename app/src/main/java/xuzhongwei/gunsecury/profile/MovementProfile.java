package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import xuzhongwei.gunsecury.util.Adapter.Point3D;
import xuzhongwei.gunsecury.util.Adapter.sensor.Sensor;

public class MovementProfile extends GenericBleProfile {
    public MovementProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService) {
        super(bluetoothLeService, bluetoothGattService);


        List<BluetoothGattCharacteristic> charalist = bluetoothGattService.getCharacteristics();
        for(BluetoothGattCharacteristic c:charalist){

            if(c.getUuid().toString().equals(GattInfo.UUID_MOV_DATA.toString())){
                this.normalData = c;
            }

            if(c.getUuid().toString().equals(GattInfo.UUID_MOV_CONF.toString())){
                this.configData = c;
            }

            if(c.getUuid().toString().equals(GattInfo.UUID_MOV_PERI.toString())){
                this.periodData = c;
            }
        }

    }


    public void updateData(byte[] value){
        Point3D v;
        String res = "";
        v = Sensor.MOVEMENT_ACC.convert(value);
        res = v.x+"-"+v.y+"-"+v.z+"\n";

        v = Sensor.MOVEMENT_GYRO.convert(value);
        res += v.x+"-"+v.y+"-"+v.z+"\n";

        v = Sensor.MOVEMENT_MAG.convert(value);
        res += v.x+"-"+v.y+"-"+v.z+"\n";

        if(mOnDataChangedListener != null){
            mOnDataChangedListener.onDataChanged(res);
        }
    }
}
