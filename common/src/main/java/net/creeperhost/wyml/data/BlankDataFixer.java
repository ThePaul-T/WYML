package net.creeperhost.wyml.data;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;

import java.util.concurrent.Executor;

public class BlankDataFixer extends DataFixerBuilder
{
    public BlankDataFixer(int dataVersion)
    {
        super(dataVersion);
    }

}
