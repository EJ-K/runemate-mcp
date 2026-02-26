package com.runemate.mcp.cache.io;

import java.io.*;
import java.util.zip.*;

public record DataBlock(int compression, int revision, byte[] data, int crc) {

//    public static final byte[] BZIP_HEADER = new byte[] { 'B', 'Z', 'h', '1' };
    public static final int COMPRESSION_NONE = 0;
    public static final int COMPRESSION_BZ2 = 1;
    public static final int COMPRESSION_GZ = 2;

    @SuppressWarnings("resource")
    public static DataBlock decompress(byte[] b, int[] keys) throws IOException {
        Js5InputStream stream = new Js5InputStream(b);
        int compression = stream.readUnsignedByte();
        int compressedLength = stream.readInt();
        if (compressedLength < 0) {
            throw new RuntimeException("Invalid data");
        }

        CRC32 crc32 = new CRC32();
        crc32.update(b, 0, 5); // compression + length

        byte[] data;
        switch (compression) {
            case COMPRESSION_NONE: {
                byte[] encryptedData = new byte[compressedLength];
                stream.readBytes(encryptedData, 0, compressedLength);

                crc32.update(encryptedData, 0, compressedLength);
                data = decrypt(encryptedData, encryptedData.length, keys);
                break;
            }
            case COMPRESSION_BZ2: {
                byte[] encryptedData = new byte[compressedLength + 4];
                stream.readBytes(encryptedData);

                crc32.update(encryptedData, 0, encryptedData.length);
                byte[] decryptedData = decrypt(encryptedData, encryptedData.length, keys);

                try (Js5InputStream decryptedStream = new Js5InputStream(decryptedData)) {
                    int decompressedLength = decryptedStream.readInt();
                    data = new byte[decompressedLength];
                    BZip2Decompressor.decompress(data, decompressedLength, decryptedStream.getRemaining(), 0);
                }
                break;
            }
            case COMPRESSION_GZ: {
                byte[] encryptedData = new byte[compressedLength + 4];
                stream.readBytes(encryptedData);

                crc32.update(encryptedData, 0, encryptedData.length);
                byte[] decryptedData = decrypt(encryptedData, encryptedData.length, keys);

                int decompressedLength;
                try (Js5InputStream decryptedStream = new Js5InputStream(decryptedData)) {
                    decompressedLength = decryptedStream.readInt();
                    data = decompressGzip(decryptedStream.getRemaining(), compressedLength);
                    assert data.length == decompressedLength;
                }
                break;
            }
            default:
                throw new RuntimeException("Unknown compression type");
        }

        int revision = -1;
        if (stream.remaining() >= 4) {
            revision = stream.readInt();
        } else if (stream.remaining() >= 2) {
            revision = stream.readUnsignedShort();
        }

        return new DataBlock(compression, revision, data, (int) crc32.getValue());
    }


    private static byte[] decrypt(byte[] data, int length, int[] keys) {
        if (keys == null) {
            return data;
        }

        Xtea xtea = new Xtea(keys);
        return xtea.decrypt(data, length);
    }

//    private static byte[] decompressBZip2(byte[] bytes, int len) throws IOException {
//        byte[] data = new byte[len + BZIP_HEADER.length];
//        System.arraycopy(BZIP_HEADER, 0, data, 0, BZIP_HEADER.length);
//        System.arraycopy(bytes, 0, data, BZIP_HEADER.length, len);
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        try (InputStream is = new BZip2CompressorInputStream(new ByteArrayInputStream(data))) {
//            is.transferTo(os);
//        }
//
//        return os.toByteArray();
//    }

    private static byte[] decompressGzip(byte[] bytes, int len) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (InputStream is = new GZIPInputStream(new ByteArrayInputStream(bytes, 0, len))) {
            is.transferTo(os);
        }
        return os.toByteArray();
    }

    private record Xtea(int[] key) {

        private static final int GOLDEN_RATIO = 0x9E3779B9;
        private static final int ROUNDS = 32;

        public byte[] decrypt(byte[] data, int len) {
            try (
                Js5InputStream in = new Js5InputStream(data); Js5OutputStream out = new Js5OutputStream(len)) {
                int numBlocks = len / 8;
                for (int block = 0; block < numBlocks; ++block) {
                    int v0 = in.readInt();
                    int v1 = in.readInt();
                    //noinspection NumericOverflow
                    int sum = GOLDEN_RATIO * ROUNDS;
                    for (int i = 0; i < ROUNDS; ++i) {
                        v1 -= (((v0 << 4) ^ (v0 >>> 5)) + v0) ^ (sum + key[(sum >>> 11) & 3]);
                        sum -= GOLDEN_RATIO;
                        v0 -= (((v1 << 4) ^ (v1 >>> 5)) + v1) ^ (sum + key[sum & 3]);
                    }
                    out.writeInt(v0);
                    out.writeInt(v1);
                }
                out.writeBytes(in.getRemaining());
                return out.flip();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
