package net.creeperhost.wyml.paperbag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperBagExpiryPolicyTest
{
    @Test
    void parsesConfiguredPoliciesCaseInsensitively()
    {
        assertEquals(PaperBagExpiryPolicy.PERSIST_WHILE_NON_EMPTY,
                PaperBagExpiryPolicy.parse("persist_while_non_empty"));
        assertEquals(PaperBagExpiryPolicy.LEGACY_VOID_WITH_WARNING,
                PaperBagExpiryPolicy.parse("LEGACY_VOID_WITH_WARNING"));
    }

    @Test
    void unknownPolicyPreservesLegacyCompatibility()
    {
        assertEquals(PaperBagExpiryPolicy.LEGACY_VOID_WITH_WARNING,
                PaperBagExpiryPolicy.parse("not-a-policy"));
    }
}
