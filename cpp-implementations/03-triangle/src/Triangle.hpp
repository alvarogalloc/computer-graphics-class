#pragma once
#include "Color.hpp"
#include "RenderData.hpp"
#include "Target.hpp"
#include <algorithm>
#include <cmath>
#include <tuple>

namespace cg {

class Triangle {
  int v1_id, v2_id, v3_id;
  RenderData *render_data;

  static auto edge_function(int x1, int y1, int x2, int y2, int x, int y)
      -> int {
    return (y2 - y1) * (x - x1) - (x2 - x1) * (y - y1);
  }

  static auto barycentric(int x1, int y1, int x2, int y2, int x3, int y3, int x,
                          int y) -> std::tuple<float, float, float> {
    float denom =
        static_cast<float>((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));

    if (denom == 0.0f)
      return {0.0f, 0.0f, 0.0f};

    float w1_num =
        static_cast<float>((y2 - y3) * (x - x3) + (x3 - x2) * (y - y3));
    float w2_num =
        static_cast<float>((y3 - y1) * (x - x3) + (x1 - x3) * (y - y3));
    float w3_num = denom - w1_num - w2_num;

    return {w1_num / denom, w2_num / denom, w3_num / denom};
  }

  [[nodiscard]] auto interpolate_color(int x, int y) const -> Color {
    auto [x1, y1] = render_data->get_vertex(v1_id);
    auto [x2, y2] = render_data->get_vertex(v2_id);
    auto [x3, y3] = render_data->get_vertex(v3_id);

    auto [w1, w2, w3] = barycentric(x1, y1, x2, y2, x3, y3, x, y);

    Color c1 = render_data->get_color(v1_id);
    Color c2 = render_data->get_color(v2_id);
    Color c3 = render_data->get_color(v3_id);

    float r = c1[0] * w1 + c2[0] * w2 + c3[0] * w3;
    float g = c1[1] * w1 + c2[1] * w2 + c3[1] * w3;
    float b = c1[2] * w1 + c2[2] * w2 + c3[2] * w3;

    return Color{static_cast<std::uint8_t>(std::round(r)),
                 static_cast<std::uint8_t>(std::round(g)),
                 static_cast<std::uint8_t>(std::round(b))};
  }

public:
  Triangle(int v1, int v2, int v3, RenderData *data)
      : v1_id(v1), v2_id(v2), v3_id(v3), render_data(data) {
    if (!data)
      throw std::invalid_argument{"RenderData cannot be null"};
  }

  void render(Target &target) const {
    auto [v1_x, v1_y] = render_data->get_vertex(v1_id);
    auto [v2_x, v2_y] = render_data->get_vertex(v2_id);
    auto [v3_x, v3_y] = render_data->get_vertex(v3_id);

    // Early exit for malformed triangles
    int area2 = edge_function(v1_x, v1_y, v2_x, v2_y, v3_x, v3_y);
    if (area2 == 0)
      return;

    // Bounding box
    int min_x = std::min({v1_x, v2_x, v3_x});
    int min_y = std::min({v1_y, v2_y, v3_y});
    int max_x = std::max({v1_x, v2_x, v3_x});
    int max_y = std::max({v1_y, v2_y, v3_y});

    // this is so we dont mind the order of the triangles
    int v3_on_edge_v1v2 = edge_function(v1_x, v1_y, v2_x, v2_y, v3_x, v3_y);
    bool v1v2_inside_is_positive = v3_on_edge_v1v2 >= 0;

    int v1_on_edge_v2v3 = edge_function(v2_x, v2_y, v3_x, v3_y, v1_x, v1_y);
    bool v2v3_inside_is_positive = v1_on_edge_v2v3 >= 0;

    int v2_on_edge_v3v1 = edge_function(v3_x, v3_y, v1_x, v1_y, v2_x, v2_y);
    bool v3v1_inside_is_positive = v2_on_edge_v3v1 >= 0;

    // Rasterize
    for (int y = min_y; y <= max_y; ++y) {
      for (int x = min_x; x <= max_x; ++x) {

        // this is: get the side on which the point is for all three lines
        // already knowing the winding order
        int test_v1v2 = edge_function(v1_x, v1_y, v2_x, v2_y, x, y);
        int test_v2v3 = edge_function(v2_x, v2_y, v3_x, v3_y, x, y);
        int test_v3v1 = edge_function(v3_x, v3_y, v1_x, v1_y, x, y);

        bool inside_v1v2 = (v1v2_inside_is_positive && test_v1v2 >= 0) ||
                           (!v1v2_inside_is_positive && test_v1v2 <= 0);
        bool inside_v2v3 = (v2v3_inside_is_positive && test_v2v3 >= 0) ||
                           (!v2v3_inside_is_positive && test_v2v3 <= 0);
        bool inside_v3v1 = (v3v1_inside_is_positive && test_v3v1 >= 0) ||
                           (!v3v1_inside_is_positive && test_v3v1 <= 0);

        if (inside_v1v2 && inside_v2v3 && inside_v3v1) {
          Color pixel_color = interpolate_color(x, y);
          // interpolate_color acts as the fragment shader here, i should
          // probably change it sometime, but it is what i was asked for this
          // class
          target.set_rgb(x, y, pixel_color);
        }
      }
    }
  }
};

} // namespace cg
