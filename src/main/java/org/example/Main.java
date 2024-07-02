package org.example;

import org.example.modbus.ModbusFactory;

import java.nio.charset.StandardCharsets;
import org.bouncycastle.util.encoders.Hex;
import org.example.modbus.ModbusHandlerResult;
import org.example.modbus.constants.ModbusErrorCodes;
import org.example.modbus.requests.ReadHoldingRegistersRequest;
import org.example.modbus.requests.ReadInputRegistersRequest;
import org.example.modbus.requests.WriteMultHoldingRegistersRequest;
import org.example.modbus.responses.ReadHoldingRegistersResponse;
import org.example.modbus.responses.ReadInputRegistersResponse;
import org.example.modbus.responses.WriteMultHoldingRegistersResponse;

import static org.example.modbus.constants.ModbusFunctionCodes.*;

public class Main {
    public static String com = "COM14";
    public static int baud = 2000000;
    public static int frtmotMs = 20;
    public static final int CNT_DEVICE = 2;
    public static ModbusFactory modbusFactory = ModbusFactory.getInstance();
    public static void main(String[] args) {
        System.out.println("Start.");
        System.out.println("Open com..");
        ComHandler comHandler = new ComHandler(com, baud);
        comHandler.open();
        System.out.println("Done.");
        canInit(comHandler);
        while (true) {
            byte[] rqst = receiveMsg(comHandler);
            ModbusHandlerResult mbRqst = modbusFactory.modbusParseRequest(rqst);
            if (mbRqst.getErrorCode() == ModbusErrorCodes.NO_ERROR) {
                sendMbMsg(modbusFactory.modbusParseResponse(modbusProcess(mbRqst)), comHandler);
            }
            System.out.println("\n");
        }
    }

    public static byte[] getBytesForCom(String s) {
        return (s + "\r").getBytes(StandardCharsets.US_ASCII);
    }

    public static boolean canInit(ComHandler comHandler) {
        System.out.println("Can init..");
        comHandler.sendBytes(getBytesForCom("C"));
        comHandler.sendBytes(getBytesForCom("V"));
        long t = System.currentTimeMillis();
        while(comHandler.getInputBufferBytesCount() <= 0) {
            if ((System.currentTimeMillis() - t) > frtmotMs)
                break;
        }
        if (comHandler.getInputBufferBytesCount() > 0) {
            comHandler.readBytes(comHandler.getInputBufferBytesCount(), frtmotMs);
            comHandler.sendBytes(getBytesForCom("S4"));
            comHandler.sendBytes(getBytesForCom("A0"));
            comHandler.sendBytes(getBytesForCom("M0"));
            comHandler.sendBytes(getBytesForCom("O"));
            System.out.println("Init done.");
            return true;
        } else {
            System.out.println("Adapter don't responds!");
            return false;
        }


    }

    public static void sendMbMsg(byte[] msg, ComHandler comHandler) {
        System.out.println("Send mb msg: " + ComHandler.bytesToHex(msg));
        byte[] pdu = new byte[msg.length - 3];
        System.arraycopy(msg, 1, pdu, 0, msg.length - 3);
        String[] ss = new String[(pdu.length / 8) + (((pdu.length % 8) > 0) ? 1 : 0)];
        int rmn = pdu.length;
        for (int i = 0; i < ss.length; i++) {
            int cnt = rmn;
            if (cnt > 8) {
                cnt = 8;
                rmn -= cnt;
            }
            byte[] b = new byte[cnt];
            System.arraycopy(pdu, i * 8, b, 0, cnt);
            ss[i] = String.format("t%03d%01d", msg[0], cnt) + b2h(b);
            System.out.println("    Frame: " + ss[i]);
        }
        for (String s : ss)
            comHandler.sendBytes(getBytesForCom(s));

        System.out.println("Send msg done.");
    }

    public static String b2h(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase().trim();
    }

    public static byte[] receiveMsg(ComHandler comHandler) {
        System.out.println("Receive msg..");
        if (comHandler.getInputBufferBytesCount() > 0)
            comHandler.readBytes(comHandler.getInputBufferBytesCount(), frtmotMs);
        byte[] res = new byte[0];
        int expLen = 256;
        boolean frst = false;
        long t = 0;
        StringBuilder rqst = new StringBuilder();
        while (true) {
            if (comHandler.getInputBufferBytesCount() > 0) {
                if (!frst) {
                    frst = true;
                    t = System.currentTimeMillis();
                }
                rqst.append(new String(comHandler.readBytes(
                        comHandler.getInputBufferBytesCount(), frtmotMs), StandardCharsets.US_ASCII));
                if (expLen == 256)
                    expLen = detectLen(rqst.toString());
                if (expLen <= rqst.length()) {
                    res = parseMbRqst(rqst.substring(0, expLen));
                    break;
                }
            }
            if (frst && (System.currentTimeMillis() - t) > frtmotMs)
                return new byte[0];
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return  res;
    }

    public static int detectLen(String s) {
        int payloadLen = 0;
        if (s.contains("\r")) {
            String sub = s.split("\r")[0];
            String frame = sub.substring(5);
            byte[] frb = Hex.decode(frame);
            payloadLen = modbusFactory.getExpectedRequestLengthByPDU(frb);
            if (payloadLen > 0)
                return ((payloadLen / 8) + ((payloadLen % 8) > 0 ? 1 : 0)) * 6 + payloadLen * 2;
        }

        return 0;
    }

    public static byte[] parseMbRqst(String rqst) {
        String[] frames = rqst.split("\r");
        byte[] buf = new byte[256];
        int p = 0;
        for (String fr : frames) {
            System.out.println("    Frame: " + fr);
            byte[] frb = Hex.decode(fr.substring(5));
            System.arraycopy(frb, 0, buf, p, frb.length);
            p += frb.length;
        }
        byte[] res = new byte[p + 3];
        res[0] = Byte.parseByte(rqst.substring(1, 4));
        System.arraycopy(buf, 0, res, 1, p);
        int crc = modbusFactory.getCrc16(res, p + 1);
        res[res.length - 2] = (byte) (crc & 0xff);
        res[res.length - 1] = (byte) ((crc >> 8) & 0xff);

        System.out.println("Receive mb msg: " + ComHandler.bytesToHex(res));

        return res;
    }

    private static int[][] dimming = new int[CNT_DEVICE][2];
    private static long[] dt = new long[CNT_DEVICE];
    public static ModbusHandlerResult modbusProcess(ModbusHandlerResult request) {
        ModbusHandlerResult response = new ModbusHandlerResult(ModbusErrorCodes.NO_ERROR);

        switch (request.getResponse().getFunctionCode()) {
            case READ_HOLDING_REG:
                ReadHoldingRegistersRequest rHr = (ReadHoldingRegistersRequest) request.getResponse();
                ReadHoldingRegistersResponse rHrResponse = new ReadHoldingRegistersResponse();
                rHrResponse.setDeviceAddress(rHr.getDeviceAddress());
                rHrResponse.setByteQuantity(rHr.getQuantRegisters() * 2);
                int[] valuesHr = new int[rHr.getQuantRegisters()];
                response.setErrorCode(readModbusRegisters(valuesHr, rHr.getStartAddress(), rHr.getDeviceAddress()));
                rHrResponse.setRegisters(valuesHr);
                response.setResponse(rHrResponse);
                break;
            case READ_INPUT_REG:
                ReadInputRegistersRequest rIr = (ReadInputRegistersRequest) request.getResponse();
                ReadInputRegistersResponse rIrResponse = new ReadInputRegistersResponse();
                rIrResponse.setDeviceAddress(rIr.getDeviceAddress());
                rIrResponse.setByteQuantity(rIr.getQuantRegisters() * 2);
                int[] valuesIr = new int[rIr.getQuantRegisters()];
                response.setErrorCode(readModbusRegisters(valuesIr, rIr.getStartAddress(), rIr.getDeviceAddress()));
                rIrResponse.setRegisters(valuesIr);
                response.setResponse(rIrResponse);
                break;
            case WRITE_MULTI_HOLDING_REG:
                WriteMultHoldingRegistersRequest wHr = (WriteMultHoldingRegistersRequest) request.getResponse();
                WriteMultHoldingRegistersResponse wHrResponse = new WriteMultHoldingRegistersResponse();
                wHrResponse.setDeviceAddress(wHr.getDeviceAddress());
                wHrResponse.setQuantRegisters(wHr.getQuantRegisters());
                wHrResponse.setStartAddress(wHr.getStartAddress());
                response.setErrorCode(writeModbusRegisters(
                        wHr.getRegisters(), wHr.getStartAddress(), wHr.getDeviceAddress()));
                response.setResponse(wHrResponse);
                break;
            default:
                break;
        }

        return response;
    }

    private static int readModbusRegisters(int[] values, int startAddress, int deviceAddress) {
        for (int index = 0; index < values.length; index++) {
            int addr = index + startAddress;
            switch (addr) {
                case 1:
                    values[index] = deviceAddress * 100;
                    break;
                case 2:
                case 3:
                case 4:
                    values[index] = 0;
                    break;
                case 5:
                    values[index] = 90;
                    break;
                case 7:
                    values[index] = (int) (dt[deviceAddress - 1] & 0xffff);
                    break;
                case 8:
                    values[index] = (int) ((dt[deviceAddress - 1] >> 16) & 0xffff);
                    break;
                case 6:
                    values[index] = 1;
                    break;
                default:
                    if (isBetween(addr, 1000, 1007)) {
                        if ((addr - 1000) % 4 == 0)
                            values[index] = (((addr - 1000) / 4) + 1) * 1000;
                        else
                            values[index] = 0;
                    } else if (isBetween(addr, 1008, 5000)) {
                        values[index] = 0;
                    } else if (isBetween(addr, 10000, 10009)) {
                        switch (addr - 10000) {
                            case 0:
                                values[index] = (addr - 10000) / 50 + 1;
                                break;
                            case 1:
                                values[index] = ((addr - 10000) / 50 + 1) * 1000;
                                break;
                            case 2:
                            case 3:
                            case 4:
                                values[index] = 0;
                                break;
                            case 5:
                                values[index] = 90;
                                break;
                            case 6:
                                values[index] = 1;
                                break;
                            case 7:
                                values[index] = dimming[deviceAddress - 1][0];
                                break;
                            case 8:
                            case 9:
                                values[index] = ((int) (Math.random() * 65535)) & 0xffff;
                                break;
                            default:
                                break;
                        }
                    } else if ((isBetween(addr, 10050, 10059))) {
                        switch (addr - 10050) {
                            case 0:
                                values[index] = (addr - 10000) / 50 + 1;
                                break;
                            case 1:
                                values[index] = ((addr - 10000) / 50 + 1) * 1000;
                                break;
                            case 2:
                            case 3:
                            case 4:
                                values[index] = 0;
                                break;
                            case 5:
                                values[index] = 90;
                                break;
                            case 6:
                                values[index] = 0;
                                break;
                            case 7:
                                values[index] = dimming[deviceAddress - 1][1];
                                break;
                            case 8:
                            case 9:
                                values[index] = ((int) (Math.random() * 65535)) & 0xffff;
                                break;
                            default:
                                break;
                        }

                    } else
                        return ModbusErrorCodes.RTU_UNAVAIBLE_REGISTERS_ADDR;
                    break;
            }
        }

        return ModbusErrorCodes.NO_ERROR;
    }

    private static int writeModbusRegisters(int[] values, int startAddress, int deviceAddress) {
        for (int index = 0; index < values.length; index++) {
            int addr = index + startAddress;
            switch (addr) {
                case 7:
                    dt[deviceAddress - 1] = (dt[deviceAddress - 1] & (~0xffffL)) | (values[index] & 0xffffL) ;
                    break;
                case 8:
                    dt[deviceAddress - 1] = (dt[deviceAddress - 1] & (~(0xffffL << 16)))
                            | ((values[index] & 0xffffL) << 16);
                    break;
                case 10007:
                    dimming[deviceAddress - 1][0] = values[index];
                    break;
                case 10057:
                    dimming[deviceAddress - 1][1] = values[index];
                    break;
                default:
                    return ModbusErrorCodes.RTU_UNAVAIBLE_REGISTERS_ADDR;
            }
        }

        return ModbusErrorCodes.NO_ERROR;
    }

    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }
}