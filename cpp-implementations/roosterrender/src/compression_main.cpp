#include "Compressor.hpp"
#include "Decompressor.hpp"
#include "PPMImage.hpp"

#include <iostream>
#include <string>

int main(int argc, char **argv) {
  if (argc != 4) {
    std::cerr << "Usage:\n"
              << "  " << argv[0] << " c input.ppm output.rooster\n"
              << "  " << argv[0] << " d input.rooster output.ppm\n";
    return 1;
  }

  std::string mode = argv[1];
  std::string input = argv[2];
  std::string output = argv[3];

  try {
    if (mode == "c") {
      // --- Compress ---
      // NOTE: Assumes you implement a PPM loader constructor or factory
      cg::PPMImage image = cg::PPMImage::load_from_file(input); // <-- you must provide this
      cg::Compressor compressor(image);
      compressor.save_to_file(output);
      std::cout << "Compressed successfully.\n";
    } else if (mode == "d") {
      // --- Decompress ---
      cg::PPMImage image = cg::Decompressor::load_from_file(input);
      image.save_to_file(output);
      std::cout << "Decompressed successfully.\n";
    } else {
      std::cerr << "Unknown mode: " << mode << "\n";
      return 1;
    }
  } catch (const std::exception &e) {
    std::cerr << "Error: " << e.what() << "\n";
    return 1;
  }

  return 0;
}
