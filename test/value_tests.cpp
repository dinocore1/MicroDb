#include <gtest/gtest.h>

#include "microdb/value.h"

using namespace std;
using namespace microdb;

TEST(value, int_type) {
  Value v1(1);

  ASSERT_TRUE(v1.IsInteger());
  ASSERT_TRUE(v1.IsNumber());
  ASSERT_TRUE(v1.IsSignedInteger());
  ASSERT_FALSE(v1.IsFloat());
  ASSERT_FALSE(v1.IsNull());

  ASSERT_EQ(1, v1.asInt());

}

TEST(value, string_copy) {
  Value v1("hello");
  ASSERT_TRUE(v1.IsString());
  ASSERT_EQ(5, v1.Size());

  Value v2(v1);

  ASSERT_TRUE(v1.IsString());
  ASSERT_TRUE(v2.IsString());
}

TEST(value, string_num) {
  Value v1("34");
  ASSERT_TRUE(v1.IsString());

  ASSERT_EQ(34, v1.asInt());

}

TEST(value, binary_type) {
  char data[10];
  data[0] = 0;
  data[1] = 1;
  data[2] = 3;

  Value v1(data, 10);

  ASSERT_TRUE(v1.IsBinary());
  ASSERT_EQ(10, v1.Size());
}
