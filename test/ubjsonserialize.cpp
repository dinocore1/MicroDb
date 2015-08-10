#include <microdb/value.h>
#include <microdb/serialize.h>

#include <gtest/gtest.h>
#include "portable_endian.h"

using namespace std;
using namespace microdb;

#include <sstream>
class SSOutputStream : public OutputStream {

public:
  stringstream mStream;

  void Write(const void* buf, const size_t len) {
    mStream.write((char*)buf, len);
  }
};

class SSInputStream : public InputStream {
public:
  stringstream mStream;

  int Read(const byte* buf, const size_t max) {
    int retval = mStream.readsome((char*)buf, max);
    if(retval == 0 && mStream.eof()) {
      retval = -1;
    }
    return retval;
  }
};

TEST(ubjsonserialize, read_simple) {
  SSInputStream in;
  in.mStream << "ZTFCxi\xbU\xFFI\x1\x3";
  in.mStream.seekg(0);

  UBJSONReader reader(in);

  Value v1;
  ASSERT_TRUE(reader.read(v1));
  ASSERT_TRUE(v1.IsNull());

  ASSERT_TRUE(reader.read(v1));
  ASSERT_TRUE(v1.IsBool());
  ASSERT_EQ(true, v1.asBool());

  ASSERT_TRUE(reader.read(v1));
  ASSERT_TRUE(v1.IsBool());
  ASSERT_TRUE(!v1.asBool());

  ASSERT_TRUE(reader.read(v1));
  ASSERT_TRUE(v1.IsChar());
  ASSERT_EQ('x', v1.asChar());

  ASSERT_TRUE(reader.read(v1));
  ASSERT_TRUE(v1.IsInteger());
  ASSERT_EQ(11, v1.asInt());

  ASSERT_TRUE(reader.read(v1));
  ASSERT_TRUE(v1.IsInteger());
  ASSERT_EQ(255, v1.asInt());
  
  ASSERT_TRUE(reader.read(v1));
  ASSERT_TRUE(v1.IsInteger());
  ASSERT_EQ(259, v1.asInt());
}

TEST(ubjsonserialize, read_float) {
  SSInputStream in;
  in.mStream << "d\x40\x48\xf5\xc3";
  in.mStream.seekg(0);

  UBJSONReader reader(in);
  
  Value v1;
  ASSERT_TRUE(reader.read(v1));
  ASSERT_TRUE(v1.IsFloat());
  ASSERT_EQ(3.14f, v1.asFloat());
}

TEST(ubjsonserialize, read_string) {
  SSInputStream in;
  in.mStream << "SU\5hello";
  in.mStream.seekg(0);
  
  UBJSONReader reader(in);
  
  Value v1;
  ASSERT_TRUE(reader.read(v1));
  ASSERT_TRUE(v1.IsString());
  ASSERT_TRUE(strcmp("hello", v1.asString().c_str()) == 0);
}

TEST(ubjsonserialize, write_simple) {
  SSOutputStream out;
  UBJSONWriter writer(out);

  writer.write(Value());
  writer.write(Value(true));
  writer.write(Value(false));
  writer.write(Value('T'));

  std::string output = out.mStream.str();
  ASSERT_TRUE(strcmp("ZTFCT", output.c_str()) == 0);
}

TEST(ubjsonserialize, write_int) {
  SSOutputStream out;
  UBJSONWriter writer(out);

  writer.write(Value(15));
  writer.write(Value(255));

  std::string output = out.mStream.str();
  const char* buf = output.data();
  ASSERT_EQ('i', buf[0]);
  ASSERT_EQ(15, buf[1]);
  ASSERT_EQ('U', buf[2]);
  ASSERT_EQ('\xFF', buf[3]);
}

TEST(ubjsonserialize, write_float) {
  SSOutputStream out;
  UBJSONWriter writer(out);

  writer.write(Value(3.14));
  std::string output = out.mStream.str();
  const char* buf = output.data();
  ASSERT_EQ('D', buf[0]);

  uint64_t* vb = (uint64_t*)&buf[1];
  uint64_t b2 = be64toh(*vb);
  double v = *reinterpret_cast<double*>(&b2);
  ASSERT_EQ(3.14, v);
}

TEST(ubjsonserialize, write_string) {
  SSOutputStream out;
  UBJSONWriter writer(out);

  writer.write(Value("hello world"));

  std::string output = out.mStream.str();
  const char* buf = output.data();

  ASSERT_EQ('S', buf[0]);
  ASSERT_EQ('i', buf[1]);
  ASSERT_EQ(11, buf[2]);
  ASSERT_EQ('h', buf[3]);
  ASSERT_EQ('e', buf[4]);
  ASSERT_EQ('l', buf[5]);
  ASSERT_EQ('l', buf[6]);
}

TEST(ubjsonserialize, write_array) {
  SSOutputStream out;
  UBJSONWriter writer(out);

  Value array;
  array.Add("1");
  array.Add("2");
  array.Add("3");

  writer.write(array);

  std::string output = out.mStream.str();
  const char* buf = output.data();

  int i = 1;
  ASSERT_EQ('[', buf[0]);
  if(buf[1] == '#') {
    ASSERT_EQ('i', buf[2]);
    ASSERT_EQ(3, buf[3]);
    i += 3;
  }
  ASSERT_EQ('S', buf[i]);
  ASSERT_EQ('i', buf[++i]);
  ASSERT_EQ(1, buf[++i]);
  ASSERT_EQ('1', buf[++i]);

  ASSERT_EQ('S', buf[++i]);
  ASSERT_EQ('i', buf[++i]);
  ASSERT_EQ(1, buf[++i]);
  ASSERT_EQ('2', buf[++i]);

  ASSERT_EQ('S', buf[++i]);
  ASSERT_EQ('i', buf[++i]);
  ASSERT_EQ(1, buf[++i]);
  ASSERT_EQ('3', buf[++i]);

  ASSERT_EQ(']', buf[++i]);
}

TEST(ubjsonserialize, write_object) {
  SSOutputStream out;
  UBJSONWriter writer(out);

  Value v;
  v["hello"] = "world";

  writer.write(v);

  std::string output = out.mStream.str();
  const char* buf = output.data();
  ASSERT_EQ('{', buf[0]);
  ASSERT_EQ('i', buf[1]);
  ASSERT_EQ(5, buf[2]);
}
