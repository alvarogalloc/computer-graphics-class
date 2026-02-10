#pragma once
#include "Color.hpp"
#include "PPMImage.hpp"
#include <cstdint>
#include <fstream>
#include <stdexcept>
#include <string_view>

namespace cg {

class Decompressor {
  static constexpr int block_size = 4;

public:
  static PPMImage load_from_file(std::string_view filename) {
    std::ifstream in(filename.data(), std::ios::binary | std::ios::in);

    if (!in)
      throw std::runtime_error("Failed to open .rooster file");

    validate_magic(in);

    auto [width, height] = read_dimensions(in);
    validate_dimensions(width, height);

    PPMImage image(width, height);
    read_blocks(in, image);

    return image;
  }

private:
  static void validate_magic(std::ifstream &in) {
    char magic[4];
    in.read(magic, 4);

    if (!in || magic[0] != 'R' || magic[1] != 'O' || magic[2] != 'O' ||
        magic[3] != 'S') {
      throw std::runtime_error("Invalid .rooster file (bad magic)");
    }
  }

  static std::pair<std::uint32_t, std::uint32_t>
  read_dimensions(std::ifstream &in) {
    std::uint32_t width;
    std::uint32_t height;

    in.read(reinterpret_cast<char *>(&width), sizeof(width));
    in.read(reinterpret_cast<char *>(&height), sizeof(height));

    if (!in)
      throw std::runtime_error("Failed to read dimensions");

    return {width, height};
  }

  static void validate_dimensions(std::uint32_t w, std::uint32_t h) {
    if (w % block_size != 0 || h % block_size != 0)
      throw std::runtime_error("Corrupted file: dimensions not divisible by 4");
  }

  static void read_blocks(std::ifstream &in, PPMImage &image) {
    const int width = image.get_width();
    const int height = image.get_height();

    for (int y = 0; y < height; y += block_size) {
      for (int x = 0; x < width; x += block_size) {
        Color c = read_color(in);
        fill_block(image, x, y, c);
      }
    }

    if (!in)
      throw std::runtime_error("Unexpected end of file");
  }

  static Color read_color(std::ifstream &in) {
    std::uint8_t rgb[3];
    in.read(reinterpret_cast<char *>(rgb), 3);

    if (!in)
      throw std::runtime_error("Failed to read block color");

    return Color{rgb[0], rgb[1], rgb[2]};
  }

  static void fill_block(PPMImage &image, int start_x, int start_y,
                         const Color &c) {
    for (int dy = 0; dy < block_size; ++dy) {
      for (int dx = 0; dx < block_size; ++dx) {
        image.set_rgb(start_x + dx, start_y + dy, c);
      }
    }
  }
};

} // namespace cg
