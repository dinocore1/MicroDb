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

  Value v2("3.14");
  ASSERT_TRUE(v2.IsString());
  ASSERT_EQ(3.14, v2.asFloat());

  Value v3(3.14);
  ASSERT_TRUE(v3.IsFloat());
  ASSERT_TRUE(strcmp("3.14", v3.asString().substr(0,4).c_str()) == 0);

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

TEST(value, construct_move) {
  Value v1("hello");
  ASSERT_TRUE(v1.IsString());

  Value v2(54);
  ASSERT_TRUE(v2.IsNumber());

  v2.MoveFrom( std::move(v1) );

  ASSERT_TRUE(v2.IsString());
  ASSERT_TRUE(v2.asString().compare("hello") == 0);
  ASSERT_TRUE(v1.IsNull());

}

TEST(value, assign_copy) {
  Value v1("Hello");

  Value v2("world");

  v1 = v2;
  ASSERT_TRUE(v1.asString().compare("world") == 0);
  ASSERT_TRUE(v2.asString().compare("world") == 0);
}

TEST(value, assign_move) {
  Value v1("Hello");

  Value v2("world");

  v1 = std::move(v2);
  ASSERT_TRUE(v1.asString().compare("world") == 0);
  ASSERT_TRUE(v2.IsNull());
}

TEST(value, array) {
  Value v1;
  ASSERT_TRUE(v1.IsNull());

  v1.Add(Value(54));
  ASSERT_TRUE(v1.IsArray());
  ASSERT_EQ(1, v1.Size());

  v1.Add(Value("neato"));
  ASSERT_EQ(2, v1.Size());

  ASSERT_EQ(54, v1[0].asInt());
  ASSERT_TRUE(strcmp("neato", v1[1].asString().c_str()) == 0);

}

TEST(value, obj) {
  Value v1;
  v1["hello"] = "world";

  ASSERT_TRUE(v1.IsObject());
  ASSERT_TRUE(strcmp("world", v1["hello"].asString().c_str()) == 0);

  v1["hello"] = 54;
  ASSERT_EQ(54, v1["hello"].asInt());

  v1.Set("cool", -30);
  ASSERT_EQ(-30, v1["cool"].asInt());

  ASSERT_EQ(54, v1.Get("hello").asInt());

}
