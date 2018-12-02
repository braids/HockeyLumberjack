package com.braids.hockey.particle;

import com.badlogic.gdx.math.Vector2;

public class IceTrailEffect extends EffectController {
    public enum TrailWidth { NARROW, WIDE }
    ParticleData lTrail, rTrail;

    public IceTrailEffect(Vector2 position, Vector2 offset) {
        lTrail = new ParticleData("iceSkateTrail.p", position, offset);
        rTrail = new ParticleData("iceSkateTrail.p", position, offset);
        pdata.add(lTrail, rTrail);

        // Set initial position
        setOffset(offset);
        setPosition(position);

        lTrail.shiftOffset(-2f, 0f);    // Shift left trail to left of offset point
        rTrail.shiftOffset(3f, 1f);     // Shift right trail right of and slightly up from offset point

        // Set initial width
        setWidth(TrailWidth.NARROW);
    }

    @Override
    public void setPosition(Vector2 pos) {
        super.setPosition(pos);

        // Set particle rotation based on last position
        for (ParticleData particle : pdata)
            particle.effect.getEmitters().first().getRotation().setHigh(
                    particle.position.cpy().sub(particle.lastPosition).angle() + 90f
            );
    }

    public void setWidth(TrailWidth width) {
        float w = 0f;

        switch(width) {
            case NARROW:
                w = 1f;
                break;
            case WIDE:
                w = 2f;
                break;
        }

        // Set ice width
        for (ParticleData particle : pdata)
            particle.effect.getEmitters().first().getXScale().setHigh(w);
    }
}
