package org.zalando.logbook.json;

import java.io.Serializable;
import java.io.Writer;

/**
 * {@link Writer} implementation that outputs to a {@link StringBuilder}.
 * Adapted from Apache commons-io equivalent. 
 * <p>
 * <strong>NOTE:</strong> This implementation, as an alternative to
 * <code>java.io.StringWriter</code>, provides an <i>un-synchronized</i>
 * (i.e. for use in a single thread) implementation for better performance.
 * For safe usage with multiple {@link Thread}s then
 * <code>java.io.StringWriter</code> should be used.
 *
 */
class StringBuilderWriter extends Writer implements Serializable {

    private final StringBuilder builder;

    /**
     * Construct a new {@link StringBuilder} instance with the specified capacity.
     *
     * @param capacity The initial capacity of the underlying {@link StringBuilder}
     */
    public StringBuilderWriter(int capacity) {
        this.builder = new StringBuilder(capacity);
    }

    /**
     * Append a single character to this Writer.
     *
     * @param value The character to append
     * @return This writer instance
     */
    @Override
    public Writer append(char value) {
        builder.append(value);
        return this;
    }

    /**
     * Append a character sequence to this Writer.
     *
     * @param value The character to append
     * @return This writer instance
     */
    @Override
    public Writer append(CharSequence value) {
        builder.append(value);
        return this;
    }

    /**
     * Append a portion of a character sequence to the {@link StringBuilder}.
     *
     * @param value The character to append
     * @param start The index of the first character
     * @param end The index of the last character + 1
     * @return This writer instance
     */
    @Override
    public Writer append(CharSequence value, int start, int end) {
        builder.append(value, start, end);
        return this;
    }

    /**
     * Closing this writer has no effect. 
     */
    @Override
    public void close() {
        // intentionally left blank; closing not necessary
    }

    /**
     * Flushing this writer has no effect. 
     */
    @Override
    public void flush() {
        // intentionally left blank; flushing not necessary
    }


    /**
     * Write a String to the {@link StringBuilder}.
     * 
     * @param value The value to write
     */
    @Override
    public void write(String value) {
        builder.append(value);
    }

    /**
     * Write a portion of a character array to the {@link StringBuilder}.
     *
     * @param value The value to write
     * @param offset The index of the first character
     * @param length The number of characters to write
     */
    @Override
    public void write(char[] value, int offset, int length) {
        builder.append(value, offset, length);
    }

    /**
     * Return the underlying builder.
     *
     * @return The underlying builder
     */
    public StringBuilder getBuilder() {
        return builder;
    }

    /**
     * Returns {@link StringBuilder#toString()}.
     *
     * @return The contents of the String builder.
     */
    @Override
    public String toString() {
        return builder.toString();
    }
}
