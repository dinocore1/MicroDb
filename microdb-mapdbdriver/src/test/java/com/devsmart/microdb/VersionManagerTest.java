package com.devsmart.microdb;

import com.devsmart.microdb.version.Change;
import com.devsmart.microdb.version.Commit;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBValueFactory;
import com.google.common.collect.Lists;
import example.MyDBObj;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.*;

public class VersionManagerTest {

    private static UBObject createObj(String type, String name) {
        UBObject obj;
        obj = UBValueFactory.createObject();
        obj.put("type", UBValueFactory.createString(type));
        obj.put("name", UBValueFactory.createString(name));
        return obj;
    }

    private static UUID insert(String type, String name, MapDBDriver driver) throws IOException {
        final UUID id = driver.genId();
        driver.insert(id, createObj(type, name));
        return id;
    }

    @Test
    public void testVersionUpdate() throws Exception {

        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        MicroDB db = new MicroDB(dbDriver, 0, new MapDBDBBuilder.NullCallback());

        VersionManager vm = new VersionManager(db, dbDriver);

        assertFalse(vm.isDirty());

        MyDBObj obj = db.insert(MyDBObj.class);
        obj.setMyString("fido");

        obj = db.insert(MyDBObj.class);
        obj.setMyString("symo");

        obj = db.insert(MyDBObj.class);
        obj.setMyString("mondo");

        obj = db.insert(MyDBObj.class);
        obj.setMyString("thuggy");

        db.flush();

        assertTrue(vm.isDirty());

        vm.commit();
        db.flush();

        assertFalse(vm.isDirty());
    }

    @Test
    public void iterateDiffsTest() throws Exception {
        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        MicroDB db = new MicroDB(dbDriver, 0, new MapDBDBBuilder.NullCallback());

        VersionManager vm = new VersionManager(db, dbDriver);

        MyDBObj obj, symo;
        obj = db.insert(MyDBObj.class);
        obj.setMyString("fido");

        symo = db.insert(MyDBObj.class);
        symo.setMyString("symo");

        obj = db.insert(MyDBObj.class);
        obj.setMyString("mondo");

        obj = db.insert(MyDBObj.class);
        obj.setMyString("thuggy");

        db.flush();

        Iterable<Change> diffs = vm.getChanges(vm.getHead().getId());
        int count = 0;
        for (Change c : diffs) {
            count++;
        }

        assertEquals(4, count);

        vm.commit();
        db.sync();

        diffs = vm.getChanges(vm.getHead().getId());
        count = 0;
        for (Change c : diffs) {
            count++;
        }

        assertEquals(0, count);

        db.delete(symo);

        db.flush();

        diffs = vm.getChanges(vm.getHead().getId());
        Change c = diffs.iterator().next();


    }

    private int countObjects(MicroDB db) {
        MapDBDriver driver = (MapDBDriver) db.getDriver();
        return driver.mObjects.size();
    }

    @Test
    public void moveHead() throws Exception {
        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        MicroDB db = new MicroDB(dbDriver, 0, new MapDBDBBuilder.NullCallback());

        VersionManager vm = new VersionManager(db, dbDriver);

        final UUID fidoId = insert("dog", "fido", dbDriver);
        final UUID symoId = insert("cat", "symo", dbDriver);

        final UUID commitId = vm.commit();
        db.sync();

        final UUID mondoId = UUID.randomUUID();

        Commit destCommit = Commit.withParent(commitId);
        vm.addChanges(destCommit.getId(), Lists.newArrayList(
                Change.createInsertChange(mondoId, createObj("dog", "mondo")),
                Change.createDeleteChange(symoId)
        ));

        vm.addCommit(destCommit);

        assertEquals(2, countObjects(db));

        vm.moveTo(destCommit.getId());
        db.sync();

        assertEquals(destCommit.getId(), vm.getHead().getId());

        assertNull(dbDriver.get(symoId));
        UBObject mondoValue = (UBObject) dbDriver.get(mondoId);
        assertNotNull(mondoValue);
        assertTrue(mondoValue.isObject());
        assertEquals("dog", mondoValue.get("type").asString());
        assertEquals("mondo", mondoValue.get("name").asString());

        UBObject fido = (UBObject) dbDriver.get(fidoId);
        assertNotNull(fido);
        assertEquals("fido", fido.get("name").asString());

    }
}
