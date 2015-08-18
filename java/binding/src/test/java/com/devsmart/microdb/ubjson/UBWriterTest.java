package com.devsmart.microdb.ubjson;


import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UBWriterTest {

    @Test
    public void testWriteNull() throws IOException {
        UBValue value;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UBWriter writer = new UBWriter(out);
        value = UBValueFactory.createNull();
        writer.write(value);

        byte[] array = out.toByteArray();
        assertEquals(1, array.length);
        assertEquals('Z', array[0]);
    }

    @Test
    public void testWriteBool() throws IOException {

        UBValue value;
        ByteArrayOutputStream out;
        UBWriter writer;
        byte[] array;

        out = new ByteArrayOutputStream();
        writer = new UBWriter(out);
        value = UBValueFactory.createBool(true);
        writer.write(value);

        array = out.toByteArray();
        assertEquals(1, array.length);
        assertEquals('T', array[0]);

        out = new ByteArrayOutputStream();
        writer = new UBWriter(out);
        value = UBValueFactory.createBool(false);
        writer.write(value);

        array = out.toByteArray();
        assertEquals(1, array.length);
        assertEquals('F', array[0]);

    }

    @Test
    public void testWriteChar() throws IOException {

        UBValue value;
        ByteArrayOutputStream out;
        UBWriter writer;
        byte[] array;

        out = new ByteArrayOutputStream();
        writer = new UBWriter(out);
        value = UBValueFactory.createChar('t');
        writer.write(value);

        array = out.toByteArray();
        assertEquals(2, array.length);
        assertEquals('C', array[0]);
        assertEquals('t', array[1]);

    }

    @Test
    public void testWriteInt() throws IOException {

        UBValue value;
        ByteArrayOutputStream out;
        UBWriter writer;
        byte[] array;

        out = new ByteArrayOutputStream();
        writer = new UBWriter(out);
        value = UBValueFactory.createInt(255);
        writer.write(value);
        array = out.toByteArray();
        assertEquals(2, array.length);
        assertEquals('U', array[0]);
        assertEquals(255, (short)(0xFF & array[1]));

        out = new ByteArrayOutputStream();
        writer = new UBWriter(out);
        value = UBValueFactory.createInt(-1);
        writer.write(value);
        array = out.toByteArray();
        assertEquals(2, array.length);
        assertEquals('i', array[0]);
        assertEquals(255, (short)(0xFF & array[1]));

        out = new ByteArrayOutputStream();
        writer = new UBWriter(out);
        value = UBValueFactory.createInt(29753);
        writer.write(value);
        array = out.toByteArray();
        assertEquals(3, array.length);
        assertEquals('I', array[0]);
        assertEquals(0x74, (short)(0xFF & array[1]));
        assertEquals(0x39, (short)(0xFF & array[2]));

    }


}
