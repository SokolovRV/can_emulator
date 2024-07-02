package org.example.modbus.constants;

public abstract class ModbusErrorCodes {
    public static final int NO_ERROR = 0;
    public static final int RTU_INVALID_FUNCTION = 1;
    public static final int RTU_UNAVAIBLE_REGISTERS_ADDR = 2;
    public static final int RTU_INVALID_REQUEST_DATA = 3;
    public static final int RTU_EXECUTION_ERROR = 4;
    public static final int RTU_SERVER_DEVICE_BUSY = 6;

    public static final int SYSTEM_INVALID_CRC = -1;
    public static final int SYSTEM_EXECUTION_ERROR = -2;
    public static final int SYSTEM_TIMEOUT = -3;
}
