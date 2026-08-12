package net.creeperhost.wyml.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AtomicConfigWriterTest
{
    @TempDir
    Path temporaryDirectory;

    @Test
    void publishesUtf8AndReplacesExistingContentWithoutTemporaryFiles() throws IOException
    {
        Path destination = temporaryDirectory.resolve("provider.json");

        AtomicConfigWriter.write(destination, "{\"name\":\"first\"}");
        AtomicConfigWriter.write(destination, "{\"name\":\"møb\"}");

        assertEquals("{\"name\":\"møb\"}", Files.readString(destination));
        try (var files = Files.list(temporaryDirectory))
        {
            assertEquals(1L, files.count());
        }
    }
}
