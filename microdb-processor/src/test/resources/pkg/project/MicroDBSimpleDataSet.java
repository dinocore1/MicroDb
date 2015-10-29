package pkg.project;

import com.devsmart.microdb.Emitter;
import com.devsmart.microdb.MapFunction;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.ObjectIterator;
import com.devsmart.ubjson.UBValue;

public class MicroDBSimpleDataSet extends SimpleDataSet {

    private MicroDB mDb;

    public MicroDBSimpleDataSet(MicroDB db) {
        mDb = db;
    }

    private void install(MicroDB db) {

        db.addIndex("SimpleDBModel.myString", new MapFunction<String>() {
            @Override
            public void map(UBValue value, Emitter<String> emitter) {
                if(value != null && value.isObject()) {
                    UBValue v = value.asObject().get("myString");
                    if(v != null && v.isString()) {
                        emitter.emit(v.asString());
                    }
                }
            }
        });

    }

    public ObjectIterator<String, SimpleDBModel> querySimpleDBModelMyString() {
        return mDb.queryIndex("SimpleDBModel.myString", SimpleDBModel.class);
    }

}