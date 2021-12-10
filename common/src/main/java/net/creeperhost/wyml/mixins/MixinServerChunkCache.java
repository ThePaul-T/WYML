package net.creeperhost.wyml.mixins;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import net.creeperhost.wyml.WhyYouMakeLag;
import net.minecraft.server.level.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(value = ServerChunkCache.class, priority = 999)
public abstract class MixinServerChunkCache
{
    @Shadow
    @Final
    private ServerLevel level;
    @Shadow
    private long lastInhabitedUpdate;
    @Shadow
    @Final
    private DistanceManager distanceManager;
    @Shadow
    @Nullable
    private NaturalSpawner.SpawnState lastSpawnState;

    @Shadow
    protected abstract void getFullChunk(long l, Consumer<LevelChunk> consumer);

    @Shadow
    @Final
    public ChunkMap chunkMap;
    @Shadow
    private boolean spawnEnemies;
    @Shadow
    private boolean spawnFriendlies;

    /**
     * @author CreeperHost
     * @reason Because streams are memory hogs
     */
    @Overwrite
    private void tickChunks()
    {
        long l = this.level.getGameTime();
        long m = l - this.lastInhabitedUpdate;
        this.lastInhabitedUpdate = l;
        LevelData levelData = this.level.getLevelData();
        boolean bl = this.level.isDebug();
        boolean bl2 = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        if (!bl)
        {
            this.level.getProfiler().push("pollingChunks");
            int i = this.level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            boolean bl3 = levelData.getGameTime() % 400L == 0L;
            this.level.getProfiler().push("naturalSpawnCount");
            int j = this.distanceManager.getNaturalSpawnChunkCount();
            NaturalSpawner.SpawnState spawnState = NaturalSpawner.createState(j, this.level.getAllEntities(), this::getFullChunk, new LocalMobCapCalculator(this.chunkMap));
            this.lastSpawnState = spawnState;
            this.level.getProfiler().pop();
            List<ChunkHolder> list = Lists.newArrayList(((AccessorChunkMap) chunkMap).getChunks1());
            WhyYouMakeLag.shuffle(list);

            for (ChunkHolder chunkHolder : list)
            {
                Optional<LevelChunk> optional = ((Either) chunkHolder.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK)).left();
                if (optional.isPresent())
                {
                    this.level.getProfiler().push("broadcast");
                    chunkHolder.broadcastChanges((LevelChunk) optional.get());
                    this.level.getProfiler().pop();
                    Optional<LevelChunk> optional2 = ((Either) chunkHolder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK)).left();
                    if (optional2.isPresent())
                    {
                        LevelChunk levelChunk = (LevelChunk) optional2.get();
                        ChunkPos chunkPos = chunkHolder.getPos();
                        if (!((AccessorChunkMap) this.chunkMap).noPlayersCloseForSpawning1(chunkPos))
                        {
                            levelChunk.setInhabitedTime(levelChunk.getInhabitedTime() + m);
                            if (bl2 && (this.spawnEnemies || this.spawnFriendlies) && this.level.getWorldBorder().isWithinBounds(levelChunk.getPos()))
                            {
                                NaturalSpawner.spawnForChunk(this.level, levelChunk, spawnState, this.spawnFriendlies, this.spawnEnemies, bl3);
                            }

                            this.level.tickChunk(levelChunk, i);
                        }
                    }
                }
            }
            this.level.getProfiler().push("customSpawners");
            if (bl2)
            {
                this.level.tickCustomSpawners(this.spawnEnemies, this.spawnFriendlies);
            }

            this.level.getProfiler().pop();
            this.level.getProfiler().pop();
        }

        ((AccessorChunkMap) this.chunkMap).tick1();
    }
}
