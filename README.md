# MicroDB #

MicroDB is an embedded NoSQL Database. MicroDB emphasizes speed, low complexity, and synchronization.
Data is stored as [UBJSON](http://ubjson.org/) objects and indices are created using a simple map-reduce framework.
MicroDB is built on top of [LevelDB](http://leveldb.org/) - a fast database engine built by some guys at google.

MicroDB has bindings libraries for Java and support for Android. Data objects are modeled using POJOs and all
serialization code is automatically generated.

### Model Data objects as Plain Old Java Objects ###

Data objects are modeled as POJOs. Fields can be of any primitive type, primitive arrays type, anything that extends
 DBObject, or special `Link<T extends DBObject>`. The Link class is used to reference other DBObject instances instead
 of embedding into the object itself.

```

@DBObj
public class Person extends DBObject {

  private String firstName;
  private String lastName;
  private int age;

  public Link<Address> address;

  //transient fields are ignored
  private transient String notSaved;

  // + Standard setters and getters here
}

@DBObj
public class Address extends DBObject {
  private String firstLine;
  private int zipCode;

  // + Standard setters and getters here
}

```

A valid DBObject must follow these rules:

* must extend DBObject
* must be annotated with `@DBObj`
* fields must be private and have corresponding getters and setters
* fields of type `Link<T>` must be public
* fields with transient are not persisted


### Database ###

MicroDB is very simple to use. 
```
mDatabase = DBBuilder.builder(new File("path/to/dbfile")
              .build();

...

Person newPersion = mDatabase.create(Person.class);
newPersion.setFirstName("Santa");

System.out.println("Created new person object. Database id is: " + newPersion.getId());

```

### Queries ###

```
ObjIterator<Person> it = mDatabase.getAll(Person.class);
while(it2.valid()) {
  Person p = it.get();
  it.next();
}
```

