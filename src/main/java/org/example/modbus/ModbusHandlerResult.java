package org.example.modbus;

public class ModbusHandlerResult {
    private int errorCode = 0;
    private ModbusRequestResponse response;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public ModbusHandlerResult() {
    }

    public ModbusHandlerResult(int errorCode) {
        this.errorCode = errorCode;
    }

    public ModbusHandlerResult(int errorCode, ModbusRequestResponse response) {
        this.errorCode = errorCode;
        this.response = response;
    }

    public ModbusRequestResponse getResponse() {
        return response;
    }

    public void setResponse(ModbusRequestResponse response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "ModbusHandlerResult{" +
                "errorCode=" + errorCode +
                ", response=" + response +
                '}';
    }
}
