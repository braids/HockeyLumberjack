package com.braids.hockey;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
//import com.braids.hockey.Movement.Displacement;
import com.braids.hockey.movement.AStar;
import com.braids.hockey.movement.Displacement;
import com.braids.hockey.object.Lumberjack;
import com.braids.hockey.object.Nazi;
import net.dermetfan.gdx.math.*;

public class TerrainGenScreen implements Screen {
    final GameInstance game;

    TiledMap map;
    TiledMapTileLayer terrain;

    // Renderer
    OrthogonalTiledMapRenderer renderer;
    OrthographicCamera camera;

    TerrainGenIProc iproc;

    // Map Data
    Array<PolygonMapObject> blockingTerrain = new Array<PolygonMapObject>();
    Array<Nazi> nazis = new Array<Nazi>();
    boolean[] iceMap;

    AStar astar;

    Lumberjack ljack;

    ShapeRenderer srend = new ShapeRenderer();
    private boolean debugDraw = true;

    public TerrainGenScreen(final GameInstance game) {
        this.game = game;

        iproc = new TerrainGenIProc(this);
        Gdx.input.setInputProcessor(iproc);
        iproc.blockInput = false;

        this.camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();

        this.map = new TmxMapLoader().load("terrain.tmx");
        this.terrain = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");
        this.renderer = new OrthogonalTiledMapRenderer(map, 1f);

        // Load blocking terrain
        for(MapObject object : map.getLayers().get("Blocking Terrain").getObjects()) {
            if(object instanceof PolygonMapObject) {
                PolygonMapObject polyobj = (PolygonMapObject) object;

                polyobj.getPolygon().setPosition(polyobj.getPolygon().getX() + 16, polyobj.getPolygon().getY() + 24);

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
        ljack = new Lumberjack(100f, 100f);

        // Load nazis
        for(MapObject object : map.getLayers().get("Nazi Locations").getObjects()) {
            nazis.add(new Nazi(
                    object.getProperties().get("x", 0f, Float.class),
                    object.getProperties().get("y", 0f, Float.class),
                    ljack,
                    terrain,
                    iceMap));
        }
    }

    @Override
    public void render(float delta) {
        // Clear scene
        Gdx.gl.glClearColor(0, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Update camera
        MoveCamera(camera);
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

        // Draw debug info
        if(debugDraw) {
            srend.setProjectionMatrix(camera.combined);
            srend.begin(ShapeRenderer.ShapeType.Line);
            srend.setColor(Color.RED);
            for (PolygonMapObject object : blockingTerrain) {
                srend.polygon(object.getPolygon().getTransformedVertices());
            }
            srend.circle(ljack.getWorldOrigin().x, ljack.getWorldOrigin().y, ljack.radius);

            srend.setColor(Color.YELLOW);
            srend.circle(ljack.getWorldOrigin().x, ljack.getWorldOrigin().y, 2f);
            for (PolygonMapObject object : blockingTerrain) {
                srend.rect(
                        object.getPolygon().getBoundingRectangle().x,
                        object.getPolygon().getBoundingRectangle().y,
                        object.getPolygon().getBoundingRectangle().width,
                        object.getPolygon().getBoundingRectangle().height);
            }
            srend.end();
        }

        camera.zoom = .5f;

        //// Updates

        // Update player position
        ljack.Update();

        // Update nazi position
        for (Nazi nazi : nazis)
            nazi.update();

        // Check collision with terrain and correct position if colliding
        ljack.checkCollision(blockingTerrain);

        int startX = (int)(ljack.getWorldOrigin().x / terrain.getTileWidth());
        int startY = (int)(ljack.getWorldOrigin().y / terrain.getTileHeight());
        IntArray path  = astar.getPath(startX, startY, 6, 93);

        if(debugDraw) {
            srend.begin(ShapeRenderer.ShapeType.Line);
            srend.setColor(Color.TEAL);
            for (Displacement d : ljack.displacements)
                srend.line(ljack.getWorldOrigin(), ljack.getWorldOrigin().cpy().add(d.disp.cpy().scl(d.scl)));
            srend.end();
            srend.begin(ShapeRenderer.ShapeType.Filled);
            srend.setColor(Color.LIME);
            for (int i = 0, n = path.size; i < n; i+= 2) {
                srend.circle(path.get(i) * terrain.getTileWidth(), path.get(i+1) * terrain.getTileHeight(), 4);
            }
            srend.setColor(Color.RED);
            for (Nazi nazi : nazis)
                for (int i = 0, n = nazi.smoothPath.size; i < n; i++)
                    srend.circle(nazi.smoothPath.get(i).x * terrain.getTileWidth(), nazi.smoothPath.get(i).y * terrain.getTileHeight(), 2);

            srend.end();
        }

        ljack.resolveCollisions();
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
    }


    public void toggleDebugDraw() {
        debugDraw = !debugDraw;
    }
}
