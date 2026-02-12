#pragma once

#include "RenderData.hpp"
#include <algorithm>
#include <glm/glm.hpp>

#include <stdexcept>
namespace cg {

struct FragmentShader {
  RenderData *m_render_data;
  FragmentShader(RenderData *render_data, std::vector<std::size_t> indices)
      : m_render_data(render_data) {
    if (!indices.size() || indices.size() % 3 != 0 ||
        *std::ranges::max_element(indices) >= render_data->vertex_count()) {
      throw std::invalid_argument{
          "cannot make a shader with non-existent vertices"};
    }
  }

  /*
   * this recieves the coords for the indices passed to the constructor
   * so it makes something with the screen coordinate (pixel coord) of
   * that point
   * TODO: make this for world space (NDCs) like opengl
   * returns the color that specific pixel should have
   */
  [[nodiscard]]
  virtual Color get_rgb(glm::vec2 coord_in_triangle) = 0;
};

} // namespace cg
