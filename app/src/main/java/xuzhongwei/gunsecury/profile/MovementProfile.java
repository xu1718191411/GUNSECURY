package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import java.util.List;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import xuzhongwei.gunsecury.util.Adapter.Point3D;
import xuzhongwei.gunsecury.util.Adapter.sensor.Sensor;

public class MovementProfile extends GenericBleProfile {
    public MovementProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService,BluetoothDevice device) {
        super(bluetoothLeService, bluetoothGattService,device);


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


    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(GattInfo.UUID_MOV_SERV.toString())) == 0) {
            return true;
        }
        else return false;
    }


    @Override
    public void enableService() {
        byte b[] = new byte[] {0x7F,0x00};

        int error = mBluetoothLeService.writeCharacteristic(this.configData, b);
        if (error != 0) {
            if (this.configData != null)
                Log.d("SensorTagMovementProfile","Sensor config failed: " + this.configData.getUuid().toString() + " Error: " + error);
        }
//        error = this.mBluetoothLeService.setCharacteristicNotification(this.configData, true);
//        if (error != 0) {
//            if (this.configData != null)
//                Log.d("SensorTagMovementProfile","Sensor notification enable failed: " + this.configData.getUuid().toString() + " Error: " + error);
//        }

        this.isEnabled = true;
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
