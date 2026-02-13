add_rules("mode.debug", "mode.release")


add_requires("glm", "stb")

set_languages("c++23")

target("01-simpleimage")
    set_kind("binary")
    add_files("src/simple_image_main.cpp", "src/stb_implement.cpp")
    add_packages("stb")

target("03-triangle")
    set_kind("binary")
    add_headerfiles("src/*.hpp")
    add_files("src/triangle_main.cpp", "src/stb_implement.cpp")
    add_packages("glm", "stb")

target("04-compression")
    set_kind("binary")
    add_files("src/compression_main.cpp")
