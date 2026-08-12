package net.creeperhost.wyml.spawn;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CategoryCapPolicyTest
{
    @Test
    void radiusSeventeenMatchesVanillaStyleUnits()
    {
        assertEquals(70, CategoryCapPolicy.calculate(70, 17 * 17, 17.0D));
    }

    @Test
    void smallerRadiusRaisesTheCapExplicitly()
    {
        assertEquals(316, CategoryCapPolicy.calculate(70, 17 * 17, 8.0D));
    }

    @Test
    void invalidInputsAreBounded()
    {
        assertEquals(0, CategoryCapPolicy.calculate(0, 289, 17.0D));
        assertEquals(0, CategoryCapPolicy.calculate(70, 0, 17.0D));
        assertEquals(20, CategoryCapPolicy.calculate(10, 2, 0.0D));
    }
}
