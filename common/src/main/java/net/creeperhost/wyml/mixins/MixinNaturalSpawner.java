package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.ChunkManager;
import net.creeperhost.wyml.WYMLReimplementedHooks;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.Random;

@Mixin(NaturalSpawner.class)
public abstract class MixinNaturalSpawner {
    @Final
    @Mutable
    @Shadow
    static int MAGIC_NUMBER;

    private static BlockPos getTopNonCollidingPos(LevelReader levelReader, EntityType<?> entityType, int i, int j) {
        int k = levelReader.getHeight(SpawnPlacements.getHeightmapType(entityType), i, j);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, k, j);
        if (levelReader.dimensionType().hasCeiling()) {
            do {
                mutableBlockPos.move(Direction.DOWN);
            } while(!levelReader.getBlockState(mutableBlockPos).isAir());

            do {
                mutableBlockPos.move(Direction.DOWN);
            } while(levelReader.getBlockState(mutableBlockPos).isAir() && mutableBlockPos.getY() > levelReader.getMinBuildHeight());
        }

        if (SpawnPlacements.getPlacementType(entityType) == SpawnPlacements.Type.ON_GROUND) {
            BlockPos blockPos = mutableBlockPos.below();
            if (levelReader.getBlockState(blockPos).isPathfindable(levelReader, blockPos, PathComputationType.LAND)) {
                return blockPos;
            }
        }

        return mutableBlockPos.immutable();
    }

    @Inject(at = @At("HEAD"), method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V", cancellable = true)
    private static void spawnCategoryForPosition(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback, CallbackInfo ci) {
        spawnCategoryForPosition1(mobCategory, serverLevel, chunkAccess, blockPos, spawnPredicate, afterSpawnCallback);
        ci.cancel();
    }

    private static void spawnCategoryForPosition1(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback) {
        if(serverLevel.isClientSide) return;
        StructureManager structureFeatureManager = serverLevel.structureManager();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        int slowTicks = WymlConfig.cached().SLOW_TICKS;
        int i = blockPos.getY();
        int MAGIC_NUMBER_2_ELECTRIC_BOOGALOO = ((int) (WhyYouMakeLag.getMagicNum() * WhyYouMakeLag.getMagicNum()));
        if (MAGIC_NUMBER != MAGIC_NUMBER_2_ELECTRIC_BOOGALOO)
        {
            //Keep this up to date if scaling is enabled.
            MAGIC_NUMBER = MAGIC_NUMBER_2_ELECTRIC_BOOGALOO;
        }
        ChunkManager spawnManager = WhyYouMakeLag.getChunkManager(chunkAccess.getPos(), serverLevel.dimensionType(), mobCategory);
        if (spawnManager.isPaused())
        {
            if (!spawnManager.isSaved()) WhyYouMakeLag.updateChunkManager(spawnManager);
            return;
        }
        if (spawnManager.isSlowMode())
        {
            int tries = WymlConfig.cached().MOB_TRIES;
            if (WymlConfig.cached().MULTIPLY_BY_PLAYERS)
                tries = (tries * WhyYouMakeLag.minecraftServer.getPlayerList().getPlayerCount());
            if (spawnManager.getSpawnsInSample() > tries)
            {
                return;
            }
            if (spawnManager.getSpawnsInSample() < tries && spawnManager.ticksSinceSlow() > slowTicks)
            {
                spawnManager.fastMode();
                if (WymlConfig.cached().DEBUG_PRINT)
                    System.out.println("Entering fast spawn mode for class " + spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + "[" + spawnManager.getFailRate() + "%]");
                WhyYouMakeLag.updateChunkManager(spawnManager);
            }
        }
        else
        {
            if(spawnManager.getClassification() != null && WhyYouMakeLag.spawnableChunkCount.get(spawnManager.getClassification()) != null)
            {
                int maxSpawnRate = WhyYouMakeLag.calculateSpawnCount(spawnManager.getClassification(), WhyYouMakeLag.mobCategoryCounts, WhyYouMakeLag.spawnableChunkCount.get(spawnManager.getClassification()));
                if (spawnManager.getSpawnsInSample() > maxSpawnRate && WymlConfig.cached().ALLOW_SLOW)
                {
                    spawnManager.slowMode();
                    if (WymlConfig.cached().DEBUG_PRINT) System.out.println("Entering slow spawn mode for class " + spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + "[" + spawnManager.getFailRate() + "%]");
                    WhyYouMakeLag.updateChunkManager(spawnManager);
                    return;
                }
            }
        }
        if ((spawnManager.getFailRate() > WymlConfig.cached().PAUSE_RATE && spawnManager.getStartRate() > WymlConfig.cached().PAUSE_MIN && spawnManager.ticksSinceSlow() > slowTicks && spawnManager.canPause() && !spawnManager.isClaimed()) || (spawnManager.getFailRate() > WymlConfig.cached().PAUSE_CLAIMED_RATE && spawnManager.getStartRate() > WymlConfig.cached().PAUSE_MIN && spawnManager.ticksSinceSlow() > slowTicks && spawnManager.canPause() && spawnManager.isClaimed()))
        {
            int pauseTicks = (spawnManager.isClaimed()) ? WymlConfig.cached().PAUSE_CLAIMED_TICKS : WymlConfig.cached().PAUSE_TICKS;
            spawnManager.pauseSpawns(pauseTicks);
            int resumeRate = spawnManager.isClaimed() ? WymlConfig.cached().RESUME_CLAIMED_RATE : WymlConfig.cached().RESUME_RATE;
            if (WymlConfig.cached().DEBUG_PRINT)
                System.out.println("Pausing spawns for " + pauseTicks + " ticks or until " + resumeRate + "% success rate for class " + spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + " due to high failure rate [" + spawnManager.getFailRate() + "%].");
            WhyYouMakeLag.updateChunkManager(spawnManager);
            return;
        }
        BlockState blockState = chunkAccess.getBlockState(blockPos);
        if (!blockState.isRedstoneConductor(chunkAccess, blockPos)) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            int j = 0;

            for(int k = 0; k < 3; ++k) {
                int l = blockPos.getX();
                int m = blockPos.getZ();
                boolean n = true;
                MobSpawnSettings.SpawnerData spawnerData = null;
                SpawnGroupData spawnGroupData = null;
                int o = Mth.ceil(serverLevel.random.nextFloat() * 4.0F);
                int p = 0;
                int sampleSpawns = spawnManager.getSpawnsInSample();
                int maxAttempts = WymlConfig.cached().MAX_CHUNK_SPAWN_REQ_TICK;
                for(int q = 0; q < o; ++q) {
                    if (sampleSpawns > maxAttempts)
                    {
                        if (WymlConfig.cached().DEBUG_PRINT) System.out.println("Skipping spawn as beyond limits..");
                        continue;
                    }
                    sampleSpawns = spawnManager.getSpawnsInSample();
                    l += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
                    m += serverLevel.random.nextInt(6) - serverLevel.random.nextInt(6);
                    mutableBlockPos.set(l, i, m);
                    if (spawnManager.isKnownBadLocation(mutableBlockPos))
                    {
                        return;
                    }
                    double d = (double)l + 0.5D;
                    double e = (double)m + 0.5D;
                    spawnManager.increaseSpawningCount(mutableBlockPos);
                    WhyYouMakeLag.updateChunkManager(spawnManager);
                    Player player = serverLevel.getNearestPlayer(d, (double)i, e, -1.0D, false);
                    if (player != null) {
                        double f = player.distanceToSqr(d, (double)i, e);
                        if (isRightDistanceToPlayerAndSpawnPoint(serverLevel, chunkAccess, mutableBlockPos, f)) {
                            if (spawnerData == null) {
                                Optional<MobSpawnSettings.SpawnerData> optional = getRandomSpawnMobAt(serverLevel, structureFeatureManager, chunkGenerator, mobCategory, serverLevel.random, mutableBlockPos);
                                if (optional.isEmpty()) {
                                    break;
                                }

                                spawnerData = (MobSpawnSettings.SpawnerData)optional.get();
                                //TODO: Block spawns here too if too many
                                o = spawnerData.minCount + serverLevel.random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
                            }

                            if (isValidSpawnPostitionForType(serverLevel, mobCategory, structureFeatureManager, chunkGenerator, spawnerData, mutableBlockPos, f) && spawnPredicate.test(spawnerData.type, mutableBlockPos, chunkAccess)) {
                                Mob mob = getMobForSpawn(serverLevel, spawnerData.type);
                                if (mob == null) {
                                    return;
                                }
                                ResourceLocation entityReg = Registry.ENTITY_TYPE.getKey(mob.getType());
                                if(spawnManager.reachedMobLimit(entityReg))
                                {
                                    if(WymlConfig.cached().DEBUG_PRINT)
                                    {
                                        System.out.println("Stopped spawning "+entityReg+" as over configured limit.");
                                    }
                                    return;
                                }
                                mob.moveTo(d, (double)i, e, serverLevel.random.nextFloat() * 360.0F, 0.0F);
                                int canSpawn = WYMLReimplementedHooks.canSpawn(mob, serverLevel, d, i, e, null, MobSpawnType.NATURAL);
                                if (canSpawn != -1 && (canSpawn == 1 || isValidPositionForMob(serverLevel, mob, f)))
                                {
                                    if (!WYMLReimplementedHooks.doSpecialSpawn(mob, serverLevel, (float) d, i, (float) e, null, MobSpawnType.NATURAL)) {
                                        spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.NATURAL, spawnGroupData, (CompoundTag) null);
                                        ++j;
                                        ++p;
                                        serverLevel.addFreshEntityWithPassengers(mob);
                                        afterSpawnCallback.run(mob, chunkAccess);
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
    }

    @Inject(at = @At("HEAD"), method = "isSpawnPositionOk", cancellable = true)
    private static void isSpawnPositionOk(SpawnPlacements.Type type, LevelReader levelReader, BlockPos blockPos, @Nullable EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir)
    {
        if (blockPos != null)
        {
            if (entityType.getCategory() != null)
            {
                ChunkPos chuck = new ChunkPos(blockPos);
                ChunkManager spawnManager = WhyYouMakeLag.getChunkManager(chuck, levelReader.dimensionType(), entityType.getCategory());
                if (spawnManager != null)
                {
                    if (spawnManager.isKnownBadLocation(blockPos))
                    {
                        cir.setReturnValue(false);
                        cir.cancel();
                        return;
                    }
                }
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "spawnMobsForChunkGeneration", cancellable = true)
    private static void spawnForChunk(ServerLevelAccessor serverLevelAccessor, Holder<Biome> holder, ChunkPos chunkPos, RandomSource random, CallbackInfo ci)
    {
        MobSpawnSettings mobSpawnSettings = ((Biome) holder.value()).getMobSettings();
        WeightedRandomList<MobSpawnSettings.SpawnerData> weightedRandomList = mobSpawnSettings.getMobs(MobCategory.CREATURE);
        int slowTicks = WymlConfig.cached().SLOW_TICKS;

        int MAGIC_NUMBER_2_ELECTRIC_BOOGALOO = ((int) (WhyYouMakeLag.getMagicNum() * WhyYouMakeLag.getMagicNum()));
        if (MAGIC_NUMBER != MAGIC_NUMBER_2_ELECTRIC_BOOGALOO)
        {
            //Keep this up to date if scaling is enabled.
            MAGIC_NUMBER = MAGIC_NUMBER_2_ELECTRIC_BOOGALOO;
        }
        ChunkManager spawnManager = WhyYouMakeLag.getChunkManager(chunkPos, serverLevelAccessor.dimensionType(), MobCategory.CREATURE);
        if (spawnManager.isPaused())
        {
            if (!spawnManager.isSaved()) WhyYouMakeLag.updateChunkManager(spawnManager);
            ci.cancel();
            return;
        }

        if (spawnManager.isSlowMode())
        {
            int tries = WymlConfig.cached().MOB_TRIES;
            if (WymlConfig.cached().MULTIPLY_BY_PLAYERS)
                tries = (tries * WhyYouMakeLag.minecraftServer.getPlayerList().getPlayerCount());
            if (spawnManager.getSpawnsInSample() > tries)
            {
                ci.cancel();
                return;
            }
            if (spawnManager.getSpawnsInSample() < tries && spawnManager.ticksSinceSlow() > slowTicks)
            {
                spawnManager.fastMode();
                if (WymlConfig.cached().DEBUG_PRINT)
                    System.out.println("Entering fast spawn mode for class " + spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + "[" + spawnManager.getFailRate() + "%]");
                WhyYouMakeLag.updateChunkManager(spawnManager);
            }
        }
        else
        {
            if(spawnManager.getClassification() != null && WhyYouMakeLag.spawnableChunkCount.get(spawnManager.getClassification()) != null)
            {
                int maxSpawnRate = WhyYouMakeLag.calculateSpawnCount(spawnManager.getClassification(), WhyYouMakeLag.mobCategoryCounts, WhyYouMakeLag.spawnableChunkCount.get(spawnManager.getClassification()));
                if (spawnManager.getSpawnsInSample() > maxSpawnRate && WymlConfig.cached().ALLOW_SLOW)
                {
                    spawnManager.slowMode();
                    if (WymlConfig.cached().DEBUG_PRINT) System.out.println("Entering slow spawn mode for class " + spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + "[" + spawnManager.getFailRate() + "%]");
                    WhyYouMakeLag.updateChunkManager(spawnManager);
                    ci.cancel();
                    return;
                }
            }
        }
        if ((spawnManager.getFailRate() > WymlConfig.cached().PAUSE_RATE && spawnManager.getStartRate() > WymlConfig.cached().PAUSE_MIN && spawnManager.ticksSinceSlow() > slowTicks && spawnManager.canPause() && !spawnManager.isClaimed()) || (spawnManager.getFailRate() > WymlConfig.cached().PAUSE_CLAIMED_RATE && spawnManager.getStartRate() > WymlConfig.cached().PAUSE_MIN && spawnManager.ticksSinceSlow() > slowTicks && spawnManager.canPause() && spawnManager.isClaimed()))
        {
            int pauseTicks = (spawnManager.isClaimed()) ? WymlConfig.cached().PAUSE_CLAIMED_TICKS : WymlConfig.cached().PAUSE_TICKS;
            spawnManager.pauseSpawns(pauseTicks);
            int resumeRate = spawnManager.isClaimed() ? WymlConfig.cached().RESUME_CLAIMED_RATE : WymlConfig.cached().RESUME_RATE;
            if (WymlConfig.cached().DEBUG_PRINT)
                System.out.println("Pausing spawns for " + pauseTicks + " ticks or until " + resumeRate + "% success rate for class " + spawnManager.getClassification().getName() + " at " + spawnManager.getChunk() + " due to high failure rate [" + spawnManager.getFailRate() + "%].");
            WhyYouMakeLag.updateChunkManager(spawnManager);
            ci.cancel();
            return;
        }

        if (!weightedRandomList.isEmpty())
        {
            int i = chunkPos.getMinBlockX();
            int j = chunkPos.getMinBlockZ();

            while (true)
            {
                Optional optional;
                do
                {
                    if (!(random.nextFloat() < mobSpawnSettings.getCreatureProbability()))
                    {
                        ci.cancel();
                        return;
                    }

                    optional = weightedRandomList.getRandom(random);
                } while (!optional.isPresent());

                MobSpawnSettings.SpawnerData spawnerData = (MobSpawnSettings.SpawnerData) optional.get();
                int k = spawnerData.minCount + random.nextInt(1 + spawnerData.maxCount - spawnerData.minCount);
                SpawnGroupData spawnGroupData = null;
                int l = i + random.nextInt(16);
                int m = j + random.nextInt(16);
                int n = l;
                int o = m;

                for (int p = 0; p < k; ++p)
                {
                    boolean bl = false;

                    for (int q = 0; !bl && q < 4; ++q)
                    {
                        BlockPos blockPos = getTopNonCollidingPos(serverLevelAccessor, spawnerData.type, l, m);
                        if (blockPos == null)
                        {
                            ci.cancel();
                            return;
                        }
                        if (spawnerData.type.canSummon())
                        {
                            float f = spawnerData.type.getWidth();
                            double d = Mth.clamp((double) l, (double) i + (double) f, (double) i + 16.0D - (double) f);
                            double e = Mth.clamp((double) m, (double) j + (double) f, (double) j + 16.0D - (double) f);
                            if (!serverLevelAccessor.noCollision(spawnerData.type.getAABB(d, (double) blockPos.getY(), e)) || !SpawnPlacements.checkSpawnRules(spawnerData.type, serverLevelAccessor, MobSpawnType.CHUNK_GENERATION, new BlockPos(d, (double) blockPos.getY(), e), serverLevelAccessor.getRandom()))
                            {
                                continue;
                            }

                            Entity entity;
                            try
                            {
                                entity = spawnerData.type.create(serverLevelAccessor.getLevel());
                            } catch (Exception var27)
                            {
                                continue;
                            }

                            ResourceLocation entityReg = Registry.ENTITY_TYPE.getKey(entity.getType());

                            if(spawnManager.reachedMobLimit(entityReg))
                            {
                                if(WymlConfig.cached().DEBUG_PRINT)
                                {
                                    System.out.println("Stopped spawning "+entityReg+" as over configured limit.");
                                }
                                continue;
                            }
                            entity.moveTo(d, (double) blockPos.getY(), e, random.nextFloat() * 360.0F, 0.0F);
                            if (entity instanceof Mob)
                            {
                                Mob mob = (Mob) entity;
                                if (mob.checkSpawnRules(serverLevelAccessor, MobSpawnType.CHUNK_GENERATION) && mob.checkSpawnObstruction(serverLevelAccessor))
                                {
                                    spawnGroupData = mob.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.CHUNK_GENERATION, spawnGroupData, (CompoundTag) null);
                                    serverLevelAccessor.addFreshEntityWithPassengers(mob);
                                    bl = true;
                                }
                            }
                        }

                        l += random.nextInt(5) - random.nextInt(5);

                        for (m += random.nextInt(5) - random.nextInt(5); l < i || l >= i + 16 || m < j || m >= j + 16; m = o + random.nextInt(5) - random.nextInt(5))
                        {
                            l = n + random.nextInt(5) - random.nextInt(5);
                        }
                    }
                }
            }
        }
        ci.cancel();
        return;
    }

    @Invoker("getRandomSpawnMobAt")
    private static Optional<MobSpawnSettings.SpawnerData> getRandomSpawnMobAt(ServerLevel serverLevel, StructureManager structureFeatureManager, ChunkGenerator chunkGenerator, MobCategory mobCategory, RandomSource random, BlockPos blockPos) {
        return null;
    }

    @Invoker("isValidPositionForMob")
    private static boolean isValidPositionForMob(ServerLevel serverLevel, Mob mob, double f) {
        return false;
    }

    @Invoker("getMobForSpawn")
    private static Mob getMobForSpawn(ServerLevel serverLevel, EntityType<?> type) {
        return null;
    }

    @Invoker("isValidSpawnPostitionForType")
    private static boolean isValidSpawnPostitionForType(ServerLevel serverLevel, MobCategory mobCategory, StructureManager structureFeatureManager, ChunkGenerator chunkGenerator, MobSpawnSettings.SpawnerData spawnerData, BlockPos.MutableBlockPos mutableBlockPos, double f) {
        return false;
    }

    @Invoker("isRightDistanceToPlayerAndSpawnPoint")
    private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos.MutableBlockPos mutableBlockPos, double f) {
        return false;
    }
}
