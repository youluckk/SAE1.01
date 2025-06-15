package fr.tournois.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ADevelopperController {
    @FXML
    private Label messageLabel;

    public void setMessage(String fonctionnalite) {
        messageLabel.setText("La fonctionnalité '" + fonctionnalite + "' sera disponible dans une version ultérieure.");
    }
}
