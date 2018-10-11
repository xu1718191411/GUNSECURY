package xuzhongwei.gunsecury.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

import xuzhongwei.gunsecury.common.GattInfo;
import xuzhongwei.gunsecury.service.BluetoothLeService;
import xuzhongwei.gunsecury.util.Adapter.Point3D;
import xuzhongwei.gunsecury.util.Adapter.sensor.BarometerCalibrationCoefficients;
import xuzhongwei.gunsecury.util.Adapter.sensor.Sensor;

public class BarometerProfile extends GenericBleProfile {
    private static final double PA_PER_METER = 12.0;

    public BarometerProfile(BluetoothLeService bluetoothLeService, BluetoothGattService bluetoothGattService, BluetoothDevice device) {
        super(bluetoothLeService, bluetoothGattService, device);

        List<BluetoothGattCharacteristic> charalist = bluetoothGattService.getCharacteristics();
        for (BluetoothGattCharacteristic c : charalist) {

            if (c.getUuid().toString().equals(GattInfo.UUID_BAR_DATA.toString())) {
                this.normalData = c;
            }

            if (c.getUuid().toString().equals(GattInfo.UUID_BAR_CONF.toString())) {
                this.configData = c;
            }

            if (c.getUuid().toString().equals(GattInfo.UUID_BAR_PERI.toString())) {
                this.periodData = c;
            }
        }

    }


    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(GattInfo.UUID_BAR_SERV.toString())) == 0) {
            return true;
        } else return false;
    }


    public void updateData(byte[] value) {

        Point3D v;
        v = Sensor.BAROMETER.convert(value);

        double h = (v.x - BarometerCalibrationCoefficients.INSTANCE.heightCalibration)
                / PA_PER_METER;
        h = (double) Math.round(-h * 10.0) / 10.0;

        if (mOnDataChangedListener != null) {
            mOnDataChangedListener.onDataChanged(String.format("%.1f mBar %.1f meter", v.x / 100, h));
        }


    }
}
