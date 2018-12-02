package com.braids.hockey.object;

import com.braids.hockey.movement.MoveVector;
import com.braids.hockey.particle.IceTrailEffect;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.braids.hockey.movement.AStar;

public class Nazi extends Skater {
    final TiledMapTileLayer terrain;
    final boolean[] blockMap;
    AStar astar;
    public IntArray path = new IntArray();
    public Array<Vector2> smoothPath = new Array<Vector2>();
    Lumberjack target;

    Sound dieSound, killSound;

    float recalcTimer = 0f;

    public Nazi(float x, float y, Lumberjack target, TiledMapTileLayer tLayer, boolean[] bMap) {
        super("nazi.png", x, y);

        this.target = target;

        this.blockMap = bMap;
        this.terrain = tLayer;

        dieSound = Gdx.audio.newSound(Gdx.files.internal("buy.wav"));
        killSound = Gdx.audio.newSound(Gdx.files.internal("buzz.wav"));

        maxdAcc = 100f;

        astar = new AStar(terrain.getWidth(), terrain.getHeight()) {
            protected boolean isValid(int x, int y) {
                return blockMap[x + y * terrain.getWidth()];
            }
        };
    }

    public void update() {
        // Clear movement input
        clearMovement();

        // update last world space position
        lastPos = getWorldOrigin();

        // Reset width of ice trail effect to narrow
        trailEffect.setWidth(IceTrailEffect.TrailWidth.NARROW);

        // If no current path and..
        if (smoothPath.size == 0) {
            // If target is close enough to self..
            if (getWorldOrigin().sub(target.getWorldOrigin()).len() < 200f) {
                // Get a* path to target
                path = astar.getPath(
                        (int) (getWorldOrigin().x / terrain.getTileWidth()),
                        (int) (getWorldOrigin().y / terrain.getTileHeight()),
                        (int) (target.getWorldOrigin().x / terrain.getTileWidth()),
                        (int) (target.getWorldOrigin().y / terrain.getTileHeight()));

                // Store smoothed out path
                smoothPath = astar.smoothPath;

                // If path size is still 0 although we are near target..
                if (smoothPath.size == 0) {
                    // Clear path (???)
                    path.clear();
                    // Add in target's position as next path location
                    path.add((int) target.getWorldOrigin().x, (int)target.getWorldOrigin().y);
                    smoothPath.add(target.getWorldOrigin());
                }
            }
        }
        // If there is a path..
        else {
            // Get next position from end of path
            Vector2 nextPos = new Vector2(smoothPath.peek().x, smoothPath.peek().y);

            // Scale path position back up to world coords
            nextPos.scl(terrain.getTileWidth(),terrain.getTileHeight());

            // If distance to next position is small enough..
            if (getWorldOrigin().sub(nextPos).len() < 5f) {
                // Remove next position
                smoothPath.pop();

                // Bail if no more path locations
                if(smoothPath.size <= 0)
                    return;

                // Get next position from end of path
                nextPos = new Vector2(smoothPath.peek().x, smoothPath.peek().y);

                // Scale path position back up to world coords
                nextPos.scl(terrain.getTileWidth(), terrain.getTileHeight());
            }

            // Get direction vector to next position
            Vector2 toNextPos = nextPos.sub(getWorldOrigin());

            if (Math.abs(MoveVector.Up.angle(toNextPos)) < 60f)
                moveUp = true;
            if (Math.abs(MoveVector.Down.angle(toNextPos)) < 60f)
                moveDown = true;
            if (Math.abs(MoveVector.Left.angle(toNextPos)) < 60f)
                moveLeft = true;
            if (Math.abs(MoveVector.Right.angle(toNextPos)) < 60f)
                moveRight = true;
        }

        // Perform movement actions
        move();

        // Recalculate path if the current path has taken too long
        recalcTimer += Gdx.graphics.getDeltaTime();
        if(recalcTimer >= 1f) {
            path.clear();
            smoothPath.clear();
            recalcTimer = 0f;
        }
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
