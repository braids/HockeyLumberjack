package com.braids.hockey.screen;

import com.braids.hockey.GameInstance;

public class Level3 extends LevelScreen {
    public Level3(GameInstance game) {
        super(game, "level3.tmx");
    }

    @Override
    public void exitLevel() {
        super.exitLevel(new Level1(game));
    }

    @Override
    public void restartLevel () {
        super.restartLevel(new Level3(game));
    }
}
