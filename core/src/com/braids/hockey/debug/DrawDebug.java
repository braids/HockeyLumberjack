package com.braids.hockey.debug;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.utils.Array;
import com.braids.hockey.object.GameObject;
import com.braids.hockey.object.Lumberjack;

public class DrawDebug {
    ShapeRenderer srend = new ShapeRenderer();
    Array<Array<PolygonMapObject>> mapObjects = new Array<Array<PolygonMapObject>>();
    Array<GameObject> gameObjects = new Array<GameObject>();

    ShapeRenderer.ShapeType shapeType = ShapeRenderer.ShapeType.Line;

    Camera camera;
    Lumberjack ljack;

    boolean drawEnabled = false;

    public DrawDebug() {

    }

    public DrawDebug(boolean drawEnabled) {
        this.drawEnabled = drawEnabled;
    }

    public void begin() {
        srend.begin();
        srend.setProjectionMatrix(camera.combined);
    }

    public void begin(ShapeRenderer.ShapeType shapeType) {
        srend.begin(shapeType);
        srend.setProjectionMatrix(camera.combined);
    }

    public void end() {
        srend.end();
    }

    //// Setters

    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void setPlayer(Lumberjack ljack) {
        this.ljack = ljack;
    }

    public void setShapeType(ShapeRenderer.ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public void changeShapeType(ShapeRenderer.ShapeType shapeType) {
        srend.end();
        srend.begin(shapeType);
    }

    public void drawToggle() {
        drawEnabled = !drawEnabled;
    }

    //// PolygonMapObjects

    public void addPolygonMapObjects(Array<PolygonMapObject> mapObjects) {
        this.mapObjects.add(mapObjects);
    }

    public void drawPolygonMapObjects() {
        if (!drawEnabled)
            return;

        // Draw objects' polygon segments
        srend.setColor(Color.RED);
        for(Array<PolygonMapObject> objectList : mapObjects) {
            for (PolygonMapObject object : objectList) {
                srend.polygon(object.getPolygon().getTransformedVertices());
            }
        }

        // Draw objects' bounding boxes
        srend.setColor(Color.YELLOW);
        for(Array<PolygonMapObject> objectList : mapObjects) {
            for (PolygonMapObject object : objectList) {
                srend.rect(
                        object.getPolygon().getBoundingRectangle().x,
                        object.getPolygon().getBoundingRectangle().y,
                        object.getPolygon().getBoundingRectangle().width,
                        object.getPolygon().getBoundingRectangle().height);
            }
        }
    }

    public void clearPolygonMapObjects() {
        mapObjects.clear();
    }

    //// GameObjects

    public void addGameObject(GameObject object) {
        gameObjects.add(object);
    }

    public void drawGameObjects() {
        if (!drawEnabled)
            return;

        for (GameObject object : gameObjects) {
            // Draw radius circle
            srend.setColor(Color.RED);
            srend.circle(object.getWorldOrigin().x, object.getWorldOrigin().y, object.radius);

            // Draw center of object
            srend.setColor(Color.YELLOW);
            srend.circle(object.getWorldOrigin().x, object.getWorldOrigin().y, 2f);

        }
    }

    public void clearGameObjects() {
        gameObjects.clear();
    }

    //// Player information

    public void drawPlayerInfo() {

    }
}
