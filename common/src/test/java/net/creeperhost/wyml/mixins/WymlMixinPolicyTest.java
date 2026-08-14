package net.creeperhost.wyml.mixins;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WymlMixinPolicyTest
{
    private static final Set<String> ALL_MIXINS = Set.of(
            "AccessorMinecraftServer",
            "MixinEntity",
            "MixinEntityPopulationIndex",
            "MixinItemEntity",
            "MixinItemEntityMergeScheduling",
            "MixinLivingEntity",
            "MixinMinecraftServer",
            "MixinMobCategory",
            "MixinNaturalSpawner",
            "MixinServerWorld",
            "MixinSpawnState",
            "MixinSpawnStateCategoryPolicy",
            "MixinPlayerTabOverlay");

    @Test
    void allModulesOffAppliesNoWymlFeatureMixin()
    {
        for (String mixin : ALL_MIXINS)
        {
            assertFalse(WymlMixinPolicy.shouldApply(mixin, ignored -> false), mixin);
        }
    }

    @Test
    void eachModuleAloneAppliesOnlyItsDeclaredDependencies()
    {
        assertApplied("paper_bags", Set.of("MixinServerWorld"));
        assertApplied("item_lifetime", Set.of("MixinEntity", "MixinItemEntity"));
        assertApplied("item_merging", Set.of("MixinItemEntityMergeScheduling"));
        assertApplied("entity_pushing", Set.of("MixinEntity", "MixinLivingEntity"));
        assertApplied("category_policy", Set.of(
                "MixinMinecraftServer", "MixinMobCategory", "MixinSpawnStateCategoryPolicy"));
        assertApplied("spawn_controller", Set.of(
                "AccessorMinecraftServer", "MixinMinecraftServer", "MixinNaturalSpawner", "MixinSpawnState"));
        assertApplied("per_mob_rules", Set.of(
                "MixinEntityPopulationIndex", "MixinMinecraftServer", "MixinNaturalSpawner", "MixinServerWorld"));
        assertApplied("post_load_gc", Set.of("MixinMinecraftServer"));
        assertApplied("numeric_ping", Set.of("MixinPlayerTabOverlay"));
    }

    @Test
    void sharedTargetsApplyWhenEitherDependentModuleIsEnabled()
    {
        assertTrue(WymlMixinPolicy.shouldApply("MixinEntity", Set.of("item_lifetime")::contains));
        assertTrue(WymlMixinPolicy.shouldApply("MixinEntity", Set.of("entity_pushing")::contains));
        assertTrue(WymlMixinPolicy.shouldApply("MixinServerWorld", Set.of("paper_bags")::contains));
        assertTrue(WymlMixinPolicy.shouldApply("MixinServerWorld", Set.of("per_mob_rules")::contains));
    }

    private static void assertApplied(String module, Set<String> expected)
    {
        Set<String> actual = ALL_MIXINS.stream()
                .filter(mixin -> WymlMixinPolicy.shouldApply(mixin, module::equals))
                .collect(java.util.stream.Collectors.toSet());
        assertEquals(expected, actual, module);
    }
}
