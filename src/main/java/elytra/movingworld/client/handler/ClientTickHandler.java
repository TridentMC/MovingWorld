package elytra.movingworld.client.handler;

import elytra.movingworld.common.core.util.ITickingTask;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Iterator;

public class ClientTickHandler {

    public static ClientTickHandler INSTANCE;

    private ArrayList<ITickingTask> runningIterables;
    private ArrayList<ITickingTask> newIterables;

    public ClientTickHandler() {
        runningIterables = new ArrayList<ITickingTask>();
        newIterables = new ArrayList<ITickingTask>();

        INSTANCE = this;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            Iterator<ITickingTask> runningIterator = runningIterables.iterator();
            while (runningIterator.hasNext()) {
                ITickingTask tickBasedIterable = runningIterator.next();

                tickBasedIterable.doTick(Side.CLIENT);

                if (tickBasedIterable.complete(Side.CLIENT))
                    runningIterator.remove();
            }

            for (ITickingTask tickBasedIterable : newIterables) {
                if (tickBasedIterable.begin(Side.CLIENT))
                    tickBasedIterable.doTick(Side.CLIENT);

                runningIterables.add(tickBasedIterable);
            }
            newIterables.clear();
        }
    }

    public void registerIterable(ITickingTask tickBasedIterable) {
        newIterables.add(tickBasedIterable);
    }
}
