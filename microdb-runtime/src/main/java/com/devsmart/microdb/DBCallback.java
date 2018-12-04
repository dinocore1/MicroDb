package com.devsmart.microdb;

import java.io.IOException;

public interface DBCallback
{
    /**
     * @param db
     * @param oldVersion
     * @param newVersion
     * @return true if you want to upgrade, false if you will upgrade later
     */
    boolean onNeedsUpgrade(MicroDB db, int oldVersion, int newVersion);

    void doUpgrade(MicroDB db, int oldVersion, int newVersion) throws IOException;
}
