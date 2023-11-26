package lepl.mathart;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import lepl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArtBase extends LBase {

    private static final int COUNT_RATIO = 15;
    private static final int SLEEP_MS = 15;

    final double paneWidth = 5000;
    private double mouseX;
    private double mouseY;
    private double transX;
    private double transY;
    AnchorPane pane = new AnchorPane();
    Canvas canvas;
    GraphicsContext gc;
    Circle mainCircle;
    Draw draw;
    List<Complex> cords = new ArrayList<>();
    Button startButton = new Button("시작");
    Random r = new Random();
    private boolean isClickActive = false, isPlaying = true;
    private final Background basic = new Background(new BackgroundFill(
            Color.rgb(82, 82, 82),
            CornerRadii.EMPTY,
            Insets.EMPTY
    ));
    private final Background entered = new Background(new BackgroundFill(
            Color.rgb(100, 100, 100),
            CornerRadii.EMPTY,
            Insets.EMPTY
    ));

    public ArtBase(Stage primaryStage) {
        super(960, 525, 20, new BaseType(BaseType.DEFAULT), primaryStage);
        setBackground(new Background(new BackgroundFill(
                Color.GRAY,
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
        setSmallestWidth(235);
        keyboardShortcuts.put(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                this::reset
        );

        pane.setCacheHint(CacheHint.QUALITY);
        pane.setPrefSize(paneWidth, paneWidth);
        pane.setLayoutX((960 - paneWidth) / 2);
        pane.setLayoutY((525 - paneWidth) / 2);
        pane.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0), CornerRadii.EMPTY, Insets.EMPTY)));

        Scale scale = new Scale(1, 1);
        scale.setPivotX(paneWidth / 2);
        scale.setPivotY(paneWidth / 2);
        pane.getTransforms().add(scale);

        Translate translate = new Translate(0, 0);
        pane.getTransforms().add(translate);


        pane.setOnScroll(e -> {
            if (!isClickActive) {
                pane.getTransforms().remove(scale);
                double preScaleX = scale.getX(), preScaleY = scale.getY();
                double scaleX, scaleY;
                if (e.getDeltaY() > 0) {
                    scaleX = preScaleX * Math.pow(1.2, e.getDeltaY() / 20);
                    scaleY = preScaleY * Math.pow(1.2, e.getDeltaY() / 20);
                } else {
                    scaleX = preScaleX / Math.pow(1.2, -e.getDeltaY() / 20);
                    scaleY = preScaleY / Math.pow(1.2, -e.getDeltaY() / 20);
                }
                scale.setX(scaleX);
                scale.setY(scaleY);

                pane.getTransforms().add(1, scale);
            }
        });
        getMainPane().setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
            transX = translate.getX();
            transY = translate.getY();
        });
        getMainPane().setOnMouseDragged(e -> {
            if (!isClickActive) {
                translate.setX(transX - mouseX + e.getX());
                translate.setY(transY - mouseY + e.getY());
            }
        });
        getMainPane().setOnMouseEntered(e -> getMainPane().setCursor(Cursor.MOVE));
        pane.setOnMouseClicked(e -> {
            if (draw == null && isClickActive) {
                cords.add(new Complex(e.getX() - paneWidth / 2, paneWidth / 2 - e.getY()));
                gc.fillOval(e.getX() - 3, e.getY() - 3, 6, 6);
                create();
            }
        });

        canvas = new Canvas(paneWidth, paneWidth);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        pane.getChildren().add(canvas);

        add(pane);

        ImageView o = new ImageView(new Image(Constant.getImageResource("dote.png")));
        o.setFitWidth(8);
        o.setFitHeight(8);
        o.setLayoutX(paneWidth / 2 - 4);
        o.setLayoutY(paneWidth / 2 - 4);
        pane.getChildren().add(o);

        Button resetButton = new Button("리셋");

        startButton.setLayoutX(95);
        startButton.setBackground(basic);
        startButton.setTextFill(Color.WHITE);
        startButton.setOnMouseEntered(e -> startButton.setBackground(entered));
        startButton.setOnMouseExited(e -> startButton.setBackground(basic));
        startButton.setOnAction(e -> {
            if (draw == null) {
                if (cords.size() > 1) {
                    startButton.setText("정지");
                    draw = new Draw();
                    draw.start();
                }
            } else {
                if (draw.isAlive()) {
                    if (draw.getState().equals(Thread.State.WAITING)) {
                        synchronized (draw) {
                            startButton.setText("정지");
                            isPlaying = true;
                            draw.notify();
                        }
                    } else {
                        startButton.setText("시작");
                        isPlaying = false;
                    }
                } else {
                    create();
                    startButton.setText("정지");
                    draw = new Draw();
                    draw.start();
                }
            }
        });
        startButton.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.SHIFT)) {
                isClickActive = true;
                getMainPane().setCursor(Cursor.DEFAULT);
            }
        });
        startButton.setOnKeyReleased(e -> {
            if (e.getCode().equals(KeyCode.SHIFT)) {
                isClickActive = false;
                getMainPane().setCursor(Cursor.MOVE);
            }
        });
        getTitleBar().add(startButton);

        resetButton.setLayoutX(135);
        resetButton.setFocusTraversable(false);
        resetButton.setBackground(basic);
        resetButton.setTextFill(Color.WHITE);
        resetButton.setOnMouseEntered(e -> resetButton.setBackground(entered));
        resetButton.setOnMouseExited(e -> resetButton.setBackground(basic));
        resetButton.setOnAction(e -> reset());
        getTitleBar().add(resetButton);

        getMainPane().requestFocus();
    }

    public void reset() {
        draw = null;
        cords.clear();
        gc.clearRect(0,0,paneWidth,paneWidth);
        removeCircle();
    }
    public void create() {
        removeCircle();
        List<Complex> a = FourierTransform.dft1Complex(cords);
        int l = a.size();
        Circle.TIME_SPEED = COUNT_RATIO * l;

        mainCircle = new Circle(a.get(l/2), 0);
        Circle circle = mainCircle;
        for (int i = 1; i < a.size(); i++) {
            if (i % 2 == 1) {
                circle = new Circle(a.get(l/2 - 1 - i/2), - 1 - i/2, circle);
            } else {
                circle = new Circle(a.get(l/2 + i/2), i/2, circle);
            }
        }

        mainCircle.setLayoutX(paneWidth / 2 - 2);
        mainCircle.setLayoutY(paneWidth / 2 - 2);
        pane.getChildren().add(mainCircle);
    }
    public void removeCircle() {
        pane.getChildren().subList(2, pane.getChildren().size()).clear();
    }

    public class Draw extends Thread {
        @Override
        public void run() {
            synchronized (this) {
                gc.setStroke(Color.rgb(r.nextInt(1, 256), r.nextInt(1, 256), r.nextInt(1, 256)));
                gc.setLineWidth(4);
                double befX = getX(), befY = getY();
                for (int i = 0; i < COUNT_RATIO * cords.size(); i++) {
                    try {
                        mainCircle.rotate();
                        gc.moveTo(befX, befY);
                        gc.lineTo(befX = getX(), befY = getY());
                        gc.stroke();
                        gc.beginPath();
                    } catch (Exception ignored) {}
                    if (!isPlaying) {
                        try {
                            wait();
                        } catch (InterruptedException ignored) {}
                    }
                    try {
                        sleep(SLEEP_MS);
                    } catch (InterruptedException ignored) {}
                }
                gc.setStroke(Color.rgb(r.nextInt(1, 256), r.nextInt(1, 256), r.nextInt(1, 256)));
                for (int i = 0; i < COUNT_RATIO * cords.size(); i++) {
                    try {
                        mainCircle.rotate();
                        gc.moveTo(befX, befY);
                        gc.lineTo(befX = getX(), befY = getY());
                        gc.stroke();
                        gc.beginPath();
                    } catch (Exception ignored) {}
                    if (!isPlaying) {
                        try {
                            wait();
                        } catch (InterruptedException ignored) {}
                    }
                    try {
                        sleep(SLEEP_MS);
                    } catch (InterruptedException ignored) {}
                }
                Platform.runLater(() -> {
                    removeCircle();
                    startButton.setText("시작");
                });
                interrupt();
            }
        }

        private double getX() {
            return paneWidth / 2 + mainCircle.getEndX();
        }
        private double getY() {
            return paneWidth / 2 + mainCircle.getEndY();
        }
    }
}
