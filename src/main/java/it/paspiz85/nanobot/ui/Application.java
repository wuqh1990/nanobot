package it.paspiz85.nanobot.ui;

import it.paspiz85.nanobot.util.BuildInfo;
import it.paspiz85.nanobot.util.Logging;
import it.paspiz85.nanobot.util.Settings;

import java.io.IOException;

import javax.swing.JOptionPane;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Main class for GUI.
 *
 * @author paspiz85
 *
 */
public class Application extends javafx.application.Application {

    public static void main(final String[] args) {
        Logging.initialize();
        try {
            String user = Settings.instance().getUserMailAddress();
            if (user == null || user.isEmpty()) {
                user = JOptionPane.showInputDialog(null, "Please, provide your email address", "Login",
                        JOptionPane.QUESTION_MESSAGE);
                if (user == null || user.isEmpty()) {
                    throw new IllegalArgumentException("Cannot proceed without an user mail address");
                }
                Settings.instance().setUserMailAddress(user);
                Settings.instance().save();
            }
            launch(Application.class, args);
        } catch (Throwable ex) {
            ex.printStackTrace();
            while (ex.getCause() != null) {
                ex = ex.getCause();
            }
            JOptionPane.showMessageDialog(null, ex.getMessage());
            System.exit(1);
        } finally {
            Logging.close();
        }
    }

    private Stage primaryStage;

    Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(final Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(BuildInfo.instance().getName());
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Application.class.getResource("MainView.fxml"));
        final Parent parent = loader.load();
        final Object controller = loader.getController();
        if (controller instanceof ApplicationAwareController) {
            ((ApplicationAwareController) controller).setApplication(this);
        }
        primaryStage.setScene(new Scene(parent));
        final boolean topright = Boolean.parseBoolean(getParameters().getNamed().get("topright"));
        if (topright) {
            primaryStage.widthProperty().addListener(new ChangeListener<Number>() {

                @Override
                public void changed(final ObservableValue<? extends Number> observable, final Number oldValue,
                        final Number newValue) {
                    final Rectangle2D screen = Screen.getPrimary().getVisualBounds();
                    final double x = screen.getWidth() - newValue.doubleValue();
                    primaryStage.setX(x);
                    primaryStage.setY(0);
                    primaryStage.widthProperty().removeListener(this);
                }
            });
        }
        primaryStage.show();
        if (controller instanceof ApplicationAwareController) {
            ((ApplicationAwareController) controller).afterShow();
        }
    }
}
