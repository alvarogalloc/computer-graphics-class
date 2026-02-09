

#include "Color.hpp"
#include "PPMImage.hpp"
#include "RenderData.hpp"
#include "Triangle.hpp"
int main() {
  cg::PPMImage image{500, 500};
  cg::RenderData vertices;
  vertices.add_vertex({0, 0}, cg::black);
  vertices.add_vertex({0, 50}, cg::red);
  vertices.add_vertex({50, 50}, cg::green);
  cg::Triangle t{0, 1, 2, &vertices};
  t.render(image);

  vertices.add_vertex({250, 300}, cg::green);
  vertices.add_vertex({400, 400}, cg::green);
  vertices.add_vertex({200, 400}, cg::green);
  cg::Triangle t2{3, 4, 5, &vertices};
  t2.render(image);

  cg::Triangle t3{0, 1, 5, &vertices};
  t3.render(image);
  image.save_to_file("triangles.ppm");

  image.clear(cg::blue);
  vertices.draw_vertices(image, 5);
  image.save_to_file("vertices.ppm");

  image.clear(cg::black);
  auto v1=vertices.add_vertex({0, image.get_height()}, cg::red);
  auto v2=vertices.add_vertex({image.get_width() / 2, 0}, cg::green);
  auto v3=vertices.add_vertex({image.get_width(), image.get_height()}, cg::blue);
  cg::Triangle class_triangle {v1,v2,v3, &vertices};
  class_triangle.render(image);

  image.save_to_file("class_triangle.ppm");
}
