package com.braids.hockey.object;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import net.dermetfan.gdx.math.MathUtils;

public class GameObject {
    Texture texture;
    PolygonRegion region;
    public PolygonSprite sprite;

    public float
            friction = 0f,
            dAcc = 0f,
            maxdAcc = 0f,
            radius = 0f;
    Vector2 acc = new Vector2(0f, 0f),
            maxAcc = null,
            lastPos = new Vector2(0f, 0f),
            resetPos = new Vector2(0f, 0f);

    public GameObject(String imagePath) {
        this(imagePath, 0.0f, 0.0f);
    }

    public GameObject(String imagePath, float xPos, float yPos) {
        resetPos.set(xPos, yPos);

        texture = new Texture(imagePath);

        region = new PolygonRegion(new TextureRegion(texture),
                new float[] {
                        0f, 0f,
                        0f, texture.getHeight(),
                        texture.getWidth(), texture.getHeight(),
                        texture.getWidth(), 0f},
                new short[] {
                        0, 1, 2,
                        0, 2, 3
                });
        sprite = new PolygonSprite(region);
        sprite.setPosition(xPos, yPos);
        sprite.setOrigin(texture.getWidth() / 2f, texture.getHeight() / 2f);
    }

    public Vector2 getOrigin() {
        return new Vector2(sprite.getOriginX(), sprite.getOriginY());
    }

    public Vector2 getWorldOrigin() {
        return new Vector2(sprite.getX() + sprite.getOriginX(), sprite.getY() + sprite.getOriginY());
    }

    public void setPosition(Vector2 v) {
        setPosition(v.x, v.y);
    }

    public void setPosition(float x, float y) {
        sprite.setPosition(x, y);
    }

    public void setPositionAtOrigin(Vector2 v) {
        setPosition(v.x, v.y);
    }

    public void setPositionAtOrigin(float x, float y) {
        sprite.setPosition(x - sprite.getWidth() / 2f, y - sprite.getHeight() / 2f);
    }

    public void setAcc(float dx, float dy) {
        acc.set(dx, dy);
        if(maxAcc != null) {
            if (Math.abs(acc.len()) > Math.abs(maxAcc.len())) {
                acc.x *= Math.abs(maxAcc.len()) / acc.len();
                acc.y *= Math.abs(maxAcc.len()) / acc.len();
            }
        }
    }

    public void setAccX(float dx) {  setAcc(dx, acc.y); }

    public void setAccY(float dy) {  setAcc(acc.x, dy); }

    public void addAcc(float dx, float dy) {
        acc.add(dx, dy);
        if(maxAcc != null) {
            if (Math.abs(acc.len()) > Math.abs(maxAcc.len())) {
                acc.x *= Math.abs(maxAcc.len()) / acc.len();
                acc.y *= Math.abs(maxAcc.len()) / acc.len();
            }
        }
    }

    public void addAccX(float dx) {  addAcc(dx, 0f); }

    public void addAccY(float dy) {  addAcc(0f, dy); }

    public void reset() {
        setPosition(resetPos.x, resetPos.y);
        sprite.setRotation(0.0f);
        acc.set(0.0f, 0.0f);
    }

    // Collision methods

    public boolean boundingBoxOverlaps(Rectangle rect) {
        return Intersector.overlaps(sprite.getBoundingRectangle(), rect);
    }

    public boolean crossingSegment(Vector2 begin, Vector2 end, Vector2 collisionPoint) {
        return Intersector.intersectSegments(
                lastPos.x, lastPos.y,
                getWorldOrigin().x, getWorldOrigin().y,
                begin.x, begin.y,
                end.x, end.y,
                collisionPoint);
    }

    public void dispose() {
        texture.dispose();
    }

    public void draw(PolygonSpriteBatch batch, float delta) {
        sprite.draw(batch);
    }
}
