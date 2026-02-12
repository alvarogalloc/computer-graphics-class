#pragma once
#include "Color.hpp"
#include "Target.hpp"
#include <format>
#include <stdexcept>
#include <vector>

namespace cg {

class RenderData {
  // this class has two buffers, one is the vertex buffer and the other is the
  // vertex color buffer all are integers, packed as x y in the case of vertex
  // and the Color

  std::vector<Color> color_buffer;
  std::vector<int> vertex_buffer;

  auto assert_in_bounds(int vertex_id) {
    if (vertex_id >= color_buffer.size() || vertex_id < 0) {
      throw std::out_of_range{std::format(
          "that vertex does not exist, max is {}", color_buffer.size()-1)};
    }
  }

public:
  // this way we can be sure that size(vertex_buffer)
  // is always double than size(color_buffer)
  // returns the id of the  new vertex
  auto add_vertex(std::pair<int, int> point, Color c) {
    vertex_buffer.emplace_back(point.first);
    vertex_buffer.emplace_back(point.second);
    color_buffer.emplace_back(c);
    return vertex_count()-1;
  }
  [[nodiscard]] auto get_vertex(int vertex_id) {
    assert_in_bounds(vertex_id);
    return std::pair{vertex_buffer[vertex_id * 2],
                     vertex_buffer[vertex_id * 2 + 1]};
  }
  [[nodiscard]] auto get_color(int vertex_id) {
    assert_in_bounds(vertex_id);
    return color_buffer[vertex_id];
  }

  auto set_vertex(int vertex_id, std::pair<int, int> p) {
    assert_in_bounds(vertex_id);
    vertex_buffer[vertex_id * 2] = p.first;
    vertex_buffer[vertex_id * 2 + 1] = p.second;
  }
  auto set_color(int vertex_id, Color c) {
    assert_in_bounds(vertex_id);
    color_buffer[vertex_id] = c;
  }
  [[nodiscard]] auto vertex_count() const -> int {
    return static_cast<int>(color_buffer.size());
  }

  void draw_vertices(Target &target, int radius = 3) const {
    for (int i = 0; i < static_cast<int>(color_buffer.size()); ++i) {
      int cx = vertex_buffer[i * 2];
      int cy = vertex_buffer[i * 2 + 1];
      Color c = color_buffer[i];

      int r_squared = radius * radius;
      for (int dy = -radius; dy <= radius; ++dy) {
        for (int dx = -radius; dx <= radius; ++dx) {
          if (dx * dx + dy * dy <= r_squared) {
            target.set_rgb(cx + dx, cy + dy, c);
          }
        }
      }
    }
  }
};
} // namespace cg
