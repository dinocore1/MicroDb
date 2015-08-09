
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
  if(in.Read(buf, size) != size) { return false; }

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

  bool readIn16(InputStream& in, Value& retval) {
    int16_t val;
    READ_FAIL(in, (byte*)&val, 2)
  }

  bool readValue(InputStream& in, Value& retval) {
    byte control;
    READ_FAIL(in, &control, 1)
    switch(control) {
      case ubjson::Null:
        retval.SetNull();
        break;
      case ubjson::True:
        retval = true;
        break;
      case ubjson::False:
        retval = false;
        break;
      case ubjson::Char:
        readChar(in, retval);
        break;
      case ubjson::Int8:
        readInt8(in, retval);
        break;
      case ubjson::Uint8:
        readUint8(in, retval);
        break;
      case ubjson::Int16:
        readInt16(in, retval);
        break;

      default:
        return false;

    }
    return true;
  }

  bool UBJSONReader::read(Value& retval) {
    return readValue(mInput, retval);

  }

} // namespace microdb
