package pkg.project;


import com.devsmart.microdb.Datum;
import com.devsmart.ubjson.UBArray;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

public class MyDatum implements Datum {

    public int a;
    public int b;

    @Override
    public UBValue toUBValue() {
        return UBValueFactory.createArray(new int[]{a, b});
    }

    @Override
    public void fromUBValue(UBValue value) {
        UBArray array = value.asArray();
        a = array.get(0).asInt();
        b = array.get(1).asInt();

    }
}
