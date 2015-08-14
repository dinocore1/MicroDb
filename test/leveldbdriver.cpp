#include "microdb.h"
#include "leveldbdriver.h"

#include <gtest/gtest.h>

using namespace microdb;

TEST(leveldbdriver, open_write_read) {
	
	LevelDBDriver driver;
	
	ASSERT_EQ(OK, driver.open("test.db"));
	
	ASSERT_EQ(OK, driver.Insert(MemSlice("hello"), MemSlice("world")));
	
}