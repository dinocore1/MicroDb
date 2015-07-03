
#include "viewquery.h"

namespace microdb {
    
    void ViewQuery::execute() {
        for(Statement* stmt : mStatements) {
            stmt->execute();
        }
    }
}