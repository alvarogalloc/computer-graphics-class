#pragma once
#include "Target.hpp"
#include "Color.hpp"
#include <cstdint>
#include <fstream>
#include <stdexcept>
#include <string_view>
#include <vector>

namespace cg {

class Compressor {
  const Target& surface;

  static constexpr int block_size = 4;

public:
  explicit Compressor(const Target& s) : surface(s) {
    validate_dimensions();
  }

  void save_to_file(std::string_view filename) const {
    std::ofstream out(filename.data(),
                      std::ios::binary | std::ios::out);

    if (!out)
      throw std::runtime_error("Failed to open output file");

    write_header(out);
    write_blocks(out);

    if (!out)
      throw std::runtime_error("Failed while writing .rooster file");
  }

private:
  void validate_dimensions() const {
    if (surface.get_width() % block_size != 0 ||
        surface.get_height() % block_size != 0) {
      throw std::runtime_error(
          "Width and height must be divisible by 4");
    }
  }

  void write_header(std::ofstream& out) const {
    const char magic[4] = {'R', 'O', 'O', 'S'};
    out.write(magic, 4);

    std::uint32_t w = surface.get_width();
    std::uint32_t h = surface.get_height();

    out.write(reinterpret_cast<const char*>(&w), sizeof(w));
    out.write(reinterpret_cast<const char*>(&h), sizeof(h));
  }

  void write_blocks(std::ofstream& out) const {
    const int width = surface.get_width();
    const int height = surface.get_height();

    for (int y = 0; y < height; y += block_size) {
      for (int x = 0; x < width; x += block_size) {
        Color avg = average_block(x, y);
        write_color(out, avg);
      }
    }
  }

  Color average_block(int start_x, int start_y) const {
    std::uint32_t r = 0;
    std::uint32_t g = 0;
    std::uint32_t b = 0;

    for (int dy = 0; dy < block_size; ++dy) {
      for (int dx = 0; dx < block_size; ++dx) {
        Color c = surface.get_rgb(start_x + dx, start_y + dy);
        r += c[0];
        g += c[1];
        b += c[2];
      }
    }

    constexpr std::uint32_t pixel_count = block_size * block_size;

    return Color{
        static_cast<std::uint8_t>(r / pixel_count),
        static_cast<std::uint8_t>(g / pixel_count),
        static_cast<std::uint8_t>(b / pixel_count)
    };
  }

  void write_color(std::ofstream& out, const Color& c) const {
    out.put(static_cast<char>(c[0]));
    out.put(static_cast<char>(c[1]));
    out.put(static_cast<char>(c[2]));
  }
};

} // namespace cg
