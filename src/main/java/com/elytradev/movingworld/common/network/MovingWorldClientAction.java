package com.elytradev.movingworld.common.network;

public enum MovingWorldClientAction {
    NONE, ALIGN, DISASSEMBLE, DISASSEMBLEWITHOVERWRITE;

    public static byte toByte(MovingWorldClientAction action) {
        switch (action) {
            case ALIGN:
                return (byte) 1;
            case DISASSEMBLE:
                return (byte) 2;
            case DISASSEMBLEWITHOVERWRITE:
                return (byte) 3;
            default:
                return (byte) 0;
        }
    }

    public static MovingWorldClientAction fromByte(byte actionInt) {
        switch (actionInt) {
            case 1:
                return ALIGN;
            case 2:
                return DISASSEMBLE;
            case 3:
                return DISASSEMBLEWITHOVERWRITE;
            default:
                return NONE;
        }
    }

    public byte toByte() {
        return MovingWorldClientAction.toByte(this);
    }
}