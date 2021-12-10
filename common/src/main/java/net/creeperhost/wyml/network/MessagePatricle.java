package net.creeperhost.wyml.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class MessagePatricle
{
    BlockPos blockPos;
    int id;

    public MessagePatricle(BlockPos blockPos, ParticleType<?> particleType)
    {
        this.blockPos = blockPos;
        this.id = Registry.PARTICLE_TYPE.getId(particleType);
    }

    public MessagePatricle(FriendlyByteBuf buffer)
    {
        this.blockPos = buffer.readBlockPos();
        this.id = buffer.readInt();
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeBlockPos(blockPos);
        buf.writeInt(id);
    }

    public void handle(Supplier<NetworkManager.PacketContext> context)
    {
        context.get().queue(() ->
        {
            Level level = context.get().getPlayer().level;
            if (level == null) return;

            if(level.isLoaded(blockPos))
            {
                ParticleType<?> particleType = Registry.PARTICLE_TYPE.byId(id);
                ParticleOptions particleOptions = particleType.getDeserializer().fromNetwork((ParticleType) particleType, null);
                level.addParticle(particleOptions, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0.0D, 0.0D, 0.0D);
            }
        });
    }
}
