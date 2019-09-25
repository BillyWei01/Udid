package utils;

import java.nio.ByteBuffer;

public class AutoExtendByteBuffer {
    private ByteBuffer mBuffer;

    public AutoExtendByteBuffer(int initSize) {
        mBuffer = ByteBuffer.allocate(initSize > 16 ? initSize : 16);
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

    public void putInt(int value) {
        ensureSize(4);
        mBuffer.putInt(value);
    }

    public void putFloat(float value) {
        ensureSize(4);
        mBuffer.putFloat(value);
    }

    public void putString(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        byte[] bytes = value.getBytes();
        ensureSize(bytes.length);
        mBuffer.put(bytes);
    }

    public final byte[] array(){
        return mBuffer.array();
    }

    public final int position(){
        return mBuffer.position();
    }



    public long getLongHash() {
        return MHash.hash64(mBuffer.array(), mBuffer.position());
    }

}
