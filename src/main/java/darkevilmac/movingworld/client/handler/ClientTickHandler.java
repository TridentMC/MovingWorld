package darkevilmac.movingworld.client.handler;

import darkevilmac.movingworld.common.core.util.ITickBasedIterable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Iterator;

public class ClientTickHandler {

    public static ClientTickHandler INSTANCE;

    private ArrayList<ITickBasedIterable> runningIterables;
    private ArrayList<ITickBasedIterable> newIterables;

    public ClientTickHandler() {
        runningIterables = new ArrayList<ITickBasedIterable>();
        newIterables = new ArrayList<ITickBasedIterable>();

        INSTANCE = this;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            Iterator<ITickBasedIterable> runningIterator = runningIterables.iterator();
            while (runningIterator.hasNext()) {
                ITickBasedIterable tickBasedIterable = runningIterator.next();

                tickBasedIterable.doTick(Side.CLIENT);

                if (tickBasedIterable.complete(Side.CLIENT))
                    runningIterator.remove();
            }

            for (ITickBasedIterable tickBasedIterable : newIterables) {
                if (tickBasedIterable.begin(Side.CLIENT))
                    tickBasedIterable.doTick(Side.CLIENT);

                runningIterables.add(tickBasedIterable);
            }
            newIterables.clear();
        }
    }

    public void registerIterable(ITickBasedIterable tickBasedIterable) {
        newIterables.add(tickBasedIterable);
    }
}
