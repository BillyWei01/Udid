package utils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class AutoExtendByteBuffer {
    private ByteBuffer mBuffer;

    public AutoExtendByteBuffer(int initSize) {
        mBuffer = ByteBuffer.allocate(Math.max(initSize, 16));
    }

    private void ensureSize(int allocate) {
        final int position = mBuffer.position();
        int capacity = mBuffer.capacity();
        int newPosition = allocate + position;
        if (newPosition <= capacity) {
            return;
        }

        while (newPosition > capacity) {
            capacity <<= 1;
        }

        ByteBuffer newBuffer = ByteBuffer.allocate(capacity);
        mBuffer.flip();
        newBuffer.put(mBuffer);
        mBuffer = newBuffer;
    }

    public AutoExtendByteBuffer put(byte value) {
        ensureSize(1);
        mBuffer.put(value);
        return this;
    }

    public AutoExtendByteBuffer putInt(int value) {
        ensureSize(4);
        mBuffer.putInt(value);
        return this;
    }

    public AutoExtendByteBuffer putFloat(float value) {
        ensureSize(4);
        mBuffer.putFloat(value);
        return this;
    }

    public AutoExtendByteBuffer putLong(long value) {
        ensureSize(8);
        mBuffer.putLong(value);
        return this;
    }

    public AutoExtendByteBuffer putString(String value) {
        if (value != null && !value.isEmpty()) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            ensureSize(bytes.length);
            mBuffer.put(bytes);
        }
        return this;
    }

//    public final byte[] array(){
//        return mBuffer.array();
//    }
//
//    public final int position(){
//        return mBuffer.position();
//    }


    public long getLongHash() {
        return MHash.hash64(mBuffer.array(), mBuffer.position());
    }

}
