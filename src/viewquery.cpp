
#include "viewquery.h"

#include <sstream>
#include <algorithm>

namespace microdb {

    ViewQuery::ViewQuery(const std::string& name)
    : mName(name) { }

    ViewQuery::~ViewQuery() {
        destroyStmtList(mStatements);
    }

    void ViewQuery::execute(Environment* env) const {
        for(Statement* stmt : mStatements) {
            stmt->execute(env);
        }
    }

    std::string ViewQuery::toString() const {
      std::stringstream buf;
      for(Statement* stmt : mStatements) {
        buf << stmt->toString() << std::endl;
      }
      return buf.str();
    }

    bool ViewQuery::operator<(const ViewQuery& other) const {
        return mName < other.mName;
    }

    std::string IfStatement::toString() {
        std::stringstream buf;
        buf << "if(" << mCondition->toString() << ") {" << std::endl;
        for(Statement* stmt : mThenStmts) {
            buf << "  " << stmt->toString() << std::endl;
        }
        buf << "}" << std::endl;
        return buf.str();
    }

    std::string FunctionCall::toString() {
        std::stringstream buf;
        buf << mFunctionName << "(";
        for(auto it = mArgList.begin(); it != mArgList.end() ; it++) {
            buf << (*it)->toString();
            if(it+1 != mArgList.end()) {
                buf << ", ";
            }
        }
        buf << ")";
        return buf.str();
    }

    std::string Condition::toString() {
        std::stringstream buf;
        buf << mLHS->toString() << " ";
        switch (mOp) {
            case Equals:
                buf << "==";
                break;

            case NotEqual:
                buf << "!=";
                break;

            case GreaterThan:
                buf << ">";
                break;

            case LessThan:
                buf << "<";
                break;

            case GreaterOrEqual:
                buf << "<=";
                break;

            case LessThanOrEqual:
                buf << ">=";
                break;

        }
        buf << mRHS->toString();
        return buf.str();
    }

    std::string MemberSelector::toString() {
        return mParent->toString() + "." + mMemberName;
    }

    ArraySelector::ArraySelector(Selector* index, Selector* parent)
    : mParent(parent), mIndex(index) {

    }

    ArraySelector::~ArraySelector() {
      delete mParent;
      delete mIndex;
    }

    void ArraySelector::select(Environment* env, rapidjson::Value& retval) {
      if(mParent != nullptr && mIndex != nullptr) {
        rapidjson::Value parent, index;

        mParent->select(env, parent);
        if(parent.IsArray()) {
          mIndex->select(env, index);
          uint64_t indexValue;
          if(index.IsUint64()) {
            indexValue = index.GetUint64();
          } else if(index.IsUint()) {
            indexValue = index.GetUint();
          }
          retval = parent[index.GetInt()];
          return;
        }
      }

      retval.SetNull();
    }

    std::string ArraySelector::toString() {
      std::stringstream buf;
      buf << mParent->toString() << '[' << mIndex->toString() << ']';
      return buf.str();
    }

    std::string IntLiteralSelector::toString() {
        std::stringstream buf;
        buf << mValue;
        return buf.str();
    }

    void find_and_replace(std::string& source, const std::string& find, const std::string& replace) {
        for(std::string::size_type i = 0; (i = source.find(find, i)) != std::string::npos;) {
            source.replace(i, find.length(), replace);
            i += replace.length();
        }
    }

    std::string StrLiteralSelector::toString() {
        std::string retval = mStrValue;
        find_and_replace(retval, "\"", "\\\\");
        return retval;
    }

}
