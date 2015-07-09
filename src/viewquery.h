#ifndef MicroDB_viewquery_h
#define MicroDB_viewquery_h

#include <string>
#include <vector>
#include <map>


#include <rapidjson/allocators.h>
#include <rapidjson/encodings.h>

#include <rapidjson/rapidjson.h>
#include <rapidjson/document.h>

namespace microdb {
    
    class Environment;
    
    class Statement {
    public:
        virtual void execute(Environment* env) = 0;
        virtual std::string toString() = 0;
    };
    
    class Selector : public Statement {
    public:
        void execute(Environment* env) {
            select(env);
        }
        
        virtual rapidjson::Value& select(Environment* env) = 0;
    };
    
    typedef std::vector< Statement* > stmtList;
    typedef std::vector< Selector* > argList;
    typedef rapidjson::Value& (*dataFunction)(Environment* env, const std::vector< Selector* >& args);
    
    class Environment {
        
    private:
        rapidjson::MemoryPoolAllocator<> mEnvAllocator;
        
    protected:
        std::map< std::string, rapidjson::Document > mVariables;
        std::map< std::string, dataFunction > mFunctions;
        
    public:
        
        rapidjson::Document& GetVar(const std::string& name) {
            rapidjson::Document& retval = mVariables[name];
            return retval;
        }
        
        void SetVar(std::string name, rapidjson::Value& value) {
            rapidjson::Document& target = mVariables[name];
            target.CopyFrom(value, target.GetAllocator());
        }
        
        dataFunction GetFunction(const std::string& name) {
            return mFunctions[name];
        }
        
        void SetFunction(const std::string& name, dataFunction fun) {
            mFunctions[name] = fun;
        }
        
        rapidjson::Document::AllocatorType& getGlobalAllocator() {
            return mEnvAllocator;
        }
        
    };
    
    class Assign : public Statement {
    public:
        const std::string mVarName;
        Selector* mSelector;
        
        Assign(const std::string& varName, Selector* selector)
        : mVarName(varName), mSelector(selector) { }
        
        void execute(Environment* env) {
            env->SetVar(mVarName, mSelector->select(env));
        }
        
        std::string toString() {
            return mVarName + " = " + mSelector->toString();
        }
    };
    
    class IfStatement : public Statement {
    private:
        Selector* mCondition;
        stmtList mThenStmts;
        
    public:
        IfStatement(Selector* condition, const stmtList& thenStatements)
        : mCondition(condition), mThenStmts(thenStatements) { }
        
        void execute(Environment* env) {
            rapidjson::Value& conditionVar = mCondition->select(env);
            if(conditionVar.IsBool() && conditionVar.IsTrue()) {
                for(Statement* stmt : mThenStmts) {
                    stmt->execute(env);
                }
            }
        }
        
        std::string toString();
    };
    
    class FunctionCall : public Selector {
    public:
        const std::string mFunctionName;
        const argList mArgList;
        
        FunctionCall(const std::string& name, const argList& arglist)
        : mFunctionName(name), mArgList(arglist) {}
        
        rapidjson::Value& select(Environment* env) {
            dataFunction fun = env->GetFunction(mFunctionName);
            if(fun != nullptr) {
                return fun(env, mArgList);
            } else {
                rapidjson::Value retval(rapidjson::kNullType);
                return retval.Move();
            }
        }
        
        std::string toString();
        
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
        
        rapidjson::Value& select(Environment* env) {
            rapidjson::Value& left = mLHS->select(env);
            rapidjson::Value& right = mRHS->select(env);
            

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
        
        std::string toString();
    };
    
    class VarSelector : public Selector {
    public:
        const std::string mVarName;
        
        VarSelector(const std::string& name)
        : mVarName(name) {}
        
        rapidjson::Value& select(Environment* env) {
            return env->GetVar(mVarName);
        }
        
        std::string toString() {
            return mVarName;
        }
    };
    
    
    class MemberSelector : public Selector {
    private:
        const std::string mMemberName;
        Selector* mParent;
        
    public:
        MemberSelector(std::string& memberName, Selector* parent = nullptr)
        : mMemberName(memberName), mParent(parent) {}
        
        rapidjson::Value& select(Environment* env) {
            
            if(mParent != nullptr){
                rapidjson::Value& value = mParent->select(env);
                const char* memberName = mMemberName.c_str();
                if(value.IsObject() && value.HasMember(memberName)){
                    return value[memberName];
                }
            }
            
            rapidjson::Value nullVal(rapidjson::kNullType);
            return nullVal.Move();
        }
        
        std::string toString();
    };
    
    class StrLiteralSelector : public Selector {
    private:
        const std::string mStrValue;
        rapidjson::Value mValue;
        
    public:
        
        StrLiteralSelector(const std::string& value)
        : mStrValue(value), mValue(mStrValue.c_str(), mStrValue.length()) { }
        
        rapidjson::Value& select(Environment* env) {
            return mValue;
        }
        
        std::string toString();
    };
    
    class IntLiteralSelector : public Selector {
    private:
        rapidjson::Value mValue;
        
    public:
        IntLiteralSelector(int value)
        : mValue(value) { }
        
        rapidjson::Value& select(Environment* env) {
            return mValue;
        }
        
        
        std::string toString();
    };
    
    typedef struct ParserStruct {
        void* svt;
        bool mParseSuccess;
        stmtList stmts;
    } ParserStruct;
    
    class ViewQuery {
    private:
        stmtList mStatements;
        
    public:
        const std::string mName;
        
        ViewQuery(const std::string& name);
        
        void setStatements(const stmtList& stmts);
        
        bool compile(const char* code);
        void map(rapidjson::Document& input, Environment* env) const;
        void execute(Environment* env) const;
        
        bool operator<(const ViewQuery& other) const;
        
    };
}

#endif