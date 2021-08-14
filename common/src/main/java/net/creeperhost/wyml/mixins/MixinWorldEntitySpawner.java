package net.creeperhost.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Random;

import static net.creeperhost.wyml.WymlConfig.MAX_CHUNK_SPAWN_REQ_TICK;


@Mixin(WorldEntitySpawner.class)
public abstract class MixinWorldEntitySpawner
{
    @Shadow protected static BlockPos getRandomPosWithin(World p_222262_0_, Chunk p_222262_1_) { return null; }
    @Invoker("getRandomSpawnMobAt") private static MobSpawnInfo.Spawners getRandomSpawnMobAt(ServerWorld p_234977_0_, StructureManager p_234977_1_, ChunkGenerator p_234977_2_, EntityClassification p_234977_3_, Random p_234977_4_, BlockPos p_234977_5_) { return null; }
    @Invoker("isValidSpawnPostitionForType") private static boolean isValidSpawnPostitionForType(ServerWorld p_234966_1_, EntityClassification p_234966_0_, StructureManager structuremanager, ChunkGenerator chunkgenerator, MobSpawnInfo.Spawners mobspawninfo$spawners, BlockPos.Mutable blockpos$mutable, double d2) { return false; }
    @Invoker("getMobForSpawn") private static MobEntity getMobForSpawn(ServerWorld p_234966_1_, EntityType<?> type) { return null; }
    @Invoker("isValidPositionForMob") private static boolean isValidPositionForMob(ServerWorld p_234966_1_, MobEntity mobentity, double d2) { return false; }
    @Invoker("isRightDistanceToPlayerAndSpawnPoint") private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerWorld p_234978_0_, IChunk p_234978_1_, BlockPos.Mutable p_234978_2_, double p_234978_3_) { return false; }
    @Shadow @Final private static Logger LOGGER;

    /**
     * @author ThePaul_T - 2021
     * @reason Cause Mojang code is questionable, would've injected but 100% sure I am rewriting this whole function soon(tm) - Sorry not sorry.
     */
    @Overwrite
    public static void spawnCategoryForPosition(EntityClassification entityClassification, ServerWorld serverWorld, IChunk chunk, BlockPos blockPos, WorldEntitySpawner.IDensityCheck iDensityCheck, WorldEntitySpawner.IOnSpawnDensityAdder iOnSpawnDensityAdder) {
        StructureManager structuremanager = serverWorld.structureFeatureManager();
        ChunkGenerator chunkgenerator = serverWorld.getChunkSource().getGenerator();
        int slowTicks = WymlConfig.SLOW_TICKS.get();
        int i = blockPos.getY();
        int MAGIC_NUMBER_2_ELECTRIC_BOOGALOO = ((int) (WhyYouMakeLag.getMagicNum() * WhyYouMakeLag.getMagicNum()));
        if(WorldEntitySpawner.MAGIC_NUMBER != MAGIC_NUMBER_2_ELECTRIC_BOOGALOO)
        {
            //Keep this up to date if scaling is enabled.
            WorldEntitySpawner.MAGIC_NUMBER = MAGIC_NUMBER_2_ELECTRIC_BOOGALOO;
        }
        WYMLSpawnManager spawnManager = WhyYouMakeLag.getSpawnManager(chunk.getPos(), entityClassification);
        if(spawnManager.isPaused())
        {
            if(!spawnManager.isSaved()) WhyYouMakeLag.updateSpawnManager(spawnManager);
            return;
        }
        if(spawnManager.isSlowMode())
        {
            int tries = WymlConfig.MOB_TRIES.get();
            if (WymlConfig.MULTIPLY_BY_PLAYERS.get()) tries = (tries * WhyYouMakeLag.minecraftServer.getPlayerList().getPlayerCount());
            if(spawnManager.getSpawnsInSample() > tries)
            {
                return;
            }
            if(spawnManager.getSpawnsInSample() < tries && spawnManager.ticksSinceSlow() > slowTicks)
            {
                spawnManager.fastMode();
                if(WymlConfig.DEBUG_PRINT.get()) System.out.println("Entering fast spawn mode for class "+spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + "["+spawnManager.getFailRate()+"%]");
                WhyYouMakeLag.updateSpawnManager(spawnManager);
            }
        } else {
            int maxSpawnRate = WhyYouMakeLag.calculateSpawnCount(spawnManager.getClassification(), WhyYouMakeLag.mobCategoryCounts, WhyYouMakeLag.spawnableChunkCount.get(spawnManager.getClassification()));
            if(spawnManager.getSpawnsInSample() > maxSpawnRate && WymlConfig.ALLOW_SLOW.get())
            {
                spawnManager.slowMode();
                if(WymlConfig.DEBUG_PRINT.get()) System.out.println("Entering slow spawn mode for class "+spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + "["+spawnManager.getFailRate()+"%]");
                WhyYouMakeLag.updateSpawnManager(spawnManager);
                return;
            }
        }
        if(spawnManager.getFailRate() > WymlConfig.PAUSE_RATE.get() && spawnManager.getStartRate() > WymlConfig.PAUSE_MIN.get() && spawnManager.ticksSinceSlow() > slowTicks && WymlConfig.ALLOW_PAUSE.get())
        {
            int pauseTicks = WymlConfig.PAUSE_TICKS.get();
            spawnManager.pauseSpawns(pauseTicks);
            if(WymlConfig.DEBUG_PRINT.get()) System.out.println("Pausing spawns for "+pauseTicks+" ticks or until "+WymlConfig.RESUME_RATE.get()+"% success rate for class "+spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + " due to high failure rate ["+spawnManager.getFailRate()+"%].");
            WhyYouMakeLag.updateSpawnManager(spawnManager);
            return;
        }
        BlockState blockstate = chunk.getBlockState(blockPos);
        if (!blockstate.isRedstoneConductor(chunk, blockPos)) {
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
            if(spawnManager.isKnownBadLocation(blockpos$mutable))
            {
                return;
            }
            int j = 0;
            int sampleSpawns = spawnManager.getSpawnsInSample();
            int maxAttempts = MAX_CHUNK_SPAWN_REQ_TICK.get();
            for (int k = 0; k < 3; ++k) {
                if(sampleSpawns > maxAttempts)
                {
                    if(WymlConfig.DEBUG_PRINT.get()) LOGGER.debug("Skipping spawn as beyond limits.");
                    continue;
                }
                sampleSpawns = spawnManager.getSpawnsInSample();
                int l = blockPos.getX();
                int i1 = blockPos.getZ();
                int j1 = 6;
                MobSpawnInfo.Spawners mobspawninfo$spawners = null;
                ILivingEntityData ilivingentitydata = null;
                int k1 = MathHelper.ceil(serverWorld.random.nextFloat() * 4.0F);
                int l1 = 0;

                for (int i2 = 0; i2 < k1; ++i2) {
                    if(sampleSpawns > maxAttempts)
                    {
                        if(WymlConfig.DEBUG_PRINT.get()) LOGGER.debug("Skipping spawn as beyond limits..");
                        continue;
                    }
                    sampleSpawns = spawnManager.getSpawnsInSample();
                    l += serverWorld.random.nextInt(6) - serverWorld.random.nextInt(6);
                    i1 += serverWorld.random.nextInt(6) - serverWorld.random.nextInt(6);
                    blockpos$mutable.set(l, i, i1);
                    if(spawnManager.isKnownBadLocation(blockpos$mutable))
                    {
                        //System.out.println("Known bad block position "+blockpos$mutable+" for "+spawnManager.getClassification().getName()+" in "+spawnManager.getChunk()+" skipped.");
                        continue;
                    }
                    double d0 = (double) l + 0.5D;
                    double d1 = (double) i1 + 0.5D;
                    spawnManager.increaseSpawningCount(blockpos$mutable);
                    WhyYouMakeLag.updateSpawnManager(spawnManager);
                    PlayerEntity playerentity = serverWorld.getNearestPlayer(d0, (double) i, d1, -1.0D, false);
                    if (playerentity != null) {
                        double d2 = playerentity.distanceToSqr(d0, (double) i, d1);
                        if (isRightDistanceToPlayerAndSpawnPoint(serverWorld, chunk, blockpos$mutable, d2)) {
                            if (mobspawninfo$spawners == null) {
                                mobspawninfo$spawners = getRandomSpawnMobAt(serverWorld, structuremanager, chunkgenerator, entityClassification, serverWorld.random, blockpos$mutable);
                                if (mobspawninfo$spawners == null) {
                                    break;
                                }

                                k1 = mobspawninfo$spawners.minCount + serverWorld.random.nextInt(1 + mobspawninfo$spawners.maxCount - mobspawninfo$spawners.minCount);
                            }

                            if (isValidSpawnPostitionForType(serverWorld, entityClassification, structuremanager, chunkgenerator, mobspawninfo$spawners, blockpos$mutable, d2) && iDensityCheck.test(mobspawninfo$spawners.type, blockpos$mutable, chunk)) {
                                MobEntity mobentity = getMobForSpawn(serverWorld, mobspawninfo$spawners.type);
                                if (mobentity == null) {
                                    return;
                                }

                                mobentity.moveTo(d0, (double) i, d1, serverWorld.random.nextFloat() * 360.0F, 0.0F);
                                int canSpawn = net.minecraftforge.common.ForgeHooks.canEntitySpawn(mobentity, serverWorld, d0, i, d1, null, SpawnReason.NATURAL);
                                if (canSpawn != -1 && (canSpawn == 1 || isValidPositionForMob(serverWorld, mobentity, d2))) {
                                    if (!net.minecraftforge.event.ForgeEventFactory.doSpecialSpawn(mobentity, serverWorld, (float) d0, (float) i, (float) d1, null, SpawnReason.NATURAL))
                                        ilivingentitydata = mobentity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(mobentity.blockPosition()), SpawnReason.NATURAL, ilivingentitydata, (CompoundNBT) null);
                                    ++j;
                                    ++l1;
                                    serverWorld.addFreshEntityWithPassengers(mobentity);
                                    iOnSpawnDensityAdder.run(mobentity, chunk);
                                    if (j >= net.minecraftforge.event.ForgeEventFactory.getMaxSpawnPackSize(mobentity)) {
                                        return;
                                    }

                                    if (mobentity.isMaxGroupSizeReached(l1)) {
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
