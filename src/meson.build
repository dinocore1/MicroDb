
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

port_src = [ 'dbfunctions.cpp', 'dbimpl.cpp',
'sha256.cpp', 'uuid.cpp', 'value.cpp', 'viewquery.cpp',
'ubjsonwriter.cpp', 'ubjsonreader.cpp', 'vectorclock.cpp', 'serialize.cpp',
'utils.cpp', 'leveldbdriver.cpp', 'index.cpp',
flexgen.process('viewquery.l'), flexgen.process('uuidlex.l'),
bisongen.process('viewquery.y')
]

dep = []
cpp_args = [ ]
	
if meson.is_cross_build()
  if host.name() == 'mingw'
    cpp_args = [ cpp_args, '-D_WIN32_WINNT=0x0600' ]
	sources = [port_src, 'uuid_generic.cpp']
  elif host.name() == 'android'
    cpp_args = [ cpp_args, '-DOS_ANDROID' ]
	sources = [port_src, 'uuid_generic.cpp']
  endif
else
  if host.name() == 'darwin'
    dep = dependency('appleframeworks', modules : 'foundation')
    sources = [port_src, 'uuid_mac.cpp']
  else
    sources = [port_src, 'uuid_generic.cpp']
  endif
endif

microdb_lib = build_target('microdb', sources,
	cpp_args : cpp_args,
	include_directories : incdir,
	dependencies : [dep, leveldb_dep],
	target_type : libtype
	)

microdb_dep = declare_dependency(
	link_with : microdb_lib,
	include_directories : incdir
)
