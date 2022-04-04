package net.frozenorb.potpvp.nametag;

import lombok.Getter;
import net.frozenorb.potpvp.PotPvPSI;
import net.frozenorb.potpvp.nametag.construct.NametagUpdate;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class NametagThread extends Thread {

    private final Map<NametagUpdate, Boolean> pendingUpdates = new ConcurrentHashMap<>();

    public NametagThread() {
        super("PotPvPSI - NameTags Thread");
        setDaemon(false);
    }

    public void run() {
        while (true) {
            Iterator<NametagUpdate> pendingUpdatesIterator = pendingUpdates.keySet().iterator();

            while(pendingUpdatesIterator.hasNext()) {
                NametagUpdate pendingUpdate = pendingUpdatesIterator.next();

                try {
                    PotPvPSI.getInstance().getNameTagEngine().applyUpdate(pendingUpdate);
                    pendingUpdatesIterator.remove();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(2 * 50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}