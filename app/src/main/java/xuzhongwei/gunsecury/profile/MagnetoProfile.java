package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.service.BluetoothLeService;

public class MagnetoProfile extends GenericBleProfile {
    public MagnetoProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService) {
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
}
