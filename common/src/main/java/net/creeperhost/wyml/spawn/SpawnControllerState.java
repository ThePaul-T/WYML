package net.creeperhost.wyml.spawn;

/**
 * Pure state machine for one server/dimension/chunk/category controller.
 * Tick values use Minecraft's wrapping signed counter.
 */
public final class SpawnControllerState
{
    private ControllerState state = ControllerState.ACTIVE;
    private int stateSinceTick;
    private int backoffTicks;
    private int probeAttemptLimit = 1;
    private int probeResumePercent;
    private int probeAttempts;
    private int probeCompleted;
    private int probeSuccesses;

    public ControllerState current(int currentTick)
    {
        if (state == ControllerState.BACKOFF && TickExpiry.hasElapsed(stateSinceTick, currentTick, backoffTicks))
        {
            enterProbe(currentTick);
        }
        return state;
    }

    public void throttle(int currentTick)
    {
        if (state == ControllerState.ACTIVE)
        {
            transition(ControllerState.THROTTLED, currentTick);
        }
    }

    public void activate(int currentTick)
    {
        transition(ControllerState.ACTIVE, currentTick);
        resetProbe();
    }

    public void backoff(int currentTick, int durationTicks, int probeAttempts, int resumePercent)
    {
        this.backoffTicks = Math.max(1, durationTicks);
        this.probeAttemptLimit = Math.max(1, probeAttempts);
        this.probeResumePercent = Math.max(0, Math.min(100, resumePercent));
        resetProbe();
        transition(ControllerState.BACKOFF, currentTick);
    }

    public boolean tryAcquireAttempt(int currentTick)
    {
        ControllerState current = current(currentTick);
        if (current == ControllerState.BACKOFF)
        {
            return false;
        }
        if (current == ControllerState.PROBE)
        {
            if (probeAttempts >= probeAttemptLimit)
            {
                return false;
            }
            probeAttempts++;
        }
        return true;
    }

    public void recordOutcome(boolean success, int currentTick)
    {
        if (current(currentTick) != ControllerState.PROBE)
        {
            return;
        }

        probeCompleted++;
        if (success) probeSuccesses++;
        if (probeCompleted < probeAttemptLimit)
        {
            return;
        }

        if ((long) probeSuccesses * 100L >= (long) probeResumePercent * probeCompleted)
        {
            activate(currentTick);
        }
        else
        {
            transition(ControllerState.BACKOFF, currentTick);
            resetProbe();
        }
    }

    public boolean blocksCategory(int currentTick)
    {
        return current(currentTick) == ControllerState.BACKOFF;
    }

    public boolean canEnterBackoff(int currentTick)
    {
        return current(currentTick) == ControllerState.THROTTLED;
    }

    public int ticksInState(int currentTick)
    {
        return (int) Math.min(Integer.MAX_VALUE, Integer.toUnsignedLong(currentTick - stateSinceTick));
    }

    private void enterProbe(int currentTick)
    {
        resetProbe();
        transition(ControllerState.PROBE, currentTick);
    }

    private void transition(ControllerState next, int currentTick)
    {
        state = next;
        stateSinceTick = currentTick;
    }

    private void resetProbe()
    {
        probeAttempts = 0;
        probeCompleted = 0;
        probeSuccesses = 0;
    }
}
