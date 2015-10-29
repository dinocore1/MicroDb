package com.devsmart.microdb;


import com.devsmart.microdb.annotations.*;
import org.junit.Test;
import pkg.project.SimpleDBModel;

public class DatasetTest {

    @DataSet(objects = { SimpleDBModel.class })
    public static class MyDataSet {

    }

    @Test
    public void testCreateDataset() {

    }
}
