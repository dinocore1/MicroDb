
gtestinc = gtest_sp.get_variable('incdir')
gtestlibsrc = gtest_sp.get_variable('libsources')
gtestmain = gtest_sp.get_variable('mainsources')

gtest_lib = static_library('gtest', gtestlibsrc, include_directories : gtestinc)

test('uuid_test', executable('uuid_test', ['uuid_test.cpp', gtestmain],
    include_directories : [ include_directories('../src'), inc, gtestinc],
    link_with : [microdb_lib, gtest_lib],
    link_args : '-lpthread'
    ))
