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
