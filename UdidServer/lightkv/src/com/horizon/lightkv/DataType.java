package com.horizon.lightkv;


public interface DataType {
    int OFFSET = 16;
    int MASK = 0xF0000;
    int ENCODE = 1 << 20;

    int BOOLEAN = 1 << OFFSET;
    int INT = 2 << OFFSET;
    int FLOAT = 3 << OFFSET;
    int LONG = 4 << OFFSET;
    int DOUBLE = 5 << OFFSET;
    int STRING = 6 << OFFSET;
    int ARRAY = 7 << OFFSET;
}
