#include <print>
#define STB_IMAGE_WRITE_IMPLEMENTATION
#include <stb_image_write.h>

using color = std::array<unsigned char, 4>;


template<int w, int h, int n_channels>
consteval auto make_image() {
  std::array<unsigned char, w * h * n_channels> image_data;
  color red{255, 0, 0, 255};
  color blue{0, 0, 255, 255};
  color green{0, 0, 255, 255};

  for (int i = 0; i < h; i++) {
    for (int j = 0; j < w; j++) {
      const int index = (i * w + j) * n_channels;
      auto &r = image_data[index];
      auto &g = image_data[index + 1];
      auto &b = image_data[index + 2];

      if (float(i)/float(h) <= float(j)/float(w)) {
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
  const int w{100};
  const int h{100};
  const int n_channels{3};
  auto image = make_image<w, h, n_channels>();
  // 1 byte =  uchar, w*h*size(uchar) = 400*400*1=160000
  if (stbi_write_jpg("/home/rooster/Pictures/imagen.jpg", w, h, 3, (void*)(image.data()), 100))
  {
    std::println("something went wrong bitch");
  }
}
