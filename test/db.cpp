#include <memory>

#include "microdb.h"

#include <gtest/gtest.h>

using namespace microdb;
using namespace std;

TEST(db, insert) {
	DB* db = nullptr;
	ASSERT_EQ(OK, DB::Open("test.db", &db));
	unique_ptr<DB> dbPtr(db);
	
	std::string firstKey;
	{
		Value v1;
		v1.Set("hello", "world first");
		
		Value key;
		ASSERT_EQ(OK, dbPtr->Insert(key, v1));
		ASSERT_TRUE(!key.IsNull());
		ASSERT_TRUE(key.IsString());
		firstKey = key.asString();
	}
	
	dbPtr->BeginTransaction();
	
	for(int i=0;i<100000;i++) {
		Value v1;
		std::stringstream buf;
		buf << "world" << i;
		v1.Set("hello", buf.str());
		
		Value key;
		ASSERT_EQ(OK, dbPtr->Insert(key, v1));
	}
	
	dbPtr->CommitTransaction();
	
	unique_ptr<Iterator> it( dbPtr->QueryIndex("primary",
		firstKey, firstKey,
		""
		));
	
	it->SeekToFirst();
	ASSERT_TRUE(it->Valid());
	Value v2 = it->GetValue();
	ASSERT_TRUE(v2.IsObject());
	ASSERT_TRUE(v2["hello"].asString().compare("world first") == 0);
	
}