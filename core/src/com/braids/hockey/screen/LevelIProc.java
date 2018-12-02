package com.braids.hockey.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

public class LevelIProc implements InputProcessor {
    LevelScreen screen;

    public boolean blockInput;

    public LevelIProc(LevelScreen screen) {
        this.screen = screen;
        this.blockInput = true;
    }

    public boolean keyDown(int keycode) {
        if (blockInput)
            return false;

        if(Gdx.input.isKeyJustPressed(keycode)) {
            switch (keycode) {
                case Input.Keys.SPACE:
                    screen.ljack.firePuckEvent();
                    break;
                case Input.Keys.E:
                    screen.toggleDebugDraw();
                    break;
                default:
            }
        }

        switch(keycode) {
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                screen.ljack.brakeEvent(true);
                break;
            case Input.Keys.LEFT:
                screen.ljack.moveLeftEvent(true);
                break;
            case Input.Keys.RIGHT:
                screen.ljack.moveRightEvent(true);
                break;
            case Input.Keys.UP:
                screen.ljack.moveUpEvent(true);
                break;
            case Input.Keys.DOWN:
                screen.ljack.moveDownEvent(true);
                break;
            case Input.Keys.ESCAPE:
                Gdx.app.exit();
                break;
            default:
        }

        return true;
    }

    public boolean keyUp(int keycode) {
        if(blockInput)
            return false;

        switch(keycode) {
            case Input.Keys.SHIFT_LEFT:
            case Input.Keys.SHIFT_RIGHT:
                screen.ljack.brakeEvent(false);
                break;
            case Input.Keys.LEFT:
                screen.ljack.moveLeftEvent(false);
                break;
            case Input.Keys.RIGHT:
                screen.ljack.moveRightEvent(false);
                break;
            case Input.Keys.UP:
                screen.ljack.moveUpEvent(false);
                break;
            case Input.Keys.DOWN:
                screen.ljack.moveDownEvent(false);
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
    public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }
}
