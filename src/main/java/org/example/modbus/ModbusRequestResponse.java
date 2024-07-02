package org.example.modbus;

import org.example.modbus.constants.ModbusMessageType;

public interface ModbusRequestResponse {
    int getFunctionCode();
    int getDeviceAddress();
    ModbusMessageType getType();
}
