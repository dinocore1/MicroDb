package com.devsmart.microdb.ubjson;


import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class UBReaderTest {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    @Test
    public void readNull() throws Exception {
        byte[] data;
        UBReader reader;
        UBValue value;

        data = new byte[] {'Z'};
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isNull());
    }

    @Test
    public void readBool() throws Exception {
        byte[] data;
        UBReader reader;
        UBValue value;

        data = new byte[] {'T'};
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isBool());
        assertEquals(true, value.asBool());

        data = new byte[] {'F'};
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isBool());
        assertEquals(false, value.asBool());
    }

    @Test
    public void readChar() throws Exception {
        byte[] data;
        UBReader reader;
        UBValue value;

        data = new byte[] {'C', 'A'};
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isChar());
        assertEquals('A', value.asChar());

        data = new byte[] {'C', 't'};
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isChar());
        assertEquals('t', value.asChar());
    }

    @Test
    public void readInt() throws Exception {

        byte[] data;
        UBReader reader;
        UBValue value;

        data = new byte[] {'U', (byte)0xff };
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isNumber());
        assertEquals(255, value.asInt());

        data = new byte[] {'i', (byte)0xff };
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isNumber());
        assertEquals(-1, value.asInt());

        data = new byte[] {'i', (byte)0xf4 };
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isNumber());
        assertEquals(-12, value.asInt());

        data = new byte[] {'I', (byte)0x74, (byte)0x39 };
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isNumber());
        assertEquals(29753, value.asInt());

    }

    @Test
    public void readFloat32() throws Exception {
        byte[] data;
        UBReader reader;
        UBValue value;

        data = new byte[] {'d', (byte)0x40, (byte)0x48, (byte)0xf5, (byte)0xc3 };
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isNumber());
        assertEquals(3.14f, value.asFloat32(), 0.000001);
    }

    @Test
    public void readArray() throws Exception {
        byte[] data;
        UBReader reader;
        UBValue value;

        data = new byte[] {'[', 'i', (byte)0xff, 'U', (byte)0xff, ']' };
        reader = new UBReader(new ByteArrayInputStream(data));
        value = reader.read();
        assertTrue(value.isArray());
        UBArray array = value.asArray();
        assertEquals(2, array.size());
        assertTrue(array.get(0).isNumber());
        assertEquals(-1, array.get(0).asInt());
        assertTrue(array.get(1).isNumber());
        assertEquals(255, array.get(1).asInt());

    }
}
