#include <microdb/value.h>
#include <microdb/serialize.h>

#include <gtest/gtest.h>

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
