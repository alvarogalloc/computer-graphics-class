package up.edu.cg;
import java.awt.image.BufferedImage;
import java.awt.Color;

/**
 * Quantizes RGB colors in a BufferedImage by reducing the number of bits per channel.
 * Uses uniform quantization to compress the color space.
 */
public class ColorQuantizer {
    private final int redBits;
    private final int greenBits;
    private final int blueBits;

    // Precomputed masks and shift values for performance
    private final int redMask;
    private final int greenMask;
    private final int blueMask;

    /**
     * Creates a quantizer with specified bits per channel.
     *
     * @param redBits Number of bits for red channel (1-8)
     * @param greenBits Number of bits for green channel (1-8)
     * @param blueBits Number of bits for blue channel (1-8)
     */
    public ColorQuantizer(int redBits, int greenBits, int blueBits) {
        validateBits(redBits, "red");
        validateBits(greenBits, "green");
        validateBits(blueBits, "blue");

        this.redBits = redBits;
        this.greenBits = greenBits;
        this.blueBits = blueBits;

        // Calculate how many bits to discard from each 8-bit channel
        int redShift = 8 - redBits;
        int greenShift = 8 - greenBits;
        int blueShift = 8 - blueBits;

        // Create masks to keep only the high bits
        this.redMask = (0xFF >> redShift) << redShift;
        this.greenMask = (0xFF >> greenShift) << greenShift;
        this.blueMask = (0xFF >> blueShift) << blueShift;
    }

    /**
     * Creates a quantizer with equal bits for all RGB channels.
     *
     * @param bitsPerChannel Number of bits for each RGB channel (1-8)
     */
    public ColorQuantizer(int bitsPerChannel) {
        this(bitsPerChannel, bitsPerChannel, bitsPerChannel);
    }

    private void validateBits(int bits, String channelName) {
        if (bits < 1 || bits > 8) {
            throw new IllegalArgumentException(
                    channelName + " bits must be between 1 and 8, got: " + bits);
        }
    }

    /**
     * Quantizes a single RGB color value.
     *
     * @param rgb Original color in RGB format
     * @return Quantized color in RGB format
     */
    public int quantizeColor(int rgb) {
        int red = (rgb >> 16) & 0xFF;
        int green = (rgb >> 8) & 0xFF;
        int blue = rgb & 0xFF;

        // Apply masks to zero out low bits
        red &= redMask;
        green &= greenMask;
        blue &= blueMask;

        // Reconstruct RGB (preserve original alpha if present)
        return (rgb & 0xFF000000) | (red << 16) | (green << 8) | blue;
    }

    /**
     * Creates a quantized copy of the input image.
     *
     * @param source Source BufferedImage
     * @return New BufferedImage with quantized colors (TYPE_INT_RGB)
     */
    public BufferedImage quantize(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();

        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Process all pixels
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalColor = source.getRGB(x, y);
                int quantizedColor = quantizeColor(originalColor);
                result.setRGB(x, y, quantizedColor);
            }
        }

        return result;
    }

    /**
     * Quantizes the image in-place, modifying the original.
     * More memory efficient than creating a copy.
     *
     * @param image BufferedImage to quantize in-place
     */
    public void quantizeInPlace(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int originalColor = image.getRGB(x, y);
                int quantizedColor = quantizeColor(originalColor);
                image.setRGB(x, y, quantizedColor);
            }
        }
    }

    /**
     * Returns the total number of possible colors after quantization.
     *
     * @return Maximum number of unique colors
     */
    public int getPaletteSize() {
        return (1 << redBits) * (1 << greenBits) * (1 << blueBits);
    }

    /**
     * Returns information about the quantization configuration.
     */
    public String getInfo() {
        return String.format(
                "ColorQuantizer[R:%d G:%d B:%d bits, %d colors]",
                redBits, greenBits, blueBits, getPaletteSize()
        );
    }

    public int getRedBits() { return redBits; }
    public int getGreenBits() { return greenBits; }
    public int getBlueBits() { return blueBits; }
}