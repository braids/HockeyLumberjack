package com.braids.hockey.object;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;

import java.util.Comparator;

public class Puck extends GameObject {
    boolean active = false;
    boolean bouncing = false;
    float timeActive = 0f;

    public static Comparator<Puck> timeAsc = new Comparator<Puck>() {
        @Override
        public int compare(Puck o1, Puck o2) {
            if (o1.timeActive == o2.timeActive) return 0;
            return o1.timeActive < o2.timeActive ? -1 : 1;
        }
    };

    public static Comparator<Puck> timeDesc = new Comparator<Puck>() {
        @Override
        public int compare(Puck o1, Puck o2) {
            if (o1.timeActive == o2.timeActive) return 0;
            return o1.timeActive > o2.timeActive ? -1 : 1;
        }
    };

    public Puck() {
        super("puck.png");

        // Physics properties
        friction = 35f;
        radius = 4f;
    }

    public void spawn(Vector2 pos, Vector2 acc) {
        this.setPosition(pos);
        this.acc.set(acc);

        active = true;
        dAcc = 350f;
        timeActive = 0f;
    }

    public void update() {
        if (!active)    // Bail if not active
            return;

        lastPos.set(getWorldOrigin());

        timeActive += Gdx.graphics.getDeltaTime();  // Increase timeActive counter

        acc.nor().scl(dAcc);    // Get new acceleration vector and adjust position
        setPositionAtOrigin(getWorldOrigin().x + (acc.x  * Gdx.graphics.getDeltaTime()),
                            getWorldOrigin().y + (acc.y  * Gdx.graphics.getDeltaTime()));

        dAcc -= friction * Gdx.graphics.getDeltaTime(); // Decrease velocity by friction

        if (dAcc <= 0f)     // If puck is stopped, set inactive
            setInactive();
    }

    public boolean reflectingOffSegment(Vector2 begin, Vector2 end) {
        Vector2 collisionPoint = new Vector2();

        if (crossingSegment(begin, end, collisionPoint)){
            Vector2 d = collisionPoint.cpy().sub(lastPos);        // Pre-reflect move distance
            Vector2 b = new Vector2(end.x - begin.x, end.y - begin.y);//end.cpy().sub(begin);                     // Segment distance
            Vector2 n = b.rotate(b.angle(d) > 0 ? 90 : -90).nor();// Segment normal at collision
            Vector2 r = d.sub(n.scl(d.dot(n) * 2f));              // Reflect distance

            bouncing = true;            // Set current bouncing state
            acc.setAngle(r.angle());    // Set acceleration angle to reflected angle
            lastPos.set(collisionPoint);// Set last position to reflect point

            return true;
        }

        return false;
    }

    public boolean isActive() { return active; }

    public void setInactive() {
        this.active = false;
    }
}
