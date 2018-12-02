package com.braids.hockey.particle;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Class that contains a particle effect and positional information.
 */
class ParticleData {
    public ParticleEffect effect = new ParticleEffect();
    public Vector2 position = new Vector2(),
                    offset = new Vector2(),
                    lastPosition = new Vector2();

    public ParticleData(String file, Vector2 position, Vector2 offset) {
        effect.load(Gdx.files.internal(file), Gdx.files.internal(""));

        this.position.set(position);
        this.offset.set(offset);
        this.lastPosition.set(this.position);

        effect.setPosition(this.position.x, this.position.y);
    }

    public void shiftOffset(float x, float y) {
        offset.add(x, y);
    }
}

/**
 * Abstract class to be extended by unique particle effects. Provides
 * support for multiple particle management with one effect.
 */
public abstract class EffectController {
    Array<ParticleData> pdata = new Array<ParticleData>();

    /**
     * Draw all particle effects to sprite batch.
     * @param batch Sprite batch to draw particle effects to.
     * @param delta Delta time between frames.
     */
    public void draw(PolygonSpriteBatch batch, float delta) {
        for(ParticleData particle : pdata)
            particle.effect.draw(batch, delta);
    }

    public void setOffset(Vector2 offset) {
        for(ParticleData particle : pdata)
            particle.offset.set(offset);
    }

    public void shiftOffset(int index, Vector2 shift) {
        pdata.get(index).offset.add(shift);
    }

    public void setPosition(Vector2 position) {
        for(ParticleData particle : pdata) {
            particle.lastPosition.set(particle.position);
            particle.position.set(position).add(particle.offset);
            particle.effect.setPosition(particle.position.x, particle.position.y);
        }
    }

    public void start() {
        for(ParticleData particle : pdata) {
            if (particle.effect.isComplete())
                particle.effect.reset();
            else
                particle.effect.start();
        }
    }

    public void start(int index) {
        ParticleData particle = pdata.get(index);

        if (particle.effect.isComplete())
            particle.effect.reset();
        else
            particle.effect.start();
    }

    public void stop() {
        for(ParticleData particle : pdata) {
            if (!particle.effect.isComplete())
                particle.effect.allowCompletion();
        }
    }

    public void stop(int index) {
        ParticleData particle = pdata.get(index);

        if (!particle.effect.isComplete())
                particle.effect.allowCompletion();

    }

    public boolean isActive() {
        boolean active = false;

        for (ParticleData particle : pdata)
            active = !particle.effect.isComplete() || active;

        return active;
    }
}
