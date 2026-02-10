#pragma once

#include "Color.hpp"
#include <string_view>
namespace cg {

class Target {
public:
  virtual void set_rgb(int x, int y, Color c) = 0;
  virtual Color get_rgb(int x, int y) const = 0;
  virtual void clear(Color c) = 0;
  virtual void save_to_file(std::string_view filename) = 0;
  [[nodiscard]] virtual auto get_width() const -> int = 0;
  [[nodiscard]] virtual auto get_height() const -> int = 0;

  virtual ~Target() {}
};
} // namespace cg
