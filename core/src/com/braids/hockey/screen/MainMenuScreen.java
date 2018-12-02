package com.braids.hockey.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.braids.hockey.GameInstance;

public class MainMenuScreen implements Screen {
    final GameInstance game;

    //Music bgmusic;

    MainMenuIProc iproc;

    public MainMenuScreen(final GameInstance game) {
        this.game = game;

        iproc = new MainMenuIProc(this);
        Gdx.input.setInputProcessor(iproc);
        iproc.blockInput = false;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.batch.begin();
        game.batch.end();
    }

    public void StartGameEvent() {
        //bgmusic.stop();
        game.setScreen(new Level1(game));
        dispose();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
       // bgmusic.dispose();
    }
}
