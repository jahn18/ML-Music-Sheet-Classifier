package com.example.musicscoreapp;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Ground for testing functions without having to actually run the applications on a phone or emulator
 *
 */
public class ExampleUnitTest {
    @Test
    public void regexTest() {
        String testNote = "clef-G2\tclef-G2\tkeySignature-EbM\ttimeSignature-C\trest-half\tnote-G4_eighth\tnote-C5_quarter\t" +
                "note-B4_eighth\tbarline\tnote-C5_thirty_second\tclef-F2\tnote-D5_thirty_second\ttimeSignature-3/1\tkeySignature-14s\tnote-Eb5_sixteenth\t" +
                "note-Eb5_quarter\tnote-D5_eighth\tnote-G5_eighth\tbarline\t";
        String output = MainActivity.failSafe(testNote);
        System.out.println(output);
    }
}
