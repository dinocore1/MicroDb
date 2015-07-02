#ifndef MicroDB_viewquery_h
#define MicroDB_viewquery_h

#include <string>
#include <vector>
#include <map>


#include <rapidjson/rapidjson.h>
#include <rapidjson/document.h>

namespace microdb {
    
    class Selector {
    public:
        virtual rapidjson::Value& select() = 0;
    };
    
    typedef std::vector< Selector* > argList;
    typedef rapidjson::Value& (*dataFunction)(const std::vector< Selector* >& args);
    
    class Statement {
        
    public:
        virtual void execute() = 0;
    };
    
    typedef std::vector< Statement* > stmtList;
    
    class Environment {
        
        std::map< std::string, rapidjson::Document > mVariables;
        std::map< std::string, dataFunction > mFunctions;
        
    public:
        
        rapidjson::Document& GetVar(const std::string& name) {
            return mVariables[name];
        }
        
        void SetVar(std::string name, rapidjson::Value& value) {
            rapidjson::Document& target = mVariables[name];
            target.CopyFrom(value, target.GetAllocator());
        }
        
        dataFunction GetFunction(const std::string& name) {
            return mFunctions[name];
        }
        
    };
    
    class Assign : public Statement {
    public:
        const std::string mVarName;
        Selector* mSelector;
        Environment* mEnv;
        
        Assign(const std::string& varName, Selector* selector)
        : mVarName(varName), mSelector(selector) { }
        
        void execute() {
            mEnv->SetVar(mVarName, mSelector->select());
        }
    };
    
    class FunctionCall : public Statement, public Selector {
    public:
        const std::string mFunctionName;
        const argList mArgList;
        Environment* mEnv;
        
        FunctionCall(const std::string& name, const argList& arglist)
        : mFunctionName(name), mArgList(arglist) {}
        
        void execute() {
            select();
        }
        
        rapidjson::Value& select() {
            dataFunction fun = mEnv->GetFunction(mFunctionName);
            if(fun == nullptr) {
                return fun(mArgList);
            } else {
                rapidjson::Value retval(rapidjson::kNullType);
                return retval.Move();
            }
        }
        
        
        
    };
    
    class VarSelector : public Selector {
    public:
        const std::string mVarName;
        Environment* mEnv;
        
        VarSelector(const std::string& name) : mVarName(name) {}
        
        rapidjson::Value& select() {
            return mEnv->GetVar(mVarName);
        }
    };
    
    
    class MemberSelector : public Selector {
    private:
        const std::string mMemberName;
        Selector* mParent;
        
    public:
        MemberSelector(std::string& memberName, Selector* parent = nullptr)
        : mMemberName(memberName), mParent(parent) {}
        
        rapidjson::Value& select() {
            
            rapidjson::Value value(rapidjson::kNullType);
            
            if(mParent != nullptr){
                value = mParent->select();
            }
            
            const char* memberName = mMemberName.c_str();
            if(value.IsObject() && value.HasMember(memberName)){
                return value[memberName];
            }
            
            return value.Move();
        }
    };
    
    class StrLiteralSelector : public Selector {
    private:
        const std::string mValue;
        
    public:
        
        StrLiteralSelector(const std::string& value)
        : mValue(value) { }
        
        rapidjson::Value& select() {
            rapidjson::Value value(rapidjson::kStringType);
            value.SetString(mValue.c_str(), mValue.length());
            
            return value.Move();
        }
    };
    
    class IntLiteralSelector : public Selector {
    private:
        const int mValue;
        
    public:
        IntLiteralSelector(int value)
        : mValue(value) { }
        
        rapidjson::Value& select() {
            rapidjson::Value value(rapidjson::kNumberType);
            value.SetInt(mValue);
            
            return value.Move();
        }
    };
    
    typedef struct ParserStruct {
        void* svt;
        stmtList stmts;
    } ParserStruct;
    
    class ViewQuery {
    private:
        stmtList mStatements;
    public:
        bool compile(const char* code);
        
        void evaluate(rapidjson::Document& input);
    };
}

#endif