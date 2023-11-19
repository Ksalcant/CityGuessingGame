import java.awt.image.BufferedImage;
import java.net.*;
import java.nio.charset.StandardCharsets;

import java.io.*;

import org.json.*;

import javax.imageio.ImageIO;


/**
 * A class to demonstrate a simple client-server connection using sockets.
 * Ser321 Foundations of Distributed Software Systems
 */
public final class SockServer {
    static int port = 8888;
    private static boolean playing = false;
    private static int Gpoints = 0;
    static String name = "";

    ////////////////////////////////////////////////////////////////Create a static variable to hold the leaderboard
    private static final String FILENAME = "board/leader.txt";

    public static void saveLeaderboard(JSONArray leaderboard) {
        try (FileWriter writer = new FileWriter(FILENAME, StandardCharsets.UTF_8)) {
            writer.write(leaderboard.toString(2));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray loadLeaderboard() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILENAME, StandardCharsets.UTF_8))) {
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String fileContent = stringBuilder.toString();
            if (fileContent.isEmpty()) {
                return new JSONArray();
            } else {
                return new JSONArray(fileContent);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    static void addOrUpdateuser(JSONArray leaderboard, JSONObject newUser) {
        try {
            String username = newUser.getString("user");
            int points = newUser.getInt("points");

            for (int i = 0; i < leaderboard.length(); i++) {
                JSONObject player = leaderboard.getJSONObject(i);
                if (player.getString("user").equals(username)) {
                    if (points > player.getInt("points")) {
                        player.put("points", points);
                    }
                    return;
                }
            }

            // Add the new user to the leaderboard
            leaderboard.put(newUser);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray loadedLeaderboard = loadLeaderboard(); // get the leaderboard from the file
    public static JSONArray leaderboard;
    static JSONObject user = new JSONObject();


    public static void main(String[] args) {


        if (loadedLeaderboard != null) {
            leaderboard = loadedLeaderboard;
        } else {
            leaderboard = new JSONArray();
        }

        Socket sock;
        try {

            //opening the socket here, just hard coded since this is just a bas example
            ServerSocket serv = new ServerSocket(SockServer.port); // TODO, should not be hardcoded
            System.out.println("Server ready for connetion");

            // placeholder for the person who wants to play a game
            while (true) {
                sock = serv.accept(); // blocking wait
                InputStream inputStream = sock.getInputStream();
                OutputStream out = sock.getOutputStream();

                StringBuilder receivedData = new StringBuilder();

                JSONObject clientmsg = Read(inputStream, receivedData);

                JSONObject response = new JSONObject();
                System.out.println("THIS IS MY READING NOW " + clientmsg);
                if ("start".equalsIgnoreCase(clientmsg.getString("type"))) {
                    if(!playing) {
                        System.out.println("- Got a start");
                        //response.put("key", 0);
                        response.put("type", "hello");
                        response.put("value", "Hello, please tell me your name.");
                        sendImg("img/hi.jpg", response); // calling a method that will manipulate the image and will make it send ready
                    }else{
                        response.put("type", "hello");
                        response.put("value", "A game is already in progress. Cannot connect.");
                        sendImg("img/hi.jpg", response); // calling a method that will manipulate the image and will make it send ready
                    }
                } else if ("name".equalsIgnoreCase(clientmsg.getString("type"))) {
                    name = clientmsg.getString("name");
                    /////////////////////////////////////////////////////////	//add new person with name to the leaderboard.

                    user.put("user", name);
                    Gpoints = 0;
                    user.put("points", Gpoints);
                    addOrUpdateuser(leaderboard, user);
                    saveLeaderboard(leaderboard);

                    sendImg("img/welcome.jpg", response);
                    response.put("type", "hello");
                    response.put("value", "Hi " + name + " would you like to view the leaderboard (leader) or play game (play)"); // get the name from the json
                    response.put("key", 0);
                } else if ("leader".equalsIgnoreCase(clientmsg.getString("type"))) {
                    showleader(response);

                } else if (clientmsg.getString("type").equalsIgnoreCase("quit")) {
                    playing = false;
                    name = "";
                    response.put("playing", false);
                    response.put("key", 0);
                    response.put("value", "You ended the game! See you next time or type your name to start again...");
                    response.put("type", "quit");
                    sendImg("img/Bye.jpg", response);
                    //sendImg("img/hi.png", response);
                    System.out.println("Game ended by client");

                } else if ("play".equalsIgnoreCase(clientmsg.getString("type"))) {

                    if (!playing) {
                        clientmsg.put("key", 1);
                        playing = true;
                    }
                    switch (clientmsg.getInt("key")) {
                        case 1:

                            response.put("key", 2);
                            response.put("value", "NEW GAME STARTED: Which city is this?");
                            response.put("type", "play");
                            Berlin ber = new Berlin(); // Display berlin1
                            sendImg(ber.Get1(), response);
                            System.out.println("The answer is : Berlin");
                            break;
                        case 2:
                            if (clientmsg.getString("answer").equalsIgnoreCase("berlin")) {
                                Gpoints += 5;
                                user.put("points", Gpoints);
                                SockServer.addOrUpdateuser(leaderboard, user);

                                Block6(response);
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {

                                response.put("key", 3);
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("type", "play");
                                Berlin ber2 = new Berlin(); // Display berlin2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is : Berlin");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);

                                response.put("key", 6);
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                response.put("type", "play");
                                Ireland ir = new Ireland(); // display ireland1
                                sendImg(ir.Get1(), response);
                                System.out.println("The answer is : Ireland");
                                break;

                            } else {

                                response.put("key", 3);
                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Berlin ber2 = new Berlin(); // Display berlin2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is : Berlin");
                                break;
                            }
                        case 3:
                            if (clientmsg.getString("answer").equalsIgnoreCase("berlin")) {
                                Gpoints += 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);

                                Block6(response);
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");

                                response.put("key", 4);
                                response.put("type", "play");
                                Berlin ber2 = new Berlin(); // display berlin 3
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is : Berlin");
                                break;

                            } else if ("next".equalsIgnoreCase(clientmsg.getString("answer"))) {
                                ReducePointsJumpTo6(response);
                                break;
                            } else {

                                response.put("key", 4);
                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Berlin ber2 = new Berlin(); // display berlin 3
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is : Berlin");
                                break;
                            }
                        case 4:
                            if (clientmsg.getString("answer").equalsIgnoreCase("berlin")) {

                                Gpoints += 3;
                                user.put("points", Gpoints);
                                SockServer.addOrUpdateuser(leaderboard, user);
                                Block6(response);
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");

                                response.put("key", 5);
                                response.put("type", "play");
                                Berlin ber2 = new Berlin(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is : Berlin");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                ReducePointsJumpTo6(response);
                            } else {

                                response.put("key", 5);
                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Berlin ber2 = new Berlin(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is : Berlin");
                                break;
                            }
                        case 5:
                            if (clientmsg.getString("answer").equalsIgnoreCase("berlin")) {
                                Gpoints += 1;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);

                                Block6(response);
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("key", 6);

                                response.put("value", "Sorry, but that was my last image for this city, but let's try this new place! ");
                                response.put("type", "play");
                                Ireland ir = new Ireland();
                                sendImg(ir.Get1(), response);
                                System.out.println("The answer is : Ireland");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                ReducePointsJumpTo6(response);
                            } else {
                                response.put("key", 6);

                                response.put("value", "You got it wrong, previous city was berlin, but let's try this one! ");
                                response.put("type", "play");
                                Ireland ir = new Ireland();
                                sendImg(ir.Get1(), response);
                                System.out.println("The answer is : Ireland");
                                break;
                            }
                        case 6://////////////////////////////////////////////////////////////////////////////////////////////////////////////
                            if (clientmsg.getString("answer").equalsIgnoreCase("ireland")) {
                                Gpoints += 5;
                                user.put("points", Gpoints);
                                SockServer.addOrUpdateuser(leaderboard, user);

                                response.put("key", 10);
                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Paris par = new Paris();
                                sendImg(par.Get1(), response);
                                System.out.println("The answer is : Paris");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 7);

                                response.put("type", "play");
                                Ireland ber2 = new Ireland();
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is : Ireland");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);

                                response.put("key", 10);

                                response.put("type", "play");
                                Paris par = new Paris();
                                sendImg(par.Get1(), response);
                                System.out.println("The answer is : Paris");
                                break;
                            } else {
                                response.put("key", 7);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Ireland ber2 = new Ireland();
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is : Ireland");
                                break;
                            }
                        case 7:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Ireland")) {
                                Gpoints += 4;
                                user.put("points", Gpoints);
                                SockServer.addOrUpdateuser(leaderboard, user);
                                response.put("key", 10);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Paris par = new Paris(); // display ireland1
                                sendImg(par.Get1(), response);
                                System.out.println("The answer is : Paris ");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 8);

                                response.put("type", "play");
                                Ireland ber2 = new Ireland();
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is : Ireland");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                ReducePointsJumpTo10(response);
                                System.out.println("The answer is : Paris ");
                                break;
                            } else {
                                response.put("key", 8);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Ireland ber2 = new Ireland();
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is : Ireland");
                                break;
                            }
                        case 8:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Ireland")) {
                                Gpoints += 3;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 10);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Paris ir = new Paris();
                                sendImg(ir.Get1(), response);
                                System.out.println("The answer is : Paris");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 9);

                                response.put("type", "play");
                                Ireland ber2 = new Ireland(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is :  Ireland");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                ReducePointsJumpTo10(response);
                                System.out.println("The answer is : Paris");
                                break;
                            } else {
                                response.put("key", 9);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Ireland ber2 = new Ireland(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is :  Ireland");
                                break;
                            }
                        case 9:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Ireland")) {
                                Gpoints += 1;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 10);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Paris ir = new Paris();
                                sendImg(ir.Get1(), response);
                                System.out.println("The answer is : Paris");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("key", 10);

                                response.put("value", "Sorry, that was my last image for Ireland,but try this new place! ");
                                response.put("type", "play");
                                Paris ir = new Paris();
                                sendImg(ir.Get1(), response);
                                System.out.println("The answer is : Paris");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                ReducePointsJumpTo10(response);
                                System.out.println("The answer is : Paris");
                                break;
                            } else {
                                response.put("key", 10);

                                response.put("value", "You got it wrong, previous city was Ireland, but let's try this one! ");
                                response.put("type", "play");
                                Paris ir = new Paris();
                                sendImg(ir.Get1(), response);
                                System.out.println("The answer is : Paris");
                                break;
                            }
                        case 10://////////////////////////////////////////////////////////////////////
                            if (clientmsg.getString("answer").equalsIgnoreCase("Paris")) {
                                Gpoints += 5;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);

                                response.put("key", 14);// now display 9
                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Phoenix ph = new Phoenix();
                                sendImg(ph.Get1(), response);
                                System.out.println("The answer is : Phoenix");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 11);

                                response.put("type", "play");
                                Paris ber2 = new Paris(); // Display  Paris2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is :  Paris");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);

                                response.put("key", 14);// now display 9

                                response.put("type", "play");
                                Phoenix ph = new Phoenix();
                                sendImg(ph.Get1(), response);
                                System.out.println("The answer is : Phoenix");
                                break;
                            } else {
                                response.put("key", 11);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Paris ber2 = new Paris(); // Display  Paris2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is :  Paris");
                                break;
                            }
                        case 11:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Paris")) {
                                Gpoints += 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 14);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Phoenix ph = new Phoenix();
                                sendImg(ph.Get1(), response);
                                System.out.println("The answer is : Phoenix");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 12);

                                response.put("type", "play");
                                Paris ber2 = new Paris(); // display  Paris 3
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is :  Paris");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 14);


                                response.put("type", "play");
                                Phoenix ph = new Phoenix();
                                sendImg(ph.Get1(), response);
                                System.out.println("The answer is : Phoenix");
                                break;
                            } else {
                                response.put("key", 12);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Paris ber2 = new Paris(); // display  Paris 3
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is :  Paris");
                                break;
                            }
                        case 12:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Paris")) {
                                Gpoints += 3;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 14);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Phoenix ph = new Phoenix();
                                sendImg(ph.Get1(), response);
                                System.out.println("The answer is : Phoenix");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 13);

                                response.put("type", "play");
                                Paris ber2 = new Paris(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is :  Paris");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 14);


                                response.put("type", "play");
                                Phoenix ph = new Phoenix();
                                sendImg(ph.Get1(), response);
                                System.out.println("The answer is : Phoenix");
                                break;
                            } else {
                                response.put("key", 13);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Paris ber2 = new Paris(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is :  Paris");
                                break;
                            }
                        case 13:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Paris")) {
                                Gpoints += 1;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 14);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Phoenix ph = new Phoenix();
                                sendImg(ph.Get1(), response);
                                System.out.println("The answer is : Phoenix");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("key", 14);

                                response.put("value", "Sorry, that was my last image for Paris, but try this new place! ");
                                response.put("type", "play");
                                Phoenix ph = new Phoenix();
                                sendImg(ph.Get1(), response);
                                System.out.println("The answer is : Phoenix");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 14);


                                response.put("type", "play");
                                Phoenix ph = new Phoenix();
                                sendImg(ph.Get1(), response);
                                System.out.println("The answer is : Phoenix");
                                break;
                            } else {
                                response.put("key", 14);

                                response.put("value", "You got it wrong, previous city was  Paris, but let's try this one! ");
                                response.put("type", "play");
                                Phoenix ph = new Phoenix();
                                sendImg(ph.Get1(), response);
                                System.out.println("The answer is : Phoenix");
                                break;
                            }
                        case 14:///////////////////////////////////////////////////////////////////////////////////////////////////////////
                            if (clientmsg.getString("answer").equalsIgnoreCase("Phoenix")) {
                                Gpoints += 5;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 18);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Rome ro = new Rome();
                                sendImg(ro.Get1(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 15);

                                response.put("type", "play");
                                Phoenix ber2 = new Phoenix(); // Display  Phoenix2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is :  Phoenix");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 18);

                                response.put("value", "Skip the city? Okay here is a new one!");
                                response.put("type", "play");
                                Rome ro = new Rome();
                                sendImg(ro.Get1(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            } else {
                                response.put("key", 15);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Phoenix ber2 = new Phoenix(); // Display  Phoenix2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is :  Phoenix");
                                break;
                            }

                        case 15:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Phoenix")) {
                                Gpoints += 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 18);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Rome ro = new Rome();
                                sendImg(ro.Get1(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 16);

                                response.put("type", "play");
                                Phoenix ber2 = new Phoenix(); // display  Phoenix 3
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is :  Phoenix");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 18);


                                response.put("type", "play");
                                Rome ro = new Rome();
                                sendImg(ro.Get1(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            } else {
                                response.put("key", 16);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Phoenix ber2 = new Phoenix(); // display  Phoenix 3
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is :  Phoenix");
                                break;
                            }
                        case 16:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Phoenix")) {
                                Gpoints += 3;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 18);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Rome ro = new Rome();
                                sendImg(ro.Get1(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 17);

                                response.put("type", "play");
                                Phoenix ber2 = new Phoenix(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is :  Phoenix");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 18);


                                response.put("type", "play");
                                Rome ro = new Rome();
                                sendImg(ro.Get1(), response);
                                System.out.println("The answer is : Rome");
                                break;

                            } else {
                                response.put("key", 17);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Phoenix ber2 = new Phoenix(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is :  Phoenix");
                                break;
                            }
                        case 17:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Phoenix")) {
                                Gpoints += 1;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 18);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Rome ro = new Rome();
                                sendImg(ro.Get1(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("key", 18);

                                response.put("value", "Sorry, that was my last image for Phoenix, but try this new place! ");
                                response.put("type", "play");
                                Rome ro = new Rome();
                                sendImg(ro.Get1(), response);
                                System.out.println("The answer is : Rome");
                                break;

                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 18);


                                response.put("type", "play");
                                Rome ro = new Rome();
                                sendImg(ro.Get1(), response);
                                System.out.println("The answer is : Rome");
                                break;

                            } else {
                                response.put("key", 18);

                                response.put("value", "You got it wrong, previous city was  Phoenix, but let's try this one! ");
                                response.put("type", "play");
                                Rome ro = new Rome();
                                sendImg(ro.Get1(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            }
                        case 18:////////////////////////////////////////////////////////////////////////////////////////////////////////////
                            if (clientmsg.getString("answer").equalsIgnoreCase("Rome")) {
                                Gpoints += 5;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 22);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                SanFrancisco sf = new SanFrancisco();
                                sendImg(sf.Get1(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 19);

                                response.put("type", "play");
                                Rome ber2 = new Rome(); // Display Rome2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 22);


                                response.put("type", "play");
                                SanFrancisco sf = new SanFrancisco();
                                sendImg(sf.Get1(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;

                            } else {
                                response.put("key", 19);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Rome ber2 = new Rome(); // Display Rome2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            }

                        case 19:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Rome")) {
                                Gpoints += 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 22);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                SanFrancisco sf = new SanFrancisco();
                                sendImg(sf.Get1(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 20);

                                response.put("type", "play");
                                Rome ber2 = new Rome(); // display Rome 3
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            } else if ("next".equalsIgnoreCase(clientmsg.getString("answer"))) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 22);


                                response.put("type", "play");
                                SanFrancisco sf = new SanFrancisco();
                                sendImg(sf.Get1(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;

                            } else {
                                response.put("key", 20);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Rome ber2 = new Rome(); // display Rome 3
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            }
                        case 20:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Rome")) {
                                Gpoints += 3;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 22);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                SanFrancisco sf = new SanFrancisco();
                                sendImg(sf.Get1(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 21);

                                response.put("type", "play");
                                Rome ber2 = new Rome(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 22);


                                response.put("type", "play");
                                SanFrancisco sf = new SanFrancisco();
                                sendImg(sf.Get1(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;

                            } else {
                                response.put("key", 21);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Rome ber2 = new Rome(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is : Rome");
                                break;
                            }
                        case 21:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Rome")) {
                                Gpoints += 1;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 22);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                SanFrancisco sf = new SanFrancisco();
                                sendImg(sf.Get1(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("key", 22);

                                response.put("value", "Sorry, that was my last image for Rome, buttry this new place! ");
                                response.put("type", "play");
                                SanFrancisco sf = new SanFrancisco();
                                sendImg(sf.Get1(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;

                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 22);


                                response.put("type", "play");
                                SanFrancisco sf = new SanFrancisco();
                                sendImg(sf.Get1(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;

                            } else {
                                response.put("key", 22);

                                response.put("value", "You got it wrong, previous city was Rome, but let's try this one! ");
                                response.put("type", "play");
                                SanFrancisco sf = new SanFrancisco();
                                sendImg(sf.Get1(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            }
                        case 22://////////////////////////////////////////////////////////////////////////////////////////////////
                            if (clientmsg.getString("answer").equalsIgnoreCase("SanFrancisco")) {
                                Gpoints += 5;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 26);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Switzerland sz = new Switzerland();
                                sendImg(sz.Get1(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 23);

                                response.put("type", "play");
                                SanFrancisco ber2 = new SanFrancisco(); // Display SanFrancisco2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 26);


                                response.put("type", "play");
                                Switzerland sz = new Switzerland();
                                sendImg(sz.Get1(), response);
                                System.out.println("The answer is : Switzerland");
                                break;

                            } else {
                                response.put("key", 23);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                SanFrancisco ber2 = new SanFrancisco(); // Display SanFrancisco2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            }

                        case 23:
                            if (clientmsg.getString("answer").equalsIgnoreCase("SanFrancisco")) {
                                Gpoints += 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 26);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Switzerland sz = new Switzerland();
                                sendImg(sz.Get1(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 24);

                                response.put("type", "play");
                                SanFrancisco ber2 = new SanFrancisco(); // display SanFrancisco 3
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 26);


                                response.put("type", "play");
                                Switzerland sz = new Switzerland();
                                sendImg(sz.Get1(), response);
                                System.out.println("The answer is : Switzerland");
                                break;

                            } else {
                                response.put("key", 24);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                SanFrancisco ber2 = new SanFrancisco(); // display SanFrancisco 3
                                sendImg(ber2.Get3(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            }
                        case 24:
                            if (clientmsg.getString("answer").equalsIgnoreCase("SanFrancisco")) {
                                Gpoints += 3;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 26);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Switzerland sz = new Switzerland();
                                sendImg(sz.Get1(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 25);

                                response.put("type", "play");
                                SanFrancisco ber2 = new SanFrancisco();
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 26);


                                response.put("type", "play");
                                Switzerland sz = new Switzerland();
                                sendImg(sz.Get1(), response);
                                System.out.println("The answer is : Switzerland");
                                break;

                            } else {
                                response.put("key", 25);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                SanFrancisco ber2 = new SanFrancisco();
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is : SanFrancisco");
                                break;
                            }
                        case 25:
                            if (clientmsg.getString("answer").equalsIgnoreCase("SanFrancisco")) {
                                Gpoints += 1;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 26);

                                response.put("value", "Which city is this?");
                                response.put("type", "play");
                                Switzerland sz = new Switzerland();
                                sendImg(sz.Get1(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("key", 26);

                                response.put("value", "That was my last image which was SanFrancisco, but try this new city! ");
                                response.put("type", "play");
                                Switzerland sz = new Switzerland();
                                sendImg(sz.Get1(), response);
                                System.out.println("The answer is : Switzerland");
                                break;

                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                response.put("value", "Skipt to the next city? Okay, then guess this one!");
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                response.put("key", 26);


                                response.put("type", "play");
                                Switzerland sz = new Switzerland();
                                sendImg(sz.Get1(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            } else {
                                response.put("key", 26);

                                response.put("value", "You got it wrong, but let's try this one! ");
                                response.put("type", "play");
                                Switzerland sz = new Switzerland();
                                sendImg(sz.Get1(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            }
                        case 26://////////////////////////////////////////////////////////////////////////////////////////////////////////
                            if (clientmsg.getString("answer").equalsIgnoreCase("Switzerland")) {
                                Gpoints += 5;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                WonOrLose(response, "Great job! Look above to see if you won.\n" + "Type your name to play again");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                WonOrLose(response, "That was the last city. Look above to see if you won.\n" + "Type your name to play again");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 27);

                                response.put("type", "play");
                                Switzerland ber2 = new Switzerland(); // Display Switzerland2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            } else {
                                response.put("key", 27);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Switzerland ber2 = new Switzerland(); // Display Switzerland2
                                sendImg(ber2.Get2(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            }

                        case 27:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Switzerland")) {
                                Gpoints += 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                WonOrLose(response, "Great job! Look above to see if you won.\n" + "Type your name to play again");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                WonOrLose(response, "That was the last city. Look above to see if you won.\n" + "Type your name to play again");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 29);

                                response.put("type", "play");
                                Switzerland ber2 = new Switzerland(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            } else {
                                response.put("key", 29);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Switzerland ber2 = new Switzerland(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            }
                        case 28:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Switzerland")) {
                                Gpoints += 3;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                WonOrLose(response, "Great job! Look above to see if you won.\n" + "Type your name to play again");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more")) {
                                response.put("value", "You asked for more pictures? okay, try to guess this new one! ");
                                response.put("key", 29);

                                response.put("type", "play");
                                Switzerland ber2 = new Switzerland(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("next")) {
                                Gpoints -= 4;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                WonOrLose(response, "That was the last city. Look above to see if you won.\n" + "Type your name to play again");
                                break;
                            } else {
                                response.put("key", 29);

                                response.put("value", "You got it wrong, but try to guess this new one! ");
                                response.put("type", "play");
                                Switzerland ber2 = new Switzerland(); // now display 3
                                sendImg(ber2.Get4(), response);
                                System.out.println("The answer is : Switzerland");
                                break;
                            }
                        case 29:
                            if (clientmsg.getString("answer").equalsIgnoreCase("Switzerland")) {
                                Gpoints += 1;
                                user.put("points", Gpoints);
                                addOrUpdateuser(leaderboard, user);
                                WonOrLose(response, "Great job! Look above to see if you won.\n" + "Type your name to play again");
                                break;
                            } else if (clientmsg.getString("answer").equalsIgnoreCase("more") || (clientmsg.getString("answer").equalsIgnoreCase("next"))) {
                                String val = "Oops! That was the last round.  Look at above to see if you won.\n" + "Type your name to play again";
                                WonOrLose(response, val);
                                break;
                            } else {
                                String val = "You got it wrong and it is the end of the game =(. Look at above to see if you won.\n" + "Type your name to play again";
                                WonOrLose(response, val);
                                break;
                            }
                    }


                } else {
                    System.out.println("not sure what you meant");
                    response.put("type", "error");
                    response.put("value", "unknown response");
                    sendImg("img/questions.jpg", response);
                }
                response.put("points", Gpoints);
                response.put("name", name);
                response.put("playing",playing);
                String jsonString = response.toString() + "\n";
                //  System.out.println("BEFORE SENDING IT BACK" + response.toString());
                byte[] jsonBytes = jsonString.getBytes();

                Writing(out, jsonBytes);
                // sock.close();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void ReducePointsJumpTo10(JSONObject response) throws IOException {
        Gpoints -= 4;
        response.put("value", "Skipt to the next city? Okay, then guess this one!");
        user.put("points", Gpoints);
        addOrUpdateuser(leaderboard, user);
        response.put("key", 10);


        response.put("type", "play");
        Paris par = new Paris(); // display ireland1
        sendImg(par.Get1(), response);
    }

    private static void ReducePointsJumpTo6(JSONObject response) throws IOException {
        Gpoints -= 4;
        response.put("value", "Skipt to the next city? Okay, then guess this one!");
        user.put("points", Gpoints);
        addOrUpdateuser(leaderboard, user);

        response.put("key", 6);

        response.put("type", "play");
        Ireland ir = new Ireland(); // display ireland1
        sendImg(ir.Get1(), response);
        System.out.println("The answer is : Ireland");
        return;
    }

    private static void Block6(JSONObject response) throws IOException {
        response.put("key", 6);// now display 9
        response.put("value", "Which city is this?");
        response.put("type", "play");
        Ireland ir = new Ireland();
        sendImg(ir.Get1(), response);
        System.out.println("The answer is : Ireland");
        return;
    }


    /* TODO this is for you to implement, I just put a place holder here */
    public static void sendImg(String filename, JSONObject obj) throws IOException, JSONException {
        BufferedImage imageToSend = ImageIO.read(new File(filename));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(imageToSend, "jpg", byteArrayOutputStream);
        byte[] imageData = byteArrayOutputStream.toByteArray();
        obj.put("image", imageData);
    }

    public static void WonOrLose(JSONObject response, String val) throws JSONException, IOException {
        if (user.getInt("points") > 12) {
            sendImg("img/win.jpg", response);
        } else if (user.getInt("points") < -3) {
            sendImg("img/lose.jpg", response);
        } else {
            sendImg("img/questions.jpg", response);
        }
        response.put("type", "play");
        response.put("value", val);
        response.put("key", 0);
        playing = false;
        name = "";
        response.put("playing", false);
        System.out.println("WE REACHED THE END OF THE GAME!");

    }


    static void showleader(JSONObject response) throws Exception {
        JSONObject player;

        String hold = "*********LEADERBOARD*********\n";
        for (int i = 0; i < leaderboard.length(); i++) {
            player = leaderboard.getJSONObject(i);
            hold += player.getString("user") + ": " + player.getInt("points") + "\n";
        }

        hold += "*************************************\n";
        response.put("type", "leader");
        response.put("value", hold);
        //sendImg("img/hi.png", response);
        response.put("key", 0);

    }

    private static void Writing(OutputStream out, byte[] jsonBytes) throws IOException {
        out.write(jsonBytes);
        out.flush();
        System.out.println("JUST SENT TO CLIENT: " + jsonBytes);
    }

    public static JSONObject Read(InputStream is, StringBuilder receivedData) throws IOException {
        int data = 0;

        while ((data = is.read()) != -1) {
            if ((char) data == '\n') {
                break; // Exit the loop when the delimiter is encountered
            }

            receivedData.append((char) data);

        }
        JSONObject j = new JSONObject(receivedData.toString());
        System.out.println("in the reading " + j);
        return j;

    }
}
