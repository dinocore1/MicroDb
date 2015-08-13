project('MicroDB an Embedded Object Database', 'cpp')
add_global_arguments('-std=c++11', language : 'cpp')

flex = find_program('flex')
bison = find_program('bison')

if get_option('shared_lib')
  libtype = 'shared_library'
else
  libtype = 'static_library'
endif


unqlite_proj = subproject('unqlite')
unqlite_incdir = unqlite_proj.get_variable('incdir')
unqlite_dep = unqlite_proj.get_variable('dep')

gtest_sp = subproject('gtest')

incdir = include_directories('include')

subdir('include')
subdir('src')
subdir('test')
