
#include "microdb/value.h"

#include <cmath>
#include <limits>
#include <sstream>

template <typename T>
std::string to_string(T value) {
    std::ostringstream os ;
    os << value ;
    return os.str() ;
}

template <typename T>
T to_number(const std::string& str) {
  T i;
  std::istringstream(str) >> i;
  return i;
}

template<typename T, typename ...Args>
std::unique_ptr<T> make_unique( Args&& ...args )
{
    return std::unique_ptr<T>( new T( std::forward<Args>(args)... ) );
}

using namespace std;

namespace microdb {

  template<typename T>
  T unique_ptr_copy(const T& src);

  template<>
  inline Value::ArrayType unique_ptr_copy(const Value::ArrayType& src) {
    Value::ArrayType rtn;
    for(auto& v : src) {
        rtn.emplace_back( make_unique<Value>(*v) );
    }
    rtn.shrink_to_fit();
    return rtn;
  }

  template<>
  inline Value::ObjectType unique_ptr_copy(const Value::ObjectType& src) {
      Value::ObjectType rtn;
      for(auto& v : src) {
          rtn.emplace( std::make_pair(v.first, make_unique<Value>(*(v.second)) ));
      }
      return rtn;
  }

  inline bool in_range(double value, double min, double max) {
    return (min <= value and value <= max);
  }

  void Value::construct_fromString(std::string&& s) {
    new( &(mValue.String)) std::string(std::move(s));
  }

  void Value::construct_fromBinary(BinaryType&& b) {
    new( &(mValue.Data)) BinaryType(std::move(b));
  }

  void Value::construct_fromArray(ArrayType&& a) {
    new( &(mValue.Array)) ArrayType(std::move(a));
  }

  void Value::construct_fromObject(ObjectType&& m) {
    new( &(mValue.Object)) ObjectType(std::move(m));
  }

  Value::Value(const Value& v)
  : Value() {
    CopyFrom(v);
  }

  Value::Value(Value&& v)
  : Value() {
    MoveFrom( std::move(v) );
  }

  Value::~Value() {
    destruct();
  }

  void Value::CopyFrom(const Value& v) {
    destruct();

    switch (v.mType) {
    case Type::Char:
        mValue.Char = v.mValue.Char;
        break;
    case Type::Bool:
        mValue.Bool = v.mValue.Bool;
        break;
    case Type::SignedInt:
        mValue.SignedInt = v.mValue.SignedInt;
        break;
    case Type::UnsignedInt:
        mValue.UnsignedInt = v.mValue.UnsignedInt;
        break;
    case Type::Float:
        mValue.Float = v.mValue.Float;
        break;
    case Type::String:
        construct_fromString( std::string( v.mValue.String ));
        break;
    case Type::Binary:
        construct_fromBinary( BinaryType( v.mValue.Data ));
        break;
    case Type::Array:
        construct_fromArray( ArrayType(unique_ptr_copy(v.mValue.Array)));
        break;
    case Type::Object:
        construct_fromObject( ObjectType(unique_ptr_copy(v.mValue.Object)));
        break;
    default:
        break;
    }
    mType = v.mType;
  }

  void Value::MoveFrom(Value&& v) {
    destruct();

    switch (v.mType) {
    case Type::Char:
        mValue.Char = v.mValue.Char;
        break;
    case Type::Bool:
        mValue.Bool = v.mValue.Bool;
        break;
    case Type::SignedInt:
        mValue.SignedInt = v.mValue.SignedInt;
        break;
    case Type::UnsignedInt:
        mValue.UnsignedInt = v.mValue.UnsignedInt;
        break;
    case Type::Float:
        mValue.Float = v.mValue.Float;
        break;
    case Type::String:
        construct_fromString( std::move(v.mValue.String) );
        break;
    case Type::Binary:
        construct_fromBinary( std::move(v.mValue.Data) );
        break;
    case Type::Array:
        construct_fromArray( std::move(v.mValue.Array) );
        break;
    case Type::Object:
        construct_fromObject( std::move(v.mValue.Object) );
        break;
    default:
        break;
    }

    mType = v.mType;
    v.destruct();
  }

  void Value::destruct() {
    switch(mType) {
      case Type::Null:
        return;
      case Type::String:
        mValue.String.~string();
        break;
      case Type::Binary:
        mValue.Data.~vector();
        break;
      case Type::Object:
        mValue.Object.~unordered_map();
        break;
      case Type::Array:
        mValue.Array.~vector();
        break;

      case Type::Char:
      case Type::Bool:
      case Type::SignedInt:
      case Type::UnsignedInt:
      case Type::Float:
        break;
    }

    mType = Type::Null;
  }

  Value::Value()
  : mType(Type::Null) {
  }

  Value::Value(int value)
  : mType(Type::SignedInt) {
    mValue.SignedInt = value;
  }

  Value::Value(bool value)
  : mType(Type::Bool) {
    mValue.Bool = value;
  }

  Value::Value(char value)
  : mType(Type::Char) {
    mValue.Char = value;
  }

  Value::Value(double value)
  : mType(Type::Float) {
    mValue.Float = value;
  }

  Value::Value(int64_t value)
  : mType(Type::SignedInt) {
    mValue.SignedInt = value;
  }

  Value::Value(uint64_t value)
  : mType(Type::UnsignedInt) {
    mValue.UnsignedInt = value;
  }

  Value::Value(const char* str)
  : Value(std::string(str)) { }

  Value::Value(std::string str)
  : mType(Type::String) {
    construct_fromString( std::move(str) );
  }

  Value::Value(const void* ptr, size_t len)
  : mType(Type::Binary) {
    const uint8_t* begin = static_cast<const uint8_t*>(ptr);
    construct_fromBinary( BinaryType(begin, begin + len) );
  }

  Value::Type Value::GetType() const {
    return mType;
  }

  bool Value::IsNull() const {
    return mType == Type::Null;
  }

  bool Value::IsChar() const {
    return mType == Type::Char;
  }

  bool Value::IsBool() const {
    return mType == Type::Bool;
  }

  bool Value::IsObject() const {
    return mType == Type::Object;
  }

  bool Value::IsArray() const {
    return mType == Type::Array;
  }

  bool Value::IsBinary() const {
    return mType == Type::Binary;
  }

  bool Value::IsString() const {
    return mType == Type::String;
  }

  bool Value::IsNumber() const {
    return IsInteger() || IsFloat();
  }

  bool Value::IsInteger() const {
    return IsSignedInteger() || IsUnsignedInteger();
  }

  bool Value::IsSignedInteger() const {
    return mType == Type::SignedInt;
  }

  bool Value::IsUnsignedInteger() const {
    return mType == Type::UnsignedInt;
  }

  bool Value::IsFloat() const {
    return mType == Type::Float;
  }

  size_t Value::Size() const {
    switch(mType) {
      case Type::Null:
        return 0;
      case Type::Array:
        return mValue.Array.size();
      case Type::String:
        return mValue.String.size();
      case Type::Object:
        return mValue.Object.size();
      case Type::Binary:
        return mValue.Data.size();
      default:
        return 1;
    }
  }

  void Value::SetNull() {
    destruct();
  }

  int Value::asInt() const {
    using limit = std::numeric_limits<int>;
    const int retval = asInt64();
    return in_range(retval, limit::lowest(), limit::max()) ? retval : 0;
  }

  unsigned int Value::asUint() const {
    using limit = std::numeric_limits<unsigned int>;
    const unsigned int retval = asUint64();
    return in_range(retval, limit::lowest(), limit::max()) ? retval : 0;
  }

  int64_t Value::asInt64() const {
    using limit = std::numeric_limits<int64_t>;

    if(IsSignedInteger())
        return mValue.SignedInt;
    if(IsUnsignedInteger())
        return in_range(mValue.UnsignedInt, limit::lowest(), limit::max()) ? mValue.UnsignedInt : 0;
    if(IsFloat())
        return in_range(mValue.Float, limit::lowest(), limit::max()) ? mValue.Float : 0;
    if(IsChar())
        return static_cast<int64_t>(mValue.Char);
    if(IsBool())
        return mValue.Bool ? 1 : 0;
    if(IsString()) {
        return to_number<int64_t>(mValue.String);
    }
    return Size();
  }

  uint64_t Value::asUint64() const {
    using limit = std::numeric_limits<uint64_t>;

    if(IsUnsignedInteger())
        return mValue.UnsignedInt;
    if(IsSignedInteger())
        return in_range(mValue.SignedInt, limit::lowest(), limit::max()) ? mValue.SignedInt : 0;
    if(IsFloat())
        return in_range(mValue.Float, limit::lowest(), limit::max()) ? mValue.Float : 0;
    if(IsChar())
        return static_cast<uint64_t>(mValue.Char);
    if(IsBool())
        return mValue.Bool ? 1 : 0;
    if(IsString()) {
        return to_number<uint64_t>(mValue.String);
       
    }

    return Size();
  }

  double Value::asFloat() const {
    if(IsFloat()) {
      return mValue.Float;
    } else if(IsString()){
      return to_number<double>(mValue.String);
    } else {
      double k1 = asUint64();
      double k2 = asInt64();
      return k1 > k2 ? k1 : k2;
    }
  }

  bool Value::asBool() const {
    if(IsBool())
        return mValue.Bool;
    if(IsUnsignedInteger())
        return mValue.UnsignedInt != 0;
    if(IsSignedInteger())
        return mValue.SignedInt != 0;
    if(IsFloat())
        return mValue.Float != 0;
    if(IsChar())
        return mValue.Char != '\0';
    return Size() != 0;
  }

  char Value::asChar() const {
    if(IsChar()) {
      return mValue.Char;
    } else {
      return 0;
    }
  }

  std::string Value::asString() const {
    if(IsString())
        return mValue.String;
    if(IsBool())
        return mValue.Bool ? "true" : "false";
    // if(isBinary())
        //What should we do for Binary? Base64? or what?
    if(IsChar())
        return {mValue.Char};
    if(IsSignedInteger())
        return to_string(mValue.SignedInt);
    if(IsUnsignedInteger())
        return to_string(mValue.UnsignedInt);
    if(IsFloat())
        return to_string(mValue.Float);
    return "";
  }

  Value& Value::operator= (const Value& v) {
    if(this != &v) {
      CopyFrom(v);
    }
    return *this;
  }

  Value& Value::operator= (Value&& v) {
    MoveFrom( std::move(v) );
    return *this;
  }

  //Array Operations
  Value& Value::operator[] (int i) {
    if(mType == Type::Array) {
        return *(mValue.Array[i]);
    } else {
      throw std::logic_error("Attempt to index 'Value'; 'Value' is not an Array!");
    }
  }

  Value const& Value::operator[] (int i) const {
    if(mType == Type::Array) {
        return *(mValue.Array[i]);
    } else {
      throw std::logic_error("Attempt to index 'Value'; 'Value' is not an Array!");
    }
  }

  void Value::Add(const Value& v) {
    switch(mType) {
      case Type::Null:
        construct_fromArray( ArrayType() );
        mType = Type::Array;
      case Type::Array:
        mValue.Array.emplace_back( make_unique<Value>(v) );
        break;
      default:
        throw std::logic_error("not an array type");
    }
  }

  void Value::Add(Value&& v) {
    switch(mType) {
      case Type::Null:
        construct_fromArray( ArrayType() );
        mType = Type::Array;
      case Type::Array:
        mValue.Array.emplace_back( make_unique<Value>( std::move(v) ));
        break;
      default:
        throw std::logic_error("not an array type");
    }
  }

  //Object Operations

  bool Value::HasKey(const std::string& key) {
    switch(mType) {
      case Type::Object:
        return mValue.Object.find(key) != mValue.Object.end();
      default:
        return false;
    }
  }

  Value::KeysType Value::GetKeys() const {
    Value::KeysType retval;
    if(mType == Type::Object) {
      retval.reserve(mValue.Object.size());
      for(const auto& entry : mValue.Object) {
        retval.push_back(entry.first);
      }
    }

    return retval;
  }

  Value const& Value::Get(const std::string& key) const {
    if(mType == Type::Object) {
      return *(const_cast<const ObjectType&>(mValue.Object).at(key));
    } else {
      throw std::logic_error("not an object type");
    }
  }

  Value const& Value::Get(const char* key) const {
    return Get( std::string(key) );
  }

  void Value::Set(const std::string& key, const Value& value) {
    switch(mType) {
      case Type::Null:
        construct_fromObject( ObjectType() );
        mType = Type::Object;
      case Type::Object:
        mValue.Object[key] = make_unique<Value>(value);
        break;
      default:
        throw std::logic_error("not an object type");
    }
  }

  void Value::Set(const std::string& key, Value&& value) {
    switch(mType) {
      case Type::Null:
        construct_fromObject( ObjectType() );
        mType = Type::Object;
      case Type::Object:
        mValue.Object[key] = make_unique<Value>( std::move(value) );
        break;
      default:
        throw std::logic_error("not an object type");
    }
  }

  Value& Value::operator[] (const std::string& key) {
    if(mType == Type::Object) {
      if(mValue.Object.find(key) == mValue.Object.end()) {
        mValue.Object.emplace( std::make_pair(key, make_unique<Value>(Value())));
      }
      return *(mValue.Object[key]);
    } else if(mType == Type::Null){
      construct_fromObject( ObjectType() );
      mType = Type::Object;
      mValue.Object.emplace( std::make_pair(key, make_unique<Value>(Value())));
      return *(mValue.Object[key]);
    } else {
      throw std::logic_error("not an object type");
    }
  }

  Value const& Value::operator[] (const std::string& key) const {
    return Get(key);
  }

  Value& Value::operator[] (const char* key) {
    return operator[] (std::string(key));
  }

  Value const& Value::operator[] (const char* key) const {
    return operator[] (std::string(key));
  }
  

  
  #define ORDER_NULL 0
  #define ORDER_CHAR 1
  #define ORDER_NUM 2
  #define ORDER_STR 3
  #define ORDER_ARRAY 4
  #define ORDER_OBJ 5
  
  int getTypeOrder(const Value& v) {
    int retval = ORDER_NULL;
    
    if(v.IsNull()) {
      retval = ORDER_NULL;
    } else if(v.IsChar()) {
      retval = ORDER_CHAR;
    } else if(v.IsNumber()) {
      retval = ORDER_NUM;
    } else if(v.IsString() || v.IsBinary()) {
      retval = ORDER_STR;
    } else if(v.IsArray()) {
      retval = ORDER_ARRAY;
    } else {
      retval = ORDER_OBJ;
    }
    return retval;
  }
  
  int compareArray(const Value& a, const Value& b) {
    const size_t aSize = a.Size();
    const size_t bSize = b.Size();
    int retval;
    const size_t min = std::min(aSize, bSize);
    for(size_t i=0;i<min;i++) {
      retval = compareValue(a[i], b[i]);
      if(retval != 0) {
        return retval;
      }
    }
    if(aSize == bSize) {
        return 0;
    } else if(aSize < bSize) {
      return -1;
    } else {
      return 1;
    }
  }
  
  int compareValue(const microdb::Value& a, const microdb::Value& b){
    int retval = getTypeOrder(a) - getTypeOrder(b);
    if(retval == 0) {
      if(a.IsNumber()) {
        double af = a.asFloat();
        double bf = b.asFloat();
        
        if(af < bf) {
          retval = -1;
        } else if(af > bf) {
          retval = 1;
        } else {
          retval = 0;
        }
        
        /*
        if(std::fabs(af - bf) < std::numeric_limits<float>::epsilon()) {
          retval = 0;
        } else if(af < bf) {
          retval = -1;
        } else {
          retval = 1;
        }
        */
        
      } else if(a.IsString()) {
        retval = a.asString().compare(b.asString());
      } else if(a.IsArray()) {
        retval = compareArray(a, b);
      } else {
        //TODO: handle compareing objects
      }
    }
    return retval;
  }

} //namespace microdb

bool operator< (const microdb::Value& lhs, const microdb::Value& rhs) {
  return compareValue(lhs, rhs) < 0;
}

bool operator<= (const microdb::Value& lhs, const microdb::Value& rhs) {
  return compareValue(lhs, rhs) < 1;
}

bool operator> (const microdb::Value& lhs, const microdb::Value& rhs) {
  return compareValue(lhs, rhs) > 0;
}

bool operator>= (const microdb::Value& lhs, const microdb::Value& rhs) {
  return compareValue(lhs, rhs) > -1;
}

bool operator==(const microdb::Value& lhs, const microdb::Value& rhs) {
  return compareValue(lhs, rhs) == 0;
}

bool operator!=(const microdb::Value& lhs, const microdb::Value& rhs) {
  return compareValue(lhs, rhs) != 0;
}
