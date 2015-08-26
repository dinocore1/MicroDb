#ifndef VALUE_H_
#define VALUE_H_

#include <memory>
#include <unordered_map>
#include <vector>
#include <string>

namespace microdb {

class Value {
public:
  typedef std::unique_ptr<Value> UqPtr;
  typedef std::unordered_map<std::string, UqPtr> ObjectType;
  typedef std::vector<UqPtr> ArrayType;
  typedef std::vector<uint8_t> BinaryType;
  typedef std::string StringType;
  typedef std::vector<std::string> KeysType;

  enum class Type {
        Null,
        Char,
        Bool,
        SignedInt,
        UnsignedInt,
        Float,
        Object,
        Array,
        Binary,
        String
  };

private:

  union ValueHolder {
    char Char;
    bool Bool;
    int64_t SignedInt;
    uint64_t UnsignedInt;
    double Float;
    ObjectType Object;
    ArrayType Array;
    BinaryType Data;
    StringType String;

    ValueHolder() {};
    ~ValueHolder() {};
  };

  ValueHolder mValue;
  Type mType;

  void destruct();
  void construct_fromString(std::string&& s);
  void construct_fromBinary(BinaryType&& b);
  void construct_fromArray(ArrayType&& a);
  void construct_fromObject(ObjectType&& o);

public:

  Value(const Value&);
  Value(Value&&);
  ~Value();

  void CopyFrom(const Value&);
  void MoveFrom(Value&&);

  /**
  * create null value
  */
  Value();
  Value(int);
  Value(bool);
  Value(char);
  Value(double);
  Value(int64_t);
  Value(uint64_t);
  Value(const char*);
  Value(std::string);
  Value(const void* ptr, size_t len);

  Value::Type GetType() const;
  bool IsNull() const;
  bool IsChar() const;
  bool IsBool() const;
  bool IsObject() const;
  bool IsArray() const;
  bool IsString() const;
  bool IsBinary() const;
  bool IsNumber() const;
  bool IsInteger() const;
  bool IsSignedInteger() const;
  bool IsUnsignedInteger() const;
  bool IsFloat() const;

  /**
  * returns the size of the string, array, or object
  */
  size_t Size() const;

  void SetNull();

  /**
  * convert to a bool.
  * correct value guarenteed if IsBool == true
  * - if is numeric, returns true if value is not 0
  * - if is char, returns true if value is not '0'
  * - if string, object, array, or binary, return true if size() > 0
  */
  bool asBool() const;

  char asChar() const;

  /**
  * convert to int
  * correct value guarenteed if IsInteger
  * - if string, attempt to convert using atoi, return Size() otherwise
  */
  int asInt() const;

  unsigned int asUint() const;

  /**
  * convert to int64
  * correct value guarenteed if IsInteger
  * - if string, attempt to convert using atoi, return Size() otherwise
  */
  int64_t asInt64() const;

  uint64_t asUint64() const;

  double asFloat() const;

  std::string asString() const;

  Value& operator= (const Value&);
  Value& operator= (Value&&);

  //Array Operations
  Value& operator[] (int);
  Value const& operator[] (int) const;
  void Add(const Value& v);
  void Add(Value&&);


  //Object Operations
  void SetObject();
  KeysType GetKeys() const;
  bool HasKey(const std::string& key);
  Value const& Get(const std::string&) const;
  Value const& Get(const char*) const;
  void Set(const std::string& key, const Value& value);
  void Set(const std::string& key, Value&& value);
  Value& operator [] (const char*);
  Value const& operator [] (const char*) const;
  Value& operator [] (const std::string&);
  Value const& operator [] (const std::string&) const;

};

int compareValue(const microdb::Value& lhs, const microdb::Value& rhs);

} // namespace microdb


bool operator< (const microdb::Value& lhs, const microdb::Value& rhs);
bool operator<= (const microdb::Value& lhs, const microdb::Value& rhs);
bool operator> (const microdb::Value& lhs, const microdb::Value& rhs);
bool operator>= (const microdb::Value& lhs, const microdb::Value& rhs);
bool operator== (const microdb::Value& lhs, const microdb::Value& rhs);
bool operator!= (const microdb::Value& lhs, const microdb::Value& rhs);

#endif // VALUE_H_
