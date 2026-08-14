package net.creeperhost.wyml.mixins;

import net.creeperhost.wyml.ChunkManager;
import net.creeperhost.wyml.WYMLReimplementedHooks;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.creeperhost.wyml.config.WymlBootConfig;
import net.creeperhost.wyml.config.WymlConfig;
import net.creeperhost.wyml.spawn.SpawnAttemptStage;
import net.creeperhost.wyml.spawn.SpawnAttemptTracker;
import net.creeperhost.wyml.spawn.SpawnFailureReason;
import net.creeperhost.wyml.spawn.PerMobLimitPolicy;
import net.creeperhost.wyml.spawn.SpawnThrottlePolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(NaturalSpawner.class)
public abstract class MixinNaturalSpawner {
    private static BlockPos getTopNonCollidingPos(LevelReader levelReader, EntityType<?> entityType, int i, int j) {
        int k = levelReader.getHeight(SpawnPlacements.getHeightmapType(entityType), i, j);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(i, k, j);
        if (levelReader.dimensionType().hasCeiling()) {
            do {
                mutableBlockPos.move(Direction.DOWN);
            } while(!levelReader.getBlockState(mutableBlockPos).isAir());

            do {
                mutableBlockPos.move(Direction.DOWN);
            } while(levelReader.getBlockState(mutableBlockPos).isAir() && mutableBlockPos.getY() > levelReader.getMinY());
        }

        return SpawnPlacements.getPlacementType(entityType).adjustSpawnPosition(levelReader, mutableBlockPos.immutable());
    }

    private static boolean controllerBlocks(ChunkManager manager, ServerLevel level)
    {
        if (!WymlBootConfig.moduleEnabled("spawn_controller")) return false;
        if (manager.isPaused())
        {
            if (!manager.isSaved()) WhyYouMakeLag.updateChunkManager(manager);
            return true;
        }

        if (manager.isSlowMode() && !manager.canSlow())
        {
            manager.fastMode();
            WhyYouMakeLag.updateChunkManager(manager);
        }

        int slowTicks = WymlConfig.cached().SLOW_TICKS;
        if (manager.isSlowMode())
        {
            boolean throttleWindowElapsed = manager.ticksSinceSlow() > slowTicks;
            boolean shouldEnterBackoff = false;
            boolean claimed = false;
            int pauseRate = 0;
            if (throttleWindowElapsed && manager.canEnterBackoff())
            {
                claimed = manager.isClaimed();
                pauseRate = claimed ? WymlConfig.cached().PAUSE_CLAIMED_RATE : WymlConfig.cached().PAUSE_RATE;
                shouldEnterBackoff = manager.getFailRate() > pauseRate
                        && manager.getStartRate() > WymlConfig.cached().PAUSE_MIN
                        && manager.canPause();
            }

            int attemptBudget = WhyYouMakeLag.getAttemptBudget(level);
            SpawnThrottlePolicy.Action action = SpawnThrottlePolicy.decide(
                    throttleWindowElapsed,
                    shouldEnterBackoff,
                    manager.getAttemptsInCurrentWindow(),
                    attemptBudget);
            if (action == SpawnThrottlePolicy.Action.BACKOFF)
            {
                int pauseTicks = claimed ? WymlConfig.cached().PAUSE_CLAIMED_TICKS : WymlConfig.cached().PAUSE_TICKS;
                int resumeRate = claimed ? WymlConfig.cached().RESUME_CLAIMED_RATE : WymlConfig.cached().RESUME_RATE;
                manager.pauseSpawns(pauseTicks);
                if (WymlConfig.cached().DEBUG_PRINT)
                    System.out.println("Entering spawn backoff for " + pauseTicks + " ticks, followed by a "
                            + WymlConfig.cached().PROBE_ATTEMPTS + "-attempt probe requiring " + resumeRate
                            + "% success for class " + manager.getClassification().getName() + " at "
                            + manager.getChunk() + " due to " + manager.getFailRate() + "% failures.");
                WhyYouMakeLag.updateChunkManager(manager);
                return true;
            }
            if (action == SpawnThrottlePolicy.Action.ACTIVATE)
            {
                manager.fastMode();
                if (WymlConfig.cached().DEBUG_PRINT)
                    System.out.println("Entering active spawn mode for class " + manager.getClassification().getName()
                            + " at " + manager.getChunk() + "[" + manager.getFailRate() + "%]");
                WhyYouMakeLag.updateChunkManager(manager);
            }
            else if (action == SpawnThrottlePolicy.Action.BLOCK)
            {
                return true;
            }
        }
        else if (manager.canSlow()
                && manager.getSpawnsInSample() >= WymlConfig.cached().MAX_CHUNK_SPAWN_REQ_TICK)
        {
            manager.slowMode();
            if (WymlConfig.cached().DEBUG_PRINT)
                System.out.println("Entering throttled spawn mode for class " + manager.getClassification().getName()
                        + " at " + manager.getChunk() + "[" + manager.getFailRate() + "%]");
            WhyYouMakeLag.updateChunkManager(manager);
            return true;
        }
        return false;
    }

    @Inject(at = @At("HEAD"), method = "spawnCategoryForPosition(Lnet/minecraft/world/entity/MobCategory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V", cancellable = true)
    private static void spawnCategoryForPosition(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback, CallbackInfo ci) {
        if (!WymlConfig.isEnabled()) return;
        spawnCategoryForPosition1(mobCategory, serverLevel, chunkAccess, blockPos, spawnPredicate, afterSpawnCallback);
        ci.cancel();
    }

    private static void spawnCategoryForPosition1(MobCategory mobCategory, ServerLevel serverLevel, ChunkAccess chunkAccess, BlockPos blockPos, NaturalSpawner.SpawnPredicate spawnPredicate, NaturalSpawner.AfterSpawnCallback afterSpawnCallback) {
        if(serverLevel.isClientSide()) return;
        StructureManager structureFeatureManager = serverLevel.structureManager();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        int i = blockPos.getY();
        boolean controllerEnabled = WymlBootConfig.moduleEnabled("spawn_controller");
        ChunkManager spawnManager = controllerEnabled
                ? WhyYouMakeLag.getChunkManager(serverLevel, chunkAccess.getPos(), mobCategory)
                : new ChunkManager(serverLevel, chunkAccess.getPos(), mobCategory);
        if (controllerBlocks(spawnManager, serverLevel)) return;
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
                int o = Mth.ceil(serverLevel.getRandom().nextFloat() * 4.0F);
                int p = 0;
                for(int q = 0; q < o; ++q) {
                    int candidateBudget = !controllerEnabled
                            ? Integer.MAX_VALUE
                            : spawnManager.isSlowMode()
                            ? WhyYouMakeLag.getAttemptBudget(serverLevel)
                            : (WymlConfig.cached().ALLOW_SLOW
                                    ? Math.max(0, WymlConfig.cached().MAX_CHUNK_SPAWN_REQ_TICK)
                                    : Integer.MAX_VALUE);
                    if (spawnManager.getAttemptsInCurrentWindow() >= candidateBudget)
                    {
                        if (WymlConfig.cached().DEBUG_PRINT) System.out.println("Skipping spawn as beyond limits..");
                        continue;
                    }
                    l += serverLevel.getRandom().nextInt(6) - serverLevel.getRandom().nextInt(6);
                    m += serverLevel.getRandom().nextInt(6) - serverLevel.getRandom().nextInt(6);
                    mutableBlockPos.set(l, i, m);
                    double d = (double)l + 0.5D;
                    double e = (double)m + 0.5D;
                    SpawnAttemptTracker.Attempt nextAttempt = controllerEnabled
                            ? spawnManager.beginNaturalSpawnAttempt()
                            : SpawnAttemptTracker.untracked();
                    if (nextAttempt == null) return;
                    try (SpawnAttemptTracker.Attempt attempt = nextAttempt) {
                        WhyYouMakeLag.updateChunkManager(spawnManager);
                        attempt.advance(SpawnAttemptStage.PLAYER_PROXIMITY);
                        Player player = serverLevel.getNearestPlayer(d, (double)i, e, -1.0D, false);
                        if (player == null) {
                            attempt.fail(SpawnFailureReason.NO_NEARBY_PLAYER);
                            continue;
                        }

                        double f = player.distanceToSqr(d, (double)i, e);
                        if (!isRightDistanceToPlayerAndSpawnPoint(serverLevel, chunkAccess, mutableBlockPos, f)) {
                            attempt.fail(SpawnFailureReason.PLAYER_DISTANCE_OR_SPAWN_POINT);
                            continue;
                        }

                        attempt.advance(SpawnAttemptStage.SPAWN_ENTRY_SELECTION);
                        if (spawnerData == null) {
                            Optional<MobSpawnSettings.SpawnerData> optional = getRandomSpawnMobAt(serverLevel, structureFeatureManager, chunkGenerator, mobCategory, serverLevel.getRandom(), mutableBlockPos);
                            if (optional.isEmpty()) {
                                attempt.fail(SpawnFailureReason.NO_SPAWN_ENTRY);
                                break;
                            }

                            spawnerData = optional.get();
                            o = spawnerData.minCount() + serverLevel.getRandom().nextInt(1 + spawnerData.maxCount() - spawnerData.minCount());
                        }

                        attempt.advance(SpawnAttemptStage.PLACEMENT_AND_PREDICATE);
                        if (!isValidSpawnPostitionForType(serverLevel, mobCategory, structureFeatureManager, chunkGenerator, spawnerData, mutableBlockPos, f)
                                || !spawnPredicate.test(spawnerData.type(), mutableBlockPos, chunkAccess)) {
                            attempt.fail(SpawnFailureReason.PLACEMENT_OR_PREDICATE_REJECTED);
                            continue;
                        }

                        attempt.advance(SpawnAttemptStage.ENTITY_CREATION);
                        Mob mob = getMobForSpawn(serverLevel, spawnerData.type());
                        if (mob == null) {
                            attempt.fail(SpawnFailureReason.ENTITY_CREATION_FAILED);
                            continue;
                        }

                        attempt.advance(SpawnAttemptStage.PER_MOB_POLICY);
                        Identifier entityReg = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
                        if (WymlBootConfig.moduleEnabled("per_mob_rules") && spawnManager.reachedMobLimit(entityReg)) {
                            if (WymlConfig.cached().DEBUG_PRINT) {
                                System.out.println("Stopped spawning " + entityReg + " as over configured limit.");
                            }
                            attempt.fail(SpawnFailureReason.PER_MOB_POPULATION_LIMIT);
                            continue;
                        }

                        mob.snapTo(d, (double)i, e, serverLevel.getRandom().nextFloat() * 360.0F, 0.0F);
                        attempt.advance(SpawnAttemptStage.LOADER_AND_POSITION_RULES);
                        int canSpawn = WYMLReimplementedHooks.canSpawn(mob, serverLevel, d, i, e, null, EntitySpawnReason.NATURAL);
                        if (canSpawn == -1 || (canSpawn != 1 && !isValidPositionForMob(serverLevel, mob, f))) {
                            attempt.fail(SpawnFailureReason.LOADER_OR_POSITION_VETO);
                            continue;
                        }

                        attempt.advance(SpawnAttemptStage.FINALIZATION);
                        spawnGroupData = mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.NATURAL, spawnGroupData);
                        serverLevel.addFreshEntityWithPassengers(mob);
                        if (serverLevel.getEntity(mob.getId()) != mob) {
                            attempt.fail(SpawnFailureReason.ADMISSION_REJECTED);
                            continue;
                        }
                        ++j;
                        ++p;
                        attempt.succeed();
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

    @Inject(at = @At("HEAD"), method = "spawnMobsForChunkGeneration", cancellable = true)
    private static void spawnForChunk(ServerLevelAccessor serverLevelAccessor, Holder<Biome> holder, ChunkPos chunkPos, RandomSource random, CallbackInfo ci)
    {
        if (!WymlConfig.isEnabled()) return;
        if (!PerMobLimitPolicy.checksWorldGeneration(WymlConfig.cached().DISABLE_COUNTING_CHUNK_GENERATED_MOBS)) return;
        MobSpawnSettings mobSpawnSettings = ((Biome) holder.value()).getMobSettings();
        WeightedList<MobSpawnSettings.SpawnerData> weightedRandomList = mobSpawnSettings.getMobs(MobCategory.CREATURE);
        ServerLevel serverLevel = serverLevelAccessor.getLevel();
        ChunkManager spawnManager = WymlBootConfig.moduleEnabled("spawn_controller")
                ? WhyYouMakeLag.getChunkManager(serverLevel, chunkPos, MobCategory.CREATURE)
                : new ChunkManager(serverLevel, chunkPos, MobCategory.CREATURE);
        if (controllerBlocks(spawnManager, serverLevel))
        {
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
                int k = spawnerData.minCount() + random.nextInt(1 + spawnerData.maxCount() - spawnerData.minCount());
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
                        BlockPos blockPos = getTopNonCollidingPos(serverLevelAccessor, spawnerData.type(), l, m);
                        if (blockPos == null)
                        {
                            ci.cancel();
                            return;
                        }
                        if (spawnerData.type().canSummon())
                        {
                            float f = spawnerData.type().getWidth();
                            double d = Mth.clamp((double) l, (double) i + (double) f, (double) i + 16.0D - (double) f);
                            double e = Mth.clamp((double) m, (double) j + (double) f, (double) j + 16.0D - (double) f);
                            if (!serverLevelAccessor.noCollision(spawnerData.type().getSpawnAABB(d, blockPos.getY(), e)) || !SpawnPlacements.checkSpawnRules(spawnerData.type(), serverLevelAccessor, EntitySpawnReason.CHUNK_GENERATION, BlockPos.containing(d, blockPos.getY(), e), serverLevelAccessor.getRandom()))
                            {
                                continue;
                            }

                            Entity entity;
                            try
                            {
                                entity = spawnerData.type().create(serverLevelAccessor.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
                            } catch (Exception var27)
                            {
                                continue;
                            }

                            Identifier entityReg = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

                            if(WymlBootConfig.moduleEnabled("per_mob_rules") && spawnManager.reachedMobLimit(entityReg))
                            {
                                if(WymlConfig.cached().DEBUG_PRINT)
                                {
                                    System.out.println("Stopped spawning "+entityReg+" as over configured limit.");
                                }
                                continue;
                            }
                            entity.snapTo(d, blockPos.getY(), e, random.nextFloat() * 360.0F, 0.0F);
                            if (entity instanceof Mob)
                            {
                                Mob mob = (Mob) entity;
                                if (mob.checkSpawnRules(serverLevelAccessor, EntitySpawnReason.CHUNK_GENERATION) && mob.checkSpawnObstruction(serverLevelAccessor))
                                {
                                    spawnGroupData = mob.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.CHUNK_GENERATION, spawnGroupData);
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
