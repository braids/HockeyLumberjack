package com.braids.hockey.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

public class MainMenuIProc implements InputProcessor {
    MainMenuScreen screen;

    public boolean blockInput;

    public MainMenuIProc(MainMenuScreen screen) {
        this.screen = screen;
        this.blockInput = true;
    }

    public boolean keyDown(int keycode) {
        if (blockInput)
            return false;

        if(Gdx.input.isKeyJustPressed(keycode)) {
            switch (keycode) {
                case Input.Keys.SPACE:
                    screen.StartGameEvent();
                    break;
                default:
            }
        }

        switch(keycode) {
            case Input.Keys.LEFT:
                break;
            case Input.Keys.RIGHT:
                break;
            case Input.Keys.DOWN:
                break;
            case Input.Keys.UP:
                break;
            default:
        }

        return true;
    }

    public boolean keyUp(int keycode) {
        if(blockInput)
            return false;

        switch(keycode) {
            case Input.Keys.LEFT:
                break;
            case Input.Keys.RIGHT:
                break;
            case Input.Keys.DOWN:
                break;
            case Input.Keys.UP:
                break;
            default:
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        //if(Gdx.input.justTouched()) {
        //mainMenuScreen.startGameEvent();
        return true;
        //}

        //return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }
}

