#include "microdb.h"
#include "leveldbdriver.h"

#include <gtest/gtest.h>

using namespace microdb;

TEST(leveldbdriver, open_write_read) {
	
	{
		LevelDBDriver driver;
		
		ASSERT_EQ(OK, driver.open("test.db"));
		ASSERT_EQ(OK, driver.Insert(CMem("hello"), CMem("world")));
	}
	
	{
		LevelDBDriver driver;
		
		ASSERT_EQ(OK, driver.open("test.db"));
		
		MemSlice value;
		ASSERT_EQ(OK, driver.Get(CMem("hello"), value) );
		ASSERT_EQ(5, value.size());
		
		const char* data = (const char*)value.get();
		ASSERT_EQ('w', data[0]);
		ASSERT_EQ('o', data[1]);
	}
	
}