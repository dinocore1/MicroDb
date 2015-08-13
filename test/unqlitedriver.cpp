#include "microdb.h"
#include "unqlitedriver.h"

#include <gtest/gtest.h>

using namespace microdb;

TEST(unqlitedriver, open_write_read) {
	
	UnQliteDriver driver;
	
	ASSERT_EQ(OK, driver.open("test.db"));
	
}