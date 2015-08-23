
gtestinc = gtest_sp.get_variable('incdir')
gtestlibsrc = gtest_sp.get_variable('libsources')
gtestmain = gtest_sp.get_variable('mainsources')

gtest_lib = static_library('gtest', gtestlibsrc, include_directories : gtestinc)

if meson.is_cross_build()
  if host.name() == 'mingw'
    cpp_args = [ '-D_WIN32_WINNT=0x0600' ]
  elif host.name() == 'android'
    cpp_args = [ '-DOS_ANDROID' ]
  endif
endif

main_incdir = [ include_directories('../src'), incdir, gtestinc, leveldb_inc]

test('uuid_test', executable('uuid_test', ['uuid_test.cpp', gtestmain],
    include_directories : main_incdir,
    dependencies : microdb_dep,
    link_with : [ gtest_lib ],
    link_args : '-lpthread'
    ))

test('viewquery_test', executable('viewquery_test', ['viewquery.cpp', gtestmain],
    include_directories : main_incdir,
    dependencies : microdb_dep,
    link_with : [ gtest_lib ],
    link_args : '-lpthread'
    ))

test('value_test', executable('value_test', ['value_tests.cpp', gtestmain],
    include_directories : main_incdir,
    dependencies : microdb_dep,
    link_with : [ gtest_lib ],
    link_args : '-lpthread'
    ))

test('ubjsonserialize', executable('ubjsonserialize', ['ubjsonserialize.cpp', gtestmain],
    include_directories : main_incdir,
    dependencies : microdb_dep,
    link_with : [ gtest_lib ],
    link_args : '-lpthread'
    ))

test('vectorclock', executable('vectorclock', ['vectorclock_test.cpp', gtestmain],
    include_directories : main_incdir,
    dependencies : microdb_dep,
    link_with : [ gtest_lib ],
    link_args : '-lpthread'
    ))

test('leveldbdriver', executable('leveldbdriver', ['leveldbdriver.cpp', gtestmain],
    include_directories : main_incdir,
    dependencies : microdb_dep,
    link_with : [ gtest_lib ],
    link_args : '-lpthread'
    ))

test('db', executable('db', ['db.cpp', gtestmain],
    include_directories : main_incdir,
    dependencies : microdb_dep,
    link_with : [ gtest_lib ],
    link_args : '-lpthread'
    ))