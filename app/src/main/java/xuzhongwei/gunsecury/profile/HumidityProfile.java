package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import xuzhongwei.gunsecury.util.Adapter.Point3D;
import xuzhongwei.gunsecury.util.Adapter.sensor.Sensor;

public class HumidityProfile extends GenericBleProfile {
    public HumidityProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService) {
        super(bluetoothLeService,bluetoothGattService);
        List<BluetoothGattCharacteristic> charalist = bluetoothGattService.getCharacteristics();
        for(BluetoothGattCharacteristic c:charalist){

            if(c.getUuid().toString().equals(GattInfo.UUID_HUM_DATA.toString())){
                this.normalData = c;
            }

            if(c.getUuid().toString().equals(GattInfo.UUID_HUM_CONF.toString())){
                this.configData = c;
            }

            if(c.getUuid().toString().equals(GattInfo.UUID_HUM_PERI.toString())){
                this.periodData = c;
            }
        }
    }

    public void configureService(){
//        mBluetoothLeService.writeCharacteristic(configData,(byte)0x01);
        mBluetoothLeService.setCharacteristicNotification(normalData,true);
    }

    public void writeCharactristic(){
        mBluetoothLeService.writeCharacteristic(this.configData);
    }


    public void updateData(byte[] value){

        Point3D v = Sensor.HUMIDITY2.convert(value);
        double a = v.x;


    }
}