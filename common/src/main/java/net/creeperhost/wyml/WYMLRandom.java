package net.creeperhost.wyml;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class WYMLRandom
{
    private final int min, max, total;
    private int current;
    private int curRandom;
    private int[] randoms;

    public WYMLRandom(int min, int max, int total)
    {
        this.min = min;
        this.max = max;
        this.total = total;
        curRandom = ThreadLocalRandom.current().nextInt(min, max);
        randoms = new int[total];
        CompletableFuture.runAsync(() -> {
            for(int i=0; i < (total-1); i++)
            {
                randoms[i] = ThreadLocalRandom.current().nextInt(min, max);
            }
        });
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
