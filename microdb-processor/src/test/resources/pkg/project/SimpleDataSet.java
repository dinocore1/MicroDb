package pkg.project;

import com.devsmart.microdb.Dataset;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.annotations.DataSet;

import java.io.IOException;

@DataSet(objects = {SimpleDBModel.class})
public class SimpleDataSet implements Dataset {

    @Override
    public void install(MicroDB db) throws IOException {

    }

}