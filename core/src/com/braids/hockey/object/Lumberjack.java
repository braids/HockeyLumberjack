package com.braids.hockey.object;

import com.braids.hockey.movement.Displacement;
import com.braids.hockey.movement.MoveVector;
import com.braids.hockey.particle.IceTrailEffect;
import com.braids.hockey.particle.IcingEffect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

import java.util.*;

class Stick extends GameObject {
    public Stick() {
        super("stick.png");
    }
}

public class Lumberjack extends Skater {
    Sound shootSound;

    // Pucks
    public PuckPool ppool = new PuckPool();
    boolean firePuck = false;

    // Stick
    Stick stick = new Stick();

    public Lumberjack(float xPos, float yPos) {
        super("hockeyman.png", xPos, yPos);

        shootSound =  Gdx.audio.newSound(Gdx.files.internal("blip.wav"));

        // Initial stick position
        stick.setPositionAtOrigin(getWorldOrigin());
    }

    public void Update() {
        // Update last world space position
        lastPos = getWorldOrigin();

        // Reset width of ice trail effect to narrow
        trailEffect.setWidth(IceTrailEffect.TrailWidth.NARROW);

        // Perform movement actions
        move();

        // Update pucks
        ppool.update();

        // Fire puck
        if(firePuck) {
            firePuck = false;
            shootSound.play(16f);
            ppool.spawnPuck(getWorldOrigin(), getWorldOrigin().sub(lastPos).nor());
        }
    }

    @Override
    public void checkCollision(Array<PolygonMapObject> blockingTerrain) {
        super.checkCollision(blockingTerrain);

        // Update stick position
        stick.setPosition(getWorldOrigin().x - 8f, getWorldOrigin().y - getOrigin().y);

        // Run puck collision updateEmitter
        ppool.checkCollisions(blockingTerrain);
    }

    public boolean checkGoal(Rectangle rect) {
        if(sprite.getBoundingRectangle().overlaps(rect))
            return true;

        return false;
    }

    @Override
    public void draw(PolygonSpriteBatch batch, float delta) {
        super.draw(batch, delta);
        ppool.draw(batch, delta);
        stick.draw(batch, delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        shootSound.dispose();
        ppool.dispose();
    }

    //// Events

    public void firePuckEvent() { firePuck = true; }
}
