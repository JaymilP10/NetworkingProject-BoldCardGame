package com.example.clienttemplateforcardsupdate2122;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javax.swing.JOptionPane;
import socketfx.Constants;
import socketfx.FxSocketClient;
import socketfx.SocketListener;
import javafx.animation.AnimationTimer;

public class HelloController implements Initializable {
    boolean areReady = false;
    boolean serverReady = false;
    @FXML
    private TextField hostTextField;
    @FXML
    private Label lblMessages, lblp2Points, lblp1Points, lblTurn;

    @FXML
    private Button sendButton,ready, btnEndTurn, btnName;
    @FXML
    private TextField sendTextField, txtName;
    @FXML
    private Button connectButton;
    @FXML
    private TextField portTextField;
    @FXML
    private GridPane gridPane;

    ArrayList<Card> cardsClicked = new ArrayList<>();

    List<Card> deck = new ArrayList<>();

    private ImageView[][] imageViews = new ImageView[5][4];

    private Card[][] cards = new Card[5][4];

    private boolean isTurn = false;

    private boolean isMatch = true;
    private boolean canClick = true;

    private double startTime;
    private long time  = System.nanoTime();

    private int p1Points;
    private int p2Points;

    private String p1Name, p2Name;

    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
    private boolean isConnected, turn, serverUNO = false, clientUNO = false;
    public enum ConnectionDisplayState {
        DISCONNECTED, WAITING, CONNECTED, AUTOCONNECTED, AUTOWAITING
    }
    private FxSocketClient socket;
    private void connect() {
        socket = new FxSocketClient(new FxSocketListener(),
                hostTextField.getText(),
                Integer.valueOf(portTextField.getText()),
                Constants.instance().DEBUG_NONE);
        socket.connect();
    }
    private void displayState(ConnectionDisplayState state) {
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isConnected = false;
        displayState(ConnectionDisplayState.DISCONNECTED);
        Runtime.getRuntime().addShutdownHook(new ShutDownThread());
    }

    class ShutDownThread extends Thread {
        @Override
        public void run() {
            if (socket != null) {
                if (socket.debugFlagIsSet(Constants.instance().DEBUG_STATUS)) {
                    LOGGER.info("ShutdownHook: Shutting down Server Socket");
                }
                socket.shutdown();
            }
        }
    }
    @FXML
    private ImageView imgS0,imgS1,imgS2,imgS3,imgS4,imgS5,imgS6,imgS7,imgS8,imgS9,
    imgC0,imgC1,imgC2,imgC3,imgC4, imgC5,imgC6,imgC7,imgC8,imgC9, imgDiscard;
    FileInputStream back,tempCard;
    Image imageBack;
    List<ImageView> hand1I = new ArrayList<>();
    List<ImageView> hand2I = new ArrayList<>();
    List<Card> hand1D = new ArrayList<>();
    List<Card> hand2D = new ArrayList<>();
    int numInServerHand=0;

    Card discardCard;
    public HelloController(){
        try {
            back = new FileInputStream("src/main/resources/Bold Images/Back.jpg");
            imageBack = new Image(back);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
    class FxSocketListener implements SocketListener {
        @Override
        public void onMessage(String line) {
//            System.out.println("message received server");
//            System.out.println(line);
            lblMessages.setText(line);
            if (line.equals("ready") && areReady){
                ready.setVisible(false);
            } else if(line.equals("ready")){
                serverReady=true;
            }
            else if(line.equals("dealt")){
//                System.out.println(line);
                String[] letters = {"D", "L", "S", "B", "C", "J", "B", "O", "Y", "B", "M", "S"};
                for (int i = 0; i < 3; i++) {
                    for (int j = 3; j < 6; j++) {
                        for (int k = 6; k < 9; k++) {
                            for (int l = 9; l < 12; l++) {
//                        System.out.println(letters[i] + letters[j] + letters[k] + letters[l]);
                                deck.add(new Card(letters[i] + letters[j] + letters[k] + letters[l]));
                            }
                        }
                    }
                }
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 4; j++) {
                        imageViews[i][j].setImage(imageBack);
                    }
                }

            } else if(line.startsWith("sCardNum")){
                numInServerHand = Integer.parseInt(line.substring(8));
//                System.out.println(numInServerHand);
            } else if (line.startsWith("discard")){
//                discardCard = new Card(line.substring(7));
//                try {
//                    tempCard = new FileInputStream(discardCard.getCardPath());
//                    imageFront = new Image(tempCard);
//                }catch (FileNotFoundException e){
//                    e.printStackTrace();
//                }
//                imgDiscard.setImage(imageFront);
            } else if (line.startsWith("deck")) {
//                System.out.println(line);
                int randNum = Integer.parseInt(line.substring(line.indexOf("m") + 1, line.indexOf("i")));
                int i = Integer.parseInt(line.substring(line.indexOf("i") + 1, line.indexOf("j")));
                int j = Integer.parseInt(line.substring(line.indexOf("j")  + 1));
                cards[i][j] = deck.get(randNum);
                deck.remove(randNum);
            } else if (line.startsWith("clicked")) {
//                System.out.println(line);
                int i = Integer.parseInt(line.substring(7, 8));
                int j = Integer.parseInt(line.substring(8));
                try {
                    tempCard = new FileInputStream(cards[i][j].getCardPath());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                cardsClicked.add(cards[i][j]);
                imageViews[i][j].setImage(new Image(tempCard));
            } else if (line.startsWith("endturn")) {
//                System.out.println(line);
                int randNum = Integer.parseInt(line.substring(line.indexOf("m") + 1, line.indexOf("i")));
                int i = Integer.parseInt(line.substring(line.indexOf("i") + 1, line.indexOf("j")));
                int j = Integer.parseInt(line.substring(line.indexOf("j") + 1));
                imageViews[i][j].setImage(imageBack);
                if (deck.size() > 0){
                    cards[i][j] = deck.get(randNum);
                    deck.remove(randNum);
                } else {
                    cards[i][j].cName = "";
                    imageViews[i][j].setImage(null);
                }

                p1Points = p1Points + (cardsClicked.size() * cardsClicked.size());
                lblp1Points.setText("P1 Points: " + p1Points);
                cardsClicked.clear();
                isTurn = true;
                lblTurn.setText("Turn: " + p2Name);
                btnEndTurn.setDisable(false);
            } else if (line.startsWith("nomatch")) {
//                System.out.println(line);
                for (Card card : cardsClicked) {
                    for (int k = 0; k < 5; k++) {
                        for (int l = 0; l < 4; l++) {
                            if (card == cards[k][l]){
                                imageViews[k][l].setImage(imageBack);
                            }
                        }
                    }
                }
                cardsClicked.clear();
                isTurn = true;
                lblTurn.setText("Turn: " + p2Name);
                btnEndTurn.setDisable(false);
            } else if (line.startsWith("load")){
                deck.clear();
                String[] letters = {"D", "L", "S", "B", "C", "J", "B", "O", "Y", "B", "M", "S"};
                for (int i = 0; i < 3; i++) {
                    for (int j = 3; j < 6; j++) {
                        for (int k = 6; k < 9; k++) {
                            for (int l = 9; l < 12; l++) {
//                        System.out.println(letters[i] + letters[j] + letters[k] + letters[l]);
                                deck.add(new Card(letters[i] + letters[j] + letters[k] + letters[l]));
                            }
                        }
                    }
                }
            } else if (line.startsWith("Load cards")) {
//                System.out.println(line);
                int k = Integer.parseInt(line.substring(line.indexOf("k:") + 2, line.indexOf("l")));
                int l = Integer.parseInt(line.substring(line.indexOf("l:") + 2, line.indexOf("n")));
//                System.out.println(k + " " + l + " " + line.substring(line.indexOf("e:") + 2));
                for (Card card : deck) {
                    if (card.cName.equals(line.substring(line.indexOf("e:") + 2))){
                        cards[k][l] = card;
                    }
                    imageViews[k][l].setImage(imageBack);
                }
            } else if (line.startsWith("Load Game")) {
//                System.out.println(line);
                p1Points = Integer.parseInt(line.substring(line.indexOf("1") + 1, line.indexOf("p2")));
                p2Points = Integer.parseInt(line.substring(line.indexOf("2") + 1));
                lblp1Points.setText("P1 Points: " + p1Points);
                lblp2Points.setText("P2 Points: " + p2Points);
            } else if (line.startsWith("name:")) {
//                System.out.println(line);
                p1Name = line.substring(line.indexOf("e:") + 2);
            } else if (line.startsWith("turn")) {
//                System.out.println(line);
                isTurn = !Boolean.parseBoolean(line.substring(line.indexOf("n") + 1));
                if (isTurn){
                    lblTurn.setText("Turn: " + p2Name);
                    btnEndTurn.setDisable(false);
                } else {
                    lblTurn.setText("Turn: " + p1Name);
                    btnEndTurn.setDisable(true);
                }

            } else if (line.startsWith("p2Name")){
                p2Name = line.substring(line.indexOf("e") + 1);
                txtName.setText(p2Name);
            } else if (line.startsWith("Remove Card")){
                String cName = line.substring(line.indexOf(":") + 1);
                for (int i = 0; i < deck.size(); i++) {
                    if (deck.get(i).cName.equals(line.substring(line.indexOf(":") + 1))){
//                        System.out.println(deck.size());
                        deck.remove(i);
//                        System.out.println("Removed Card:" + cName);
//                        System.out.println(deck.size());
                        i--;
//                        socket.sendMessage("Remove Card:" + deck.get(i).cName);
                    }
                }
//                System.out.println("ds" + deck.size());
            } else if (line.startsWith("Restart")) {
                p1Points = 0;
                p2Points = 0;
                lblp1Points.setText("P1 Points: " + p1Points);
                lblp2Points.setText("P2 Points: " + p2Points);
                deck.clear();
                cardsClicked.clear();

                String[] letters = {"D", "L", "S", "B", "C", "J", "B", "O", "Y", "B", "M", "S"};
                for (int i = 0; i < 3; i++) {
                    for (int j = 3; j < 6; j++) {
                        for (int k = 6; k < 9; k++) {
                            for (int l = 9; l < 12; l++) {
//                        System.out.println(letters[i] + letters[j] + letters[k] + letters[l]);
                                deck.add(new Card(letters[i] + letters[j] + letters[k] + letters[l]));
                            }
                        }
                    }
                }
//                System.out.println("Server hand");
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 4; j++) {
                        imageViews[i][j].setImage(imageBack);
                    }
                }
            } else if (line.startsWith("endgame")){
                if (!anyMoreMatches()){
                    gridPane.setVisible(false);
                }
            }
        }
        @Override
        public void onClosedStatus(boolean isClosed) {

        }
    }
    @FXML
    private void handleReady(ActionEvent event) {
//        if (!sendTextField.getText().equals("")) {
//            String x = sendTextField.getText();
//            socket.sendMessage(x);
//            System.out.println("sent message client");
//        }
        areReady=true;
        socket.sendMessage("ready");

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
                imageViews[i][j] = new ImageView();
                imageViews[i][j].setFitHeight(100);
                imageViews[i][j].setFitWidth(100);
                imageViews[i][j].setRotate(90);
                gridPane.add(imageViews[i][j], j, i);
            }
        }
        EventHandler<MouseEvent> z = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //all button code goes here
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 4; j++) {
                        if (((ImageView) event.getSource()) == imageViews[i][j] && isTurn && canClick){
//                            System.out.println("oc:"+i+"or:"+j);
                            try {
                                tempCard = new FileInputStream(cards[i][j].getCardPath());
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                            imageViews[i][j].setImage(new Image(tempCard));
                            cardsClicked.add(cards[i][j]);
                            socket.sendMessage("clicked" + i + j);
                            if (!checkMatch()){
                                canClick = false;
//                                System.out.println("CARDS DONT MATCH");
                                if (!checkMatch()){
                                    startTime = System.nanoTime();
                                    new AnimationTimer(){
                                        @Override
                                        public void handle(long now) {
                                            if(startTime>0){
                                                if (now - startTime > (900000000.0 * 2)){
                                                    this.stop();
                                                    canClick = true;
                                                    endTurn();
                                                }
                                            }
                                        }
                                    }.start();
                                }
                            }
                        }
                    }
                }
            }
        };
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
//                btn[i][j].setOnMouseClicked(z);
                imageViews[i][j].setOnMouseClicked(z);
            }
        }
        if (serverReady){
            ready.setVisible(false);
        }else{
            ready.setDisable(true);
        }
    }
    public boolean checkMatch(){

        if (cardsClicked.size() == 1){
            return true;
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < cardsClicked.size(); j++) {
                for (int k = 0; k < cardsClicked.size(); k++) {
                    if (cardsClicked.get(j).cName.substring(i, i + 1).equals(cardsClicked.get(k).cName.substring(i, i + 1))){
//                        System.out.println(cardsClicked.get(j).cName.substring(i, i + 1));
//                        System.out.println(cardsClicked.get(k).cName.substring(i, i + 1));
                        isMatch = true;
                    }
                    else {
                        isMatch = false;
                        break;
                    }
                }
                if (isMatch){
                    for (Card card : cardsClicked) {
                        socket.sendMessage("Save card: " + card.cName);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @FXML
    private void enterName(){
        p2Name = txtName.getText();
        btnName.setVisible(true);
        btnName.setDisable(true);
        socket.sendMessage("name:" + p2Name);
    }
    @FXML
    private void endTurn(){
        if (isMatch){
            for (Card card : cardsClicked) {
                for (int k = 0; k < 5; k++) {
                    for (int l = 0; l < 4; l++) {
                        if (card == cards[k][l]){
                            imageViews[k][l].setImage(imageBack);
                            int randNum = (int) (Math.random() * deck.size());
//                            System.out.println("deck size: " + deck.size());
                            socket.sendMessage("endturn" + "randNum" + randNum + "i" + k + "j" + l);
//                            System.out.println("deck" + randNum + k + l);
                            if (deck.size() > 0){
                                cards[k][l] = deck.get(randNum);
                                deck.remove(randNum);
                            } else {
                                cards[k][l].cName = "";
                                imageViews[k][l].setImage(null);
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < deck.size(); i++) {
                socket.sendMessage("CHECK" + deck.get(i).cName + "i" + i);
            }
            p2Points = p2Points + (cardsClicked.size() * cardsClicked.size());
            lblp1Points.setText("P1 Points: " + p1Points);
        } else{
            for (Card card : cardsClicked) {
                for (int k = 0; k < 5; k++) {
                    for (int l = 0; l < 4; l++) {
                        if (card == cards[k][l]){
                            imageViews[k][l].setImage(imageBack);
                            socket.sendMessage("nomatch");
                        }
                    }
                }
            }
        }
        lblp2Points.setText("P2 Points: " + p2Points);
        cardsClicked.clear();
        isTurn = false;
        lblTurn.setText("Turn: " + p1Name);
        btnEndTurn.setDisable(true);
        socket.sendMessage("turn" + isTurn);

        if (!anyMoreMatches()){
            gridPane.setVisible(false);
            socket.sendMessage("endgame");
        }
    }

    public boolean anyMoreMatches(){
        boolean anyMoreMatches = true;

        for (int i = 0; i < 3; i++) {
            for (Card[] card : cards) {
                for (int k = 0; k < card.length - 1; k++) {
                    if (!card[k].cName.equals("") && !card[k + 1].cName.equals("")) {
                        if (card[k].cName.substring(i, i + 1).equals(card[k + 1].cName.substring(i, i + 1))) {
//                        System.out.println(deck.get(j).cName.substring(i, i + 1));
//                        System.out.println(deck.get(k).cName.substring(i, i + 1));
                            anyMoreMatches = true;
                            return anyMoreMatches;
                        } else {
                            anyMoreMatches = false;
                        }
                    }
                }
            }
        }

        if (!anyMoreMatches){
            if (p1Points > p2Points){
                lblTurn.setText("WINNER IS " + p1Name + "!!");
                System.out.println("WINNER IS " + p1Name + "!!");
            } else if (p1Points == p2Points) {
                lblTurn.setText("DRAW");
                System.out.println("DRAW");
            } else {
                lblTurn.setText("WINNER IS " + p2Name + "!!");
                System.out.println("WINNER iS " + p2Name + "!!");
            }
        } else {
            if (deck.size() > 1) {
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < deck.size(); j++) {
                        for (int k = j + 1; k < deck.size() - 1; k++) {
                            if (deck.get(j).cName.substring(i, i + 1).equals(deck.get(k).cName.substring(i, i + 1))) {
//                        System.out.println(deck.get(j).cName.substring(i, i + 1));
//                        System.out.println(deck.get(k).cName.substring(i, i + 1));
                                anyMoreMatches = true;
                                return anyMoreMatches;
                            } else {
                                anyMoreMatches = false;
                            }
                        }
                    }
                }

                if (!anyMoreMatches){
                    if (p1Points > p2Points){
                        lblTurn.setText("WINNER IS " + p1Name + "!!!!!!!!!");
                        System.out.println("WINNER IS " + p1Name + "!!!!!!!!!");
                    } else if (p1Points == p2Points) {
                        lblTurn.setText("DRAW");
                        System.out.println("DRAW");
                    } else {
                        lblTurn.setText("WINNER IS " + p2Name + "!!!!!!!!!");
                        System.out.println("WINNER iS " + p2Name + "!!!!!!!!!");
                    }
                }
            }
        }
        return anyMoreMatches;
    }

    @FXML
    private void handleConnectButton(ActionEvent event) {
        connectButton.setDisable(true);
        displayState(ConnectionDisplayState.WAITING);
        connect();
    }

}