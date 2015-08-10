
#include <cstdint>
#include <cstring>
#include <memory>
#include "portable_endian.h"

#include <microdb/value.h>
#include <microdb/serialize.h>

#include "ubjsonhelper.h"

namespace microdb {

  UBJSONReader::UBJSONReader(InputStream& in)
  : mInput(in) { }

  UBJSONReader::~UBJSONReader() { }

  #define READ_FAIL(in, buf, size) \
  if(!in.ReadFully(buf, size)) { return false; }
  
  #define CHECK_FAIL(x) \
  if(!(x)) { return false; }

  bool readChar(InputStream& in, Value& retval) {
    char val;
    READ_FAIL(in, (byte*)&val, 1)
    retval = val;
    return true;
  }

  bool readInt8(InputStream& in, int64_t& retval) {
    int8_t val;
    READ_FAIL(in, (byte*)&val, 1)
    retval = val;
    return true;
  }

  bool readUint8(InputStream& in, int64_t& retval) {
    uint8_t val;
    READ_FAIL(in, (byte*)&val, 1)
    retval = val;
    return true;
  }

  bool readInt16(InputStream& in, int64_t& retval) {
    int16_t val;
    READ_FAIL(in, (byte*)&val, 2)
    val = be16toh(val);
    retval = val;
    return true;
  }
  
  bool readInt32(InputStream& in, int64_t& retval) {
    int32_t val;
    READ_FAIL(in, (byte*)&val, 4);
    val = be32toh(val);
    retval = val;
    return true;
  }
  
  bool readInt64(InputStream& in, int64_t& retval) {
    int64_t val;
    READ_FAIL(in, (byte*)&val, 8);
    val = be64toh(val);
    retval = val;
    return true;
  }
  
  bool readInt(InputStream& in, const byte type, int64_t& retval) {
    switch(type) {
      case ubjson::Int8:
        return readInt8(in, retval);

      case ubjson::Uint8:
        return readUint8(in, retval);

      case ubjson::Int16:
        return readInt16(in, retval);

      case ubjson::Int32:
        return readInt32(in, retval);
        
      case ubjson::Int64:
        return readInt64(in, retval);
        
      default:
        return false;
    }
  }
  
  #define FLOAT_CONV(x) *reinterpret_cast<float*>(&x);
  
  bool readFloat32(InputStream& in, Value& retval) {
    int32_t val;
    READ_FAIL(in, (byte*)&val, 4);
    val = be32toh(val);
    retval = FLOAT_CONV(val);
    return true;
  }
  
  #define DOUBLE_CONV(x) *reinterpret_cast<double*>(&x)
  
  bool readFloat64(InputStream& in, Value& retval) {
    int64_t val;
    READ_FAIL(in, (byte*)&val, 8);
    val = be64toh(val);
    retval = DOUBLE_CONV(val);
    return true;
  }
  
  bool readString(InputStream& in, Value& retval) {
    byte control;
    READ_FAIL(in, &control, 1)
    int64_t size;
    CHECK_FAIL(readInt(in, control, size))
    
    std::unique_ptr<char> buf(new char[size]);
    CHECK_FAIL(in.ReadFully( (byte*)buf.get(), size) != size)
    retval = std::string( buf.get(), (size_t)size );
    return true;
  }
  
  bool readArray(InputStream& in, Value& retval) {
    
    
  }

  bool readValue(InputStream& in, Value& retval) {
    byte control;
    READ_FAIL(in, &control, 1)
    switch(control) {
      case ubjson::Null:
        retval.SetNull();
        return true;
        
      case ubjson::True:
        retval = true;
        return true;
        
      case ubjson::False:
        retval = false;
        return true;
        
      case ubjson::Char:
        return readChar(in, retval);

      case ubjson::Int8:
      case ubjson::Uint8:
      case ubjson::Int16:
      case ubjson::Int32:
      case ubjson::Int64:
        int64_t val;
        CHECK_FAIL(readInt(in, control, val))
        retval = val;
        return true;

      case ubjson::Float32:
        return readFloat32(in, retval);

      case ubjson::Float64:
        return readFloat64(in, retval);

      case ubjson::String:
        return readString(in, retval);
        
      case ubjson::Array_Start:
        return readArray(in, retval);

    }
    return false;
  }

  bool UBJSONReader::read(Value& retval) {
    return readValue(mInput, retval);

  }

} // namespace microdb
