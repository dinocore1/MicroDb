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
        virtual ~Statement() { };
        virtual void execute(Environment* env) = 0;
        virtual std::string toString() = 0;
    };

    class Selector : public Statement {
    public:
        void execute(Environment* env) {
            rapidjson::Value retval;
            select(env, retval);
        }

        virtual void select(Environment* env, rapidjson::Value& retval) = 0;
    };

    typedef std::vector< Statement* > stmtList;
    typedef std::vector< Selector* > argList;
    typedef void (*dataFunction)(Environment* env, rapidjson::Value& retval, const std::vector< Selector* >& args);

    inline void destroyStmtList(stmtList& list) {
        for(Statement* stmt : list) {
            delete stmt;
        }
    }

    inline void destroyArgList(argList& list) {
        for(Selector* arg : list) {
            delete arg;
        }
    }

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
    private:
        const std::string mVarName;
        Selector* mSelector;

    public:

        ~Assign() {
            delete mSelector;
        }

        Assign(const std::string& varName, Selector* selector)
        : mVarName(varName), mSelector(selector) { }

        void execute(Environment* env) {
          rapidjson::Value value;
          mSelector->select(env, value);
          env->SetVar(mVarName, value);
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

        ~IfStatement() {
            delete mCondition;
            destroyStmtList(mThenStmts);
        }

        void execute(Environment* env) {
          rapidjson::Value conditionVal;
          mCondition->select(env, conditionVal);
          if(conditionVal.IsBool() && conditionVal.IsTrue()) {
              for(Statement* stmt : mThenStmts) {
                  stmt->execute(env);
            }
          }
        }

        std::string toString();
    };

    class FunctionCall : public Selector {
    private:
        const std::string mFunctionName;
        argList mArgList;

    public:

        FunctionCall(const std::string& name, const argList& arglist)
        : mFunctionName(name), mArgList(arglist) {}

        ~FunctionCall() {
            destroyArgList(mArgList);
        }

        void select(Environment* env, rapidjson::Value& retval) {
            dataFunction fun = env->GetFunction(mFunctionName);
            if(fun != nullptr) {
                fun(env, retval, mArgList);
            } else {
                retval.SetNull();
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

        ~Condition() {
            delete mLHS;
            delete mRHS;
        }

        void select(Environment* env, rapidjson::Value& retval) {
            rapidjson::Value left, right;
            mLHS->select(env, left);
            mRHS->select(env, right);

            switch(mOp) {
                case Equals:
                    retval = left == right;
                    return;
                case GreaterThan:
                    break;
                case LessThan:
                    break;
                case GreaterOrEqual:
                    break;
                case LessThanOrEqual:
                    break;
                case NotEqual:
                    retval = left != right;
                    return;
            }
        }

        std::string toString();
    };

    class VarSelector : public Selector {
    public:
        const std::string mVarName;

        VarSelector(const std::string& name)
        : mVarName(name) {}

        void select(Environment* env, rapidjson::Value& retval) {
            rapidjson::Document& storage = env->GetVar(mVarName);
            retval.CopyFrom(storage, storage.GetAllocator());
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

        ~MemberSelector() {
            delete mParent;
        }

        void select(Environment* env, rapidjson::Value& retval) {

            if(mParent != nullptr){
                rapidjson::Value parent;
                mParent->select(env, parent);
                const char* memberName = mMemberName.c_str();
                if(parent.IsObject() && parent.HasMember(memberName)){
                    retval = parent[memberName];
                    return;
                }
            }

            retval.SetNull();
        }

        std::string toString();
    };

    class StrLiteralSelector : public Selector {
    private:
        const std::string mStrValue;

    public:

        StrLiteralSelector(const std::string& value)
        : mStrValue(value) { }

        void select(Environment* env, rapidjson::Value& retval) {
          retval.SetString(rapidjson::StringRef(mStrValue.data(), mStrValue.size()));
        }

        std::string toString();
    };

    class IntLiteralSelector : public Selector {
    private:
        const int mValue;

    public:
        IntLiteralSelector(int value)
        : mValue(value) { }

        void select(Environment* env, rapidjson::Value& retval) {
            retval.SetInt(mValue);
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
        ~ViewQuery();

        void setStatements(const stmtList& stmts);

        bool compile(const char* code);
        void map(rapidjson::Document& input, Environment* env) const;
        void execute(Environment* env) const;

        std::string toString() const;

        bool operator<(const ViewQuery& other) const;

    };
}

#endif
