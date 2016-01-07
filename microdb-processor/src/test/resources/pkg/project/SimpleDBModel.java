
package pkg.project;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.Link;
import com.devsmart.microdb.annotations.AutoIncrement;
import com.devsmart.microdb.annotations.DBObj;
import com.devsmart.microdb.annotations.Index;
import com.devsmart.ubjson.UBValue;

@DBObj
public class SimpleDBModel extends DBObject {

    //only private fields will be persisted. You must create
    //getters and setters for every field
    @Index
    private String myString;

    public String getMyString() {
        return myString;
    }

    public void setMyString(String myString) {
        this.myString = myString;
    }


    //transient fields are ignored
    private transient String notSaved;

    private boolean myBool;
    public boolean getMyBool() {
        return myBool;
    }

    public void setMyBool(boolean myBool) {
        this.myBool = myBool;
    }

    private byte myByte;
    public byte getMyByte() {
        return myByte;
    }

    public void setMyByte(byte myByte) {
        this.myByte = myByte;
    }

    private short myShort;
    public short getMyShort() {
        return myShort;
    }

    public void setMyShort(short myShort) {
        this.myShort = myShort;
    }

    private int myInt;
    public int getMyInt() {
        return myInt;
    }
    public void setMyInt(int value) {
        myInt = value;
    }

    @AutoIncrement
    private long myLong;
    public long getMyLong() {
        return myLong;
    }

    public void setMyLong(long myLong) {
        this.myLong = myLong;
    }

    @Index
    private long indexLong;

    public long getIndexLong() {
        return indexLong;
    }

    public void setIndexLong(long indexLong) {
        this.indexLong = indexLong;
    }

    private SimpleDBModel internal;
    public SimpleDBModel getInternal() {
        return internal;
    }

    public void setInternal(SimpleDBModel value) {
        internal = value;
    }

    //Links must be public
    public Link<SimpleDBModel> link;

    private float[] myFloatArray;
    public float[] getMyFloatArray() {
        return myFloatArray;
    }
    public void setMyFloatArray(float[] value) {
        myFloatArray = value;
    }

    private double[] myDoubleArray;
    public double[] getMyDoubleArray() {
        return myDoubleArray;
    }

    public void setMyDoubleArray(double[] myDoubleArray) {
        this.myDoubleArray = myDoubleArray;
    }

    private SimpleDBModel[] addresses;

    public SimpleDBModel[] getAddresses() {
        return addresses;
    }

    public void setAddresses(SimpleDBModel[] addresses) {
        this.addresses = addresses;
    }

    private UBValue genericValue;

    public UBValue getGenericValue() {
        return genericValue;
    }

    public void setGenericValue(UBValue genericValue) {
        this.genericValue = genericValue;
    }

    private MyDatum myDatum;

    public MyDatum getMyDatum() {
        return myDatum;
    }

    public void setMyDatum(MyDatum myDatum) {
        this.myDatum = myDatum;
    }


}