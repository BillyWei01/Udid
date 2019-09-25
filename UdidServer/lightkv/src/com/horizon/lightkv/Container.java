package com.horizon.lightkv;


class Container {
    static class BaseContainer {
        int offset;
    }

    static class BooleanContainer extends BaseContainer {
        boolean value;

        BooleanContainer(int offset, boolean value) {
            this.offset = offset;
            this.value = value;
        }
    }

    static class IntContainer extends BaseContainer {
        int value;

        IntContainer(int offset, int value) {
            this.offset = offset;
            this.value = value;
        }
    }

    static class FloatContainer extends BaseContainer {
        float value;

        FloatContainer(int offset, float value) {
            this.offset = offset;
            this.value = value;
        }
    }

    static class LongContainer extends BaseContainer {
        long value;

        LongContainer(int offset, long value) {
            this.offset = offset;
            this.value = value;
        }
    }

    static class DoubleContainer extends BaseContainer {
        double value;

        DoubleContainer(int offset, double value) {
            this.offset = offset;
            this.value = value;
        }
    }

    static class StringContainer extends BaseContainer {
        String value;
        byte[] bytes;

        StringContainer(int offset, String value, byte[] bytes) {
            this.offset = offset;
            this.value = value;
            this.bytes = bytes;
        }
    }

    static class ArrayContainer extends BaseContainer {
        byte[] value;
        byte[] bytes;

        ArrayContainer(int offset, byte[] value, byte[] bytes) {
            this.offset = offset;
            this.value = value;
            this.bytes = bytes;
        }
    }
}

