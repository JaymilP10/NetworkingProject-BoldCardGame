package com.example.servertemplateforcardsupdate2122;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
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
import socketfx.FxSocketServer;
import socketfx.SocketListener;
import javafx.animation.AnimationTimer;
public class HelloController implements Initializable {
    boolean areReady = false;
    boolean clientReady = false;

    @FXML
    private ImageView imgS0,imgS1,imgS2,imgS3,imgS4,imgS5,imgS6,imgS7,imgS8,imgS9,
            imgC0,imgC1,imgC2,imgC3,imgC4, imgC5,imgC6,imgC7,imgC8,imgC9, imgDiscard;

    FileInputStream back, tempCard, gameNames;
    Image imageBack;
    List<Card> deck = new ArrayList<>();

    ArrayList<Card> cardsClicked = new ArrayList<>();

    ArrayList<Card> saveRemovedCards = new ArrayList<>();

    private ImageView[][] imageViews = new ImageView[5][4];
    private Card[][] cards = new Card[5][4];

    @FXML
    private ListView lstGames;
    @FXML
    private Button sendButton,ready, btnEndTurn, btnEnterName;
    @FXML
    private Button connectButton, btnSave, btnLoad;
    @FXML
    private TextField sendTextField, portTextField, txtSaveAs, txtName;
    @FXML
    private Label lblMessages, lblp1Points, lblp2Points, lblTurn;
    @FXML
    private GridPane gridPane;

    ArrayList<Card> discard = new ArrayList<>();

    private final static Logger LOGGER =
            Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private boolean isConnected;
    private int counter = 0;

    private boolean isTurn = true;
    private boolean isMatch = true;
    private boolean canClick = true;

    private int p1Points;
    private int p2Points;

    private String p1Name, p2Name;

    private double startTime;

    public enum ConnectionDisplayState {

        DISCONNECTED, WAITING, CONNECTED, AUTOCONNECTED, AUTOWAITING
    }

    private FxSocketServer socket;

    private void connect() {
        socket = new FxSocketServer(new FxSocketListener(),
                Integer.valueOf(portTextField.getText()),
                Constants.instance().DEBUG_NONE);
        socket.connect();
    }

    private void displayState(ConnectionDisplayState state) {
//        switch (state) {
//            case DISCONNECTED:
//                connectButton.setDisable(false);
//                sendButton.setDisable(true);
//                sendTextField.setDisable(true);
//                break;
//            case WAITING:
//            case AUTOWAITING:
//                connectButton.setDisable(true);
//                sendButton.setDisable(true);
//                sendTextField.setDisable(true);
//                break;
//            case CONNECTED:
//                connectButton.setDisable(true);
//                sendButton.setDisable(false);
//                sendTextField.setDisable(false);
//                break;
//            case AUTOCONNECTED:
//                connectButton.setDisable(true);
//                sendButton.setDisable(false);
//                sendTextField.setDisable(false);
//                break;
//        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isConnected = false;
        displayState(ConnectionDisplayState.DISCONNECTED);




        Runtime.getRuntime().addShutdownHook(new ShutDownThread());

        /*
         * Uncomment to have autoConnect enabled at startup
         */
//        autoConnectCheckBox.setSelected(true);
//        displayState(ConnectionDisplayState.WAITING);
//        connect();
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
    private void handleConnectButton(ActionEvent event) {
        connectButton.setDisable(true);

        displayState(ConnectionDisplayState.WAITING);
        connect();
    }
    //****************************************************************
    class FxSocketListener implements SocketListener {

        @Override
        public void onMessage(String line) {
            System.out.println("message received client");
            lblMessages.setText(line);
            if (line.equals("ready") && areReady){

                ready.setVisible(false);
                initGame();
            } else if(line.equals("ready")){
                clientReady=true;
            } else if (line.startsWith("clicked")) {
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
                int randNum = Integer.parseInt(line.substring(line.indexOf("m") + 1, line.indexOf("i")));
                int i = Integer.parseInt(line.substring(line.indexOf("i") + 1, line.indexOf("j")));
                int j = Integer.parseInt(line.substring(line.indexOf("j")  + 1));
                for (Card card : cardsClicked) {
                    for (int k = 0; k < 5; k++) {
                        for (int l = 0; l < 4; l++) {
                            if (card == cards[k][l]){
                                imageViews[k][l].setImage(imageBack);
                            }
                        }
                    }
                }
                if (deck.size() > 0){
                    saveRemovedCards.add(cards[i][j]);
                    cards[i][j] = deck.get(randNum);
//                    saveRemovedCards.add(deck.get(randNum));
                    deck.remove(randNum);

                } else {
                    cards[i][j].cName = "";
                    imageViews[i][j].setImage(null);
                }


                p2Points = p2Points + (cardsClicked.size() * cardsClicked.size());
                lblp2Points.setText("P2 Points: " + p2Points);
                isTurn = true;
                lblTurn.setText("Turn: " + p1Name);
                cardsClicked.clear();
                btnEndTurn.setDisable(false);
            } else if (line.startsWith("nomatch")) {
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
                lblTurn.setText("Turn: " + p1Name);
                btnEndTurn.setDisable(false);
            } else if (line.startsWith("name:")) {
                p2Name = line.substring(line.indexOf("e:") + 2 );
            } else if (line.startsWith("CHECK")){
                if (line.substring(line.indexOf("K") + 1, line.indexOf("i")).equals(deck.get(Integer.parseInt(line.substring(line.indexOf("i") + 1))).cName)){
                    System.out.println("YEEEEEAH");
                } else{
                    System.out.println("NOOOOOOOOOOOOOOOOOOO");
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
    private void handleSendMessageButton(ActionEvent event) {
        if (!sendTextField.getText().equals("")) {
            socket.sendMessage(sendTextField.getText());
            System.out.println("Message sent client");
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
        if (clientReady){
            ready.setVisible(false);
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
//                                System.out.println("oc:"+i+"or:"+j);
                                try {
                                    tempCard = new FileInputStream(cards[i][j].getCardPath());
                                } catch (FileNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                                imageViews[i][j].setImage(new Image(tempCard));
                                cardsClicked.add(cards[i][j]);
                                btnEndTurn.setDisable(cardsClicked.size() <= 1);
                                socket.sendMessage("clicked" + i + j);
                                if (!checkMatch()){
                                    canClick = false;
                                    startTime = System.nanoTime();
                                    new AnimationTimer(){
                                        @Override
                                        public void handle(long now) {
                                            if(startTime>0){
                                                if (now - startTime > 900000000.0 * 2){
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
            };
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 4; j++) {
//                btn[i][j].setOnMouseClicked(z);
                    imageViews[i][j].setOnMouseClicked(z);
                }
            }
            initGame();
        }else{
            ready.setDisable(true);
        }
    }

    public void initGame(){

    }
    @FXML
    private void handleDeal(ActionEvent event){
        socket.sendMessage("dealt");
        deck.clear();
        if (Math.random() > .5){
            lblTurn.setText("Turn: " + p1Name);
        } else {
            isTurn = false;
            lblTurn.setText("Turn: " + p2Name);
        }

        socket.sendMessage("turn" + isTurn);


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
                int randNum = (int) (Math.random() * deck.size());
                socket.sendMessage("deck" + "randNum" + randNum + "i" + i + "j" + j);
//                System.out.println("deck" + randNum + i + j);
                cards[i][j] = deck.get(randNum);
                deck.remove(randNum);
            }
        }
        System.out.println("Server hand");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
                imageViews[i][j].setImage(imageBack);
            }
        }
    }

    @FXML
    private void enterName(){
        p1Name = txtName.getText();
        btnEnterName.setVisible(true);
        btnEnterName.setDisable(true);
        socket.sendMessage("name:" + p1Name);
    }

    public boolean checkMatch(){

        if (cardsClicked.size() == 1){
            return true;
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 1; j < cardsClicked.size(); j++) {
                if (cardsClicked.get(0).cName.charAt(i) == (cardsClicked.get(j).cName.charAt(i))) {
//                    System.out.println(cardsClicked.get(j).cName);
//                    System.out.println(cardsClicked.get(j).cName.charAt(i));
//                        System.out.println(cardsClicked.get(k).cName.substring(i, i + 1));
                    isMatch = true;
                } else {
                    isMatch = false;
                    break;
                }

            }
            if (isMatch){
                saveRemovedCards.addAll(cardsClicked);
                return true;
            }
        }

        return false;
    }

    @FXML
    private void saveGame(){
        txtSaveAs.setVisible(true);
        btnSave.setVisible(true);
    }

    @FXML
    private void save() {
        ArrayList<String> names = new ArrayList<>();
        String outFile = "src/main/resources/Game Files/names";
        String saveAs = txtSaveAs.getText();
        try {
            gameNames = new FileInputStream("src/main/resources/Game Files/names");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            File game = new File("src/main/resources/Game Files/" + saveAs);
            if (game.createNewFile()) {
                System.out.println("File created: " + game.getName());

            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            FileReader reader = new FileReader("src/main/resources/Game Files/names");
            Scanner in = new Scanner(reader);
            while (in.hasNextLine()){
                names.add(in.nextLine());
            }
            names.add(saveAs);
            PrintWriter out = new PrintWriter(outFile);
            /*This is where you would save your data.  Each time you
            run the line out.println(______) you will save a line of data
            in the text file.
             */
            //you don't need a loop.  Just type the line below as many times as you want
            //out.println();
            for (String name : names) {
                System.out.println(name);
                out.println(name);
            }
            out.close();
        } catch (FileNotFoundException var1) {
            System.out.println("no file");
        }
        try {
            PrintWriter currentGame = new PrintWriter("src/main/resources/Game Files/" + saveAs);
            currentGame.println();
            currentGame.println("p1Name:" + p1Name);
            currentGame.println("p2Name:" + p2Name);
            currentGame.println("p1points:" + p1Points);
            currentGame.println("p2points:" + p2Points);
            currentGame.println("Turn" + isTurn);
//            currentGame.println("Cards");
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 4; j++) {
                    currentGame.println("Card: i:" + i + "j:" + j + "name:" + cards[i][j].cName);
                }
            }

            for (Card card : saveRemovedCards) {
                currentGame.println("Removed Card:" + card.cName);
            }
            currentGame.close();
        } catch (FileNotFoundException var1) {
            System.out.println("no file");
        }
    }


    @FXML
    private void loadGame() throws FileNotFoundException {
        btnLoad.setVisible(true);
        lstGames.setVisible(true);

        FileReader reader = new FileReader("src/main/resources/Game Files/names");
        Scanner in = new Scanner(reader);

        lstGames.getItems().clear();
        while(in.hasNextLine()){
            lstGames.getItems().add(in.nextLine());
        }
    }

    @FXML
    private void load() throws FileNotFoundException {
        String fileToLoad = lstGames.getSelectionModel().getSelectedItem().toString();
        FileReader reader = new FileReader("src/main/resources/Game Files/" + fileToLoad);
        Scanner in = new Scanner(reader);

        socket.sendMessage("load");

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

        while(in.hasNextLine()){
            String line = in.nextLine();
            if (line.startsWith("p1Name:")){
                p1Name = line.substring(line.indexOf(":") + 1);
            } else if (line.startsWith("p2Name:")){
                p2Name = line.substring(line.indexOf(":") + 1);
            } else if (line.startsWith("p1points:")){
                p1Points = Integer.parseInt(line.substring(line.indexOf(":") + 1));
            } else if (line.startsWith("p2points:")){
                p2Points = Integer.parseInt(line.substring(line.indexOf(":") + 1));
            } else if (line.startsWith("Turn")) {
                System.out.println(line.substring(line.indexOf("n") + 1));
                isTurn = Boolean.parseBoolean(line.substring(line.indexOf("n") + 1));
            } else if (line.startsWith("Card")){
                int i = Integer.parseInt(line.substring(line.indexOf("i:") + 2, line.indexOf("j")));
                int j = Integer.parseInt(line.substring(line.indexOf("j:") + 2, line.indexOf("n")));
//                System.out.println(i + " " + j + " " + line.substring(line.indexOf("e:") + 2));
                for (Card card : deck) {
                    if (card.cName.equals(line.substring(line.indexOf("e:") + 2))){
                        cards[i][j] = card;
                        socket.sendMessage("Load cards" + "k:" + i + "l:" + j + "name:" + cards[i][j].cName);
                    }
                    imageViews[i][j].setImage(imageBack);
                }
            } else if (line.startsWith("Removed Card")){
                System.out.println(line);
                for (int i = 0; i < deck.size(); i++) {
                    if (deck.get(i).cName.equals(line.substring(line.indexOf(":") + 1))){
                        socket.sendMessage("Remove Card:" + deck.get(i).cName);
//                        System.out.println("Removed Card:" + deck.get(i).cName);
                        deck.remove(i);
                        i--;
                    }
                }
            }
        }

        System.out.println("ds" + deck.size());

        for (int k = 0; k < 5; k++) {
            for (int l = 0; l < 4; l++) {
                System.out.println("Load cards " + "k:" + k + "l:" + l + "name:" + cards[k][l].cName);
//                socket.sendMessage("Load cards" + "k:" + k + "l:" + l + "name:" + cards[k][l].cName);
            }
        }
        lblp1Points.setText("P1 Points: " + p1Points);
        lblp2Points.setText("P2 Points: " + p2Points);

        txtName.setText(p1Name);
        enterName();

        socket.sendMessage("p2Name" + p2Name);

        if (isTurn){
            lblTurn.setText("Turn: " + p1Name);
            btnEndTurn.setDisable(false);
        } else {
            lblTurn.setText("Turn: " + p2Name);
            btnEndTurn.setDisable(true);
        }

        socket.sendMessage("turn" + isTurn);
        socket.sendMessage("Load Game" + "p1" + p1Points + "p2" + p2Points);
        in.close();

    }

    @FXML
    public void endTurn(){

        if (isMatch){
            for (Card card : cardsClicked) {
                for (int k = 0; k < 5; k++) {
                    for (int l = 0; l < 4; l++) {
                        if (card == cards[k][l]){
                            imageViews[k][l].setImage(imageBack);
                            System.out.println("saveremoved cards len: " + saveRemovedCards.size());
                            int randNum = (int) (Math.random() * deck.size());
                            socket.sendMessage("endturn" + "randNum" + randNum + "i" + k + "j" + l);
                            System.out.println("deck" + randNum + k + l);
                            if (deck.size() > 0){
                                cards[k][l] = deck.get(randNum);
                                deck.remove(randNum);
                            } else {
                                imageViews[k][l].setImage(null);
                            }
                        }
                    }
                }
            }
            p1Points = p1Points + (cardsClicked.size() * cardsClicked.size());
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
        lblTurn.setText("Turn: " + p2Name);
        btnEndTurn.setDisable(true);
        socket.sendMessage("turn" + isTurn);

        if (!anyMoreMatches()){
            gridPane.setVisible(false);
            socket.sendMessage("endgame");
        }
    }

    public boolean anyMoreMatches(){
        boolean anyMoreMatches = true;

        for (int i = 0; i < 4; i++) {
            for (Card[] card : cards) {
                for (int k = 0; k < card.length - 1; k++) {
                    if (!card[k].cName.equals("") && !card[k + 1].cName.equals("")) {
                        if (card[k].cName.charAt(i) == (card[k + 1].cName.charAt(i))) {
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
                lblTurn.setText("WINNER IS " + p1Name + "!!!!!!!!!");
                System.out.println("WINNER IS " + p1Name + "!!!!!!!!!");
            } else if (p1Points == p2Points) {
                lblTurn.setText("DRAW");
                System.out.println("DRAW");
            } else {
                lblTurn.setText("WINNER IS " + p2Name + "!!!!!!!!!");
                System.out.println("WINNER iS " + p2Name + "!!!!!!!!!");
            }
        } else {
            if (deck.size() > 1) {
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < deck.size(); j++) {
                        for (int k = j + 1; k < deck.size() - 1; k++) {
                            if (deck.get(j).cName.charAt(i) == (deck.get(k).cName.charAt(i))) {
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
                        lblTurn.setText("WINNER IS " + p1Name + "!!");
                        System.out.println("WINNER IS " + p1Name + "!!");
                    } else if (p1Points == p2Points) {
                        lblTurn.setText("DRAW");
                        System.out.println("DRAW");
                    } else {
                        lblTurn.setText("WINNER IS " + p2Name + "!!");
                        System.out.println("WINNER iS " + p2Name + "!!");
                    }
                }
            }
        }
        return anyMoreMatches;
    }

    @FXML
    public void restart(){
        socket.sendMessage("Restart");
        p1Points = 0;
        p2Points = 0;
        lblp1Points.setText("P1 Points: " + p1Points);
        lblp2Points.setText("P2 Points: " + p2Points);
        deck.clear();
        cardsClicked.clear();
        saveRemovedCards.clear();

        if (Math.random() > .5){
            lblTurn.setText("Turn: " + p1Name);
            btnEndTurn.setDisable(false);
        } else {
            isTurn = false;
            lblTurn.setText("Turn: " + p2Name);
        }

        socket.sendMessage("turn" + isTurn);


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
                int randNum = (int) (Math.random() * deck.size());
                socket.sendMessage("deck" + "randNum" + randNum + "i" + i + "j" + j);
//                System.out.println("deck" + randNum + i + j);
                cards[i][j] = deck.get(randNum);
                deck.remove(randNum);
            }
        }
        System.out.println("Server hand");
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
                imageViews[i][j].setImage(imageBack);
            }
        }
    }

    public HelloController(){
        try {
            back = new FileInputStream("src/main/resources/Bold Images/Back.jpg");
            imageBack = new Image(back);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
}

//Image key:
// 1st letter - D = dots; L = lines; S = stars
// 2nd letter - B = bottle; C = glass; J = milk bottle
// 3rd letter - B = blue; O = orange; Y = yellow
// 4th letter - B = big; M = medium; S = small
