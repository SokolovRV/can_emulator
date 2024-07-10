package org.example;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortTimeoutException;
import org.apache.log4j.BasicConfigurator;

public class ComHandler {
    private String com;
    private int baud;
    private SerialPort serial;

    static {
        BasicConfigurator.configure();
        System.out.println();
    }

    private ComHandler() {}

    public ComHandler(String com, int baud) {
        this.com = com;
        this.baud = baud;
    }

    public synchronized String getCom() {
        return com;
    }

    public synchronized int getBaud() {
        return baud;
    }

    public synchronized boolean open() {
        if (serial == null || !serial.isOpened()) {
            serial = new SerialPort(com);
            try {
                serial.openPort();
                serial.setParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (SerialPortException e) {
                return false;
            }
        }

        return true;
    }

    public synchronized boolean serialPortIsOpened() {
        if (serial != null) {
            return serial.isOpened();
        }

        return false;
    }

    public synchronized boolean close() {
        if (serial != null) {
            try {
                serial.closePort();
                return true;
            } catch (SerialPortException e) {
                return false;
            }
        }

        return false;
    }

    public synchronized int getInputBufferBytesCount() {
        if (serial != null) {
            try {
                return serial.getInputBufferBytesCount();
            } catch (SerialPortException e) {
                return -1;
            }
        }

        return -1;
    }

    public synchronized boolean sendBytes(byte[] bytes) {
        if (serial != null && serial.isOpened()) {
            try {
                if (serial.getInputBufferBytesCount() > 0) {
                    serial.readBytes();
                }
                serial.writeBytes(bytes);
                //System.out.println("[" + (System.currentTimeMillis() & 0xffff) + "] COM: TX>>> " + bytesToHex(bytes));
                long startTime = System.currentTimeMillis();
                while (serial.getOutputBufferBytesCount() > 0) {
                    if ((System.currentTimeMillis() - startTime) > 500) {
                        return false;
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                return true;
            } catch (SerialPortException e) {
                System.out.println("[" + (System.currentTimeMillis() & 0xffff) + "] COM: ERR SEND!");
                return false;
            }
        }

        return false;
    }

    public synchronized byte[] readBytes(int cntBytes, int timeOutMs) {
        byte[] bytes = new byte[0];

        if (serial != null && serial.isOpened()) {
            try {
                bytes = serial.readBytes(cntBytes, timeOutMs);
                //System.out.println("[" + (System.currentTimeMillis() & 0xffff) + "] COM: RX<<< " + bytesToHex(bytes));
            } catch (SerialPortException | SerialPortTimeoutException e) {
                try {
                    if (serial.getInputBufferBytesCount() > 0) {
                        bytes = serial.readBytes();
                        System.out.println("[" + (System.currentTimeMillis() & 0xffff) + "] COM: RX<<< " + bytesToHex(bytes));
                    } else
                    {
                        System.out.println("[" + (System.currentTimeMillis() & 0xffff) + "] COM: TMOT!");
                    }
                } catch (SerialPortException ex) {}
            }
        }

        return bytes;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b));
        }
        return sb.toString().toUpperCase().trim();
    }
}
