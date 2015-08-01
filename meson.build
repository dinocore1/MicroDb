project('MicroDB an Embedded Object Database', 'cpp')
add_global_arguments('-std=c++11', language : 'cpp')

flex = find_program('flex')
bison = find_program('bison')

if get_option('shared_lib')
  libtype = 'shared_library'
else
  libtype = 'static_library'
endif


leveldb_dep = dependency('leveldb', required : false)
if leveldb_dep.found()
	inc = []
	link = []
else
	leveldb_proj = subproject('leveldb')
	leveldb_dep = leveldb_proj.get_variable('dep')
endif

gtest_sp = subproject('gtest')

incdir = include_directories('include')

subdir('include')
subdir('src')
subdir('test')
