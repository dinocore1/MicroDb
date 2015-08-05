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

private:

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
    ~ValueHolder() {}
  };

  ValueHolder mValue;
  Type mType;

  void destruct();

public:

  Value(const Value&);
  ~Value();

  void Copy(const Value&);

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
  Value(const std::string&);
  Value(const void* ptr, size_t len);

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

  /**
  * convert to a bool.
  * correct value guarenteed if IsBool == true
  * - if is numeric, returns true if value is not 0
  * - if is char, returns true if value is not '0'
  * - if string, object, array, or binary, return true if size() > 0
  */
  bool asBool() const;

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

  void Add(const Value& v);


};

} // namespace microdb

#endif // VALUE_H_
