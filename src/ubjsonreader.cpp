
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
  
  bool readValue(InputStream& in, const byte control, Value& retval);
  bool readInt(InputStream& in, const byte type, int64_t& retval);
  
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
  
  bool readFloat32(InputStream& in, Value& retval) {

    #define FLOAT_CONV(x) *reinterpret_cast<float*>(&x);

    int32_t val;
    READ_FAIL(in, (byte*)&val, 4);
    val = be32toh(val);
    retval = FLOAT_CONV(val);
    return true;
  }

  bool readFloat64(InputStream& in, Value& retval) {
    
    #define DOUBLE_CONV(x) *reinterpret_cast<double*>(&x)
    
    int64_t val;
    READ_FAIL(in, (byte*)&val, 8);
    val = be64toh(val);
    retval = DOUBLE_CONV(val);
    return true;
  }
  
  bool readData(InputStream& in, size_t numBytes, std::unique_ptr<byte>& retval) {
    std::unique_ptr<byte> buf(new byte[numBytes]);
    CHECK_FAIL(in.ReadFully( buf.get(), numBytes ) != numBytes)
    retval = std::move(buf);
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
    
    Value v;
    byte control, type;
    READ_FAIL(in, &control, 1)
    if(control == ubjson::Optimized_Type) {
      READ_FAIL(in, &type, 1)
      
      //if optimized type appears, optimized size must come right after
      READ_FAIL(in, &control, 1)
      CHECK_FAIL(control == ubjson::Optimized_Size)
      
      //int type
      READ_FAIL(in, &control, 1)
      int64_t size;
      CHECK_FAIL(readInt(in, control, size))
      
      for(int64_t i=0;i<size;i++) {
        readValue(in, type, v);
        retval.Add( std::move(v) );
      }
    } else if(control == ubjson::Optimized_Size) {
      //int type
      READ_FAIL(in, &control, 1)
      int64_t size;
      CHECK_FAIL(readInt(in, control, size))
      
      
      for(int64_t i=0;i<size;i++) {
        READ_FAIL(in, &type, 1)
        readValue(in, type, v);
        retval.Add( std::move(v) );
      }
    } else {
      
      while(control != ubjson::Array_End) {
        
        CHECK_FAIL(readValue(in, control, v));
        retval.Add( std::move(v) );
        
        READ_FAIL(in, &control, 1)
      }
    }
    
  }
  
  bool readObject(InputStream& in, Value& retval) {
    
    Value key, value;
    byte control, type;
    READ_FAIL(in, &control, 1)
    if(control == ubjson::Optimized_Type) {
      READ_FAIL(in, &type, 1)
      
      //if optimized type appears, optimized size must come right after
      READ_FAIL(in, &control, 1)
      CHECK_FAIL(control == ubjson::Optimized_Size)
      
      //int type
      READ_FAIL(in, &control, 1)
      int64_t size;
      CHECK_FAIL(readInt(in, control, size))
      
      for(int64_t i=0;i<size;i++) {
        CHECK_FAIL(readString(in, key));
        CHECK_FAIL(readValue(in, type, value));
        retval[key.asString()] = std::move(value);
      }
    } else if(control == ubjson::Optimized_Size) {

      READ_FAIL(in, &control, 1)
      int64_t size;
      CHECK_FAIL(readInt(in, control, size))
      
      for(int64_t i=0;i<size;i++) {
        CHECK_FAIL(readString(in, key));
        
        READ_FAIL(in, &type, 1)
        CHECK_FAIL(readValue(in, type, value));
        retval.Set(key.asString(), std::move(value));
      }
    } else {
      
      int64_t keySize;
      std::unique_ptr<byte> strBuf;
      
      while(control != ubjson::Object_End) {
          
        CHECK_FAIL(readInt(in, control, keySize))
        CHECK_FAIL(readData(in, keySize, strBuf))
        
        READ_FAIL(in, &control, 1)
        CHECK_FAIL(readValue(in, control, value));
        
        retval.Set(std::string((const char*)strBuf.get(), keySize), std::move(value));
        
        READ_FAIL(in, &control, 1)
      }
    }
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
  
  bool readValue(InputStream& in, const byte control, Value& retval) {
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
      
      case ubjson::Object_Start:
        return readObject(in, retval);

    }
    return false;
  }

  bool UBJSONReader::read(Value& retval) {
    byte type;
    READ_FAIL(mInput, &type, 1)
    return readValue(mInput, type, retval);

  }

} // namespace microdb
