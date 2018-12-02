package com.braids.hockey.screen;

import com.braids.hockey.GameInstance;

public class Level1 extends LevelScreen {
    public Level1(GameInstance game) {
        super(game, "level1.tmx");
    }

    @Override
    public void exitLevel() {
        super.exitLevel(new Level2(game));

    }

    @Override
    public void restartLevel () {
        super.restartLevel(new Level1(game));
    }
}
