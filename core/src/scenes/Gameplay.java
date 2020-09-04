package scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.sharmajikapps.delicatebird.GameMain;

import bird.Bird;
import ground.GroundBody;
import helpers.GameInfo;
import hud.UIHud;
import pipes.Pipes;


public class Gameplay implements Screen, ContactListener {

    private GameMain game;

    private World world;

    private OrthographicCamera mainCamera;
    private Viewport gameViewport;

    private OrthographicCamera debugCamera;
    private Box2DDebugRenderer debugRenderer;

    private Array<Sprite> backgrounds = new Array<Sprite>();
    private Array<Sprite> grounds = new Array<Sprite>();

    private Bird bird;

    private GroundBody groundBody;

    private UIHud hud;

    private boolean firstTouch;

    private Array<Pipes> pipesArray = new Array<Pipes>();
    private final int DISTANCE_BETWEEN_PIPES = 150;

    private Sound scoreSound, birdDiedSound, birdFlapSound;

    public Gameplay(GameMain game) {
        this.game = game;

        mainCamera = new OrthographicCamera(GameInfo.WIDTH, GameInfo.HEIGHT);
        mainCamera.position.set(GameInfo.WIDTH / 2f, GameInfo.HEIGHT / 2, 0);

        gameViewport = new StretchViewport(GameInfo.WIDTH, GameInfo.HEIGHT,
                mainCamera);

        debugCamera = new OrthographicCamera();
        debugCamera.setToOrtho(false, GameInfo.WIDTH / GameInfo.PPM,
                GameInfo.HEIGHT / GameInfo.PPM);
        debugCamera.position.set(GameInfo.WIDTH / 2f, GameInfo.HEIGHT / 2f, 0);

        //debugRenderer = new Box2DDebugRenderer();

        hud = new UIHud(game);

        createBackgrounds();
        createGrounds();

        world = new World(new Vector2(0, -9.8f), true);
        world.setContactListener(this);

        bird = new Bird(world, GameInfo.WIDTH / 2f - 80, GameInfo.HEIGHT / 2f);

        groundBody = new GroundBody(world, grounds.get(0));

        scoreSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/Score.mp3"));
        birdDiedSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/Dead.mp3"));
        birdFlapSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/Fly.mp3"));

    }

    void checkForFirstTouch() {
        if(!firstTouch) {
            if(Gdx.input.justTouched()) {
                firstTouch = true;
                bird.activateBird();
                createAllPipes();
            }
        }
    }

    void update(float dt) {

        checkForFirstTouch();

        if(bird.getAlive()) {
            moveBackgrounds();
            moveGrounds();
            birdFlap();
            updatePipes();
            movePipes();
        }
    }

    void createAllPipes() {
        RunnableAction run = new RunnableAction();
        run.setRunnable(new Runnable() {
            @Override
            public void run() {
                // put custom code
                createPipes();
            }
        });

        SequenceAction sa = new SequenceAction();
        sa.addAction(Actions.delay(1.5f));
        sa.addAction(run);

        hud.getStage().addAction(Actions.forever(sa));
    }

    void birdFlap() {
        if(Gdx.input.justTouched()) {
            birdFlapSound.play(0.5f);
            bird.birdFlap();
        }
    }

    void createBackgrounds() {
        for(int i = 0; i < 3; i++) {
            Sprite bg = new Sprite(new Texture("Backgrounds/Day.jpg"));
            bg.setPosition(i * bg.getWidth(), 0);
            backgrounds.add(bg);
        }
    }

    void createGrounds() {
        for(int i = 0; i < 3; i++) {
            Sprite ground = new Sprite(new Texture("Backgrounds/Ground.png"));
            ground.setPosition(i
                    * ground.getWidth(), -ground.getHeight() / 2 );
            grounds.add(ground);
        }
    }

    void drawBackgrounds(SpriteBatch batch) {
        for(Sprite s : backgrounds) {
            batch.draw(s, s.getX(), s.getY());
        }
    }

    void drawGrounds(SpriteBatch batch) {
        for(Sprite ground : grounds) {
            batch.draw(ground, ground.getX(), ground.getY());
        }
    }

    void moveBackgrounds() {

        for(Sprite bg : backgrounds) {
            float x1 = bg.getX() - 3f;
            bg.setPosition(x1, bg.getY());

            if(bg.getX() + GameInfo.WIDTH + (bg.getWidth() / 2f)
                    < mainCamera.position.x) {

                float x2 = bg.getX() + bg.getWidth() * backgrounds.size;
                bg.setPosition(x2, bg.getY());

            }

        }

    }

    void moveGrounds() {

        for(Sprite ground : grounds) {

            float x1 = ground.getX() - 4.5f;
            ground.setPosition(x1, ground.getY());

            if(ground.getX() + GameInfo.WIDTH + (ground.getWidth() / 2f)
                    < mainCamera.position.x) {

                float x2 = ground.getX() + ground.getWidth() * grounds.size;
                ground.setPosition(x2, ground.getY());
            }

        }

    }

    void createPipes() {
        Pipes p = new Pipes(world, GameInfo.WIDTH + DISTANCE_BETWEEN_PIPES);
        p.setMainCamera(mainCamera);
        pipesArray.add(p);
    }

    void drawPipes(SpriteBatch batch) {
        for(Pipes pipe : pipesArray) {
            pipe.drawPipes(batch);
        }
    }

    void updatePipes() {
        for(Pipes pipe : pipesArray) {
            pipe.updatePipes();
        }
    }

    void movePipes() {
        for(Pipes pipe : pipesArray) {
            pipe.movePipes();
        }
    }

    void stopPipes() {
        for(Pipes pipe : pipesArray) {
            pipe.stopPipes();
        }
    }

    void birdDied() {
        bird.setAlive(false);
        bird.birdDied();
        stopPipes();

        hud.getStage().clear();
        hud.showScore();

        Preferences prefs = Gdx.app.getPreferences("Data");
        int highscore = prefs.getInteger("Score");

        if(highscore < hud.getScore()) {
            prefs.putInteger("Score", hud.getScore());
            prefs.flush();
        }

        hud.createButtons();
        Gdx.input.setInputProcessor(hud.getStage());

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {

        update(delta);

        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.getBatch().begin();

        drawBackgrounds(game.getBatch());

        drawGrounds(game.getBatch());

        bird.drawIdle(game.getBatch());
        bird.animateBird(game.getBatch());

        // drawing the pipes
        drawPipes(game.getBatch());

        game.getBatch().end();

        //debugRenderer.render(world, debugCamera.combined);

        game.getBatch().setProjectionMatrix(hud.getStage().getCamera().combined);
        hud.getStage().draw();
        hud.getStage().act();

        bird.updateBird();

        world.step(Gdx.graphics.getDeltaTime(), 6, 2);

    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
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

        world.dispose();

        for(Sprite bg : backgrounds) {
            bg.getTexture().dispose();
        }

        for(Sprite ground: grounds) {
            ground.getTexture().dispose();
        }

        for(Pipes pipe: pipesArray) {
            pipe.disposeAll();
        }

        scoreSound.dispose();
        birdFlapSound.dispose();
        birdDiedSound.dispose();

    }

    @Override
    public void beginContact(Contact contact) {
        Fixture body1, body2;

        if(contact.getFixtureA().getUserData() == "Bird") {
            body1 = contact.getFixtureA();
            body2 = contact.getFixtureB();
        } else {
            body1 = contact.getFixtureB();
            body2 = contact.getFixtureA();
        }

        if(body1.getUserData() == "Bird" && body2.getUserData() == "Pipe") {
            if(bird.getAlive()) {
                birdDiedSound.play(0.4f);
                birdDied();
            }
        }

        if(body1.getUserData() == "Bird" && body2.getUserData() == "Ground") {
            if(bird.getAlive()) {
                birdDiedSound.play(0.4f);
                birdDied();
            }
        }

        if(body1.getUserData() == "Bird" && body2.getUserData() == "Score") {
            scoreSound.play(0.5f);
            hud.incrementScore();
        }

    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }

}
