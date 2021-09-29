package net.creeperhost.wyml;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class WYMLRandom
{
    private static int total;
    private static int current;
    private static int curRandom;
    private static int[] randoms;

    public static void generate(int min, int max, int total)
    {
        WYMLRandom.total = total;
        curRandom = ThreadLocalRandom.current().nextInt(min, max);
        randoms = new int[total];
        CompletableFuture.runAsync(() ->
        {
            for (int i = 0; i < (total - 1); i++)
            {
                randoms[i] = ThreadLocalRandom.current().nextInt(min, max);
            }
            return;
        });
    }

    public static int get() throws Exception
    {
        if (current >= total) throw new Exception("Too many randoms!");
        int rand = curRandom;
        curRandom = randoms[current];
        current++;
        return rand;
    }
}
