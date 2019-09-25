/*
 * Copyright (C) 2018 Horizon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.horizon.lightkv;

import com.horizon.lightkv.Container.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * LightKV is a Lightweight key-value storage component. <br/>
 * <br/>
 * LightKV has two modes: SyncKV & AsyncKV <br/>
 * SyncKV is more reliable, but it will block until data flush to disk after committing.,<br/>
 * AsyncKV is not atomicity(no blocking when writing data), but more faster. <br/>
 *
 */
public abstract class LightKV {
    private static final String TAG = "LightKV";

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * container size, index ref to {@link DataType}
     */
    private static final int[] CONTAINER_SIZE = {0, 5, 8, 8, 12, 12};

    static final int PAGE_SIZE = 4096;

    public static final int ASYNC_MODE = 0;
    public static final int SYNC_MODE = 1;
    public final int mMode;

    final String mFileName;
    final LightKV.Logger mLogger;
    final LightKV.Encoder mEncoder;
    final Map<Integer, Object> mData = new LinkedHashMap<>(16);
    int mDataEnd;
    private final Object mWaiter = new Object();

    LightKV(final String path,
            final String name,
            Executor executor,
            LightKV.Logger logger,
            LightKV.Encoder encoder,
            int mode) {
        mFileName = name;
        mLogger = logger;
        mEncoder = encoder;
        mMode = mode;

        if (executor == null) {
            getData(path);
        } else {
            synchronized (mWaiter) {
                executor.execute(() -> getData(path));
                try {
                    // wait util loadData() get the object (current object) lock
                    mWaiter.wait();
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    protected abstract ByteBuffer loadData(String path) throws IOException;

    private synchronized void getData(String path) {
        // we got the object lock, notify waiter to continue the procedure on that thread
        synchronized (mWaiter) {
            mWaiter.notify();
        }

        try {
            ByteBuffer buffer = loadData(path);
            int invalidBytes;
            try {
                invalidBytes = Parser.parseData(mData, buffer, mEncoder);
            } catch (Exception e) {
                invalidBytes = -1;
                if (mLogger != null) {
                    mLogger.e(TAG, e);
                }
            }

            clean(invalidBytes);
        } catch (Exception e) {
            if (mLogger != null) {
                mLogger.e(TAG, e);
            }
            throw new IllegalStateException("init " + mFileName + " failed", e);
        }
    }

    protected void clean(int invalidBytes) throws IOException {
        // declare for AsyncKV
    }

    int getContainerLength(int key, BaseContainer container) {
        if (container != null) {
            int type = key & DataType.MASK;
            if (type <= DataType.DOUBLE) {
                return CONTAINER_SIZE[type >> DataType.OFFSET];
            } else if (type == DataType.STRING) {
                return 8 + ((StringContainer) container).bytes.length;
            } else if (type == DataType.ARRAY) {
                return 8 + ((ArrayContainer) container).bytes.length;
            }
        }
        return 0;
    }

    static long alignLength(long len) {
        if (len <= 0) {
            return PAGE_SIZE;
        } else if ((len & 0xFFF) != 0) {
            return ((len + PAGE_SIZE) >> 12) << 12;
        }
        return len;
    }

    public synchronized boolean getBoolean(int key) {
        BooleanContainer container = (BooleanContainer) mData.get(key);
        return container != null && container.value;
    }

    public synchronized int getInt(int key) {
        IntContainer container = (IntContainer) mData.get(key);
        return container == null ? 0 : container.value;
    }

    public synchronized float getFloat(int key) {
        FloatContainer container = (FloatContainer) mData.get(key);
        return container == null ? 0F : container.value;
    }

    public synchronized long getLong(int key) {
        LongContainer container = (LongContainer) mData.get(key);
        return container == null ? 0L : container.value;
    }

    public synchronized double getDouble(int key) {
        DoubleContainer container = (DoubleContainer) mData.get(key);
        return container == null ? 0D : container.value;
    }

    public synchronized String getString(int key) {
        StringContainer container = (StringContainer) mData.get(key);
        return container == null ? "" : container.value;
    }

    public synchronized byte[] getArray(int key) {
        ArrayContainer container = (ArrayContainer) mData.get(key);
        return container == null ? EMPTY_BYTE_ARRAY : container.value;
    }

    public abstract void putBoolean(int key, boolean value);

    public abstract void putInt(int key, int value);

    public abstract void putFloat(int key, float value);

    public abstract void putLong(int key, long value);

    public abstract void putDouble(int key, double value);

    public abstract void putString(int key, String value);

    public abstract void putArray(int key, byte[] value);

    public abstract void remove(int key);

    public abstract void clear();

    /**
     * copy from src to current
     */
    public abstract void copy(LightKV src);

    public abstract void commit();

    public synchronized boolean contains(int key) {
        return mData.containsKey(key);
    }

    @Override
    public synchronized String toString() {
        return Parser.toString(mData, mFileName);
    }

    public interface Logger {
        void e(String tag, Throwable e);
    }

    public interface Encoder {
        byte[] encode(byte[] src);

        byte[] decode(byte[] des);
    }

    public static class Builder {
        private String path;
        private String name;
        private Executor executor;
        private Logger logger;
        private Encoder encoder;

        /**
         * @param path file path (directory and name) of file to save data
         */
        public Builder(String path, String name) {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("path is empty");
            }
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("name is empty");
            }
            this.path = path;
            this.name = name;
        }

        /**
         * If not set, AsyncKV will load data in current thread
         *
         * @param executor executor to provider thread to load data asynchronously
         */
        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        /**
         * @param logger to log exceptions
         */
        public Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }


        public Builder encoder(Encoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public AsyncKV async() {
            return new AsyncKV(path, name,  executor, logger, encoder);
        }

        public SyncKV sync() {
            return new SyncKV(path, name, executor, logger, encoder);
        }
    }
}
