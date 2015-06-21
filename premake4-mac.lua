solution "MicroDB"
  configurations { "Debug", "Release" }

  project "MicroDB"
    language "C++"
    kind "SharedLib"
    files { "src/**.cpp", "src/**.h", "src/viewquery.y", "src/viewquery.l" }

    configuration "Debug*"
      defines { "DEBUG" }
      flags { "Symbols" }

    configuration "Release*"
      flags { "Optimize" }

  project "MicroDBTest"
    language "C++"
    kind "ConsoleApp"
    links { "MicroDB" }
    includedirs { "src/include" }
    files { "test/**.cpp" }
