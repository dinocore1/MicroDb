#include <gtest/gtest.h>

#include "microdb/value.h"
#include "vectorclock.h"


using namespace std;
using namespace microdb;

void increment(VectorClock& clock, const string& key, int num) {
  for(int i=0;i<num;i++) {
    clock.increment(key);
  }
}

TEST(vectorclock, lessthan) {

  VectorClock v1;


  increment(v1, "A", 2);
  increment(v1, "B", 4);
  increment(v1, "C", 1);

  {
    VectorClock v2;
    increment(v2, "A", 2);
    ASSERT_TRUE(v2 < v1);
  }

  {
    VectorClock v2;
    increment(v2, "A", 2);
    increment(v2, "B", 2);
    increment(v2, "C", 1);
    ASSERT_TRUE(v2 < v1);
  }

  {
    VectorClock v2;
    increment(v2, "B", 4);
    increment(v2, "C", 1);
    ASSERT_TRUE(v2 < v1);
  }

  {
    VectorClock v2;
    increment(v2, "B", 3);
    increment(v2, "C", 2);
    ASSERT_TRUE(!(v2 < v1));
  }

  {
    VectorClock v2;
    increment(v2, "A", 2);
    increment(v2, "B", 4);
    increment(v2, "C", 1);
    ASSERT_TRUE(!(v2 < v1));
  }
}

TEST(vectorclock, serialize) {
  VectorClock v1;
  increment(v1, "A", 2);
  increment(v1, "B", 4);
  increment(v1, "C", 1);

  Value value = v1.toValue();
  ASSERT_EQ(2, value["A"].asInt());
  ASSERT_EQ(4, value["B"].asInt());
  ASSERT_EQ(1, value["C"].asInt());
}
