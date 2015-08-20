
package pkg.project;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.Link;
import com.devsmart.microdb.annotations.DBObj;

@DBObj
public class SimpleDBModel extends DBObject {

    //only private fields will be persisted. You must create
    //getters and setters for every field
    private String myString;

    public String getMyString() {
        return myString;
    }

    public void setMyString(String myString) {
        this.myString = myString;
    }


    //transient fields are ignored
    private transient String notSaved;

    private int myInt;

    public int getMyInt() {
        return myInt;
    }
    public void setMyInt(int value) {
        myInt = value;
    }


    private SimpleDBModel internal;
    public SimpleDBModel getInternal() {
        return internal;
    }

    public void setInternal(SimpleDBModel value) {
        internal = value;
    }

    //Links must be public and must not have getter and setter
    public Link<SimpleDBModel> link;

}