#pragma once
#include "Color.hpp"
#include "Target.hpp"
#include <cstdint>
#include <fstream>
#include <stb_image.h>
#include <stb_image_write.h>
#include <stdexcept>
#include <string_view>
#include <vector>

namespace cg {

class JPGImage : public Target {
  int width;
  int height;
  std::vector<std::uint8_t> image;

  [[nodiscard]] auto get_index(int x, int y) const -> std::size_t {
    return static_cast<std::size_t>((y * width + x) * 3);
  }
  JPGImage(std::uint8_t *ptr, int w, int h)
      : width(w), height(h), image(ptr, ptr + (w * h * 3)) {}

public:
  JPGImage(int w, int h) : width(w), height(h), image(w * h * 3, 0) {
    if (w <= 0 || h <= 0)
      throw std::invalid_argument{"Width and height must be positive"};
  }
  void clear(Color c) override {
    // TODO: get fancy with std::views and ranges
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        set_rgb(x, y, c);
      }
    }
  }
  Color get_rgb(int x, int y) const override {
    if (x < 0 || y < 0 || x >= width || y >= height)
      return Color{0, 0, 0};

    auto idx = get_index(x, y);
    return Color{
        image[idx],     // R
        image[idx + 1], // G
        image[idx + 2]  // B
    };
  }

  void set_rgb(int x, int y, Color c) override {
    if (x < 0 || y < 0 || x >= width || y >= height)
      return;

    auto idx = get_index(x, y);
    image[idx] = c[0];     // R
    image[idx + 1] = c[1]; // G
    image[idx + 2] = c[2]; // B
  }

  void save_to_file(std::string_view filename) override {
    stbi_write_jpg(filename.data(), width, height, 3, image.data(), 100);
  }

  [[nodiscard]] auto get_width() const -> int override { return width; }
  [[nodiscard]] auto get_height() const -> int override { return height; }

  static JPGImage load_from_file(std::string_view filename) {
    int x;
    int y;
    int n;
    auto *data =
        stbi_load_from_file(std::fopen(filename.data(), "r"), &x, &y, &n, 3);

    if (!data || n != 3)
      throw std::runtime_error("Failed to load JPG or incorrect format");

    JPGImage img{data, x, y};
    stbi_image_free(data);
    return img;
  }
};

} // namespace cg
