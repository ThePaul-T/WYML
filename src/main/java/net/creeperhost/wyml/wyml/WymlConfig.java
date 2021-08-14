package net.creeperhost.wyml.wyml;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

import java.nio.file.Path;

public class WymlConfig
{
    public static final String CATEGORY_GENERAL = "general";
    private static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec.ConfigValue<Integer> MOB_TRIES;
    public static ForgeConfigSpec.ConfigValue<Boolean> MULTIPLY_BY_PLAYERS;
    public static ForgeConfigSpec.ConfigValue<Integer> FAIL_COUNT;
    public static ForgeConfigSpec.ConfigValue<Integer> FAIL_DELAY;

    static
    {
        COMMON_BUILDER.comment("General settings").push(CATEGORY_GENERAL);
        COMMON_BUILDER.pop();


        MOB_TRIES = COMMON_BUILDER.comment("Set the max amount of spawn tries")
                .defineInRange("maxSpawns", 1, 1,  100);

        MULTIPLY_BY_PLAYERS = COMMON_BUILDER.comment("Set to true to multiply by player count")
                .define("multiplyByPlayers", true);

        FAIL_COUNT = COMMON_BUILDER.comment("")
                .defineInRange("failCount", 50, 10,  10000);

        FAIL_DELAY = COMMON_BUILDER.comment("")
                .defineInRange("failDelay", 1800, 90,  6000);

        COMMON_CONFIG = COMMON_BUILDER.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path)
    {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        spec.setConfig(configData);
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading configEvent) {}

    @SubscribeEvent
    public static void onReload(final ModConfig.Reloading configEvent) {}
}
