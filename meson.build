project('MicroDB an Embedded Object Database', 'cpp')
add_global_arguments('-std=c++11', language : 'cpp')

inc_microdb = include_directories('include')

subdir('include')
subdir('src')
subdir('test')

pkgconfig_gen(libraries : microdb_lib,
							version : '1.0',
							name : 'libmicrodb',
							filebase : '',
							description : 'A embedded object database')
