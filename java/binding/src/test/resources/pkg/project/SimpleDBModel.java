
package pkg.project;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.Link;
import com.devsmart.microdb.annotations.DBObj;

@DBObj
public class SimpleDBModel extends DBObject {

    public String myString;

    //transient fields are ignored
    transient String notSaved;

    //if instance value is private, you must provide getters and setters
    private int myInt;

    public int getMyInt() {
        return myInt;
    }

    public void setMyInt(int value) {
        myInt = value;
    }

    public SimpleDBModel internal;

    //Links must be public
    public Link<SimpleDBModel> link;

}