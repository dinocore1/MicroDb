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
        
        Assign(const std::string& varName, Selector* selector, Environment* env)
        : mVarName(varName), mSelector(selector), mEnv(env) { }
        
        void execute() {
            mEnv->SetVar(mVarName, mSelector->select());
        }
    };
    
    class IfStatement : public Statement {
    private:
        Selector* mCondition;
        stmtList mThenStmts;
        
    public:
        IfStatement(Selector* condition, const stmtList& thenStatements)
        : mCondition(condition), mThenStmts(thenStatements) { }
        
        void execute() {
            rapidjson::Value& conditionVar = mCondition->select();
            if(conditionVar.IsBool() && conditionVar.IsTrue()) {
                for(Statement* stmt : mThenStmts) {
                    stmt->execute();
                }
            }
        }
    };
    
    class FunctionCall : public Statement, public Selector {
    public:
        const std::string mFunctionName;
        const argList mArgList;
        Environment* mEnv;
        
        FunctionCall(const std::string& name, const argList& arglist, Environment* env)
        : mFunctionName(name), mArgList(arglist), mEnv(env) {}
        
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
    
    class Condition : public Selector {
    public:
        
        enum OperatorType {
            Equals,
            GreaterThan,
            LessThan,
            GreaterOrEqual,
            LessThanOrEqual,
            NotEqual
        };
        
        Selector* mLHS;
        Selector* mRHS;
        OperatorType mOp;
        
        Condition(Selector* lhs, Selector* rhs, OperatorType op)
        : mLHS(lhs), mRHS(rhs), mOp(op) { }
        
        rapidjson::Value& select() {
            rapidjson::Value& left = mLHS->select();
            rapidjson::Value& right = mRHS->select();
            

#define RETURN_TRUE rapidjson::Value(rapidjson::kTrueType).Move()
#define RETURN_FALSE rapidjson::Value(rapidjson::kFalseType).Move()
            
            switch(mOp) {
                case Equals:
                    return left == right ? RETURN_TRUE : RETURN_FALSE;
                    break;
                case GreaterThan:
                    break;
                case LessThan:
                    break;
                case GreaterOrEqual:
                    break;
                case LessThanOrEqual:
                    break;
                case NotEqual:
                    return left != right ? RETURN_TRUE : RETURN_FALSE;
                    break;
            }
            
            return rapidjson::Value(rapidjson::kFalseType).Move();
        }
    };
    
    class VarSelector : public Selector {
    public:
        const std::string mVarName;
        Environment* mEnv;
        
        VarSelector(const std::string& name, Environment* env)
        : mVarName(name), mEnv(env) {}
        
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
            } else {
                value.SetNull();
                return value.Move();
            }
            
        }
    };
    
    class StrLiteralSelector : public Selector {
    private:
        const std::string mStrValue;
        rapidjson::Value mValue;
        
    public:
        
        StrLiteralSelector(const std::string& value)
        : mStrValue(value), mValue(mStrValue.c_str(), mStrValue.size()) { }
        
        rapidjson::Value& select() {
            return mValue;
        }
    };
    
    class IntLiteralSelector : public Selector {
    private:
        rapidjson::Value mValue;
        
    public:
        IntLiteralSelector(int value)
        : mValue(value) { }
        
        rapidjson::Value& select() {
            return mValue;
        }
    };
    
    typedef struct ParserStruct {
        void* svt;
        Environment* mEnv;
        stmtList stmts;
    } ParserStruct;
    
    class ViewQuery {
    private:
        stmtList mStatements;
    public:
        Environment* mEnv;
        
        bool compile(const char* code);
        
        void map(rapidjson::Document& input);
        void execute();
    };
}

#endif