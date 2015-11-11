package com.devsmart.microdb;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;

public class VersionManagerTest {

    @Test
    public void testVersionUpdate() throws Exception {

        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        MicroDB db = new MicroDB(dbDriver, 0, new DBBuilder.NullCallback());

        VersionManager vm = new VersionManager(db, dbDriver);

        assertFalse(vm.isDirty());
    }
}
