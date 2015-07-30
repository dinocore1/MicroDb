project('MicroDB an Embedded Object Database', 'cpp')
add_global_arguments('-std=c++11', language : 'cpp')

leveldb_dep = dependency('leveldb', required : false)
if leveldb_dep.found()
	include_external = []
	link = []
else
	leveldb_sp = subproject('leveldb')
	include_external = leveldb_sp.get_variable('pub_headers')
	link = leveldb_sp.get_variable('leveldb_staticlib')
endif

inc_microdb = [include_directories('include'), include_external ]

subdir('include')
subdir('src')
subdir('test')

pkgconfig_gen(libraries : microdb_lib,
							version : '1.0',
							name : 'libmicrodb',
							filebase : '',
							description : 'A embedded object database')
