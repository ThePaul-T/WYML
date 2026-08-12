package net.creeperhost.wyml.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class AtomicConfigWriter
{
    private AtomicConfigWriter()
    {
    }

    static void write(Path destination, String contents) throws IOException
    {
        Path parent = destination.toAbsolutePath().getParent();
        if (parent == null)
        {
            throw new IOException("Config destination has no parent: " + destination);
        }

        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, destination.getFileName().toString(), ".tmp");
        try
        {
            Files.writeString(temporary, contents, StandardCharsets.UTF_8);
            try
            {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (AtomicMoveNotSupportedException ignored)
            {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        finally
        {
            Files.deleteIfExists(temporary);
        }
    }
}
