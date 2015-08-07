
#include <microdb/value.h>
#include <microdb/serialize.h>
#include "serializerhelper.h"
#include "ubjsonhelper.h"



namespace microdb {

UBJSONWriter::UBJSONWriter(OutputStream& out)
: mOutput(out) { }

UBJSONWriter::~UBJSONWriter() {}

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

}

void writeUnsignedInt(OutputStream& out, const uint64_t v) {

}

void writeString(OutputStream& out, const std::string& v) {

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
    case Value::Type::String:
      writeString(out, value.asString());
      break;

    break;
  }
}

void UBJSONWriter::write(const Value& v) {
  writeValue(mOutput, v);
}

} //namespace microdb
