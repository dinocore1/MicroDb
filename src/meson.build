
flexgen = generator(
							flex,
							output : ['@BASENAME@.lex.cpp', '@BASENAME@.lex.h'],
							arguments : ['-o', '@OUTPUT0@', '--header-file=@OUTPUT1@', '@INPUT@' ]
							)

bisongen = generator(
							bison,
							output : ['@BASENAME@.yy.cpp', '@BASENAME@.yy.hpp'],
							arguments : ['-d', '-o', '@OUTPUT0@', '@INPUT@']
							)

port_src = [ 'dbfunctions.cpp', 'dbimpl.cpp', 'indexutils.cpp',
'membuffer.cpp', 'sha256.cpp', 'uuid.cpp', 'value.cpp', 'viewquery.cpp',
'ubjsonwriter.cpp', 'ubjsonreader.cpp', 'vectorclock.cpp', 
flexgen.process('viewquery.l'), flexgen.process('uuidlex.l'),
bisongen.process('viewquery.y')
]

if host.name() == 'darwin'
	dep = dependency('appleframeworks', modules : 'foundation')
  cpp_args = []
	sources = [port_src, 'uuid_mac.cpp']
else
  dep = []
  cpp_args = []
	sources = [port_src, 'uuid_generic.cpp']
endif

microdb_lib = build_target('microdb', sources,
	include_directories : incdir,
	dependencies : [dep, leveldb_dep],
	target_type : libtype
	)

microdb_dep = declare_dependency(
		link_with : microdb_lib,
	  include_directories : incdir
		)
