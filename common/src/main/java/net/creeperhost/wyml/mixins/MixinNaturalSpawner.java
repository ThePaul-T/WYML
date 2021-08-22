package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.WYMLReimplementedHooks;
import net.creeperhost.wyml.WYMLSpawnManager;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.WymlConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;


@Mixin(NaturalSpawner.class)
public abstract class MixinNaturalSpawner
{
    @Shadow @Final private static Logger LOGGER;
    @Mutable @Shadow private static int MAGIC_NUMBER;
    @Invoker("isValidPositionForMob") private static boolean isValidPositionForMob(ServerLevel serverLevel, Mob mob, double d) {return false;}
    @Invoker("getMobForSpawn") @Nullable private static Mob getMobForSpawn(ServerLevel serverLevel, EntityType<?> entityType) {return null;}
    @Invoker("isValidSpawnPostitionForType") private static boolean isValidSpawnPostitionForType(ServerLevel serverLevel, MobCategory mobCategory, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, MobSpawnSettings.SpawnerData spawnerData, BlockPos.MutableBlockPos mutableBlockPos, double d) {return false;}
    @Invoker("getRandomSpawnMobAt") @Nullable private static MobSpawnSettings.SpawnerData getRandomSpawnMobAt(ServerLevel serverLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, Random random, BlockPos blockPos) {return null;}
    @Invoker("isRightDistanceToPlayerAndSpawnPoint") private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos.MutableBlockPos mutableBlockPos, double d) { return false; };

    /**
     * @author ThePaul_T - 2021
     * @reason Cause Mojang code is questionable, would've injected but 100% sure I am rewriting this whole function soon(tm) - Sorry not sorry.
     */

    @Inject(at = @At("HEAD"), method = "spawnCategoryForPosition", cancellable = true)
    private static void spawnBlah(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback, CallbackInfo ci)
    {
        spawnCategoryForPosition1(mobCategory, serverLevel, chunkAccess, blockPos, spawnPredicate, afterSpawnCallback);
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "isSpawnPositionOk", cancellable = true)
    private static void isSpawnPosition(SpawnPlacements.Type type, LevelReader levelReader, BlockPos blockPos, EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir)
    {
        if(blockPos != null) {
            if(entityType.getCategory() != null) {
                ChunkPos chuck = new ChunkPos(blockPos);
                WYMLSpawnManager spawnManager = WhyYouMakeLag.getSpawnManager(chuck, entityType.getCategory());
                if (spawnManager != null) {
                    if (spawnManager.isKnownBadLocation(blockPos)) {
                        cir.setReturnValue(false);
                        cir.cancel();
                        return;
                    }
                }
            }
        }
    }

    private static void spawnCategoryForPosition1(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback)
    {
        StructureFeatureManager structureFeatureManager = serverLevel.structureFeatureManager();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        int slowTicks = WymlConfig.cached().SLOW_TICKS;
        int i = blockPos.getY();
        int MAGIC_NUMBER_2_ELECTRIC_BOOGALOO = ((int) (WhyYouMakeLag.getMagicNum() * WhyYouMakeLag.getMagicNum()));
        if(MAGIC_NUMBER != MAGIC_NUMBER_2_ELECTRIC_BOOGALOO)
        {
            //Keep this up to date if scaling is enabled.
            MAGIC_NUMBER = MAGIC_NUMBER_2_ELECTRIC_BOOGALOO;
        }
        WYMLSpawnManager spawnManager = WhyYouMakeLag.getSpawnManager(chunkAccess.getPos(), mobCategory);
        if(spawnManager.isPaused())
        {
            if(!spawnManager.isSaved()) WhyYouMakeLag.updateSpawnManager(spawnManager);
            return;
        }
        if(spawnManager.isSlowMode())
        {
            int tries = WymlConfig.cached().MOB_TRIES;
            if (WymlConfig.cached().MULTIPLY_BY_PLAYERS) tries = (tries * WhyYouMakeLag.minecraftServer.getPlayerList().getPlayerCount());
            if(spawnManager.getSpawnsInSample() > tries)
            {
                return;
            }
            if(spawnManager.getSpawnsInSample() < tries && spawnManager.ticksSinceSlow() > slowTicks)
            {
                spawnManager.fastMode();
                if(WymlConfig.cached().DEBUG_PRINT) System.out.println("Entering fast spawn mode for class "+spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + "["+spawnManager.getFailRate()+"%]");
                WhyYouMakeLag.updateSpawnManager(spawnManager);
            }
        } else {
            int maxSpawnRate = WhyYouMakeLag.calculateSpawnCount(spawnManager.getClassification(), WhyYouMakeLag.mobCategoryCounts, WhyYouMakeLag.spawnableChunkCount.get(spawnManager.getClassification()));
            if(spawnManager.getSpawnsInSample() > maxSpawnRate && WymlConfig.cached().ALLOW_SLOW)
            {
                spawnManager.slowMode();
                if(WymlConfig.cached().DEBUG_PRINT) System.out.println("Entering slow spawn mode for class "+spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + "["+spawnManager.getFailRate()+"%]");
                WhyYouMakeLag.updateSpawnManager(spawnManager);
                return;
            }
        }
        if(
                (spawnManager.getFailRate() > WymlConfig.cached().PAUSE_RATE &&
                spawnManager.getStartRate() > WymlConfig.cached().PAUSE_MIN &&
                spawnManager.ticksSinceSlow() > slowTicks &&
                spawnManager.canPause() &&
                !spawnManager.isClaimed())
                ||
                (spawnManager.getFailRate() > WymlConfig.cached().PAUSE_CLAIMED_RATE &&
                spawnManager.getStartRate() > WymlConfig.cached().PAUSE_MIN &&
                spawnManager.ticksSinceSlow() > slowTicks &&
                spawnManager.canPause() &&
                spawnManager.isClaimed())
        )
        {
            int pauseTicks = (spawnManager.isClaimed()) ? WymlConfig.cached().PAUSE_CLAIMED_TICKS : WymlConfig.cached().PAUSE_TICKS;
            spawnManager.pauseSpawns(pauseTicks);
            int resumeRate = spawnManager.isClaimed() ? WymlConfig.cached().RESUME_CLAIMED_RATE : WymlConfig.cached().RESUME_RATE;
            if(WymlConfig.cached().DEBUG_PRINT) System.out.println("Pausing spawns for "+pauseTicks+" ticks or until "+resumeRate+"% success rate for class "+spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + " due to high failure rate ["+spawnManager.getFailRate()+"%].");
            WhyYouMakeLag.updateSpawnManager(spawnManager);
            return;
        }
        BlockState blockState = chunkAccess.getBlockState(blockPos);
        if (!blockState.isRedstoneConductor(chunkAccess, blockPos)) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            int j = 0;

            for(int k = 0; k < 3; ++k) {
                int l = blockPos.getX();
                int m = blockPos.getZ();
                MobSpawnSettings.SpawnerData spawnerData = null;
                SpawnGroupData spawnGroupData = null;
                int o = Mth.ceil(serverLevel.random.nextFloat() * 4.0F);
                int p = 0;
                int sampleSpawns = spawnManager.getSpawnsInSample();
                int maxAttempts = WymlConfig.cached().MAX_CHUNK_SPAWN_REQ_TICK;
                for(int q = 0; q < o; ++q) {
                    if(sampleSpawns > maxAttempts)
                    {
                        if(WymlConfig.cached().DEBUG_PRINT) LOGGER.debug("Skipping spawn as beyond limits..");
                        continue;
                    }
                    sampleSpawns = spawnManager.getSpawnsInSample();
                    l += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
                    m += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
                    mutableBlockPos.set(l, i, m);
                    if(spawnManager.isKnownBadLocation(mutableBlockPos))
                    {
                        return;
                    }
                    double d = (double)l + 0.5D;
                    double e = (double)m + 0.5D;
                    spawnManager.increaseSpawningCount(mutableBlockPos);
                    WhyYouMakeLag.updateSpawnManager(spawnManager);
                    Player player = serverLevel.getNearestPlayer(d, (double)i, e, -1.0D, false);
                    if (player != null) {
                        double f = player.distanceToSqr(d, (double)i, e);
                        if (isRightDistanceToPlayerAndSpawnPoint(serverLevel, chunkAccess, mutableBlockPos, f)) {
                            if (spawnerData == null) {
                                spawnerData = getRandomSpawnMobAt(serverLevel, structureFeatureManager, chunkGenerator, mobCategory, serverLevel.random, mutableBlockPos);
                                if (spawnerData == null) {
                                    break;
                                }

                                o = spawnerData.minCount + serverLevel.random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
                            }

                            if (isValidSpawnPostitionForType(serverLevel, mobCategory, structureFeatureManager, chunkGenerator, spawnerData, mutableBlockPos, f) && spawnPredicate.test(spawnerData.type, mutableBlockPos, chunkAccess)) {
                                Mob mob = getMobForSpawn(serverLevel, spawnerData.type);
                                if (mob == null) {
                                    return;
                                }

                                mob.moveTo(d, (double)i, e, serverLevel.random.nextFloat() * 360.0F, 0.0F);

                                int canSpawn = WYMLReimplementedHooks.canSpawn(mob, serverLevel, d, i, f, null, MobSpawnType.NATURAL);
                                if (canSpawn != -1 && (canSpawn == 1 || isValidPositionForMob(serverLevel, mob, f))) {
                                    if (!WYMLReimplementedHooks.doSpecialSpawn(mob, serverLevel, (float) d, i, (float) f, null, MobSpawnType.NATURAL)) {
                                        spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawnGroupData, (CompoundTag) null);
                                        ++j;
                                        ++p;
                                        serverLevel.addFreshEntityWithPassengers(mob);
                                        afterSpawnCallback.run(mob, chunkAccess);
                                        if (j >= WYMLReimplementedHooks.getMaxGroupSize(mob)) {
                                            return;
                                        }

                                        if (mob.isMaxGroupSizeReached(p)) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
