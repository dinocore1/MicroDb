project('MicroDB an Embedded Object Database', 'cpp')
add_global_arguments('-std=c++11', language : 'cpp')

flex = find_program('flex')
bison = find_program('bison')



leveldb_dep = dependency('leveldb', required : false)
if leveldb_dep.found()
	inc = []
	link = []
else
	leveldb_sp = subproject('leveldb')
	leveldb_dep = leveldb_sp.get_variable('dep')
endif

gtest_sp = subproject('gtest')

inc = include_directories('include')

subdir('include')
subdir('src')
subdir('test')

pkgconfig_gen(libraries : microdb_lib,
							version : '1.0',
							name : 'libmicrodb',
							filebase : '',
							description : 'A embedded object database')
