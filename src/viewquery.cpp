
#include "viewquery.h"

#include <sstream>

namespace microdb {
    
    ViewQuery::ViewQuery(const std::string& name)
    : mName(name) { }
    
    void ViewQuery::execute(Environment* env) {
        for(Statement* stmt : mStatements) {
            stmt->execute(env);
        }
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
    
}