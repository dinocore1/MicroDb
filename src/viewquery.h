#ifndef MicroDB_viewquery_h
#define MicroDB_viewquery_h

#include <string>
#include <vector>
#include <map>


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
            Value retval;
            select(env, retval);
        }

        virtual void select(Environment* env, Value& retval) = 0;
    };

    typedef std::vector< Statement* > stmtList;
    typedef std::vector< Selector* > argList;
    typedef void (*dataFunction)(Environment* env, Value& retval, const std::vector< Selector* >& args);

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

    protected:
        std::map< std::string, Value > mVariables;
        std::map< std::string, dataFunction > mFunctions;

    public:

        Value& GetVar(const std::string& name) {
            Value& retval = mVariables[name];
            return retval;
        }

        void SetVar(std::string name, const Value& value) {
            mVariables[name] = value;
        }

        dataFunction GetFunction(const std::string& name) {
            return mFunctions[name];
        }

        void SetFunction(const std::string& name, dataFunction fun) {
            mFunctions[name] = fun;
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
          Value value;
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
        Statement* mThenStmt;
        Statement* mElseStmt;

    public:

        IfStatement(Selector* condition, Statement* thenStmt, Statement* elseStmt = nullptr)
        : mCondition(condition), mThenStmt(thenStmt), mElseStmt(elseStmt) { }

        ~IfStatement() {
            delete mCondition;
            delete mThenStmt;
            if(mElseStmt != nullptr) {
              delete mElseStmt;
            }
        }

        void execute(Environment* env) {
          Value conditionVal;
          mCondition->select(env, conditionVal);
          if(conditionVal.IsBool() && conditionVal.asBool()) {
            mThenStmt->execute(env);
          } else if(mElseStmt != nullptr) {
            mElseStmt->execute(env);
          }
        }

        std::string toString();
    };

    class BlockStatement : public Statement {
    private:
      stmtList mStatements;

    public:
      BlockStatement(const stmtList& stmts)
      : mStatements(stmts) { }

      ~BlockStatement() {
        destroyStmtList(mStatements);
      }

      void execute(Environment* env) {
        for(Statement* stmt : mStatements) {
            stmt->execute(env);
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

        void select(Environment* env, Value& retval) {
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

        void select(Environment* env, Value& retval) {
            Value left, right;
            mLHS->select(env, left);
            mRHS->select(env, right);
            
            switch(mOp) {
                case Equals:
                    retval = (left == right);
                    break;
                case GreaterThan:
                    retval = (left > right);
                    break;
                case LessThan:
                    retval = (left < right);
                    break;
                case GreaterOrEqual:
                    retval = left >= right;
                    break;
                case LessThanOrEqual:
                    retval = left <= right;
                    break;
                case NotEqual:
                    retval = left != right;
                    break;
                    
                default:
                    retval = false;
                
            }
            
            retval = retval;
        }

        std::string toString();
    };

    class VarSelector : public Selector {
    public:
        const std::string mVarName;

        VarSelector(const std::string& name)
        : mVarName(name) {}

        void select(Environment* env, Value& retval) {
            retval = env->GetVar(mVarName);
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

        void select(Environment* env, Value& retval) {

            if(mParent != nullptr){
                Value parent;
                mParent->select(env, parent);
                const char* memberName = mMemberName.c_str();
                if(parent.IsObject() && parent.HasKey(memberName)){
                    retval = parent[memberName];
                    return;
                }
            }

            retval.SetNull();
        }

        std::string toString();
    };

    class ArraySelector : public Selector {
    private:
      Selector* mParent;
      Selector* mIndex;

    public:
      ArraySelector(Selector* index, Selector* parent);
      ~ArraySelector();

      void select(Environment* env, Value& retval);
      std::string toString();
    };

    class StrLiteralSelector : public Selector {
    private:
        const std::string mStrValue;

    public:

        StrLiteralSelector(const std::string& value)
        : mStrValue(value) { }

        void select(Environment* env, Value& retval) {
            retval = mStrValue;
        }

        std::string toString();
    };

    class IntLiteralSelector : public Selector {
    private:
        const int mValue;

    public:
        IntLiteralSelector(int value)
        : mValue(value) { }

        void select(Environment* env, Value& retval) {
            retval = mValue;
        }

        std::string toString();
    };

    class FloatLiteralSelector : public Selector {
    private:
      const double mValue;

    public:
      FloatLiteralSelector(double value)
      : mValue(value) { }

      void select(Environment* env, Value& retval) {
        retval = mValue;
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
        ViewQuery();
        ~ViewQuery();

        void setStatements(const stmtList& stmts);

        bool compile(const char* code);
        void map(Value& input, Environment* env) const;
        void execute(Environment* env) const;

        std::string toString() const;

    };
}

#endif
