
#include <string>
#include <iostream>

#include <microdb/status.h>
#include <microdb/microdb.h>

#include "uuid.h"

using namespace std;
using namespace microdb;


int main(int argc, char** argv) {
    
    DB* db = nullptr;
    DB::Open("testdb", &db);
    
    
    delete db;

}
