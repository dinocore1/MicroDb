package com.devsmart.microdb;


import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValueFactory;
import example.MyDBObj;
import org.junit.Test;
import static org.junit.Assert.*;

public class ReadWriteTest {

    @Test
    public void testReadWrite() throws Exception {

        MicroDB db = DBBuilder.builder()
                .buildMemoryDB();

        MyDBObj obj1 = db.insert(MyDBObj.class);
        obj1.setMyString("obj1");

        MyDBObj obj2 = db.create(MyDBObj.class);
        obj2.setMyString("obj2");

        obj1.setMyDBO(obj2);

        UBObject data = UBValueFactory.createObject();
        obj1.writeToUBObject(data);

        assertNotNull(data);
        assertTrue(data.isObject());
        UBObject dobj1 = data.asObject();
        assertNotNull(dobj1.get("id"));
        assertNotNull(dobj1.get("myDBO"));
        assertTrue(dobj1.get("myDBO").isObject());

    }
}
