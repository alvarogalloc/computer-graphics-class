add_rules("mode.debug", "mode.release")

set_languages("c++23")
target("03-triangle")
    set_kind("binary")
    add_files("src/main.cpp")

target("04-compression")
    set_kind("binary")
    add_files("src/compression_main.cpp")

