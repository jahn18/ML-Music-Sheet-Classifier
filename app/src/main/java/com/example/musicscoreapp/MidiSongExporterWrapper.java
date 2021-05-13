package com.example.musicscoreapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

import es.ua.dlsi.im3.core.io.ExportException;
import es.ua.dlsi.im3.core.played.Key;
import es.ua.dlsi.im3.core.played.Meter;
import es.ua.dlsi.im3.core.played.PlayedNote;
import es.ua.dlsi.im3.core.played.PlayedSong;
import es.ua.dlsi.im3.core.played.SongTrack;
import es.ua.dlsi.im3.core.played.Tempo;
import es.ua.dlsi.im3.core.played.io.MidiSongExporter;

public class MidiSongExporterWrapper {
    private static final String ZERO = "0";
    private static final String PREFIX_HEXA = "0x";
    private static final String EMPTY = "";
    private static final double LOG2 = Math.log(2.0D);
    private boolean resetEWSCWordBuilderMessage;

    public MidiSongExporterWrapper() {

    }

    public static byte parseByte(String hex) throws NumberFormatException {
        if (hex == null) {
            throw new IllegalArgumentException("Null string in hexadecimal notation.");
        } else if (hex.equals("")) {
            return 0;
        } else {
            Integer num = Integer.decode("0x" + hex);
            int n = num;
            if (n <= 255 && n >= 0) {
                return num.byteValue();
            } else {
                throw new NumberFormatException("Out of range for byte.");
            }
        }
    }

    public static byte[] parseSeq(String str) throws NumberFormatException {
        if (str != null && !str.equals("")) {
            int len = str.length();
            if (len % 2 != 0) {
                str = "0" + str;
                ++len;
            }

            int numOfOctets = len / 2;
            byte[] seq = new byte[numOfOctets];

            for(int i = 0; i < numOfOctets; ++i) {
                String hex = str.substring(i * 2, i * 2 + 2);
                seq[i] = parseByte(hex);
            }

            return seq;
        } else {
            return null;
        }
    }

    public static byte[] parseInt(int i) {
        return parseSeq(Integer.toHexString(i));
    }

    public static byte[] parseLong(long i) {
        return parseSeq(Long.toHexString(i));
    }

    private void putTimeSignatureChange(Track track, Meter meter, long tick) throws InvalidMidiDataException {
        MetaMessage mm = new MetaMessage();
        int den = (int)(Math.log((double)meter.getDenominator()) / LOG2);
        byte[] msg = new byte[]{(byte)meter.getNumerator(), (byte)den, 24, 8};
        mm.setMessage(88, msg, msg.length);
        MidiEvent e = new MidiEvent(mm, tick);
        track.add(e);
    }

    private void putTrackName(Track track, String trackName) throws InvalidMidiDataException {
        MetaMessage mm = new MetaMessage();
        byte[] msg = trackName.getBytes();
        mm.setMessage(3, msg, msg.length);
        MidiEvent e = new MidiEvent(mm, 0L);
        track.add(e);
    }

    private void putKeySignature(Track track, Key element, long tick) throws InvalidMidiDataException {
        MetaMessage mm = new MetaMessage();
        int mode = element.getMode() == Key.Mode.MAJOR ? 0 : 1;
        int sf = element.getFifths();
        byte[] msg = new byte[]{(byte)sf, (byte)mode};
        mm.setMessage(89, msg, msg.length);
        MidiEvent e = new MidiEvent(mm, tick);
        track.add(e);
    }

    private void putLyric(Track track, String lyric, long time) throws InvalidMidiDataException {
        MetaMessage mm = new MetaMessage();
        byte[] msg = lyric.getBytes();
        mm.setMessage(5, msg, msg.length);
        MidiEvent e = new MidiEvent(mm, time);
        track.add(e);
    }

    private void putTempoChange(Track track, Tempo tempo, long tick) throws InvalidMidiDataException {
        byte[] btempo = parseLong(60000000L / (long)tempo.getTempo());
        byte[] bytes = new byte[3];

        int b;
        for(b = 0; b < 3 - btempo.length; ++b) {
            bytes[b] = 0;
        }

        for(int bb = 0; b < 3; ++b) {
            bytes[b] = btempo[bb];
            ++bb;
        }

        byte[] msg = new byte[]{bytes[0], bytes[1], bytes[2]};
        MetaMessage mm = new MetaMessage();
        mm.setMessage(81, msg, msg.length);
        MidiEvent e = new MidiEvent(mm, tick);
        track.add(e);
    }

    public void addResetEWSCWordBuilderMessage() {
        this.resetEWSCWordBuilderMessage = true;
    }

    public void exportSongHook(FileOutputStream file, PlayedSong playedSong) throws ExportException {
        MidiSongExporter actualMidiExporter = new MidiSongExporter();


        try {
            Sequence sequence = new Sequence(0.0F, playedSong.getResolution());
            int i = -1;
            Iterator var5 = playedSong.getTracks().iterator();

            while(var5.hasNext()) {
                SongTrack part = (SongTrack)var5.next();
                Track track = sequence.createTrack();
                if (this.resetEWSCWordBuilderMessage) {
                    ShortMessage resetWBMessage = new ShortMessage(176, 21, 127);
                    MidiEvent event = new MidiEvent(resetWBMessage, 0L);
                    track.add(event);
                }

                ++i;
                if (i == 0) {
                    Iterator var19 = playedSong.getMeters().iterator();

                    while(var19.hasNext()) {
                        Meter ts = (Meter)var19.next();
                        this.putTimeSignatureChange(track, ts, ts.getTime());
                    }

                    var19 = playedSong.getKeys().iterator();

                    while(var19.hasNext()) {
                        Key ks = (Key)var19.next();
                        this.putKeySignature(track, ks, ks.getTime());
                    }

                    var19 = playedSong.getTempoChanges().iterator();

                    while(var19.hasNext()) {
                        Tempo tp = (Tempo)var19.next();
                        this.putTempoChange(track, tp, tp.getTime());
                    }
                }

                int defaultChannel;
                if (part.isDefaultMidiChannelSet()) {
                    defaultChannel = part.getDefaultMidiChannel() - 1;
                } else {
                    defaultChannel = i;
                }

                String trackName = part.getName();
                if (trackName != null) {
                    this.putTrackName(track, trackName);
                }

                Iterator var10 = part.getPlayedNotes().iterator();

                while(var10.hasNext()) {
                    PlayedNote note = (PlayedNote)var10.next();
                    int channel = note.getMidiChannel();
                    if (channel == -1) {
                        channel = defaultChannel;
                    }

                    ShortMessage messageON = new ShortMessage();
                    int vel = note.getVelocity();
                    messageON.setMessage(144, channel, note.getMidiPitch(), vel == 0 ? 127 : vel);
                    track.add(new MidiEvent(messageON, note.getTime()));
                    ShortMessage messageOFF = new ShortMessage();
                    messageOFF.setMessage(128, channel, note.getMidiPitch(), 0);
                    track.add(new MidiEvent(messageOFF, note.getTime() + note.getDurationInTicks()));
                    String lyrics = note.getText();
                    if (lyrics != null) {
                        this.putLyric(track, lyrics, note.getTime());
                    }
                }
            }

            if (MidiSystem.getMidiFileTypes(sequence).length == 0) {
                throw new ExportException("No MIDI file types supported");
            } else {
                MidiSystem.write(sequence, MidiSystem.getMidiFileTypes(sequence)[0], file);
            }
        } catch (InvalidMidiDataException var17) {
            var17.printStackTrace();
            throw new ExportException(var17);
        } catch (IOException var18) {
            throw new ExportException(var18);
        }
    }
}
