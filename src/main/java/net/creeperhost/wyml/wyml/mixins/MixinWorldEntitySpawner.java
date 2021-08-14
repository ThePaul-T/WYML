package net.creeperhost.wyml.wyml.mixins;

import net.creeperhost.wyml.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.wyml.WymlConfig;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(WorldEntitySpawner.class)
public abstract class MixinWorldEntitySpawner
{
    @Shadow protected static BlockPos getRandomPosWithin(World p_222262_0_, Chunk p_222262_1_) { return null; }
    @Shadow public static void spawnCategoryForPosition(EntityClassification p_234966_0_, ServerWorld p_234966_1_, IChunk p_234966_2_, BlockPos p_234966_3_, WorldEntitySpawner.IDensityCheck p_234966_4_, WorldEntitySpawner.IOnSpawnDensityAdder p_234966_5_) {}

    @Inject(at = @At("HEAD"), method = "spawnCategoryForChunk", cancellable = true)
    private static void spawnForChunk(EntityClassification entityClassification, ServerWorld serverWorld, Chunk chunk, WorldEntitySpawner.IDensityCheck densityCheck, WorldEntitySpawner.IOnSpawnDensityAdder iOnSpawnDensityAdder, CallbackInfo ci)
    {
        String id = chunk.getPos() + entityClassification.getName();

        if(WhyYouMakeLag.ticks > (WhyYouMakeLag.lastTick2 + WymlConfig.FAIL_DELAY.get()) || WhyYouMakeLag.lastTick2 > WhyYouMakeLag.ticks)
        {
            WhyYouMakeLag.lastTick2 = WhyYouMakeLag.ticks;
            System.out.println("Fail delay cleanup");
            if(WhyYouMakeLag.FAIL_COUNT.get().containsKey(id) && WhyYouMakeLag.FAIL_COUNT.get().get(id) >= WymlConfig.FAIL_COUNT.get())
            {
                WhyYouMakeLag.FAIL_COUNT.getAndUpdate((thing) -> {
                    thing.put(id, 1);
                    return thing;
                });
            }
        }

        if(WhyYouMakeLag.FAIL_COUNT.get().containsKey(id) && WhyYouMakeLag.FAIL_COUNT.get().get(id) >= WymlConfig.FAIL_COUNT.get())
        {
//            System.out.println("Canceling spawn");
            ci.cancel();
        }

        if(WhyYouMakeLag.mobCategoryCounts != null && WhyYouMakeLag.spawnableChunkCount.containsKey(entityClassification))
        {
//            if(!WhyYouMakeLag.ITWORKS)
//            {
//                System.out.println("IT WORKS!!!!");
//                WhyYouMakeLag.ITWORKS = true;
//            }
            int count = WhyYouMakeLag.calculateSpawnCount(entityClassification, WhyYouMakeLag.mobCategoryCounts, WhyYouMakeLag.spawnableChunkCount.get(entityClassification));
            int tries = WymlConfig.MOB_TRIES.get();
            if(WymlConfig.MULTIPLY_BY_PLAYERS.get()) tries = (tries * WhyYouMakeLag.minecraftServer.getPlayerList().getPlayerCount());
            if(count < tries || WhyYouMakeLag.ticks < 900)
            {
//                System.out.println("Fast spawn enabled");

                BlockPos blockpos = getRandomPosWithin(serverWorld, chunk);
                if (blockpos.getY() >= 1)
                {
                     WhyYouMakeLag.FAIL_COUNT.getAndUpdate((thing) -> {
                        if(thing.containsKey(id)) thing.put(id, thing.get(id)+1);
                        else thing.put(id, 1);
                        return thing;
                    });
//                    System.out.println("FailCount " + id + " " + i);
                    spawnCategoryForPosition(entityClassification, serverWorld, chunk, blockpos, densityCheck, iOnSpawnDensityAdder);
                }
            }
            else
            {
                if(!WhyYouMakeLag.ITWORKS)
                {
                    System.out.println("Switching to slowmode");
                    WhyYouMakeLag.ITWORKS = true;
                }
                if(WhyYouMakeLag.ticks != WhyYouMakeLag.lastTick)
                {
                    WhyYouMakeLag.lastTick = WhyYouMakeLag.ticks;
                    WhyYouMakeLag.mobsInTick = 0;
                }
                if(WhyYouMakeLag.ticks == WhyYouMakeLag.lastTick)
                {
                    if (WhyYouMakeLag.mobsInTick < tries)
                    {
                        BlockPos blockpos = getRandomPosWithin(serverWorld, chunk);
                        if (blockpos.getY() >= 1)
                        {
                            int i = 0;
                            if (WhyYouMakeLag.FAIL_COUNT.get().containsKey(id)) i = WhyYouMakeLag.FAIL_COUNT.get().get(id);
                            WhyYouMakeLag.FAIL_COUNT.getAndUpdate((thing) -> {
                                if(thing.containsKey(id)) thing.put(id, thing.get(id)+1);
                                else thing.put(id, 1);
                                return thing;
                            });
                            spawnCategoryForPosition(entityClassification, serverWorld, chunk, blockpos, densityCheck, iOnSpawnDensityAdder);
                        }
                    }
                }
            }
        }

        ci.cancel();
    }
}
