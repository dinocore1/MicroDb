package pkg.project;

import com.devsmart.microdb.Emitter;
import com.devsmart.microdb.MapFunction;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.ObjectIterator;
import com.devsmart.microdb.SimpleDBModel_pxy;
import com.devsmart.microdb.Utils;
import com.devsmart.ubjson.UBValue;
import java.io.IOException;

public class MicroDBSimpleDataSet extends SimpleDataSet {

    private final MicroDB mDb;

    public MicroDBSimpleDataSet(MicroDB db) {
        mDb = db;
    }

    public void install(MicroDB db) throws IOException {

        db.addIndex("SimpleDBModel.myString", new MapFunction<String>() {
            @Override
            public void map(UBValue value, Emitter<String> emitter) {
                if(Utils.isValidObject(value, SimpleDBModel_pxy.TYPE)) {
                    UBValue v = value.asObject().get("myString");
                    if(v != null && v.isString()) {
                        emitter.emit(v.asString());
                    }
                }
            }
        });

    }

    public ObjectIterator<String, SimpleDBModel> querySimpleDBModelBymyString() throws IOException {
        return mDb.queryIndex("SimpleDBModel.myString", SimpleDBModel.class);
    }

}