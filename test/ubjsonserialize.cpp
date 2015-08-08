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

  double v = be64toh((static_cast<uint64_t>(buf[1])));
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
