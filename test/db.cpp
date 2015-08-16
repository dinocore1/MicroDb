#include <memory>

#include "microdb.h"

#include <gtest/gtest.h>

using namespace microdb;
using namespace std;

TEST(db, insert) {
	DB* db = nullptr;
	ASSERT_EQ(OK, DB::Open("test.db", &db));
	unique_ptr<DB> dbPtr(db);
	
	
}