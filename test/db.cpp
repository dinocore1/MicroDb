#include <memory>

#include "microdb.h"

#include <gtest/gtest.h>

using namespace microdb;
using namespace std;

#include <stdlib.h>

void deleteIfExists(const std::string& path) {
	std::stringstream buf;
	buf << "rm -rf " << path;
	system(buf.str().c_str());
}

TEST(db, insert) {
	
	deleteIfExists("test.db");
	
	DB* db = nullptr;
	ASSERT_EQ(OK, DB::Open("test.db", &db));
	unique_ptr<DB> dbPtr(db);
	
	std::string firstKey;
	
	for(int i=0;i<100;i++) {
		Value v1;
		
		std::stringstream buf;
		buf << "world" << i;
		v1.Set("hello", buf.str());
		
		Value key;
		ASSERT_EQ(OK, dbPtr->Insert(key, v1));
	}
	
	{
		Value v1;
		v1.Set("hello", "world first");
		
		Value key;
		ASSERT_EQ(OK, dbPtr->Insert(key, v1));
		ASSERT_TRUE(!key.IsNull());
		ASSERT_TRUE(key.IsString());
		firstKey = key.asString();
	}
	
	for(int i=0;i<100;i++) {
		Value v1;
		
		std::stringstream buf;
		buf << "world" << i;
		v1.Set("hello", buf.str());
		
		Value key;
		ASSERT_EQ(OK, dbPtr->Insert(key, v1));
	}
	
	unique_ptr<Iterator> it( dbPtr->QueryIndex("primary", ""));
	
	it->SeekTo(firstKey);
	ASSERT_TRUE(it->Valid());
	Value v2 = it->GetValue();
	ASSERT_TRUE(v2.IsObject());
	ASSERT_TRUE(v2["hello"].asString().compare("world first") == 0);
	
}

TEST(db, iterate) {
	
	deleteIfExists("test.db");
	
	DB* db = nullptr;
	ASSERT_EQ(OK, DB::Open("test.db", &db));
	unique_ptr<DB> dbPtr(db);
	
	for(int i=0;i<100;i++) {
		Value v1;
		
		std::stringstream buf;
		buf << "world" << i;
		v1.Set("hello", buf.str());
		
		Value key;
		ASSERT_EQ(OK, dbPtr->Insert(key, v1));
	}
	
	unique_ptr<Iterator> it( dbPtr->QueryIndex("primary", ""));
	
	int count = 0;
	while(it->Valid()) {
		Value key = it->GetKey();
		ASSERT_TRUE(key.IsString());
		printf("obj : %s\n", key.asString().c_str());
		count++;
		it->Next();
	}
	
	ASSERT_EQ(100, count);
	
}

TEST(db, range_iterate) {
	
	deleteIfExists("test.db");
	
	DB* db = nullptr;
	ASSERT_EQ(OK, DB::Open("test.db", &db));
	unique_ptr<DB> dbPtr(db);
	
	dbPtr->AddIndex("cnt_idx", "emit(obj.hello)");
	
	for(int i=0;i<100;i++) {
		Value v1;
		v1.Set("hello", i);
		Value key;
		ASSERT_EQ(OK, dbPtr->Insert(key, v1));
	}
	
	unique_ptr<Iterator> it( dbPtr->QueryIndex("cnt_idx", ""));
	
	int count = 0;
	for(it->SeekTo(30); it->Valid() && it->GetKey() < 40; it->Next()) {
		Value key = it->GetKey();
		ASSERT_TRUE(key.IsInteger());
		printf("obj : %s\n", key.asString().c_str());
		count++;
	}
	
	ASSERT_EQ(10, count);
	
}