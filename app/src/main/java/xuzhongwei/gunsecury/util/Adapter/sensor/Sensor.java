package xuzhongwei.gunsecury.util.Adapter.sensor;


/*
* referenced from SensorTag sample code
* */

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

import static java.lang.Math.pow;
import static xuzhongwei.gunsecury.common.GattInfo.UUID_HUM_CONF;
import static xuzhongwei.gunsecury.common.GattInfo.UUID_HUM_DATA;
import static xuzhongwei.gunsecury.common.GattInfo.UUID_HUM_SERV;
import static xuzhongwei.gunsecury.common.GattInfo.UUID_IRT_CONF;
import static xuzhongwei.gunsecury.common.GattInfo.UUID_IRT_DATA;
import static xuzhongwei.gunsecury.common.GattInfo.UUID_IRT_SERV;
import xuzhongwei.gunsecury.util.Adapter.Point3D;

/**
 * This enum encapsulates the differences amongst the sensors. The differences include UUID values and how to interpret the
 * characteristic-containing-measurement.
 */
public enum Sensor {
    IR_TEMPERATURE(UUID_IRT_SERV, UUID_IRT_DATA, UUID_IRT_CONF) {
        @Override
        public Point3D convert(final byte [] value) {

            /*
             * The IR Temperature sensor produces two measurements; Object ( AKA target or IR) Temperature, and Ambient ( AKA die ) temperature.
             * Both need some conversion, and Object temperature is dependent on Ambient temperature.
             * They are stored as [ObjLSB, ObjMSB, AmbLSB, AmbMSB] (4 bytes) Which means we need to shift the bytes around to get the correct values.
             */

            double ambient = extractAmbientTemperature(value);
            double target = extractTargetTemperature(value, ambient);
            double targetNewSensor = extractTargetTemperatureTMP007(value);
            return new Point3D(ambient, target, targetNewSensor);
        }

        private double extractAmbientTemperature(byte [] v) {
            int offset = 2;
            return shortUnsignedAtOffset(v, offset) / 128.0;
        }

        private double extractTargetTemperature(byte [] v, double ambient) {
            Integer twoByteValue = shortSignedAtOffset(v, 0);

            double Vobj2 = twoByteValue.doubleValue();
            Vobj2 *= 0.00000015625;

            double Tdie = ambient + 273.15;

            double S0 = 5.593E-14; // Calibration factor
            double a1 = 1.75E-3;
            double a2 = -1.678E-5;
            double b0 = -2.94E-5;
            double b1 = -5.7E-7;
            double b2 = 4.63E-9;
            double c2 = 13.4;
            double Tref = 298.15;
            double S = S0 * (1 + a1 * (Tdie - Tref) + a2 * pow((Tdie - Tref), 2));
            double Vos = b0 + b1 * (Tdie - Tref) + b2 * pow((Tdie - Tref), 2);
            double fObj = (Vobj2 - Vos) + c2 * pow((Vobj2 - Vos), 2);
            double tObj = pow(pow(Tdie, 4) + (fObj / S), .25);

            return tObj - 273.15;
        }
        private double extractTargetTemperatureTMP007(byte [] v) {
            int offset = 0;
            return shortUnsignedAtOffset(v, offset) / 128.0;
        }
    },
    HUMIDITY(UUID_HUM_SERV, UUID_HUM_DATA, UUID_HUM_CONF) {
        @Override
        public Point3D convert(final byte[] value) {
            int a = shortUnsignedAtOffset(value, 2);
            // bits [1..0] are status bits and need to be cleared according
            // to the user guide, but the iOS code doesn't bother. It should
            // have minimal impact.
            a = a - (a % 4);

            return new Point3D((-6f) + 125f * (a / 65535f), 0, 0);
        }
    },
    HUMIDITY2(UUID_HUM_SERV, UUID_HUM_DATA, UUID_HUM_CONF) {
        @Override
        public Point3D convert(final byte[] value) {
            int a = shortUnsignedAtOffset(value, 2);

            return new Point3D(100f * (a / 65535f), 0, 0);
        }
    };


    /**
     * Gyroscope, Magnetometer, Barometer, IR temperature all store 16 bit two's complement values as LSB MSB, which cannot be directly parsed
     * as getIntValue(FORMAT_SINT16, offset) because the bytes are stored as little-endian.
     *
     * This function extracts these 16 bit two's complement values.
     * */
    private static Integer shortSignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
        return (upperByte << 8) + lowerByte;
    }

    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }
    private static Integer twentyFourBitUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer mediumByte = (int) c[offset+1] & 0xFF;
        Integer upperByte = (int) c[offset + 2] & 0xFF;
        return (upperByte << 16) + (mediumByte << 8) + lowerByte;
    }

    public void onCharacteristicChanged(BluetoothGattCharacteristic c) {
        throw new UnsupportedOperationException("Error: the individual enum classes are supposed to override this method.");
    }


    public Point3D convert(byte[] value) {
        throw new UnsupportedOperationException("Error: the individual enum classes are supposed to override this method.");
    }

    private final UUID service, data, config;
    private byte enableCode; // See getEnableSensorCode for explanation.
    public static final byte DISABLE_SENSOR_CODE = 0;
    public static final byte ENABLE_SENSOR_CODE = 1;
    public static final byte CALIBRATE_SENSOR_CODE = 2;

    /**
     * Constructor called by the Gyroscope and Accelerometer because it more than a boolean enable
     * code.
     */
    private Sensor(UUID service, UUID data, UUID config, byte enableCode) {
        this.service = service;
        this.data = data;
        this.config = config;
        this.enableCode = enableCode;
    }

    /**
     * Constructor called by all the sensors except Gyroscope
     * */
    private Sensor(UUID service, UUID data, UUID config) {
        this.service = service;
        this.data = data;
        this.config = config;
        this.enableCode = ENABLE_SENSOR_CODE; // This is the sensor enable code for all sensors except the gyroscope
    }


    public UUID getData() {
        return data;
    }



}
