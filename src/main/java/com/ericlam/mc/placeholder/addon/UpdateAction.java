package com.ericlam.mc.placeholder.addon;

import java.util.UUID;

public class UpdateAction {

    public final UUID player;
    public final String game;

    public UpdateAction(UUID player, String game) {
        this.player = player;
        this.game = game;
    }
}
