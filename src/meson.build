
microdb_srcs = [
			'dbimpl.cpp',
			'dbfunctions.cpp',
			'membuffer.cpp',
			'viewquery.cpp',
			'sha256.cpp'
		]

microdb_lib = shared_library('microdb',
														sources : microdb_srcs,
														include_directories : inc_microdb,
														link_with : link,
														install : true)
