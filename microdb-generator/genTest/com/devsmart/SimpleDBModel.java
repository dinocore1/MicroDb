package com.devsmart;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.MicroDB;
import com.devsmart.microdb.Utils;
import com.devsmart.ubjson.UBArray;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

public class SimpleDBModel extends DBObject {
  public static final UBString TYPE = UBValueFactory.createString("SimpleDBModel");

  private float myDouble;

  private SimpleDBModel[] myObj;

  @Override
  public void writeToUBObject(UBObject obj) {
    super.writeToUBObject(obj);
    final MicroDB db = getDB();
    obj.put("myDouble", UBValueFactory.createFloat32(myDouble));
    obj.put("myObj", Utils.createArrayOrNull(db, myObj));
  }

  @Override
  public void readFromUBObject(UBObject obj) {
    super.readFromUBObject(obj);
    final MicroDB db = getDB();
    UBValue value = null;
    value = obj.get("myDouble");
    if (value != null) {
      this.myDouble = value.asFloat32();
    }
    value = obj.get("myObj");
    if (value != null && value.isArray()) {
      UBArray array = value.asArray();
      final int size = array.size();
      this.myObj = new SimpleDBModel[size];
      for (int i=0;i<size;i++) {
        this.myObj[i] = Utils.readDBObj(db, array.get(i), new SimpleDBModel());
      }
    } else {
      this.myObj = null;
    }
  }

  public float getMyDouble() {
    return myDouble;
  }

  public void setMyDouble(float value) {
    this.myDouble = value;
    setDirty();
  }

  public SimpleDBModel[] getMyObj() {
    return myObj;
  }

  public void setMyObj(SimpleDBModel[] value) {
    this.myObj = value;
    setDirty();
  }
}
