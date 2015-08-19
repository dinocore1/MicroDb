
package pkg.project;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.annotations.DBObj;
import com.devsmart.microdb.annotations.Link;

@DBObj
public class SimpleDBModel extends DBObject {

    private String myString;

    //transient fields are ignored
    private transient String notSaved;

    //only private fields are persisted, public fields are ignored
    public String pubString;

    private int myInt;

    private SimpleDBModel internal;

    @Link
    private SimpleDBModel link;

}