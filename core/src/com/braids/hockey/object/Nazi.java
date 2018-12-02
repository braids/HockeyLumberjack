package com.braids.hockey.object;

import com.braids.hockey.particle.IceTrailEffect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.braids.hockey.movement.AStar;

public class Nazi extends GameObject {
    final TiledMapTileLayer terrain;
    final boolean[] blockMap;
    AStar astar;
    public IntArray path = new IntArray();
    public Array<Vector2> smoothPath = new Array<Vector2>();
    Lumberjack target;

    IceTrailEffect trailEffect;

    Sound dieSound, killSound;

    float recalcTimer = 0f;


    public Nazi(float x, float y, Lumberjack target, TiledMapTileLayer tLayer, boolean[] bMap) {
        super("nazi.png", x, y);

        this.target = target;

        radius = (sprite.getWidth() + sprite.getHeight() ) / 4;

        this.blockMap = bMap;
        this.terrain = tLayer;

        dieSound = Gdx.audio.newSound(Gdx.files.internal("buy.wav"));
        killSound = Gdx.audio.newSound(Gdx.files.internal("buzz.wav"));

        dAcc = 0f;
        maxdAcc = 125f;

        astar = new AStar(terrain.getWidth(), terrain.getHeight()) {
            protected boolean isValid(int x, int y) {
                return blockMap[x + y * terrain.getWidth()];
            }
        };

        // Ice trail particle effects
        trailEffect = new IceTrailEffect(getWorldOrigin(), new Vector2(0f, -12f));

    }

    public void update() {
        // If no path to target found, skip updateEmitter
        if (smoothPath.size == 0) {

            if (getWorldOrigin().sub(target.getWorldOrigin()).len() < 200f) {
                path = astar.getPath(
                        (int) (getWorldOrigin().x / terrain.getTileWidth()),
                        (int) (getWorldOrigin().y / terrain.getTileHeight()),
                        (int) (target.getWorldOrigin().x / terrain.getTileWidth()),
                        (int) (target.getWorldOrigin().y / terrain.getTileHeight()));

                smoothPath = astar.smoothPath;

                if (smoothPath.size == 0) {
                    path.clear();
                    path.add((int) target.getWorldOrigin().x, (int)target.getWorldOrigin().y);
                    smoothPath.add(target.getWorldOrigin());
                }
            }

            dAcc -= 2f;
            dAcc = Math.max(dAcc, 0f);

            acc.nor().scl(dAcc);
        }
        else {
            Vector2 nextPos = new Vector2(smoothPath.peek().x, smoothPath.peek().y);

            // Scale back up to world coords
            nextPos.scl(terrain.getTileWidth(),terrain.getTileHeight());

            if (getWorldOrigin().sub(nextPos).len() < 5f) {
                smoothPath.pop();

                if(smoothPath.size <= 0)
                    return;

                nextPos = new Vector2(smoothPath.peek().x, smoothPath.peek().y);

                // Scale back up to world coords
                nextPos.scl(terrain.getTileWidth(), terrain.getTileHeight());
            }

            Vector2 toNextPos = nextPos.sub(getWorldOrigin());

            dAcc += 5f;
            dAcc = Math.min(dAcc, maxdAcc);

            toNextPos.nor().scl(dAcc);

            acc.set(toNextPos);
        }

        sprite.setPosition(sprite.getX() + (acc.x  * Gdx.graphics.getDeltaTime()),
                           sprite.getY() + (acc.y  * Gdx.graphics.getDeltaTime()));

        recalcTimer += Gdx.graphics.getDeltaTime();
        if(recalcTimer >= 1f) {
            path.clear();
            smoothPath.clear();
            recalcTimer = 0f;
        }

        // Start/restart ice trail particle effects
        if (acc.isZero())
            trailEffect.stop();
        else
            trailEffect.start();
        trailEffect.setPosition(getWorldOrigin());

        // Reset width of ice trail effect to narrow
        trailEffect.setWidth(IceTrailEffect.TrailWidth.NARROW);
    }

    public boolean checkPuckCollision(PuckPool ppool) {
        for (Puck puck : ppool.active) {
            float minDist = radius + puck.radius;
            if(getWorldOrigin().sub(puck.getWorldOrigin()).len() < minDist) {
                puck.setInactive();
                dieSound.play(16f);
                return true;
            }
        }

        return false;
    }

    @Override
    public void draw(PolygonSpriteBatch batch, float delta) {
        trailEffect.draw(batch, delta);
        super.draw(batch, delta);
    }

    @Override
    public void dispose() {
        super.dispose();
        dieSound.dispose();
    }

    public boolean checkPlayerCollision(Lumberjack ljack) {
        float minDist = radius + ljack.radius;
        if(getWorldOrigin().sub(ljack.getWorldOrigin()).len() < minDist) {
            killSound.play(16f);
            return true;
        }

        return false;
    }
}
