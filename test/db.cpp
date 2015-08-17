#include <memory>

#include "microdb.h"

#include <gtest/gtest.h>

using namespace microdb;
using namespace std;

TEST(db, insert) {
	DB* db = nullptr;
	ASSERT_EQ(OK, DB::Open("test.db", &db));
	unique_ptr<DB> dbPtr(db);
	
	Value v1;
	v1.Set("hello", "world");
	
	Value key;
	ASSERT_EQ(OK, dbPtr->Insert(key, v1));
	
	ASSERT_TRUE(!key.IsNull());
	ASSERT_TRUE(key.IsString());
	
	std::stringbuf queryStr;
	queryStr << "if(obj.id==\"" << key.asString() << "\")emit(obj)";
	Iterator it;
	
	ASSERT_TRUE(OK, dbPtr->Query(queryStr.str(), it);
	
}