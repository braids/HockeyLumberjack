package com.braids.hockey.object;

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class PuckPool {
    Array<Puck> pucks = new Array<Puck>();
    Array<Puck> inactive = new Array<Puck>();
    Array<Puck> active = new Array<Puck>();

    int maxPucks = 10;

    public PuckPool() {
        for(int i = 0; i < maxPucks; i++)
            pucks.add(new Puck());

        inactive.addAll(pucks);
    }

    public void spawnPuck(Vector2 pos, Vector2 acc) {
        if (inactive.size == 0) {           /// If no inactive pucks left, remove oldest puck
            active.sort(Puck.timeAsc);      // Sort by timeAlive descending
            active.peek().setInactive();    // Set oldest puck to inactive state
            inactive.add(active.pop());     // Move oldest puck to inactive array
        }

        inactive.peek().spawn(pos, acc);    // Get and spawn inactive puck
        active.add(inactive.pop());         // Move puck to active array
    }

    public void update() {
        for(int i = 0; i < active.size; i++) {
            active.get(i).update();     // Update each active puck
            if (cullInactive(i)) i--;   // If puck is no longer active, cull puck and check index again
        }
    }

    public void draw(PolygonSpriteBatch batch, float delta) {
        for(int i = 0; i < active.size; i++)
            active.get(i).draw(batch, delta);
    }

    public boolean cullInactive(int index) {
        if (active.get(index).isActive())   // Ignore index if puck is active
            return false;

        inactive.add(active.get(index));    // Add inactive puck to inactive array
        active.removeIndex(index);          // Remove inactive puck from active array
        return true;
    }

    public void checkCollisions(Array<PolygonMapObject> blockingTerrain) {
        for (Puck puck : active) {
            if (puck.bouncing) {
                puck.bouncing = false;
                continue;
            }

            for (int j = 0; j < blockingTerrain.size; j++) {
                PolygonMapObject object = blockingTerrain.get(j);

                if (puck.boundingBoxOverlaps(object.getPolygon().getBoundingRectangle())) {
                    float[] verts = object.getPolygon().getTransformedVertices();

                    if (!Intersector.isPointInPolygon(verts, 0, verts.length, puck.getWorldOrigin().x, puck.getWorldOrigin().y))
                        continue;

                    for (int i = 0; i < verts.length; i += 2) {
                        Vector2
                                vert1 = new Vector2(verts[i], verts[(i + 1) % verts.length]),
                                vert2 = new Vector2(verts[(i + 2) % verts.length], verts[(i + 3) % verts.length]);

                        // Get point of collision where puck will reflect
                        if (puck.reflectingOffSegment(vert1, vert2)) {
                            break; // Exit segment loop
                        }
                    }
                }
            }
        }
    }

    public void resolveCollisions() {

    }

    public void dispose() {
        for (Puck puck : pucks) puck.dispose();
        for (Puck puck : inactive) puck.dispose();
        for (Puck puck : active) puck.dispose();
    }
}
