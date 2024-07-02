package org.example.modbus.requests;

import org.example.modbus.ModbusRequestResponse;
import org.example.modbus.constants.ModbusFunctionCodes;
import org.example.modbus.constants.ModbusMessageType;

import java.util.Arrays;

public class WriteMultHoldingRegistersRequest implements ModbusRequestResponse {
    private int deviceAddress;
    private final int functionCode;
    private final ModbusMessageType type;

    private int startAddress = 0;
    private int quantRegisters = 0;
    private int quantBytes = 0;
    private int[] registers;

    public WriteMultHoldingRegistersRequest() {
        this.functionCode = ModbusFunctionCodes.WRITE_MULTI_HOLDING_REG;
        this.type = ModbusMessageType.REQUEST;
    }

    public WriteMultHoldingRegistersRequest(int deviceAddress, int startAddress, int quantRegisters, int quantBytes, int[] registers) {
        this.functionCode = ModbusFunctionCodes.WRITE_MULTI_HOLDING_REG;
        this.type = ModbusMessageType.REQUEST;
        this.deviceAddress = deviceAddress;
        this.startAddress = startAddress;
        this.quantRegisters = quantRegisters;
        this.quantBytes = quantBytes;
        this.registers = registers;
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

    public int getQuantBytes() {
        return quantBytes;
    }

    public void setQuantBytes(int quantBytes) {
        this.quantBytes = quantBytes;
    }

    public int[] getRegisters() {
        return registers;
    }

    public void setRegisters(int[] registers) {
        this.registers = registers;
    }

    public int getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(int deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    @Override
    public String toString() {
        return "WriteMultHoldingRegistersRequest{" +
                "DeviceAddress=" + deviceAddress +
                ", functionCode=" + functionCode +
                ", startAddress=" + startAddress +
                ", quantRegisters=" + quantRegisters +
                ", quantBytes=" + quantBytes +
                ", registers=" + Arrays.toString(registers) +
                '}';
    }
}
