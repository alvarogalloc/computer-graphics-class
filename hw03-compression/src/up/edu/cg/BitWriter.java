package up.edu.cg;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Helper class for writing individual bits to a stream.
 */
public class BitWriter {
  private final DataOutputStream out;
  private int buffer;
  private int bitsInBuffer;

  public BitWriter(DataOutputStream out) {
    this.out = out;
    this.buffer = 0;
    this.bitsInBuffer = 0;
  }

  public void writeBits(int value, int numBits) throws IOException {
    buffer = (buffer << numBits) | (value & ((1 << numBits) - 1));
    bitsInBuffer += numBits;

    while (bitsInBuffer >= 8) {
      bitsInBuffer -= 8;
      out.writeByte((buffer >> bitsInBuffer) & 0xFF);
    }
  }

  public void flush() throws IOException {
    if (bitsInBuffer > 0) {
      out.writeByte((buffer << (8 - bitsInBuffer)) & 0xFF);
      buffer = 0;
      bitsInBuffer = 0;
    }
  }
}
