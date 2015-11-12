package com.devsmart.microdb;

import com.devsmart.microdb.version.Change;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValueFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;
import java.util.UUID;

public class VersionManagerTest {

    private static UUID insert(String type, String name, MapDBDriver driver) throws IOException {
        UBObject obj;
        obj = UBValueFactory.createObject();
        obj.put("type", UBValueFactory.createString(type));
        obj.put("name", UBValueFactory.createString(name));

        return driver.insert(obj);
    }

    @Test
    public void testVersionUpdate() throws Exception {

        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        MicroDB db = new MicroDB(dbDriver, 0, new DBBuilder.NullCallback());

        VersionManager vm = new VersionManager(db, dbDriver);

        assertFalse(vm.isDirty());

        insert("dog", "fido", dbDriver);
        insert("cat", "symo", dbDriver);
        insert("dog", "mondo", dbDriver);
        insert("cat", "thuggy", dbDriver);

        assertTrue(vm.isDirty());

        vm.commit();
        db.sync();

        assertFalse(vm.isDirty());
    }

    @Test
    public void iterateDiffsTest() throws Exception {
        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        MicroDB db = new MicroDB(dbDriver, 0, new DBBuilder.NullCallback());

        VersionManager vm = new VersionManager(db, dbDriver);

        insert("dog", "fido", dbDriver);
        UUID symo = insert("cat", "symo", dbDriver);
        insert("dog", "mondo", dbDriver);
        insert("cat", "thuggy", dbDriver);

        Iterable<Change> diffs = vm.getChanges(vm.getHead().getId());
        int count = 0;
        for(Change c : diffs) {
            count++;
        }

        assertEquals(4, count);

        vm.commit();
        db.sync();

        diffs = vm.getChanges(vm.getHead().getId());
        count = 0;
        for(Change c : diffs) {
            count++;
        }

        assertEquals(0, count);

        dbDriver.delete(symo);
        diffs = vm.getChanges(vm.getHead().getId());
        Change c = diffs.iterator().next();


    }
}
