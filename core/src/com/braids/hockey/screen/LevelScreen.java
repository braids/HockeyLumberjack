package com.braids.hockey.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.braids.hockey.*;
import com.braids.hockey.debug.DrawDebug;
import com.braids.hockey.object.Lumberjack;
import com.braids.hockey.movement.AStar;
import com.braids.hockey.object.Nazi;
import net.dermetfan.gdx.math.GeometryUtils;

public class LevelScreen implements Screen {
    final GameInstance game;

    TiledMap map;
    TiledMapTileLayer terrain;

    // Renderer
    OrthogonalTiledMapRenderer renderer;
    OrthographicCamera camera;

    LevelIProc iproc;

    // Map Data
    //String mapFilename;
    Array<PolygonMapObject> blockingTerrain = new Array<PolygonMapObject>();
    Array<Nazi> nazis = new Array<Nazi>();
    boolean[] iceMap;
    RectangleMapObject goal;
    LevelScreen goalDestination;

    // Pathing
    AStar astar;

    // Player Data
    public Lumberjack ljack;

    // Debug
    DrawDebug drawDebug = new DrawDebug(false);

    public LevelScreen(final GameInstance game, String mapFilename) {
        this.game = game;

        iproc = new LevelIProc(this);
        Gdx.input.setInputProcessor(iproc);
        iproc.blockInput = false;

        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        this.map = new TmxMapLoader().load(mapFilename);
        this.terrain = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");
        this.renderer = new OrthogonalTiledMapRenderer(map, 1f);

        // Load blocking terrain
        for(MapObject object : map.getLayers().get("Blocking Terrain").getObjects()) {
            if(object instanceof PolygonMapObject) {
                PolygonMapObject polyobj = (PolygonMapObject) object;

                polyobj.getPolygon().setPosition(polyobj.getPolygon().getX(), polyobj.getPolygon().getY());

                if (GeometryUtils.isConvex(polyobj.getPolygon())) {
                    float[] fixedverts = polyobj.getPolygon().getVertices();
                    GeometryUtils.arrangeConvexPolygon(fixedverts, true);
                    polyobj.getPolygon().setVertices(fixedverts);
                    blockingTerrain.add(polyobj);
                }
                else {
                    float[][] convexpolys = GeometryUtils.decompose(polyobj.getPolygon().getTransformedVertices());
                    for(float[] poly : convexpolys) {
                        GeometryUtils.arrangeConvexPolygon(poly, true);
                        blockingTerrain.add(new PolygonMapObject(poly));
                    }
                }
            }
        }

        // Load goal
        goal = (RectangleMapObject) map.getLayers().get("Goal").getObjects().get(0);

        // Get map size and init iceMap
        int mapSize =  terrain.getWidth() * terrain.getHeight();
        iceMap = new boolean[mapSize];
        astar = new AStar(
                (int)terrain.getWidth(),
                (int)terrain.getHeight()) {
            protected boolean isValid (int x, int y) {
                return iceMap[x + y * (int)(terrain.getWidth())];
            }
        };

        // Store terrain info
        for (int i = 0; i < terrain.getWidth(); i++) {
            for (int j = 0; j < terrain.getHeight(); j++) {
                String cellTerrain = (String) terrain.getCell(i, j).getTile().getProperties().get("terrain");
                boolean hasIce = cellTerrain.contains("0") ? false : true;
                int cellIndex = i + (j * terrain.getWidth());
                iceMap[cellIndex] = hasIce;
            }
        }

        // Load lumberjack
        MapProperties playerStart = map.getLayers().get("Player Start").getObjects().get(0).getProperties();
        ljack = new Lumberjack(playerStart.get("x", Float.class), playerStart.get("y", Float.class));

        // Load nazis
        for(MapObject object : map.getLayers().get("Nazi Locations").getObjects()) {
            nazis.add(new Nazi(
                    object.getProperties().get("x", 0f, Float.class),
                    object.getProperties().get("y", 0f, Float.class),
                    ljack,
                    terrain,
                    iceMap));
        }

        // Debug
        drawDebug.setCamera(camera);
        drawDebug.addPolygonMapObjects(blockingTerrain);
        drawDebug.addGameObject(ljack);
        for (Nazi nazi : nazis)
            drawDebug.addGameObject(nazi);
    }

    @Override
    public void render(float delta) {
        // Clear scene
        Gdx.gl.glClearColor(0, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        MoveCamera(camera);
        camera.zoom = .5f;
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        //// Draw scene

        // Draw Terrain
        renderer.setView(camera);
        renderer.render();

        // Draw player
        game.batch.begin();
        ljack.draw(game.batch, delta);
        for(Nazi nazi : nazis)
            nazi.draw(game.batch, delta);
        game.batch.end();



        //// Updates

        // Update player position
        ljack.update();

        // Update nazi position
        for (Nazi nazi : nazis)
            nazi.update();

        // Check collision with terrain and correct position if colliding
        ljack.checkCollision(blockingTerrain);
        // Update nazi position
        for (Nazi nazi : nazis) {
            nazi.checkCollision(blockingTerrain);
            if(nazi.checkPuckCollision(ljack.ppool))
                nazis.removeValue(nazi, true);
            if(nazi.checkPlayerCollision(ljack)) {
                restartLevel();
                break;
            }
        }

        ljack.resolveCollisions();
        for (Nazi nazi : nazis)
            nazi.resolveCollisions();

        if(ljack.checkGoal(goal.getRectangle()))
            exitLevel();

// Draw debug info
        drawDebug.begin(ShapeRenderer.ShapeType.Line);
        drawDebug.drawPolygonMapObjects();
        drawDebug.drawGameObjects();
        drawDebug.end();
    }

    /**
     * Allows LevelScreens to define the next LevelScreen derived class
     * to exit to.
     */
    public void exitLevel() {}

    public <T extends LevelScreen> void exitLevel(T level) {
        game.setScreen(level);
        dispose();
    }

    public void restartLevel() {}

    public <T extends LevelScreen> void restartLevel(T level) {
        game.setScreen(level);
        dispose();
    }

    public void MoveCamera(OrthographicCamera camera) {
        float moveScalar = 8;

        camera.position.x = ljack.sprite.getX();
        camera.position.y = ljack.sprite.getY();

        if (camera.position.x < 200)
            camera.position.x = 200;
        if (camera.position.x > 1400)
            camera.position.x = 1400;
        if (camera.position.y < 120)
            camera.position.y = 120;
        if (camera.position.y > 1480)
            camera.position.y = 1480;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        map.dispose();
        ljack.dispose();
        for(Nazi nazi: nazis)
            nazi.dispose();
    }

    public void toggleDebugDraw() {
        drawDebug.drawToggle();
    }
}
