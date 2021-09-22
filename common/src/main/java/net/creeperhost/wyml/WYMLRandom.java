package net.creeperhost.wyml;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class WYMLRandom
{
    private static final Random random = new Random();

    private final int total;
    private int current;
    private int curRandom;
    private int[] randoms;

    public WYMLRandom(int min, int max, int total)
    {
        this.total = total;
        curRandom = min + random.nextInt(max - min + 1);
        randoms = new int[total];

        for(int i=0; i < (total-1); i++)
        {
            randoms[i] = min + random.nextInt(max - min + 1);
        }
    }

    public int get() throws Exception
    {
        if(current >= total) throw new Exception("Too many randoms!");
        int rand = curRandom;
        curRandom = randoms[current];
        current++;
        return rand;
    }
}
