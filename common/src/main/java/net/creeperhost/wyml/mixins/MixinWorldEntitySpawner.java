package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.WYMLSpawnManager;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.WymlConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;


@Mixin(NaturalSpawner.class)
public abstract class MixinWorldEntitySpawner
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

//    @Overwrite
    private static void spawnCategoryForPosition1(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback) {
        StructureFeatureManager structureFeatureManager = serverLevel.structureFeatureManager();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        int slowTicks = WymlConfig.cached().SLOW_TICKS;
        int i = blockPos.getY();
        int MAGIC_NUMBER_2_ELECTRIC_BOOGALOO = ((int) (WhyYouMakeLag.getMagicNum() * WhyYouMakeLag.getMagicNum()));
        //TODO
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
        if(spawnManager.getFailRate() > WymlConfig.cached().PAUSE_RATE && spawnManager.getStartRate() > WymlConfig.cached().PAUSE_MIN && spawnManager.ticksSinceSlow() > slowTicks && spawnManager.canPause())
        {
            int pauseTicks = WymlConfig.cached().PAUSE_TICKS;
            spawnManager.pauseSpawns(pauseTicks);
            if(WymlConfig.cached().DEBUG_PRINT) System.out.println("Pausing spawns for "+pauseTicks+" ticks or until "+WymlConfig.cached().RESUME_RATE+"% success rate for class "+spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + " due to high failure rate ["+spawnManager.getFailRate()+"%].");
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
                                if (isValidPositionForMob(serverLevel, mob, f)) {
                                    spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawnGroupData, (CompoundTag)null);
                                    ++j;
                                    ++p;
                                    serverLevel.addFreshEntityWithPassengers(mob);
                                    afterSpawnCallback.run(mob, chunkAccess);
                                    WhyYouMakeLag.doForgeStuff(j, mob);
                                    if (j >= mob.getMaxSpawnClusterSize()) {
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
//    @Overwrite
//    public static void spawnCategoryForPosition(MobCategory entityClassification, ServerLevel serverWorld, ChunkAccess chunk, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback) {
//        StructureFeatureManager structuremanager = serverWorld.structureFeatureManager();
//        ChunkGenerator chunkgenerator = serverWorld.getChunkSource().getGenerator();
//        int slowTicks = WymlConfig.instance.SLOW_TICKS.get();
//        int i = blockPos.getY();
//        int MAGIC_NUMBER_2_ELECTRIC_BOOGALOO = ((int) (WhyYouMakeLag.getMagicNum() * WhyYouMakeLag.getMagicNum()));
//        //TODO
////        if(NaturalSpawner.MAGIC_NUMBER != MAGIC_NUMBER_2_ELECTRIC_BOOGALOO)
////        {
////            //Keep this up to date if scaling is enabled.
////            WorldEntitySpawner.MAGIC_NUMBER = MAGIC_NUMBER_2_ELECTRIC_BOOGALOO;
////        }
//        WYMLSpawnManager spawnManager = WhyYouMakeLag.getSpawnManager(chunk.getPos(), entityClassification);
//        if(spawnManager.isPaused())
//        {
//            if(!spawnManager.isSaved()) WhyYouMakeLag.updateSpawnManager(spawnManager);
//            return;
//        }
//        if(spawnManager.isSlowMode())
//        {
//            int tries = WymlConfig.instance.MOB_TRIES.get();
//            if (WymlConfig.instance.MULTIPLY_BY_PLAYERS.get()) tries = (tries * WhyYouMakeLag.minecraftServer.getPlayerList().getPlayerCount());
//            if(spawnManager.getSpawnsInSample() > tries)
//            {
//                return;
//            }
//            if(spawnManager.getSpawnsInSample() < tries && spawnManager.ticksSinceSlow() > slowTicks)
//            {
//                spawnManager.fastMode();
//                if(WymlConfig.instance.DEBUG_PRINT.get()) System.out.println("Entering fast spawn mode for class "+spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + "["+spawnManager.getFailRate()+"%]");
//                WhyYouMakeLag.updateSpawnManager(spawnManager);
//            }
//        } else {
//            int maxSpawnRate = WhyYouMakeLag.calculateSpawnCount(spawnManager.getClassification(), WhyYouMakeLag.mobCategoryCounts, WhyYouMakeLag.spawnableChunkCount.get(spawnManager.getClassification()));
//            if(spawnManager.getSpawnsInSample() > maxSpawnRate && WymlConfig.instance.ALLOW_SLOW.get())
//            {
//                spawnManager.slowMode();
//                if(WymlConfig.instance.DEBUG_PRINT.get()) System.out.println("Entering slow spawn mode for class "+spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + "["+spawnManager.getFailRate()+"%]");
//                WhyYouMakeLag.updateSpawnManager(spawnManager);
//                return;
//            }
//        }
//        if(spawnManager.getFailRate() > WymlConfig.instance.PAUSE_RATE.get() && spawnManager.getStartRate() > WymlConfig.instance.PAUSE_MIN.get() && spawnManager.ticksSinceSlow() > slowTicks && WymlConfig.instance.ALLOW_PAUSE.get())
//        {
//            int pauseTicks = WymlConfig.instance.PAUSE_TICKS.get();
//            spawnManager.pauseSpawns(pauseTicks);
//            if(WymlConfig.instance.DEBUG_PRINT.get()) System.out.println("Pausing spawns for "+pauseTicks+" ticks or until "+WymlConfig.instance.RESUME_RATE.get()+"% success rate for class "+spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + " due to high failure rate ["+spawnManager.getFailRate()+"%].");
//            WhyYouMakeLag.updateSpawnManager(spawnManager);
//            return;
//        }
//        BlockState blockstate = chunk.getBlockState(blockPos);
//        if (!blockstate.isRedstoneConductor(chunk, blockPos)) {
//            BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
//            if(spawnManager.isKnownBadLocation(blockpos$mutable))
//            {
//                return;
//            }
//            int j = 0;
//            int sampleSpawns = spawnManager.getSpawnsInSample();
//            int maxAttempts = WymlConfig.instance.MAX_CHUNK_SPAWN_REQ_TICK.get();
//            for (int k = 0; k < 3; ++k) {
//                if(sampleSpawns > maxAttempts)
//                {
//                    if(WymlConfig.instance.DEBUG_PRINT.get()) LOGGER.debug("Skipping spawn as beyond limits.");
//                    continue;
//                }
//                sampleSpawns = spawnManager.getSpawnsInSample();
//                int l = blockPos.getX();
//                int i1 = blockPos.getZ();
//                int j1 = 6;
//                MobSpawnSettings.SpawnerData mobspawninfo$spawners = null;
//                SpawnGroupData ilivingentitydata = null;
//                int k1 = Mth.ceil(serverWorld.random.nextFloat() * 4.0F);
//                int l1 = 0;
//
//                for (int i2 = 0; i2 < k1; ++i2) {
//                    if(sampleSpawns > maxAttempts)
//                    {
//                        if(WymlConfig.instance.DEBUG_PRINT.get()) LOGGER.debug("Skipping spawn as beyond limits..");
//                        continue;
//                    }
//                    sampleSpawns = spawnManager.getSpawnsInSample();
//                    l += serverWorld.random.nextInt(6) - serverWorld.random.nextInt(6);
//                    i1 += serverWorld.random.nextInt(6) - serverWorld.random.nextInt(6);
//                    blockpos$mutable.set(l, i, i1);
//                    if(spawnManager.isKnownBadLocation(blockpos$mutable))
//                    {
//                        //System.out.println("Known bad block position "+blockpos$mutable+" for "+spawnManager.getClassification().getName()+" in "+spawnManager.getChunk()+" skipped.");
//                        continue;
//                    }
//                    double d0 = (double) l + 0.5D;
//                    double d1 = (double) i1 + 0.5D;
//                    spawnManager.increaseSpawningCount(blockpos$mutable);
//                    WhyYouMakeLag.updateSpawnManager(spawnManager);
//                    Player playerentity = serverWorld.getNearestPlayer(d0, (double) i, d1, -1.0D, false);
//                    if (playerentity != null) {
//                        double d2 = playerentity.distanceToSqr(d0, (double) i, d1);
//                        if (isRightDistanceToPlayerAndSpawnPoint(serverWorld, chunk, blockpos$mutable, d2)) {
//                            if (mobspawninfo$spawners == null) {
//                                mobspawninfo$spawners = getRandomSpawnMobAt(serverWorld, structuremanager, chunkgenerator, entityClassification, serverWorld.random, blockpos$mutable);
//                                if (mobspawninfo$spawners == null) {
//                                    break;
//                                }
//
//                                k1 = mobspawninfo$spawners.minCount + serverWorld.random.nextInt(1 + mobspawninfo$spawners.maxCount - mobspawninfo$spawners.minCount);
//                            }
//
//                            if (isValidSpawnPostitionForType(serverWorld, entityClassification, structuremanager, chunkgenerator, mobspawninfo$spawners, blockpos$mutable, d2) && spawnPredicate.test(mobspawninfo$spawners.type, blockpos$mutable, chunk)) {
//                                Mob mobentity = getMobForSpawn(serverWorld, mobspawninfo$spawners.type);
//                                if (mobentity == null) {
//                                    return;
//                                }
//
//                                mobentity.moveTo(d0, (double) i, d1, serverWorld.random.nextFloat() * 360.0F, 0.0F);
//                                //TODO this is now a forge problem :P
////                                int canSpawn = net.minecraftforge.common.ForgeHooks.canEntitySpawn(mobentity, serverWorld, d0, i, d1, null, SpawnReason.NATURAL);
////                                if (canSpawn != -1 && (canSpawn == 1 || isValidPositionForMob(serverWorld, mobentity, d2))) {
////                                    if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(mobentity, serverWorld, (float) d0, (float) i, (float) d1, null, SpawnReason.NATURAL))
////                                        ilivingentitydata = mobentity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(mobentity.blockPosition()), SpawnReason.NATURAL, ilivingentitydata, (CompoundNBT) null);
////                                    ++j;
////                                    ++l1;
////                                    serverWorld.addFreshEntityWithPassengers(mobentity);
////                                    iOnSpawnDensityAdder.run(mobentity, chunk);
////                                    if (j >= net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(mobentity)) {
////                                        return;
////                                    }
////
////                                    if (mobentity.isMaxGroupSizeReached(l1)) {
////                                        break;
////                                    }
////                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}
