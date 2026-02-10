package up.edu.cg;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.IOException;

/**
 * Simple CLI for color quantization using .rooster binary format.
 * Efficiently packs pixels using only the required number of bits.
 * it only works for uncompressed formats
 * jpg and png already compress the images better than us
 * so we need to use RAW or TIFF or somethings uncompressed for
 * our algorithm to shine
 */
public class Main {

  private static final int MAGIC_NUMBER = 0x524F4F53; // "ROOS" in hex, just a footprint of my format
  private static final byte VERSION = 1;

  public static void main(String[] args) {
    if (args.length < 3) {
      printUsage();
      System.exit(1);
    }

    String command = args[0];
    String inputPath = args[1];
    String outputPath = args[2];
    int bits = 4; // Default -> should halve the size

    for (int i = 3; i < args.length - 1; i++) {
      if ("-b".equals(args[i])) {
        try {
          bits = Integer.parseInt(args[i + 1]);
          if (bits < 1 || bits > 8) {
            System.err.println("Error: bits must be between 1 and 8");
            System.exit(1);
          }
        } catch (NumberFormatException e) {
          System.err.println("Error: invalid number for -b flag");
          System.exit(1);
        }
        break;
      }
    }

    try {
      switch (command.toLowerCase()) {
        case "c":
          compress(inputPath, outputPath, bits);
          break;
        case "d":
          decompress(inputPath, outputPath);
          break;
        default:
          System.err.println("Unknown command: " + command);
          printUsage();
          System.exit(1);
      }
    } catch (IOException e) {
      System.err.println("Error: " + e.getMessage());
      System.exit(1);
    }
  }

  private static void compress(String inputPath, String outputPath, int bits) throws IOException {
    System.out.println("Compressing: " + inputPath);
    System.out.println("Output: " + outputPath);
    System.out.println("Bits per channel: " + bits);

    // Read input image
    BufferedImage input = ImageIO.read(new File(inputPath));
    if (input == null) {
      throw new IOException("Could not read input file: " + inputPath);
    }

    int width = input.getWidth();
    int height = input.getHeight();

    // Quantize the image
    ColorQuantizer quantizer = new ColorQuantizer(bits);
    BufferedImage quantized = quantizer.quantize(input);

    // Write .rooster binary format
    try (DataOutputStream out = new DataOutputStream(
        new BufferedOutputStream(new FileOutputStream(outputPath)))) {

      // Header
      out.writeInt(MAGIC_NUMBER);
      out.writeByte(VERSION);
      out.writeByte(bits);
      out.writeInt(width);
      out.writeInt(height);

      BitWriter bitWriter = new BitWriter(out);
      int shift = 8 - bits;
      int mask = (0xFF >> shift) << shift;

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int rgb = quantized.getRGB(x, y);
          int red = (rgb >> 16) & 0xFF;
          int green = (rgb >> 8) & 0xFF;
          int blue = rgb & 0xFF;

          // Extract only the significant bits
          red = (red & mask) >> shift;
          green = (green & mask) >> shift;
          blue = (blue & mask) >> shift;

          // Write exactly 'bits' bits for each channel
          bitWriter.writeBits(red, bits);
          bitWriter.writeBits(green, bits);
          bitWriter.writeBits(blue, bits);
        }
      }
      bitWriter.flush();
    }

    File inputFile = new File(inputPath);
    File outputFile = new File(outputPath);
    long originalSize = inputFile.length();
    long compressedSize = outputFile.length();
    float compressionRatio = (1 - (float) compressedSize / originalSize) * 100;

    int bitsPerPixel = bits * 3;
    int theoreticalSize = 14 + (width * height * bitsPerPixel + 7) / 8;
    int originalColors = 256 * 256 * 256;
    int quantizedColors = quantizer.getPaletteSize();

    System.out.println("Done!");
    System.out.println("Image dimensions: " + width + "x" + height);
    System.out.println("Original size: " + originalSize + " bytes");
    System.out.println("Compressed size: " + compressedSize + " bytes");
    System.out.println("Theoretical size: " + theoreticalSize + " bytes (" + bitsPerPixel + " bits/pixel)");
    System.out.println("Compression ratio: " + String.format("%.1f", compressionRatio) + "%");
    System.out.println("Color palette: " + originalColors + " → " + quantizedColors);
  }

  private static void decompress(String inputPath, String outputPath) throws IOException {
    System.out.println("Decompressing: " + inputPath);
    System.out.println("Output: " + outputPath);

    // Read .rooster binary format
    try (DataInputStream in = new DataInputStream(
        new BufferedInputStream(new FileInputStream(inputPath)))) {

      // Read header
      int magic = in.readInt();
      if (magic != MAGIC_NUMBER) {
        throw new IOException("Invalid .rooster file: bad magic number");
      }

      byte version = in.readByte();
      if (version != VERSION) {
        throw new IOException("Unsupported .rooster version: " + version);
      }

      int bits = in.readByte() & 0xFF;
      int width = in.readInt();
      int height = in.readInt();

      System.out.println("Image: " + width + "x" + height + ", " + bits + " bits per channel");

      // Create output image
      BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

      // Read pixel data
      BitReader bitReader = new BitReader(in);
      int leftShift = 8 - bits;

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          // Read exactly 'bits' bits for each channel
          int red = bitReader.readBits(bits);
          int green = bitReader.readBits(bits);
          int blue = bitReader.readBits(bits);

          // Expand back to 8 bits by shifting left
          red <<= leftShift;
          green <<= leftShift;
          blue <<= leftShift;

          int rgb = 0xFF000000 | (red << 16) | (green << 8) | blue;
          output.setRGB(x, y, rgb);
        }
      }

      // Write output image
      String format = getFormat(outputPath);
      boolean success = ImageIO.write(output, format, new File(outputPath));

      if (!success) {
        throw new IOException("Failed to write output file");
      }

      System.out.println("Done! Restored to " + width + "x" + height + " " + format.toUpperCase());
    }
  }

  private static String getFormat(String path) {
    int dotIndex = path.lastIndexOf('.');
    if (dotIndex < 0) {
      return "png";
    }
    return path.substring(dotIndex + 1).toLowerCase();
  }

  private static void printUsage() {
    System.out.println("ColorQuantizer CLI - .rooster format");
    System.out.println();
    System.out.println("Usage:");
    System.out.println("  Compress:   java Main c <input> <output.rooster> [-b <bits>]");
    System.out.println("  Decompress: java Main d <input.rooster> <output>");
    System.out.println();
    System.out.println("Options:");
    System.out.println("  -b <bits>   Number of bits per channel (1-8, default: 4)");
  }

}
