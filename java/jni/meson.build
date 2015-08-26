

ld_flags = []

if meson.is_cross_build()
  if host.name() == 'android'
    jni_inc = []
    ld_flags = '-llog'
  endif
else
  jni_inc = include_directories( get_option('jdk_home_inc') )
endif

sources = [ 'microdb.cpp', 'driver.cpp', 'iterator.cpp' ]

shared_library('microdb-jni', sources,
	dependencies : microdb_dep,
  link_args : ld_flags,
	include_directories : jni_inc )