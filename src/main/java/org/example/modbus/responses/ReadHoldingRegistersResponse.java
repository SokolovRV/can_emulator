package org.example.modbus.responses;

import org.example.modbus.ModbusRequestResponse;
import org.example.modbus.constants.ModbusFunctionCodes;
import org.example.modbus.constants.ModbusMessageType;

import java.util.Arrays;

public class ReadHoldingRegistersResponse implements ModbusRequestResponse {
    private int deviceAddress;
    private final int functionCode;
    private final ModbusMessageType type;
    private int byteQuantity = 0;
    private int[] registers;

    public ReadHoldingRegistersResponse() {
        this.functionCode = ModbusFunctionCodes.READ_HOLDING_REG;
        this.type = ModbusMessageType.RESPONSE;
    }

    public ReadHoldingRegistersResponse(int deviceAddress, int byteQuantity, int[] registers) {
        this.functionCode = ModbusFunctionCodes.READ_HOLDING_REG;
        this.type = ModbusMessageType.RESPONSE;
        this.deviceAddress = deviceAddress;
        this.byteQuantity = byteQuantity;
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

    public int getByteQuantity() {
        return byteQuantity;
    }

    public void setByteQuantity(int byteQuantity) {
        this.byteQuantity = byteQuantity;
    }

    public int[] getRegisters() {
        return registers;
    }

    public void setRegisters(int[] registers) {
        this.registers = registers;
    }

    @Override
    public int getDeviceAddress() {
        return 0;
    }

    public void setDeviceAddress(int deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    @Override
    public String toString() {
        return "ReadHoldingRegistersResponse{" +
                "deviceAddress=" + deviceAddress +
                ", functionCode=" + functionCode +
                ", byteQuantity=" + byteQuantity +
                ", registers=" + Arrays.toString(registers) +
                '}';
    }
}
