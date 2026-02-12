#include "Color.hpp"
#include "FragmentShader.hpp"
#include "JPGImage.hpp"
#include "RenderData.hpp"
#include "Triangle.hpp"
int main() {
  cg::JPGImage image{500, 500};
  cg::RenderData vertices;

  cg::Triangle t{vertices.add_vertex({250, 300}, cg::green),
                 vertices.add_vertex({400, 400}, cg::green),
                 vertices.add_vertex({200, 400}, cg::green)};
  t.render(image, &vertices);

  t = cg::Triangle{vertices.add_vertex({0, 0}, cg::black),
                   vertices.add_vertex({0, 50}, cg::red),
                   vertices.add_vertex({50, 50}, cg::green)};
  t.render(image, &vertices);

  image.save_to_file("triangles.jpg");

  image.clear(cg::blue);
  vertices.draw_vertices(image, 5);
  image.save_to_file("vertices.jpg");

  image.clear(cg::black);
  auto v1 = vertices.add_vertex({0, image.get_height()}, cg::red);
  auto v2 = vertices.add_vertex({image.get_width() / 2, 0}, cg::green);
  auto v3 =
      vertices.add_vertex({image.get_width(), image.get_height()}, cg::blue);
  t = cg::Triangle{v1, v2, v3};
  t.render(image, &vertices);

  image.save_to_file("class_triangle.jpg");
}
