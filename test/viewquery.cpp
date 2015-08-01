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

rapidjson::Value& helloWorldFun(Environment* env, const std::vector< Selector* >& args) {

  rapidjson::Value retval;
  //printf("I am ehre %f\n", retval.GetDouble());
  retval.SetString("hello to you", env->getGlobalAllocator());
  //retval = 4;
  //retval.SetObject();
  //retval.AddMember("message", "hello to you", env->getGlobalAllocator());

  return retval.Move();
}

void helloWorld2(Environment* env, Value& retval) {
  retval.SetString("hello to you");
}

TEST(viewquery, function_test1) {
  Environment env;
  Value retval;
  helloWorld2(&env, retval);

  ASSERT_TRUE(retval.IsString());
  ASSERT_TRUE(strcmp("hello to you", retval.GetString()) == 0);
}

TEST(viewquery, function_test) {

  vector< Selector* > args;
  Environment env;
  Value& retval = helloWorldFun(&env, args);

  ASSERT_TRUE(retval.IsString());
  ASSERT_TRUE(strcmp("hello to you", retval.GetString()) == 0);

}

TEST(viewquery, simple_assign) {

  ViewQuery query("test");
  ASSERT_TRUE(query.compile("x = \"hello world\""));

  Environment env;

  query.execute(&env);

  auto& x = env.GetVar("x");

  //ASSERT_TRUE(x.IsString());
  //ASSERT_TRUE(strcmp("hello world", x.GetString()) == 0);

}

TEST(viewquery, simple_functioncall) {

  ViewQuery query("test");
  ASSERT_TRUE(query.compile("x = hello()"));

  Environment env;
  env.SetFunction("hello", helloWorldFun);

  query.execute(&env);

  Value& x = env.GetVar("x");
  ASSERT_TRUE(x.IsString());
  printValue(x);


  printf("x = %s\n", x.GetString());

  ASSERT_TRUE(strcmp("hello to you", x.GetString()) == 0);

}
