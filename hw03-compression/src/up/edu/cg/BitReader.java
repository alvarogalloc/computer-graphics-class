package up.edu.cg;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Helper class for reading individual bits from a stream.
 */
public class BitReader {
  private final DataInputStream in;
  private int buffer;
  private int bitsInBuffer;

  public BitReader(DataInputStream in) {
    this.in = in;
    this.buffer = 0;
    this.bitsInBuffer = 0;
  }

  public int readBits(int numBits) throws IOException {
    while (bitsInBuffer < numBits) {
      int nextByte = in.read();
      if (nextByte == -1) {
        throw new IOException("Unexpected end of file");
      }
      buffer = (buffer << 8) | nextByte;
      bitsInBuffer += 8;
    }

    bitsInBuffer -= numBits;
    int value = (buffer >> bitsInBuffer) & ((1 << numBits) - 1);
    return value;
  }
}
