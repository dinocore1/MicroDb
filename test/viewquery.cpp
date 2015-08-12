#include <gtest/gtest.h>

#include <microdb/value.h>
#include "viewquery.h"

using namespace std;
using namespace microdb;

class HelloWorld : public ::testing::Test {

};

void helloWorldFun(Environment* env, Value& retval, const std::vector< Selector* >& args) {
  retval = "hello to you";
}

TEST(viewquery, simple_assign) {

  ViewQuery query;
  ASSERT_TRUE(query.compile("x = \"hello world\""));

  Environment env;

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("hello world", x.asString().c_str()) == 0);
}

TEST(viewquery, float_literal) {
  ViewQuery query;
  ASSERT_TRUE(query.compile("x = 3.14"));

  Environment env;
  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsFloat());
  ASSERT_EQ(3.14, x.asFloat());
}

TEST(viewquery, int_literal) {
  ViewQuery query;
  ASSERT_TRUE(query.compile("x = 123"));

  Environment env;
  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsInteger());
  ASSERT_EQ(123, x.asInt());
}

TEST(viewquery, simple_functioncall) {

  ViewQuery query;
  ASSERT_TRUE(query.compile("x = hello()"));

  Environment env;
  env.SetFunction("hello", helloWorldFun);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("hello to you", x.asString().c_str()) == 0);

}

TEST(viewquery, member_access) {
  ViewQuery query;
  ASSERT_TRUE(query.compile("x = obj.hello"));

  Environment env;

  Value obj;
  obj.Set("hello", Value("world"));

  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("world", x.asString().c_str()) == 0);
}

TEST(viewquery, nullobj_access) {
  ViewQuery query;
  ASSERT_TRUE(query.compile("x = obj.hello"));

  Environment env;

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsNull());
}

TEST(viewquery, array_access) {
  ViewQuery query;
  ASSERT_TRUE(query.compile("x = obj[0]"));

  Environment env;

  Value obj;
  obj.Add(Value("hello"));

  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("hello", x.asString().c_str()) == 0);
}

TEST(viewquery, array_access_sizeerror) {
  ViewQuery query;
  ASSERT_TRUE(query.compile("x = obj[1]"));

  Environment env;

  Value obj;
  obj.Add(Value("hello"));

  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsNull());
}

TEST(viewquery, if_condition) {
  ViewQuery query;
  ASSERT_TRUE(query.compile("if(obj == \"hello\") { x = \"world\" }"));

  Environment env;

  Value obj;
  obj = "hello";

  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("world", x.asString().c_str()) == 0);
}

TEST(viewquery, negitive_if_condition) {
  ViewQuery query;
  ASSERT_TRUE(query.compile("if(obj == \"hello\") { x = \"world\" }"));

  Environment env;

  Value obj;
  obj = "foo";

  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsNull());
}

TEST(viewquery, if_condition_else) {
  ViewQuery query;
  ASSERT_TRUE(query.compile("if(obj == \"hello\") { x = \"world\" } else { x = \"foo\"}"));

  Environment env;

  Value obj;
  obj = "goodbye";
  env.SetVar("obj", obj);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  ASSERT_TRUE(strcmp("foo", x.asString().c_str()) == 0);
}
