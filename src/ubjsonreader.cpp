
#include <cstdint>
#include <cstring>
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

  bool readChar(InputStream& in, Value& retval) {
    char val;
    READ_FAIL(in, (byte*)&val, 1)
    retval = val;
  }

  bool readInt8(InputStream& in, Value& retval) {
    int8_t val;
    READ_FAIL(in, (byte*)&val, 1)
    retval = val;
  }

  bool readUint8(InputStream& in, Value& retval) {
    uint8_t val;
    READ_FAIL(in, (byte*)&val, 1)
    retval = val;
  }

  bool readInt16(InputStream& in, Value& retval) {
    int16_t val;
    READ_FAIL(in, (byte*)&val, 2)
    val = be16toh(val);
    retval = val;
  }
  
  bool readInt32(InputStream& in, Value& retval) {
    int32_t val;
    READ_FAIL(in, (byte*)&val, 4);
    val = be32toh(val);
    retval = val;
  }
  
  bool readInt64(InputStream& in, Value& retval) {
    int64_t val;
    READ_FAIL(in, (byte*)&val, 8);
    val = be64toh(val);
    retval = val;
  }
  
  #define FLOAT_CONV(x) *reinterpret_cast<float*>(&x);
  
  bool readFloat32(InputStream& in, Value& retval) {
    int32_t val;
    READ_FAIL(in, (byte*)&val, 4);
    val = be32toh(val);
    retval = FLOAT_CONV(val);
  }
  
  #define DOUBLE_CONV(x) *reinterpret_cast<double*>(&x)
  
  bool readFloat64(InputStream& in, Value& retval) {
    int64_t val;
    READ_FAIL(in, (byte*)&val, 8);
    val = be64toh(val);
    retval = DOUBLE_CONV(val);
  }
  
  bool readString(InputStream& in, Value& retval) {
    
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
        return readInt8(in, retval);

      case ubjson::Uint8:
        return readUint8(in, retval);

      case ubjson::Int16:
        return readInt16(in, retval);

      case ubjson::Int32:
        return readInt32(in, retval);

      case ubjson::Float32:
        return readFloat32(in, retval);

      case ubjson::Float64:
        return readFloat64(in, retval);

      case ubjson::String:
        return readString(in, retval);

    }
    return false;
  }

  bool UBJSONReader::read(Value& retval) {
    return readValue(mInput, retval);

  }

} // namespace microdb
