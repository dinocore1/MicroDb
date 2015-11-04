package pkg.project;

import com.devsmart.microdb.DefaultChangeListener;
import com.devsmart.microdb.Driver;
import com.devsmart.microdb.Emitter;
import com.devsmart.microdb.MapFunction;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.ObjectIterator;
import com.devsmart.microdb.SimpleDBModel_pxy;
import com.devsmart.microdb.Utils;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;
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

        db.addChangeListener(new DefaultChangeListener() {
            @Override
            public void onBeforeInsert(Driver driver, UBValue value) {
                if(Utils.isValidObject(value, SimpleDBModel_pxy.TYPE)) {
                    final long longValue = driver.incrementLongField("varSimpleDBModel.myLong");
                    value.asObject().put("myLong", UBValueFactory.createInt(longValue));
                }

            }

        });

    }

    public ObjectIterator<String, SimpleDBModel> querySimpleDBModelBymyString() throws IOException {
        return mDb.queryIndex("SimpleDBModel.myString", SimpleDBModel.class);
    }

}