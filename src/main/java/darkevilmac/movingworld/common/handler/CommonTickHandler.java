package darkevilmac.movingworld.common.handler;

import darkevilmac.movingworld.common.core.util.ITickingTask;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Iterator;

public class CommonTickHandler {

    public static CommonTickHandler INSTANCE;

    private ArrayList<ITickingTask> runningIterables;
    private ArrayList<ITickingTask> newIterables;

    public CommonTickHandler() {
        runningIterables = new ArrayList<ITickingTask>();
        newIterables = new ArrayList<ITickingTask>();

        INSTANCE = this;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (event.phase.equals(TickEvent.Phase.START)) {
            Iterator<ITickingTask> runningIterator = runningIterables.iterator();
            while (runningIterator.hasNext()) {
                ITickingTask tickBasedIterable = runningIterator.next();

                tickBasedIterable.doTick(Side.SERVER);

                if (tickBasedIterable.complete(Side.SERVER))
                    runningIterator.remove();
            }

            for (ITickingTask tickBasedIterable : newIterables) {
                if (tickBasedIterable.begin(Side.SERVER))
                    tickBasedIterable.doTick(Side.SERVER);

                runningIterables.add(tickBasedIterable);
            }
            newIterables.clear();
        }
    }

    public void registerIterable(ITickingTask tickBasedIterable) {
        newIterables.add(tickBasedIterable);
    }

}
