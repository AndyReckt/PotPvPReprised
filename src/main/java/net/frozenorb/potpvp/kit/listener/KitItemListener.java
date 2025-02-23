package net.frozenorb.potpvp.kit.listener;

import net.frozenorb.potpvp.PotPvPRP;
import net.frozenorb.potpvp.kit.KitItems;
import net.frozenorb.potpvp.kit.menu.kits.KitsMenu;
import net.frozenorb.potpvp.kit.kittype.menu.select.SelectKitTypeMenu;
import net.frozenorb.potpvp.lobby.LobbyHandler;
import net.frozenorb.potpvp.util.ItemListener;

public final class KitItemListener extends ItemListener {

    public KitItemListener() {
        addHandler(KitItems.OPEN_EDITOR_ITEM, player -> {
            LobbyHandler lobbyHandler = PotPvPRP.getInstance().getLobbyHandler();

            if (lobbyHandler.isInLobby(player)) {
                new SelectKitTypeMenu(true, kitType -> {
                    new KitsMenu(kitType).openMenu(player);
                }, "Select a kit to edit...").openMenu(player);
            }
        });
    }

}