package org.example.modbus.constants;

public abstract class ModbusFunctionCodes {
    public static final int READ_HOLDING_REG = 3;
    public static final int READ_INPUT_REG = 4;
    public static final int WRITE_MULTI_HOLDING_REG = 16;
    public static final int WRITE_FILE = 21;
    public static final int MANUAL_BRUTEFORCE = 100;
    public static final int MANUAL_CYCLE_EXCHANGE = 101;
    public static final int MANUAL_TIME_UPDATE = 102;
}
