package org.example.modbus.responses;

import org.example.modbus.ModbusRequestResponse;
import org.example.modbus.constants.ModbusFunctionCodes;
import org.example.modbus.constants.ModbusMessageType;

import java.util.Arrays;

public class WriteFileResponse implements ModbusRequestResponse {
    private int address;
    private int functionCode;
    private ModbusMessageType type;

    private int quantBytes;
    private int fileNum;
    private int recordNum;
    private int recordLen;
    private int[] registers;

    public WriteFileResponse() {
        this.functionCode = ModbusFunctionCodes.WRITE_FILE;
        this.type = ModbusMessageType.RESPONSE;
    }

    public WriteFileResponse(int address, int quantBytes, int fileNum, int recordNum, int recordLen, int[] registers) {
        this.functionCode = ModbusFunctionCodes.WRITE_FILE;
        this.type = ModbusMessageType.RESPONSE;
        this.address = address;
        this.quantBytes = quantBytes;
        this.fileNum = fileNum;
        this.recordNum = recordNum;
        this.recordLen = recordLen;
        this.registers = registers;
    }

    @Override
    public int getFunctionCode() {
        return this.functionCode;
    }

    @Override
    public int getDeviceAddress() {
        return this.address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public ModbusMessageType getType() {
        return this.type;
    }

    public int getQuantBytes() {
        return quantBytes;
    }

    public void setQuantBytes(int quantBytes) {
        this.quantBytes = quantBytes;
    }

    public int getFileNum() {
        return fileNum;
    }

    public void setFileNum(int fileNum) {
        this.fileNum = fileNum;
    }

    public int getRecordNum() {
        return recordNum;
    }

    public void setRecordNum(int recordNum) {
        this.recordNum = recordNum;
    }

    public int getRecordLen() {
        return recordLen;
    }

    public void setRecordLen(int recordLen) {
        this.recordLen = recordLen;
    }

    public int[] getRegisters() {
        return registers;
    }

    public void setRegisters(int[] registers) {
        this.registers = registers;
    }

    @Override
    public String toString() {
        return "WriteFileResponse{" +
                "address=" + address +
                ", functionCode=" + functionCode +
                ", type=" + type +
                ", quantBytes=" + quantBytes +
                ", fileNum=" + fileNum +
                ", recordNum=" + recordNum +
                ", recordLen=" + recordLen +
                ", registers=" + Arrays.toString(registers) +
                '}';
    }
}
