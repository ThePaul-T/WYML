package net.creeperhost.wyml.spawn;

public final class CategoryCapPolicy
{
    private CategoryCapPolicy()
    {
    }

    public static int calculate(int instancesPerChunk, int spawnableChunks, double scalingRadius)
    {
        if (instancesPerChunk <= 0 || spawnableChunks <= 0) return 0;
        double radius = Math.max(1.0D, scalingRadius);
        double cap = ((double) instancesPerChunk * spawnableChunks) / (radius * radius);
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(cap));
    }
}
