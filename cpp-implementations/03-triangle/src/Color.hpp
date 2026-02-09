#pragma once

#include <array>
#include <cstdint>
#include <tuple>

namespace cg {

struct Color : public std::array<std::uint8_t, 3> {
  // color given in 0xRRGGBB format
  constexpr explicit Color(std::uint8_t r, std::uint8_t g, std::uint8_t b)
      : std::array<std::uint8_t, 3>({r, g, b}) {}
  constexpr explicit Color(int rgb) {
    (*this)[0] = (rgb >> 16) & 0xFF;
    (*this)[1] = (rgb >> 8) & 0xFF;
    (*this)[2] = rgb & 0xFF;
  }
  [[nodiscard]] auto unpack() {
    return std::tie((*this)[0], (*this)[1], (*this)[2]);
  }
  [[nodiscard]] auto unpack() const {
    return std::tie((*this)[0], (*this)[1], (*this)[2]);
  }
  [[nodiscard]] auto r() const { return (*this)[0]; }
  [[nodiscard]] auto g() const { return (*this)[1]; }
  [[nodiscard]] auto b() const { return (*this)[2]; }
  [[nodiscard]] auto &r() { return (*this)[0]; }
  [[nodiscard]] auto &g() { return (*this)[1]; }
  [[nodiscard]] auto &b() { return (*this)[2]; }

  // some definitions
};

static constexpr auto black = Color{0, 0, 0};
static constexpr auto white = Color{255, 255, 255};
static constexpr auto red = Color{255, 0, 0};
static constexpr auto green = Color{0, 255, 0};
static constexpr auto blue = Color{0, 0, 255};
} // namespace cg
