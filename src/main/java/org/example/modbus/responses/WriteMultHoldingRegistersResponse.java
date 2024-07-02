package org.example.modbus.responses;

import org.example.modbus.ModbusRequestResponse;
import org.example.modbus.constants.ModbusFunctionCodes;
import org.example.modbus.constants.ModbusMessageType;

public class WriteMultHoldingRegistersResponse implements ModbusRequestResponse {
    private int deviceAddress;
    private final int functionCode;
    private final ModbusMessageType type;
    private int startAddress = 0;
    private int quantRegisters = 0;

    public WriteMultHoldingRegistersResponse() {
        this.functionCode = ModbusFunctionCodes.WRITE_MULTI_HOLDING_REG;
        this.type = ModbusMessageType.RESPONSE;
    }

    public WriteMultHoldingRegistersResponse(int deviceAddress, int startAddress, int quantRegisters) {
        this.functionCode = ModbusFunctionCodes.WRITE_MULTI_HOLDING_REG;
        this.type = ModbusMessageType.RESPONSE;
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

    @Override
    public int getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(int deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    @Override
    public String toString() {
        return "WriteMultHoldingRegistersResponse{" +
                "deviceAddress=" + deviceAddress +
                ", functionCode=" + functionCode +
                ", startAddress=" + startAddress +
                ", quantRegisters=" + quantRegisters +
                '}';
    }
}
