package com.wokgui.schedulewidget;
import java.nio.file.*;

/** Exports the actual Java-generated runtime, including legacy compatibility repairs. */
public class ExportUi {
    public static void main(String[] args) throws Exception {
        Path out = Path.of(args[0]);
        Files.createDirectories(out);
        String[] chunks = ChunkedUiScripts.all();
        for (int i = 0; i < chunks.length; i++) {
            Files.writeString(out.resolve(String.format("chunk-%02d.js", i)), chunks[i]);
        }
        System.out.println("Exported " + chunks.length + " actual application UI chunks");
    }
}
