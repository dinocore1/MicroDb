package com.devsmart.microdb;


import org.mapdb.DB;
import org.mapdb.DBMaker;

public class MapDBDriverTest extends DriverTest {


    @Override
    public Driver createNewDriver() {
        DB mapdb = DBMaker.newMemoryDB()
                .make();

        MapDBDriver dbDriver = new MapDBDriver(mapdb);
        return dbDriver;
    }
}
