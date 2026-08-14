package net.creeperhost.wyml.mixins;

import java.util.function.Predicate;

/** Pure restart-only module-to-mixin dependency map. */
public final class WymlMixinPolicy
{
    private WymlMixinPolicy()
    {
    }

    public static boolean shouldApply(String mixin, Predicate<String> enabled)
    {
        return switch (mixin)
        {
            case "MixinEntity" -> any(enabled, "item_lifetime", "entity_pushing");
            case "MixinEntityPopulationIndex" -> enabled.test("per_mob_rules");
            case "MixinItemEntity" -> enabled.test("item_lifetime");
            case "MixinItemEntityMergeScheduling" -> enabled.test("item_merging");
            case "MixinLivingEntity" -> enabled.test("entity_pushing");
            case "MixinMobCategory" -> enabled.test("category_policy");
            case "MixinNaturalSpawner" -> any(enabled, "spawn_controller", "per_mob_rules");
            case "AccessorMinecraftServer" -> enabled.test("spawn_controller");
            case "MixinSpawnState" -> enabled.test("spawn_controller");
            case "MixinSpawnStateCategoryPolicy" -> enabled.test("category_policy");
            case "MixinServerWorld" -> any(enabled, "paper_bags", "per_mob_rules");
            case "MixinMinecraftServer" -> any(
                    enabled, "spawn_controller", "category_policy", "per_mob_rules", "post_load_gc");
            case "MixinPlayerTabOverlay" -> enabled.test("numeric_ping");
            default -> true;
        };
    }

    private static boolean any(Predicate<String> enabled, String... modules)
    {
        for (String module : modules)
        {
            if (enabled.test(module)) return true;
        }
        return false;
    }
}
