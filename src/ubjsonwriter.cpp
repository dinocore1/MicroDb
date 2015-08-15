#include "portable_endian.h"

#include <cstdint>
#include <cstring>


#include <microdb/value.h>
#include <microdb/serialize.h>

#include "ubjsonhelper.h"

#include <limits>

namespace microdb {

  /*
  template<typename V, typename T>
  bool in_range(V value);

  template<typename T>
  bool in_range(uint64_t value) {
    bool retval = value >= std::numeric_limits<T>::min() && value <= std::numeric_limits<T>::max();
    return retval;
  }

  template<typename T>
  bool in_range(int64_t value) {
    bool retval = value >= std::numeric_limits<T>::min() && value <= std::numeric_limits<T>::max();
    return retval;
  }
  */

  template<typename T>
  bool in_range(double value) {
    bool retval = value >= std::numeric_limits<T>::min() && value <= std::numeric_limits<T>::max();
    return retval;
  }

UBJSONWriter::UBJSONWriter(OutputStream& out)
: mOutput(out) { }

UBJSONWriter::~UBJSONWriter() {}

void writeValue(OutputStream& out, const Value& value);

void writeNull(OutputStream& out) {
  out.Write(&ubjson::Null, 1);
}

void writeBool(OutputStream& out, bool v) {
  if(v) {
    out.Write(&ubjson::True, 1);
  } else {
    out.Write(&ubjson::False, 1);
  }
}

void writeChar(OutputStream& out, const char v) {
  out.Write(&ubjson::Char, 1);
  out.Write(&v, 1);
}

void writeSignedInt(OutputStream& out, const int64_t v) {
  if(in_range<int8_t>(v)) {
    out.Write(&ubjson::Int8, 1);
    int8_t buf = static_cast<int8_t>(v);
    out.Write(&buf, 1);
  } else if(in_range<uint8_t>(v)) {
    out.Write(&ubjson::Uint8, 1);
    uint8_t buf = static_cast<uint8_t>(v);
    out.Write(&buf, 1);
  } else if(in_range<int16_t>(v)) {
    out.Write(&ubjson::Int16, 1);
    int16_t buf = htobe16(static_cast<int16_t>(v));
    out.Write(&buf, 2);
  } else if(in_range<int32_t>(v)) {
    out.Write(&ubjson::Int32, 1);
    int32_t buf = htobe32(static_cast<int32_t>(v));
    out.Write(&buf, 4);
  } else if(in_range<int64_t>(v)) {
    out.Write(&ubjson::Int64, 1);
    int64_t buf = htobe64(static_cast<int64_t>(v));
    out.Write(&buf, 8);
  }
}

void writeUnsignedInt(OutputStream& out, const uint64_t v) {

  if(in_range<int8_t>(v)) {
    out.Write(&ubjson::Int8, 1);
    int8_t buf = static_cast<int8_t>(v);
    out.Write(&buf, 1);
  } else if(in_range<uint8_t>(v)) {
    out.Write(&ubjson::Uint8, 1);
    uint8_t buf = static_cast<uint8_t>(v);
    out.Write(&buf, 1);
  } else if(in_range<int16_t>(v)) {
    out.Write(&ubjson::Int16, 1);
    int16_t buf = htobe16(static_cast<int16_t>(v));
    out.Write(&buf, 2);
  } else if(in_range<int32_t>(v)) {
    out.Write(&ubjson::Int32, 1);
    int32_t buf = htobe32(static_cast<int32_t>(v));
    out.Write(&buf, 4);
  } else if(in_range<int64_t>(v)) {
    out.Write(&ubjson::Int64, 1);
    int64_t buf = htobe64(static_cast<int64_t>(v));
    out.Write(&buf, 8);
  }

}

#define DOUBLE_CONV(x) *reinterpret_cast<const uint64_t*>(&x)

void writeFloat(OutputStream& out, const double v) {
  out.Write(&ubjson::Float64, 1);
  uint64_t buf = htobe64(DOUBLE_CONV(v));
  out.Write(&buf, 8);
}

void writeString(OutputStream& out, const std::string& v) {
  out.Write(&ubjson::String, 1);
  size_t strLen = v.size();
  writeUnsignedInt(out, strLen);
  const char* rawStr = v.data();
  out.Write(rawStr, strLen);

}

void writeArray(OutputStream& out, const Value& v) {
  out.Write(&ubjson::Array_Start, 1);
  const uint64_t size = v.Size();
  for(uint64_t i=0;i<size;i++) {
    writeValue(out, v[i]);
  }
  out.Write(&ubjson::Array_End, 1);
}

void writeObject(OutputStream& out, const Value& v) {
  out.Write(&ubjson::Object_Start, 1);
  for(const auto& key : v.GetKeys()) {

    //write string key (no 'S' because it is redundant)
    const size_t strLen = key.size();
    writeUnsignedInt(out, strLen);
    const char* rawStr = key.data();
    out.Write(rawStr, strLen);

    writeValue(out, v.Get(key));
  }
  out.Write(&ubjson::Object_End, 1);
}

void writeValue(OutputStream& out, const Value& value) {
  switch(value.GetType()) {
    case Value::Type::Null:
      writeNull(out);
      break;
    case Value::Type::Bool:
      writeBool(out, value.asBool());
      break;
    case Value::Type::Char:
      writeChar(out, value.asChar());
      break;
    case Value::Type::SignedInt:
      writeSignedInt(out, value.asInt64());
      break;
    case Value::Type::UnsignedInt:
      writeUnsignedInt(out, value.asUint64());
      break;
    case Value::Type::Float:
      writeFloat(out, value.asFloat());
      break;
    case Value::Type::String:
      writeString(out, value.asString());
      break;
    case Value::Type::Array:
      writeArray(out, value);
      break;
    case Value::Type::Object:
      writeObject(out, value);
      break;

    break;
  }
}

void UBJSONWriter::write(const Value& v) {
  writeValue(mOutput, v);
}

} //namespace microdb
