package dev.lightfoot.client.controller;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ByteBufferTest {

    @Test
    void playground() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 16);
        System.out.println(buffer);
        buffer.position(1024);
        System.out.println(buffer);

        System.out.println("\n");

        ByteBuffer firstSlice = buffer.alignedSlice(2048);
        System.out.println("First Slice: " + firstSlice);
        firstSlice.putInt(32);
        System.out.println("First Slice after putInt: " + firstSlice);

        System.out.println("\n");

        System.out.println("Buffer after: " + buffer);

        ByteBuffer secondSlice = buffer.alignedSlice(2048);
        System.out.println("Second Slice: " + secondSlice);
        System.out.println("Buffer after: " + buffer);

        printDirectByteBuffer(buffer);
        printDirectByteBuffer(firstSlice);
        printDirectByteBuffer(secondSlice);
    }

    public static void printDirectByteBuffer(ByteBuffer buffer) {
        // Save the current position
        int originalPosition = buffer.position();

        // Create a byte array to hold the buffer's data
        byte[] byteArray = new byte[buffer.remaining()];

        // Copy the buffer's data into the byte array
        buffer.get(byteArray);

        // Convert the byte array to a string and print
        String content = new String(byteArray);
        System.out.println("Buffer contents: " + content);

        // Restore the original position
        buffer.position(originalPosition);
    }

}
