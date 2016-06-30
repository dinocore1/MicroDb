# MicroDB #

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.dev-smart/microdb-runtime/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.dev-smart/microdb-runtime)

MicroDB is an embedded NoSQL Database. MicroDB emphasizes speed, low complexity, and ease.
Data is stored as [UBJSON](http://ubjson.org/) objects and indices are created using a simple map-reduce framework.
MicroDB is built on top of [MapDB](http://mapdb.org/) - a fast database engine pure java with no dependencies.

Data objects are modeled using a simple definition language and all serialization code is automatically generated.

### Model Data ###

Data objects are modeled using DBO definition language.

```
package org.example;

dbo MyDBObj {

    int myInt;
    float myFloat;

}


```
MicroDB compiles .dbo files and outputs Java source files that handle all the serialization to/from
UBJSON values. The above example will output the following Java source:

```
package org.example;

import com.devsmart.microdb.DBObject;
import com.devsmart.microdb.MicroDB;
import com.devsmart.ubjson.UBObject;
import com.devsmart.ubjson.UBString;
import com.devsmart.ubjson.UBValue;
import com.devsmart.ubjson.UBValueFactory;

public class MyDBObj extends DBObject {
  public static final UBString TYPE = UBValueFactory.createString("MyDBObj");

  private int myInt;

  private float myFloat;

  @Override
  public void writeToUBObject(UBObject obj) {
    super.writeToUBObject(obj);
    final MicroDB db = getDB();
    obj.put("myInt", UBValueFactory.createInt(myInt));
    obj.put("myFloat", UBValueFactory.createFloat32(myFloat));
  }

  @Override
  public void readFromUBObject(UBObject obj) {
    super.readFromUBObject(obj);
    final MicroDB db = getDB();
    UBValue value = null;
    value = obj.get("myInt");
    if (value != null) {
      this.myInt = value.asInt();
    }
    value = obj.get("myFloat");
    if (value != null) {
      this.myFloat = value.asFloat32();
    }
  }

  public int getMyInt() {
    return myInt;
  }

  public void setMyInt(int value) {
    this.myInt = value;
    setDirty();
  }

  public float getMyFloat() {
    return myFloat;
  }

  public void setMyFloat(float value) {
    this.myFloat = value;
    setDirty();
  }
}
```

If you are using gradle to build your Java or Android project, you can easily install the
DBO compiler by adding the following to your build.gradle:

```
buildscript {

    repositories {
        mavenCentral()
    }

    dependencies {
        classpath 'com.dev-smart:microdb-gradleplugin:0.3.0-SNAPSHOT'
    }
}

apply plugin: 'java'
apply plugin: 'com.devsmart.microdb'

dependencies {
    compile 'com.dev-smart:microdb-runtime:0.3.0-SNAPSHOT'
}
```
Place .dbo files somewhere under the src/main/java and the compiler will output java source
into the generated-sources directory.

### Database ###

MicroDB is very simple to use. 
```
mDatabase = DBBuilder.builder(new File("path/to/dbfile")
              .build();

...

Person newPersion = mDatabase.insert(Person.class);
newPersion.setFirstName("Santa");

System.out.println("Created new person object. Database id is: " + newPersion.getId());

```

### Queries ###

```
while(Person p : mDatabase.getAllOfType(Person.class)) {
  //do something with p - its a POJO!
  ...
}
```

You can also query object by any `@Index` annotation:

```
while(Person p : mDatabase.queryIndex("age", Person.class, 30, true, 45, true)) {
  //do something with p - its a POJO!
  ...
}
```
