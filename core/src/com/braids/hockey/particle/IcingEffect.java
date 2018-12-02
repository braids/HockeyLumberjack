package com.braids.hockey.particle;

import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.math.Vector2;

/**
 * Particle effect that simulates an ice spray from an ice skate.
 */
public class IcingEffect extends EffectController {
    ParticleData icingData;
    float angle = 0f;

    /**
     * Constructor for ice spray particle effect controller.
     * @param position Initial world position to spawn effect at.
     * @param offset Relative offset position from the world position.
     */
    public IcingEffect(Vector2 position, Vector2 offset) {
        icingData = new ParticleData("icingEffect.p", position, offset);
        pdata.add(icingData);
    }

    /**
     * Sets the ice spray ang to the specified angle.
     * @param angle Forward angle for ice spray.
     */
    public void setAngle(float angle) { this.angle = angle; }

    /**
     * Sets the ice spray angle 90 degrees counter-clockwise from the forward angle.
     * @param forwardAngle Forward angle to rotate ice spray from.
     */
    public void setAngleLeft(float forwardAngle) { angle = forwardAngle + 90f; }

    /**
     * Sets the ice spray angle 90 degrees clockwise from the forward angle.
     * @param forwardAngle Forward angle to rotate ice spray from.
     */
    public void setAngleRight(float forwardAngle) { angle = forwardAngle - 90f; }

    /**
     * Sets the active state of the particle. <br><br>
     *
     * - If activeState is set to true and the effect is not active, start the effect. <br>
     * - If activeState is set to false and the effect is active, stop the effect. <br>
     * - If the previous conditions are not met, no action occurs.
     * @param activeState The desired active state. True if active, false if inactive.
     */
    public void updateState(boolean activeState) {
        if(activeState && !isActive())
            start();
        else if (!activeState && isActive())
            stop();
    }

    /**
     * Updates the ice spray emitter angles to spray in the general direction
     * of the current ice spray angle.
     */
    public void updateEmitter() {
        ParticleEmitter emitter = icingData.effect.getEmitters().first();
        emitter.getAngle().setHigh(angle - 30f, angle + 30f);
        emitter.getAngle().setLow(angle - 80f, angle + 80f);
    }
}
