

jni_inc = include_directories( get_option('jdk_home_inc') )

sources = [ 'microdb.cpp' ]

shared_library('microdb-jni', sources,
	include_directories : jni_inc )