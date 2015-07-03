
#include "viewquery.h"

namespace microdb {
    
    void ViewQuery::execute(Environment* env) {
        for(Statement* stmt : mStatements) {
            stmt->execute(env);
        }
    }
}