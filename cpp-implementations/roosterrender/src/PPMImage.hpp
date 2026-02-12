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

  static PPMImage load_from_file(std::string_view filename) {
    std::ifstream file(filename.data(), std::ios::binary | std::ios::in);

    if (!file)
      throw std::runtime_error("Failed to open PPM file");

    auto read_token = [&]() -> std::string {
      std::string token;
      char ch;

      // Skip whitespace and comments
      while (file.get(ch)) {
        if (ch == '#') {
          file.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
          continue;
        }
        if (!std::isspace(static_cast<unsigned char>(ch))) {
          token += ch;
          break;
        }
      }

      while (file.get(ch)) {
        if (std::isspace(static_cast<unsigned char>(ch)))
          break;
        token += ch;
      }

      return token;
    };

    // --- Header ---
    if (read_token() != "P6")
      throw std::runtime_error("Only binary P6 PPM supported");

    int w = std::stoi(read_token());
    int h = std::stoi(read_token());
    int maxval = std::stoi(read_token());

    if (w <= 0 || h <= 0)
      throw std::runtime_error("Invalid PPM dimensions");

    if (maxval != 255)
      throw std::runtime_error("Only max value 255 supported");


    PPMImage img(w, h);

    const std::size_t expected_size = static_cast<std::size_t>(w) * h * 3;

    file.read(reinterpret_cast<char *>(img.image.data()),
              static_cast<std::streamsize>(expected_size));

    if (!file)
      throw std::runtime_error("Failed to read PPM pixel data");

    return img;
  }
};

} // namespace cg
