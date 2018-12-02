package com.braids.hockey.object;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.braids.hockey.movement.Displacement;
import com.braids.hockey.movement.MoveVector;
import com.braids.hockey.particle.IceTrailEffect;
import com.braids.hockey.particle.IcingEffect;

import java.util.ArrayList;

public class Skater extends GameObject {
    // Movement
    boolean moveUp = false, moveDown = false, moveLeft = false, moveRight = false;

    public ArrayList<Displacement> displacements = new ArrayList<Displacement>();

    Vector2 moveVec = MoveVector.None.cpy();

    boolean braking = false;

    // Particles
    IcingEffect icingEffect;
    IceTrailEffect trailEffect;

    public Skater(String file, float xpos, float ypos) {
        super(file, xpos, ypos);

        // Collision information
        radius = (sprite.getWidth() + sprite.getHeight() ) / 4;
        lastPos = new Vector2(getWorldOrigin());

        // Acceleration information
        friction = 3f;
        maxdAcc = 250f;

        // Ice trail particle effects
        trailEffect = new IceTrailEffect(getWorldOrigin(), new Vector2(0f, -12f));
        // Icing particle effect
        icingEffect = new IcingEffect(getWorldOrigin(), new Vector2(0f, -12f));
    }

    public void GetMovementInputNormal() {
        // Clear destination vector
        moveVec.set(MoveVector.None);

        if(braking)
            return;

        // Check up/down movement, add angle to destination angle
        if(moveUp && !moveDown)
            moveVec.add(MoveVector.Up);
        else if(moveDown && !moveUp)
            moveVec.add(MoveVector.Down);

        // Check left/right movement, add angle to destination angle
        if(moveLeft && !moveRight)
            moveVec.add(MoveVector.Left);
        else if(moveRight && !moveLeft)
            moveVec.add(MoveVector.Right);

        // Normalize destination vector (for diagonals, no effect on cardinals)
        moveVec.nor();
    }

    public void move() {
        float
                turnSpeed = 5f,
                turnAcc = 10f,
                destAcc = 20f;

        // Determine movement destination normal
        GetMovementInputNormal();

        // If no direction was pressed
        if (moveVec.equals(MoveVector.None)) {
            if (braking)
                trailEffect.setWidth(IceTrailEffect.TrailWidth.WIDE);

            if(braking && !acc.isZero() && !icingEffect.isActive())
                icingEffect.start();
            else if (acc.isZero(1.0f) && icingEffect.isActive())      // End icing effect
                icingEffect.stop();

            dAcc -= braking ? friction * 2f : friction;           // Reduce acceleration
            dAcc = Math.max(dAcc, 0f);  // Set acceleration to 0 if negative

            if (dAcc == 0f || acc.isZero(1.0f)) {               // If movement stopped...
                trailEffect.setWidth(IceTrailEffect.TrailWidth.NARROW);
                acc.set(MoveVector.None);   // Set acceleration to zero
                trailEffect.stop();         // Stop ice trails
            }
            else {
                acc.set(acc.nor().scl(dAcc));   // Set acceleration
                icingEffect.setAngle(acc.angle());
            }
        }
        else {  // If a direction was pressed...
            // If acc is zeroed (no directional acceleration), set to destination vector
            if (acc.equals(MoveVector.None))
                acc.set(moveVec.cpy());

            float destArc = moveVec.angle(acc);         // Get deg arc between current and destination angles
            if (destArc > -0.01f && destArc < 0.01f)    // Fix any tiny rounding errors
                destArc = 0f;

            // Rotate angle to destination vector if not equal
            if(destArc != 0f) {
                boolean sharpTurn = Math.abs(destArc) > 90f;

                /// Turn-specific actions
                // If turning left
                if (destArc > 0f) {
                    // Set new acceleration angle
                    acc.setAngle(acc.angle() - (turnSpeed * (sharpTurn ? 1.3f : 1f)));

                    // If destination angle is passed, set to destination
                    if (destArc < 0f)
                        acc.set(moveVec.cpy());

                    // update icing effect angle
                    icingEffect.setAngleLeft(acc.angle());
                }
                // If turning right
                else {
                    // Set new acceleration angle
                    acc.setAngle(acc.angle() + (turnSpeed * (sharpTurn ? 1.3f : 1f)));

                    // If destination angle is passed, set to destination
                    if (destArc > 0f)
                        acc.set(moveVec.cpy());

                    // update icing effect angle
                    icingEffect.setAngleRight(acc.angle());
                }

                // Increase ice trail width on sharp turn
                if(sharpTurn)
                    trailEffect.setWidth(IceTrailEffect.TrailWidth.WIDE);

                // update icing effect state
                icingEffect.updateState(sharpTurn);
            }
            else {
                if (icingEffect.isActive())
                    icingEffect.stop();
            }

            // Add speed based on angle (faster when going in destination direction)
            dAcc += (destArc == 0f) ? destAcc : turnAcc;
            // Cap max acceleration
            dAcc = Math.min(dAcc, (destArc == 0f) ? maxdAcc : maxdAcc * MathUtils.lerp(1f, 0.2f, Math.abs(destArc/180f)));
            // Set acceleration vector
            acc.nor().scl(dAcc);

            // Start/restart ice trail particle effects
            trailEffect.start();

        }

        // Set icing effect angle
        icingEffect.updateEmitter();

        // Set position after acceleration calculated
        sprite.setPosition(sprite.getX() + (acc.x  * Gdx.graphics.getDeltaTime()),
                sprite.getY() + (acc.y  * Gdx.graphics.getDeltaTime()));
    }

    public void clearMovement() {
        moveUp = false;
        moveDown = false;
        moveLeft = false;
        moveRight = false;
    }

    public void checkCollision(Array<PolygonMapObject> blockingTerrain) {
        for (PolygonMapObject object : blockingTerrain) {
            if (Intersector.overlaps(sprite.getBoundingRectangle(), object.getPolygon().getBoundingRectangle())) {
                float[] verts = object.getPolygon().getTransformedVertices();

                if (Intersector.isPointInPolygon(verts, 0, verts.length, getWorldOrigin().x, getWorldOrigin().y)) {
                    setPosition(lastPos);
                    displacements.clear();
                }

                for (int i = 0; i < verts.length; i += 2) {
                    // Init displacement unit vector
                    Vector2 disp = new Vector2(0f, 0f);
                    // Get displacement vector and length
                    Float length = Intersector.intersectSegmentCircleDisplace(
                            new Vector2(verts[i], verts[(i + 1) % verts.length]),
                            new Vector2(verts[(i + 2) % verts.length], verts[(i + 3) % verts.length]),
                            getWorldOrigin(),
                            radius,
                            disp);

                    // If displacement found, add to displacements
                    if (length != Float.POSITIVE_INFINITY) {
                        displacements.add(new Displacement(disp, radius - length));
                    }
                }
            }
        }
    }

    public void resolveCollisions() {
        if(displacements.size() > 0) {
            for (Displacement d : displacements)
                setPosition(sprite.getX() + d.disp.cpy().scl(d.scl).x,
                        sprite.getY() + d.disp.cpy().scl(d.scl).y);

            displacements.clear();
        }

        // update particles
        trailEffect.setPosition(getWorldOrigin());
        icingEffect.setPosition(getWorldOrigin());
    }

    @Override
    public void draw(PolygonSpriteBatch batch, float delta) {
        trailEffect.draw(batch, delta);
        icingEffect.draw(batch, delta);
        super.draw(batch, delta);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    //// Events

    public void moveLeftEvent(boolean movingLeft) { moveLeft = movingLeft; }

    public void moveRightEvent(boolean movingRight) { moveRight = movingRight; }

    public void moveUpEvent(boolean movingUp) { moveUp = movingUp; }

    public void moveDownEvent(boolean movingDown) { moveDown = movingDown; }

    public void brakeEvent(boolean brake) { braking = brake; }
}
