package org.example.modbus.requests;

import org.example.modbus.ModbusRequestResponse;
import org.example.modbus.constants.ModbusFunctionCodes;
import org.example.modbus.constants.ModbusMessageType;

public class ReadInputRegistersRequest implements ModbusRequestResponse {
    private int deviceAddress;
    private final int functionCode;
    private final ModbusMessageType type;
    private int startAddress = 0;
    private int quantRegisters = 0;

    public ReadInputRegistersRequest() {
        this.type = ModbusMessageType.REQUEST;
        this.functionCode = ModbusFunctionCodes.READ_INPUT_REG;
    }

    public ReadInputRegistersRequest(int deviceAddress, int startAddress, int quantRegisters) {
        this.type = ModbusMessageType.REQUEST;
        this.functionCode = ModbusFunctionCodes.READ_INPUT_REG;
        this.deviceAddress = deviceAddress;
        this.startAddress = startAddress;
        this.quantRegisters = quantRegisters;
    }

    @Override
    public int getFunctionCode() {
        return functionCode;
    }

    @Override
    public ModbusMessageType getType() {
        return type;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(int startAddress) {
        this.startAddress = startAddress;
    }

    public int getQuantRegisters() {
        return quantRegisters;
    }

    public void setQuantRegisters(int quantRegisters) {
        this.quantRegisters = quantRegisters;
    }

    public int getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(int deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    @Override
    public String toString() {
        return "ReadInputRegistersRequest{" +
                "DeviceAddress=" + deviceAddress +
                ", functionCode=" + functionCode +
                ", startAddress=" + startAddress +
                ", quantRegisters=" + quantRegisters +
                '}';
    }
}
