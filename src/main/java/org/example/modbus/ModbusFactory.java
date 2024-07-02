package org.example.modbus;

import org.example.modbus.constants.ModbusErrorCodes;
import org.example.modbus.constants.ModbusFunctionCodes;
import org.example.modbus.constants.ModbusMessageType;
import org.example.modbus.requests.ReadHoldingRegistersRequest;
import org.example.modbus.requests.ReadInputRegistersRequest;
import org.example.modbus.requests.WriteFileRequest;
import org.example.modbus.requests.WriteMultHoldingRegistersRequest;
import org.example.modbus.responses.ReadHoldingRegistersResponse;
import org.example.modbus.responses.ReadInputRegistersResponse;
import org.example.modbus.responses.WriteFileResponse;
import org.example.modbus.responses.WriteMultHoldingRegistersResponse;

import java.util.Arrays;

import static org.example.modbus.constants.ModbusFunctionCodes.*;

public class ModbusFactory {
    public static final int RESPONSE_LEN_DIFF_TCP_RTU = 4;
    private static final ModbusFactory modbusFactoryInstance = new ModbusFactory();

    private ModbusFactory() {
    }

    public static ModbusFactory getInstance() {
        return modbusFactoryInstance;
    }

    public int getCrc16(byte[] array, int length) {
        int crc = 0x0000ffff;
        for (int pos = 0; pos < length; pos++) {
            crc ^= ((int) array[pos] & 0x000000ff);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x00000001) != 0) {
                    crc >>= 1;
                    crc ^= 0x0000a001;
                } else crc >>= 1;
            }
        }

        return crc;
    }

    private byte[] intArrayToBigEndianByteArray(int[] registers) {
        byte[] resArray = new byte[registers.length * 2];
        for (int i = 0; i < registers.length; i++) {
            resArray[i * 2] = (byte) (registers[i] >> 8);
            resArray[i * 2 + 1] = (byte) registers[i];
        }

        return resArray;
    }

    private int[] byteBigEndianArrayToIntArray(byte[] bytes) {
        int[] resArray = new int[bytes.length / 2];
        for (int i = 0; i < resArray.length; i++)
            resArray[i] = concat2BytesToInt(bytes[i * 2], bytes[i * 2 + 1]);

        return resArray;
    }

    private int concat2BytesToInt(byte high, byte low) {
        return ((high & 0xff) << 8) | (low & 0xff);
    }

    private byte[] getReadRegRequest(int deviceAddress, int startAddress, int quantityRegisters, int functionCode) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) deviceAddress;
        bytes[1] = (byte) functionCode;
        bytes[2] = (byte) (startAddress >> 8);
        bytes[3] = (byte) startAddress;
        bytes[4] = (byte) (quantityRegisters >> 8);
        bytes[5] = (byte) quantityRegisters;
        bytes[6] = (byte) (quantityRegisters * 2);
        int crc = getCrc16(bytes, bytes.length - 2);
        bytes[bytes.length - 2] = (byte) crc;
        bytes[bytes.length - 1] = (byte) (crc >> 8);

        return bytes;
    }

    public byte[] getWriteMultiHoldingRegRequest(int deviceAddress, int startAddress, int quantityRegisters, int[] registers) {
        byte[] bytes = new byte[9 + quantityRegisters * 2];
        bytes[0] = (byte) deviceAddress;
        bytes[1] = (byte) WRITE_MULTI_HOLDING_REG;
        bytes[2] = (byte) (startAddress >> 8);
        bytes[3] = (byte) startAddress;
        bytes[4] = (byte) (quantityRegisters >> 8);
        bytes[5] = (byte) quantityRegisters;
        bytes[6] = (byte) (quantityRegisters * 2);
        System.arraycopy(intArrayToBigEndianByteArray(registers), 0, bytes, 7, quantityRegisters * 2);
        int crc = getCrc16(bytes, bytes.length - 2);
        bytes[bytes.length - 2] = (byte) crc;
        bytes[bytes.length - 1] = (byte) (crc >> 8);

        return bytes;
    }

    public byte[] getReadHoldingRegRequest(int deviceAddress, int startAddress, int quantityRegisters) {
        return getReadRegRequest(deviceAddress, startAddress, quantityRegisters, READ_HOLDING_REG);
    }

    public byte[] getReadInputRegRequest(int deviceAddress, int startAddress, int quantityRegisters) {
        return getReadRegRequest(deviceAddress, startAddress, quantityRegisters, READ_INPUT_REG);
    }

    public byte[] getWriteFileRequest(int deviceAddress, int fileNum, int recordNum, int recordLen, int[] registers) {
        byte[] bytes = new byte[12 + (registers.length * 2)];
        bytes[0] = (byte) deviceAddress;
        bytes[1] = (byte) WRITE_FILE;
        bytes[2] = (byte) (7 + (registers.length * 2));
        bytes[3] = 0x06;
        bytes[4] = (byte) ((fileNum >> 8) & 0xff);
        bytes[5] = (byte) (fileNum & 0xff);
        bytes[6] = (byte) ((recordNum >> 8) & 0xff);
        bytes[7] = (byte) (recordNum & 0xff);
        bytes[8] = (byte) ((recordLen >> 8) & 0xff);
        bytes[9] = (byte) (recordLen & 0xff);
        for (int i = 0; i < registers.length; i++) {
            bytes[10 + (i * 2)] = (byte) (registers[i] & 0xff);
            bytes[11 + (i * 2)] = (byte) ((registers[i] >> 8) & 0xff);
        }
        int crc = getCrc16(bytes, bytes.length - 2);
        bytes[bytes.length - 2] = (byte) crc;
        bytes[bytes.length - 1] = (byte) (crc >> 8);

        return bytes;
    }

    public byte[] getRequest(ModbusRequestResponse request) {
        byte[] bytes = new byte[0];

        switch (request.getFunctionCode()) {
            case READ_HOLDING_REG:
                ReadHoldingRegistersRequest readHoldRqst = (ReadHoldingRegistersRequest) request;
                bytes = getReadHoldingRegRequest(readHoldRqst.getDeviceAddress(), readHoldRqst.getStartAddress(),
                        readHoldRqst.getQuantRegisters());
                break;
            case READ_INPUT_REG:
                ReadInputRegistersRequest readInputRqst = (ReadInputRegistersRequest) request;
                bytes = getReadInputRegRequest(readInputRqst.getDeviceAddress(), readInputRqst.getStartAddress(),
                        readInputRqst.getQuantRegisters());
                break;
            case WRITE_MULTI_HOLDING_REG:
                WriteMultHoldingRegistersRequest writeHoldRqst = (WriteMultHoldingRegistersRequest) request;
                bytes = getWriteMultiHoldingRegRequest(writeHoldRqst.getDeviceAddress(),
                        writeHoldRqst.getStartAddress(), writeHoldRqst.getQuantRegisters(), writeHoldRqst.getRegisters());
                break;
            case WRITE_FILE:
                WriteFileRequest writeFileRequest = (WriteFileRequest) request;
                bytes = getWriteFileRequest(writeFileRequest.getDeviceAddress(), writeFileRequest.getFileNum(),
                        writeFileRequest.getRecordNum(), writeFileRequest.getRecordLen(), writeFileRequest.getRegisters());
                break;

            default: break;
        }

        return bytes;
    }


    public ModbusHandlerResult modbusRequestHandler(byte[] bytes) {
        ModbusHandlerResult response = new ModbusHandlerResult();
        if (bytes.length > 0) {
            try {
                if (getCrc16(bytes, bytes.length - 2) == concat2BytesToInt(
                        bytes[bytes.length - 1], bytes[bytes.length - 2])) {
                    switch (bytes[1]) {
                        case WRITE_MULTI_HOLDING_REG:
                            WriteMultHoldingRegistersResponse responseWriteMultHold = new WriteMultHoldingRegistersResponse();
                            responseWriteMultHold.setDeviceAddress(bytes[0] & 0xff);
                            responseWriteMultHold.setStartAddress(concat2BytesToInt(bytes[2], bytes[3]));
                            responseWriteMultHold.setQuantRegisters(concat2BytesToInt(bytes[4], bytes[5]));
                            response.setResponse(responseWriteMultHold);
                            break;
                        case READ_HOLDING_REG:
                            ReadHoldingRegistersResponse responseReadHold = new ReadHoldingRegistersResponse();
                            responseReadHold.setDeviceAddress(bytes[0] & 0xff);
                            responseReadHold.setByteQuantity(bytes[2] & 0xff);
                            responseReadHold.setRegisters(byteBigEndianArrayToIntArray(
                                    Arrays.copyOfRange(bytes, 3, bytes.length - 2)));
                            response.setResponse(responseReadHold);
                            break;
                        case READ_INPUT_REG:
                            ReadInputRegistersResponse responseReadInput = new ReadInputRegistersResponse();
                            responseReadInput.setDeviceAddress(bytes[0] & 0xff);
                            responseReadInput.setByteQuantity(bytes[2] & 0xff);
                            responseReadInput.setRegisters(byteBigEndianArrayToIntArray(
                                    Arrays.copyOfRange(bytes, 3, bytes.length - 2)));
                            response.setResponse(responseReadInput);
                            break;
                        case WRITE_FILE:
                            WriteFileResponse responseWriteFile = new WriteFileResponse();
                            responseWriteFile.setAddress(bytes[0] & 0xff);
                            responseWriteFile.setQuantBytes(bytes[2] & 0xff);
                            responseWriteFile.setFileNum(concat2BytesToInt(bytes[4], bytes[5]));
                            responseWriteFile.setRecordNum(concat2BytesToInt(bytes[6], bytes[7]));
                            responseWriteFile.setRecordLen(concat2BytesToInt(bytes[8], bytes[9]));
                            int regCnt = (responseWriteFile.getQuantBytes() - 7) / 2;
                            int[] regs = new int[regCnt];
                            for (int i = 0; i < regCnt; i++)
                                regs[i] = concat2BytesToInt(bytes[11 + (i * 2)], bytes[10 + (i * 2)]);
                            responseWriteFile.setRegisters(regs);
                            response.setResponse(responseWriteFile);
                            break;
                        default:
                            if ((bytes[1] & 0xff) > 0x80)
                                response.setErrorCode(bytes[2]);
                            else
                                response.setErrorCode(ModbusErrorCodes.RTU_INVALID_FUNCTION);
                            break;
                    }
                } else response.setErrorCode(ModbusErrorCodes.SYSTEM_INVALID_CRC);
            } catch (Exception e) {
                response.setErrorCode(ModbusErrorCodes.SYSTEM_EXECUTION_ERROR);
            }
        } else response.setErrorCode(ModbusErrorCodes.SYSTEM_TIMEOUT);

        return response;
    }

    public int getExpectedResponseLength(ModbusRequestResponse request) {
        int result = 0;

        if (request.getType() == ModbusMessageType.REQUEST) {
            switch (request.getFunctionCode()) {
                case READ_HOLDING_REG:
                    result = ((ReadHoldingRegistersRequest) request).getQuantRegisters() * 2 + 5;
                    break;
                case READ_INPUT_REG:
                    result = ((ReadInputRegistersRequest) request).getQuantRegisters() * 2 + 5;
                    break;
                case WRITE_MULTI_HOLDING_REG:
                    result = 8;
                    break;
                case WRITE_FILE:
                    result = ((WriteFileRequest) request).getRegisters().length * 2 + 12;
                default: break;
            }
        }

        return result;
    }

    public int getExpectedRequestLengthByPDU(byte[] pdu) {
        int res = 0;
        if ((pdu[0] & 0x80) > 0)
            res += 2;
        else if (pdu.length >= 5) {
            switch (pdu[0]) {
                case READ_HOLDING_REG:
                case READ_INPUT_REG:
                    res += 5;
                    break;
                case WRITE_MULTI_HOLDING_REG:
                    res += 6 + (pdu[5] & 0xff);
                    break;
                default:
                    break;
            }
        }

        return res;
    }

    public byte[] convertRTURequestToTCP(byte[] bytes) {
        byte[] result = new byte[(bytes.length - 2) + 6];
        result[0] = (byte)(Math.random() * 256 - 128);
        result[1] = (byte)(Math.random() * 256 - 128);
        result[2] = result[3] = 0;
        result[4] = (byte) ((bytes.length - 2) >> 8);
        result[5] = (byte) (bytes.length - 2);

        System.arraycopy(bytes, 0, result, 6, bytes.length - 2);

        return result;
    }

    public byte[] convertTCPResponseToRTU(byte[] bytes) {
        byte[] result = new byte[(bytes.length - 6) + 2];
        System.arraycopy(bytes, 6, result, 0, bytes.length - 6);
        int crc16 = getCrc16(result, result.length - 2);
        result[result.length - 2] = (byte) crc16;
        result[result.length - 1] = (byte) (crc16 >> 8);

        return result;
    }

    public ModbusHandlerResult modbusParseRequest(byte[] bytes) {
        ModbusHandlerResult request = new ModbusHandlerResult();
        if (bytes.length > 0) {
            try {
                if (getCrc16(bytes, bytes.length - 2) == concat2BytesToInt(
                        bytes[bytes.length - 1], bytes[bytes.length - 2])) {
                    switch (bytes[1]) {
                        case WRITE_MULTI_HOLDING_REG:
                            WriteMultHoldingRegistersRequest requestWriteHold = new WriteMultHoldingRegistersRequest();
                            requestWriteHold.setDeviceAddress(bytes[0] & 0xff);
                            requestWriteHold.setStartAddress(concat2BytesToInt(bytes[2], bytes[3]));
                            requestWriteHold.setQuantRegisters(concat2BytesToInt(bytes[4], bytes[5]));
                            requestWriteHold.setQuantBytes(bytes[6]);
                            requestWriteHold.setRegisters(byteBigEndianArrayToIntArray(
                                    Arrays.copyOfRange(bytes, 7, bytes.length - 2)));
                            request.setResponse(requestWriteHold);
                            break;
                        case READ_HOLDING_REG:
                            ReadHoldingRegistersRequest requestReadHold = new ReadHoldingRegistersRequest();
                            requestReadHold.setDeviceAddress(bytes[0] & 0xff);
                            requestReadHold.setStartAddress(concat2BytesToInt(bytes[2], bytes[3]));
                            requestReadHold.setQuantRegisters(concat2BytesToInt(bytes[4], bytes[5]));
                            request.setResponse(requestReadHold);
                            break;
                        case READ_INPUT_REG:
                            ReadInputRegistersRequest requestReadInput = new ReadInputRegistersRequest();
                            requestReadInput.setDeviceAddress(bytes[0] & 0xff);
                            requestReadInput.setStartAddress(concat2BytesToInt(bytes[2], bytes[3]));
                            requestReadInput.setQuantRegisters(concat2BytesToInt(bytes[4], bytes[5]));
                            request.setResponse(requestReadInput);
                            break;
                        default:
                            request.setErrorCode(ModbusErrorCodes.RTU_INVALID_FUNCTION);
                            break;
                    }
                } else request.setErrorCode(ModbusErrorCodes.SYSTEM_INVALID_CRC);
            } catch (Exception e) {
                request.setErrorCode(ModbusErrorCodes.SYSTEM_EXECUTION_ERROR);
            }
        } else request.setErrorCode(ModbusErrorCodes.SYSTEM_TIMEOUT);

        return request;
    }

    public byte[] modbusParseResponse(ModbusHandlerResult response) {
        byte[] bytes = new byte[0];
        if (response.getErrorCode() == ModbusErrorCodes.NO_ERROR) {
            switch (response.getResponse().getFunctionCode()) {
                case READ_HOLDING_REG:
                    ReadHoldingRegistersResponse readHoldRspns = (ReadHoldingRegistersResponse) response.getResponse();
                    bytes = getReadRegResponse(
                            readHoldRspns.getDeviceAddress(), readHoldRspns.getFunctionCode(),
                            readHoldRspns.getByteQuantity(), readHoldRspns.getRegisters());
                    break;
                case READ_INPUT_REG:
                    ReadInputRegistersResponse readInputRspns = (ReadInputRegistersResponse) response.getResponse();
                    bytes = getReadRegResponse(
                            readInputRspns.getDeviceAddress(), readInputRspns.getFunctionCode(),
                            readInputRspns.getByteQuantity(), readInputRspns.getRegisters());
                    break;
                case WRITE_MULTI_HOLDING_REG:
                    WriteMultHoldingRegistersResponse writeHoldRspns = (WriteMultHoldingRegistersResponse) response.getResponse();
                    bytes = getWriteRegResponse(writeHoldRspns.getDeviceAddress(), writeHoldRspns.getStartAddress(),
                            writeHoldRspns.getQuantRegisters());
                    break;

                default: break;
            }
        } else {
            bytes = getErorResponse(response.getResponse().getDeviceAddress(),
                    response.getResponse().getFunctionCode(), response.getErrorCode());
        }

        return bytes;
    }

    private byte[] getReadRegResponse(int deviceAddress, int function, int byteQuant, int[] values) {
        byte[] res = new byte[5 + byteQuant];
        res[0] = (byte) deviceAddress;
        res[1] = (byte) function;
        res[2] = (byte) byteQuant;
        System.arraycopy(intArrayToBigEndianByteArray(values), 0, res, 3, byteQuant);
        int crc16 = getCrc16(res, res.length - 2);
        res[res.length - 2] = (byte) crc16;
        res[res.length - 1] = (byte) (crc16 >> 8);

        return res;
    }

    private byte[] getWriteRegResponse(int deviceAddress, int startAddress, int regQuant) {
        byte[] res = new byte[8];
        res[0] = (byte) deviceAddress;
        res[1] = (byte) WRITE_MULTI_HOLDING_REG;
        res[2] = (byte) (startAddress & 0xff);
        res[3] = (byte) ((startAddress >> 8) & 0xff);
        res[4] = (byte) (regQuant & 0xff);
        res[5] = (byte) ((regQuant >> 8) & 0xff);
        int crc16 = getCrc16(res, res.length - 2);
        res[res.length - 2] = (byte) crc16;
        res[res.length - 1] = (byte) (crc16 >> 8);

        return res;
    }

    private byte[] getErorResponse(int deviceAddress, int function, int errCode) {
        byte[] res = new byte[5];
        res[0] = (byte) deviceAddress;
        res[1] = (byte) ((function + 128) & 0xff);
        res[2] = (byte) (errCode & 0xff);
        int crc16 = getCrc16(res, res.length - 2);
        res[res.length - 2] = (byte) crc16;
        res[res.length - 1] = (byte) (crc16 >> 8);

        return res;
    }
}
