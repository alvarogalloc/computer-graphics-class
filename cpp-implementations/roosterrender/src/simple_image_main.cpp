#include <array>
#include <cstdio>
#include <stb_image_write.h>

using color = std::array<unsigned char, 4>;
constexpr static color red{255, 0, 0, 255};
constexpr static color blue{0, 0, 255, 255};
constexpr static color green{0, 0, 255, 255};

template <int w, int h, int n_channels> constexpr auto make_image() {
  std::array<unsigned char, w * h * n_channels> image_data;

  for (int i = 0; i < h; i++) {
    for (int j = 0; j < w; j++) {
      const int index = (i * w + j) * n_channels;
      auto &r = image_data[index];
      auto &g = image_data[index + 1];
      auto &b = image_data[index + 2];

      if (i * w * w <= j * h * h) {
        r = red[0];
        g = red[1];
        b = red[2];
      } else {
        r = blue[0];
        g = blue[1];
        b = blue[2];
      }
    }
  }
  return image_data;
}

int main(int argc, char **argv) {
  constexpr static int w{1000};
  constexpr static int h{100};
  constexpr static int n_channels{3};
  const auto image = make_image<w, h, n_channels>();
  // 1 byte =  uchar, w*h*size(uchar) = 400*400*1=160000
  const int write_result{
      stbi_write_jpg("imagen.jpg", w, h, 3, (void *)(image.data()), 100)};
  if (w != 0) {
    std::puts("something went wrong bitch\n");
  }
}
