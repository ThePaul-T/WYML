package net.creeperhost.wyml.paperbag;

import java.util.Locale;

public enum PaperBagExpiryPolicy
{
    LEGACY_VOID_WITH_WARNING,
    PERSIST_WHILE_NON_EMPTY;

    public static PaperBagExpiryPolicy parse(String configured)
    {
        if (configured == null) return LEGACY_VOID_WITH_WARNING;
        try
        {
            return valueOf(configured.trim().toUpperCase(Locale.ROOT));
        }
        catch (IllegalArgumentException ignored)
        {
            return LEGACY_VOID_WITH_WARNING;
        }
    }
}
