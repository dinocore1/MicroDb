#include <gtest/gtest.h>

#include <microdb/value.h>
#include "viewquery.h"

using namespace std;
using namespace microdb;

class HelloWorld : public ::testing::Test {

};

void helloWorldFun(Environment* env, Value& retval, const std::vector< Selector* >& args) {
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

TEST(viewquery, float_literal) {
  ViewQuery query("test");
  ASSERT_TRUE(query.compile("x = 3.14"));

  Environment env;
  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsDouble());
  ASSERT_EQ(3.14, x.GetDouble());
}

TEST(viewquery, int_literal) {
  ViewQuery query("test");
  ASSERT_TRUE(query.compile("x = 123"));

  Environment env;
  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsInt());
  ASSERT_EQ(123, x.GetInt());
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

TEST(viewquery, if_condition) {
  ViewQuery query("test");
  ASSERT_TRUE(query.compile("if(obj == \"hello\") { x = \"world\" }"));

  Environment env;

  Value obj;
  obj.SetString("hello");

  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("world", x.GetString()) == 0);
}

TEST(viewquery, negitive_if_condition) {
  ViewQuery query("test");
  ASSERT_TRUE(query.compile("if(obj == \"hello\") { x = \"world\" }"));

  Environment env;

  Value obj;
  obj.SetString("foo");

  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsNull());
}

TEST(viewquery, if_condition_else) {
  ViewQuery query("test");
  ASSERT_TRUE(query.compile("if(obj == \"hello\") { x = \"world\" } else { x = \"foo\"}"));

  Environment env;

  Value obj;
  obj.SetString("goodbye");
  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("foo", x.GetString()) == 0);
}
