package lepl.mathart;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import lepl.Constant;
import lepl.LApplication;

public class MathArt extends LApplication {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        super.start(primaryStage);

        ArtBase base = new ArtBase(primaryStage);

        setTitle(primaryStage, base, "Math x Art");
        setIcon(primaryStage, base, new Image(Constant.getImageResource("icon.png")));

        primaryStage.setScene(base.makeScene());
        primaryStage.show();
    }
}
