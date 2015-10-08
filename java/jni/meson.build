

ld_flags = []

if meson.is_cross_build()
  if host_machine.system() == 'android'
    ld_flags = '-llog'
  endif
endif

sources = [ 'microdb.cpp', 'driver.cpp', 'iterator.cpp' ]

shared_library('microdb-jni', sources,
	dependencies : microdb_dep,
	link_args : ld_flags )
