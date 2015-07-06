
#ifndef MICRODB_DB_H_
#define MICRODB_DB_H_

#include <stdio.h>

namespace microdb{
    
    static const int kMajorVersion = 1;
    static const int kMinorVersion = 0;
    
    class DB {
    public:
        static Status Open(const std::string& dbdirpath, DB** dbptr);
        
        virtual ~DB();
        
        virtual Status SetView(const std::string& viewName, const std::string& mapQuery) = 0;
        
        virtual Status Insert(const std::string& value, std::string* key = nullptr) = 0;
        virtual Status Update(const std::string& key, const std::string& value) = 0;
        virtual Status Delete(const std::string& key) = 0;
    };
}

#endif
