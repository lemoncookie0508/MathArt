package lepl.mathart;

import javafx.scene.CacheHint;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.transform.Rotate;
import lepl.Complex;
import lepl.Constant;

public class Circle extends AnchorPane {

    public static double TIME_SPEED = 1;

    private final double width;
    private final double speed;

    private static final Background d = new Background(new BackgroundImage(
            new Image(Constant.getImageResource("dote.png")),
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.DEFAULT,
            new BackgroundSize(4, 4, false, false, false, false)
    ));

    public Circle(Complex c, double speed) {
        this.width = c.abs();
        this.rotate.setAngle(-c.angleDegree());
        getTransforms().add(rotate);
        this.speed = speed;

        setPrefHeight(4);
        setCacheHint(CacheHint.ROTATE);
        setBackground(d);

        ImageView stick = new ImageView(new Image(Constant.getImageResource("white.png")));
        stick.setFitHeight(2);
        stick.setFitWidth(width);
        stick.setX(1);
        stick.setY(1);
        getChildren().add(stick);
    }
    public Circle(Complex c, double speed, Circle depend) {
        this(c, speed);
        this.depend = depend;
        depend.setLinkedCircle(this);

        double angle = rotate.getAngle() - depend.getTotalAngle();
        rotate.setAngle(angle);
    }

    private Circle depend;
    private Circle linkedCircle;
    private void setLinkedCircle(Circle circle) {
        if (linkedCircle != null) getChildren().remove(linkedCircle);
        linkedCircle = circle;
        circle.setLayoutX(width);
        circle.setLayoutY(0);
        getChildren().add(circle);
    }

    private final Rotate rotate = new Rotate(0, 2, 2);
    public void rotate() {
        //double newAngle = rotate.getAngle() - (speed - (depend != null ? depend.speed : 0)) * 360 / TIME_SPEED;

        //if (newAngle >= 360 || newAngle < 0) newAngle %= 360;

        rotate.setAngle(rotate.getAngle() - (speed - (depend != null ? depend.speed : 0)) * 360 / TIME_SPEED);

        if (linkedCircle != null) linkedCircle.rotate();
    }

    public double getTotalAngle() {
        return (rotate.getAngle() + (depend != null ? depend.getTotalAngle() : 0)) % 360;
    }
    public double getEndX() {
        return Math.cos(Math.toRadians(getTotalAngle())) * width + (linkedCircle != null ? linkedCircle.getEndX() : 0);
    }
    public double getEndY() {
        return Math.sin(Math.toRadians(getTotalAngle())) * width + (linkedCircle != null ? linkedCircle.getEndY() : 0);
    }
}
