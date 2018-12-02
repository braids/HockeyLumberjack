package com.braids.hockey.screen;

import com.braids.hockey.GameInstance;

public class Level2 extends LevelScreen {
    public Level2(GameInstance game) {
        super(game, "level2.tmx");
    }

    @Override
    public void exitLevel() {
        super.exitLevel(new Level3(game));
    }

    @Override
    public void restartLevel () {
        super.restartLevel(new Level2(game));
    }
}
