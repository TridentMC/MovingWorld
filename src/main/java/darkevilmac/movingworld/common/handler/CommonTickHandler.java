package darkevilmac.movingworld.common.handler;

import darkevilmac.movingworld.common.core.util.ITickBasedIterable;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Iterator;

public class CommonTickHandler {

    public static CommonTickHandler INSTANCE;

    private ArrayList<ITickBasedIterable> runningIterables;
    private ArrayList<ITickBasedIterable> newIterables;

    public CommonTickHandler() {
        runningIterables = new ArrayList<ITickBasedIterable>();
        newIterables = new ArrayList<ITickBasedIterable>();

        INSTANCE = this;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            Iterator<ITickBasedIterable> runningIterator = runningIterables.iterator();
            while (runningIterator.hasNext()) {
                ITickBasedIterable tickBasedIterable = runningIterator.next();

                tickBasedIterable.doTick(Side.SERVER);

                if (tickBasedIterable.complete(Side.SERVER))
                    runningIterator.remove();
            }

            for (ITickBasedIterable tickBasedIterable : newIterables) {
                if (tickBasedIterable.begin(Side.SERVER))
                    tickBasedIterable.doTick(Side.SERVER);

                runningIterables.add(tickBasedIterable);
            }
            newIterables.clear();
        }
    }

    public void registerIterable(ITickBasedIterable tickBasedIterable) {
        newIterables.add(tickBasedIterable);
    }

}
