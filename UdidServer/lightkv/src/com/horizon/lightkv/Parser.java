package com.horizon.lightkv;



import com.horizon.lightkv.Container.ArrayContainer;
import com.horizon.lightkv.Container.BaseContainer;
import com.horizon.lightkv.Container.BooleanContainer;
import com.horizon.lightkv.Container.DoubleContainer;
import com.horizon.lightkv.Container.FloatContainer;
import com.horizon.lightkv.Container.IntContainer;
import com.horizon.lightkv.Container.LongContainer;
import com.horizon.lightkv.Container.StringContainer;

import java.nio.ByteBuffer;
import java.util.Map;

class Parser {
    static int parseData(Map<Integer, Object> data, ByteBuffer buffer, AsyncKV.Encoder encoder) {
        int invalidBytes = 0;
        while (buffer.remaining() > 4) {
            int key = buffer.getInt();
            if (key == 0) {
                buffer.position(buffer.position() - 4);
                break;
            }
            int offset = buffer.position() - 4;
            boolean valid = (key > 0);
            switch (key & DataType.MASK) {
                case DataType.BOOLEAN:
                    if (valid) {
                        data.put(key, new BooleanContainer(offset, buffer.get() == 1));
                    } else {
                        buffer.position(buffer.position() + 1);
                        invalidBytes += 5;
                    }
                    break;
                case DataType.STRING:
                    int length = buffer.getInt();
                    if (valid) {
                        byte[] bytes = new byte[length];
                        buffer.get(bytes);
                        byte[] value = (key & DataType.ENCODE) != 0 ? encoder.decode(bytes) : bytes;
                        data.put(key, new StringContainer(offset, new String(value), bytes));
                    } else {
                        buffer.position(buffer.position() + length);
                        invalidBytes += 8 + length;
                    }
                    break;
                case DataType.LONG:
                    if (valid) {
                        data.put(key, new LongContainer(offset, buffer.getLong()));
                    } else {
                        buffer.position(buffer.position() + 8);
                        invalidBytes += 12;
                    }
                    break;
                case DataType.INT:
                    if (valid) {
                        data.put(key, new IntContainer(offset, buffer.getInt()));
                    } else {
                        buffer.position(buffer.position() + 4);
                        invalidBytes += 8;
                    }
                    break;
                case DataType.FLOAT:
                    if (valid) {
                        data.put(key, new FloatContainer(offset, buffer.getFloat()));
                    } else {
                        buffer.position(buffer.position() + 4);
                        invalidBytes += 8;
                    }
                    break;
                case DataType.DOUBLE:
                    if (valid) {
                        data.put(key, new DoubleContainer(offset, buffer.getDouble()));
                    } else {
                        buffer.position(buffer.position() + 8);
                        invalidBytes += 12;
                    }
                    break;
                case DataType.ARRAY:
                    int arrayLen = buffer.getInt();
                    if (valid) {
                        byte[] bytes = new byte[arrayLen];
                        buffer.get(bytes);
                        byte[] value = (key & DataType.ENCODE) != 0 ? encoder.decode(bytes) : bytes;
                        data.put(key, new ArrayContainer(offset, value, bytes));
                    } else {
                        buffer.position(buffer.position() + arrayLen);
                        invalidBytes += 8 + arrayLen;
                    }
                    break;
                default:
                    return -1;
            }
        }
        return invalidBytes;
    }

    /**
     * collect data to buffer
     */
    static void collect(Map<Integer, Object> data, ByteBuffer buffer) throws IllegalStateException {
        int offset = 0;
        for(Map.Entry<Integer, Object> entry : data.entrySet()){
            int key = entry.getKey();
            BaseContainer container = (BaseContainer) entry.getValue();
            container.offset = offset;
            buffer.putInt(key);
            switch (key & DataType.MASK) {
                case DataType.BOOLEAN:
                    BooleanContainer booleanContainer = (BooleanContainer) container;
                    buffer.put((byte) (booleanContainer.value ? 1 : 0));
                    break;
                case DataType.STRING:
                    StringContainer stringContainer = (StringContainer) container;
                    buffer.putInt(stringContainer.bytes.length);
                    buffer.put(stringContainer.bytes);
                    break;
                case DataType.LONG:
                    LongContainer longContainer = (LongContainer) container;
                    buffer.putLong(longContainer.value);
                    break;
                case DataType.INT:
                    IntContainer intContainer = (IntContainer) container;
                    buffer.putInt(intContainer.value);
                    break;
                case DataType.FLOAT:
                    FloatContainer floatContainer = (FloatContainer) container;
                    buffer.putFloat(floatContainer.value);
                    break;
                case DataType.DOUBLE:
                    DoubleContainer doubleContainer = (DoubleContainer) container;
                    buffer.putDouble(doubleContainer.value);
                    break;
                case DataType.ARRAY:
                    ArrayContainer arrayContainer = (ArrayContainer) container;
                    buffer.putInt(arrayContainer.bytes.length);
                    buffer.put(arrayContainer.bytes);
                    break;
                default:
                    throw new IllegalStateException("invalid key " + key);
            }
            offset = buffer.position();
        }
    }

    static String toString(Map<Integer, Object> data, String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append(fileName).append("'s data: \n");

        int size = data.size();
        for(Map.Entry<Integer, Object> entry : data.entrySet()){
            int key = entry.getKey();
            sb.append("type:").append(key & DataType.MASK);
            sb.append(" key:").append(key&0xFFFF).append(" value:");
            BaseContainer container = (BaseContainer) entry.getValue();
            switch (key & DataType.MASK) {
                case DataType.BOOLEAN:
                    BooleanContainer booleanContainer = (BooleanContainer) container;
                    sb.append(booleanContainer.value).append('\n');
                    break;
                case DataType.STRING:
                    StringContainer stringContainer = (StringContainer) container;
                    sb.append(stringContainer.value).append('\n');
                    break;
                case DataType.LONG:
                    LongContainer longContainer = (LongContainer) container;
                    sb.append(longContainer.value).append('\n');
                    break;
                case DataType.INT:
                    IntContainer intContainer = (IntContainer) container;
                    sb.append(intContainer.value).append('\n');
                    break;
                case DataType.FLOAT:
                    FloatContainer floatContainer = (FloatContainer) container;
                    sb.append(floatContainer.value).append('\n');
                    break;
                case DataType.DOUBLE:
                    DoubleContainer doubleContainer = (DoubleContainer) container;
                    sb.append(doubleContainer.value).append('\n');
                    break;
                case DataType.ARRAY:
                    ArrayContainer arrayContainer = (ArrayContainer) container;
                    int len = arrayContainer.value == null ? 0 : arrayContainer.value.length;
                    sb.append("length:").append(len).append('\n');
                    break;
                default:
                    break;
            }
        }
        return sb.toString();
    }
}
