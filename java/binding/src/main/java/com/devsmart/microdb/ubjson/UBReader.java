package com.devsmart.microdb.ubjson;

import java.io.*;
import java.util.ArrayList;


public class UBReader implements Closeable {

    private final InputStream mInputStream;

    public UBReader(InputStream in) {
        mInputStream = in;
    }

    @Override
    public void close() throws IOException {
        mInputStream.close();
    }

    private byte readControl() throws IOException {
        int value = mInputStream.read();
        if(value == -1) {
            throw new IOException("eof");
        } else {
            return (byte)value;
        }
    }


    private long readInt(byte control) throws IOException {
        long value;
        switch (control) {
            case UBValue.MARKER_INT8:
                value = readControl();
                break;

            case UBValue.MARKER_UINT8:
                value = (0xFF & readControl());
                break;

            case UBValue.MARKER_INT16:
                value = (readControl() & 0xFF) << 8 | (readControl() & 0xFF);
                break;

            case UBValue.MARKER_INT32:
                value = (readControl() & 0xFF) << 24 | (readControl() & 0xFF) << 16
                        | (readControl() & 0xFF) << 8 | (readControl() & 0xFF);
                break;

            case UBValue.MARKER_INT64:
                value = (readControl() & 0xFF) << 56 | (readControl() & 0xFF) << 48
                        | (readControl() & 0xFF) << 40 | (readControl() & 0xFF) << 32
                        | (readControl() & 0xFF) << 24 | (readControl() & 0xFF) << 16
                        | (readControl() & 0xFF) << 8 | (readControl() & 0xFF);
                break;

            default:
                throw new IOException("not an int type");

        }

        return value;
    }

    private char readChar() throws IOException {
        char value = (char) readControl();
        return value;
    }

    private float readFloat32() throws IOException {
        int intvalue = (readControl() & 0xFF) << 24 | (readControl() & 0xFF) << 16
                | (readControl() & 0xFF) << 8 | (readControl() & 0xFF);

        float value = Float.intBitsToFloat(intvalue);
        return value;
    }

    private double readFloat64() throws IOException {
        long intvalue = (readControl() & 0xFF) << 56 | (readControl() & 0xFF) << 48
                | (readControl() & 0xFF) << 40 | (readControl() & 0xFF) << 32
                | (readControl() & 0xFF) << 24 | (readControl() & 0xFF) << 16
                | (readControl() & 0xFF) << 8 | (readControl() & 0xFF);


        double value = Double.longBitsToDouble(intvalue);
        return value;
    }

    private byte[] readString() throws IOException {
        int size = (int) readInt(readControl());
        byte[] value = new byte[size];

        int bytesRead = mInputStream.read(value, 0, size);
        if(bytesRead != size) {
            throw new IOException("eof reached");
        }
        return value;
    }

    private void readOptimizedArray(int size, byte type) {
        switch(type) {
            case UBValue.MARKER_FLOAT32:
                break;

            default:
                ArrayList<UBValue> mValue = new ArrayList<UBValue>();

                break;

        }

        for(int i=0;i<size;i++) {

        }
    }

    private UBArray readArray() throws IOException {
        byte control, type;
        int size;

        control = readControl();
        if(control == UBValue.MARKER_OPTIMIZED_TYPE) {
            type = readControl();

            if(readControl() != UBValue.MARKER_OPTIMIZED_SIZE) {
                throw new IOException("optimized size missing");
            }
            size = (int)readInt(readControl());

            switch (type) {
                case UBValue.MARKER_FLOAT32:

                default:

            }


        } else if(control == UBValue.MARKER_OPTIMIZED_SIZE) {
            size = (int)readInt(readControl());
            for(int i=0;i<size;i++){

            }
        }

    }

    private UBValue readValue(byte control) throws IOException {
        UBValue retval = null;
        switch(control) {
            case UBValue.MARKER_NULL:
                retval = UBValueFactory.createNull();
                break;

            case UBValue.MARKER_TRUE:
                retval = UBValueFactory.createBool(true);
                break;

            case UBValue.MARKER_FALSE:
                retval = UBValueFactory.createBool(false);
                break;

            case UBValue.MARKER_CHAR:
                retval = UBValueFactory.createChar(readChar());
                break;

            case UBValue.MARKER_INT8:
            case UBValue.MARKER_UINT8:
            case UBValue.MARKER_INT16:
            case UBValue.MARKER_INT32:
            case UBValue.MARKER_INT64:
                retval = UBValueFactory.createInt(readInt(control));
                break;

            case UBValue.MARKER_FLOAT32:
                retval = UBValueFactory.createFloat32(readFloat32());
                break;

            case UBValue.MARKER_FLOAT64:
                retval = UBValueFactory.createFloat64(readFloat64());
                break;

            case UBValue.MARKER_STRING:
                retval = UBValueFactory.createString(readString());
                break;

            case UBValue.MARKER_ARRAY_START:
                retval = readArray();
        }

        return retval;
    }

    public UBValue read() throws IOException {
        byte control = readControl();
        return readValue(control);
    }
}
