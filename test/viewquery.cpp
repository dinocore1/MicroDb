#include <gtest/gtest.h>

#include "rapidjson/document.h"
#include "rapidjson/writer.h"
#include "rapidjson/stringbuffer.h"

#include "viewquery.h"

using namespace std;
using namespace microdb;
using namespace rapidjson;

class HelloWorld : public ::testing::Test {

};

void printValue(rapidjson::Value& value) {
  StringBuffer buffer;
  Writer<StringBuffer> writer(buffer);
  value.Accept(writer);
  const char* output = buffer.GetString();

  printf("doc: %s", output);
}

void helloWorldFun(Environment* env, rapidjson::Value& retval, const std::vector< Selector* >& args) {
  retval.SetString("hello to you");
}

TEST(viewquery, simple_assign) {

  ViewQuery query("test");
  ASSERT_TRUE(query.compile("x = \"hello world\""));

  Environment env;

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("hello world", x.GetString()) == 0);
}

TEST(viewquery, simple_functioncall) {

  ViewQuery query("test");
  ASSERT_TRUE(query.compile("x = hello()"));

  Environment env;
  env.SetFunction("hello", helloWorldFun);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("hello to you", x.GetString()) == 0);

}

TEST(viewquery, member_access) {
  ViewQuery query("test");
  ASSERT_TRUE(query.compile("x = obj.hello"));

  Environment env;

  Value obj;
  obj.SetObject();
  obj.AddMember("hello", Value("world"), env.getGlobalAllocator());

  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("world", x.GetString()) == 0);
}

TEST(viewquery, nullobj_access) {
  ViewQuery query("test");
  ASSERT_TRUE(query.compile("x = obj.hello"));

  Environment env;

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsNull());
}

TEST(viewquery, array_access) {
  ViewQuery query("test");
  ASSERT_TRUE(query.compile("x = obj[0]"));

  Environment env;

  Value obj;
  obj.SetArray();
  obj.PushBack(Value("hello"), env.getGlobalAllocator());

  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("hello", x.GetString()) == 0);
}

TEST(viewquery, array_access_sizeerror) {
  ViewQuery query("test");
  ASSERT_TRUE(query.compile("x = obj[1]"));

  Environment env;

  Value obj;
  obj.SetArray();
  obj.PushBack(Value("hello"), env.getGlobalAllocator());

  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsNull());
}
