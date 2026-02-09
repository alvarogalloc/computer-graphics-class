#pragma once
#include "Color.hpp"
#include "Target.hpp"
#include <cstdint>
#include <fstream>
#include <stdexcept>
#include <string_view>
#include <vector>

namespace cg {

class PPMImage : public Target {
  int width;
  int height;
  std::vector<std::uint8_t> image;

  [[nodiscard]] auto get_index(int x, int y) const -> std::size_t {
    return static_cast<std::size_t>((y * width + x) * 3);
  }

public:
  PPMImage(int w, int h) : width(w), height(h), image(w * h * 3, 0) {
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

  void set_rgb(int x, int y, Color c) override {
    if (x < 0 || y < 0 || x >= width || y >= height)
      return;

    auto idx = get_index(x, y);
    image[idx] = c[0];     // R
    image[idx + 1] = c[1]; // G
    image[idx + 2] = c[2]; // B
  }

  void save_to_file(std::string_view filename) override {
    std::ofstream file{filename.data(),
                       std::ios_base::out | std::ios_base::binary};

    if (!file)
      throw std::runtime_error{"Failed to open file"};

    file << "P6\n" << width << ' ' << height << "\n255\n";

    file.write(reinterpret_cast<const char *>(image.data()),
               static_cast<std::streamsize>(image.size()));

    if (!file)
      throw std::runtime_error{"Failed to write image data"};
  }

  [[nodiscard]] auto get_width() const -> int override { return width; }
  [[nodiscard]] auto get_height() const -> int override { return height; }
};

} // namespace cg
