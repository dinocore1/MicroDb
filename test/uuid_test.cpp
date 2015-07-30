
#include "uuid.h"

#include <gtest/gtest.h>

using namespace std;
using namespace microdb;

TEST(uuidtests, badparse) {
  UUID id1;

  ASSERT_FALSE(id1.parse(""));
}

TEST(uuidtests, goodparse) {
  UUID id1;

  ASSERT_TRUE(id1.parse("de305d54-75b4-431b-adb2-eb6b9e546014"));

  auto out = id1.getString();
  ASSERT_TRUE(out.compare("de305d54-75b4-431b-adb2-eb6b9e546014") == 0);
}

TEST(uuidtests, goodparse2) {
  UUID id1;

  ASSERT_TRUE(id1.parse("de305d5475b4431badb2eb6b9e546014"));

  auto out = id1.getString();
  ASSERT_TRUE(out.compare("de305d54-75b4-431b-adb2-eb6b9e546014") == 0);
}

TEST(uuidtests, testequals) {

  UUID id1("de305d54-75b4-431b-adb2-eb6b9e546014");
  UUID id2("de305d54-75b4-431b-adb2-eb6b9e546014");

  ASSERT_TRUE(id1 == id2);
}

TEST(uuidtests, testnotequals) {

  UUID id1("de305d54-75b4-431b-adb2-eb6b9e546014");
  UUID id2("fe305d54-75b4-431b-adb2-eb6b9e546014");

  ASSERT_TRUE(id1 != id2);
}
