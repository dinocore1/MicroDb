
package pkg.project;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.annotations.DBObj;

@DBObj
public class SimpleDBModel extends DBObject {


    String myString;
    transient String notSaved;
    int myInt;
    long myLong;

}