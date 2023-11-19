import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static java.lang.System.exit;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status.
 * <p>
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 * -> modal means that it opens the GUI and suspends background processes. Processing
 * still happens in the GUI. If it is desired to continue processing in the
 * background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * <p>
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 */
public class ClientGui implements OutputPanel.EventHandlers {
    JDialog frame;
    PicturePanel picPanel;
    OutputPanel outputPanel;
    String currentMess;
    Socket sock;
    OutputStream out;
    InputStream inputStream;

    // TODO: SHOULD NOT BE HARDCODED
    String host;
    int port;
    StringBuilder receivedData = new StringBuilder();
    ByteArrayInputStream byteArrayInputStream;

    /**
     * Construct dialog
     *
     * @throws IOException
     */
    public ClientGui(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        frame = new JDialog();
        frame.setLayout(new GridBagLayout());
        frame.setMinimumSize(new Dimension(500, 500));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // setup the top picture frame
        picPanel = new PicturePanel();
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.25;
        frame.add(picPanel, c);

        // setup the input, button, and output area
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 0.75;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        outputPanel = new OutputPanel();
        outputPanel.addEventHandlers(this);
        frame.add(outputPanel, c);
        picPanel.newGame(1);
        insertImage("img/hi.png", 0, 0);

        open(); // opening server connection here
        currentMess = "{'type': 'start'}" + "\n"; // very initial start message for the connection

        try {
            Writing(out, currentMess.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = Read(inputStream);
        System.out.println("Got a connection to server");
        if(jsonObject.getBoolean("playing")==true){
            insertImage2(jsonObject, 0, 0);
            outputPanel.appendOutput(jsonObject.getString("value")); // putting the message in the outputpanel
            System.out.println("A game is already in progress. Cannot connect.");
            exit(-1);

        }
        insertImage2(jsonObject, 0, 0);
        outputPanel.appendOutput(jsonObject.getString("value")); // putting the message in the outputpanel

        // reading out the image (abstracted here as just a string)
        //System.out.println("Pretend I got an image: " + json.getString("image"));
        /// would put image in picture panel
        close(); //closing the connection to server

        // Now Client interaction only happens when the submit button is used, see "submitClicked()" method

    }

    public JSONObject Read(InputStream is) throws IOException {
        receivedData.setLength(0); // Clear the StringBuilder before reading new data
        int data;
        while ((data = is.read()) != -1) {
            if ((char) data == '\n') {
                break; // Exit the loop when the delimiter is encountered
            }
            receivedData.append((char) data);
        }
        return new JSONObject(receivedData.toString());
    }

    private static void Writing(OutputStream out, byte[] jsonBytes) throws IOException {
        out.write(jsonBytes);
        System.out.println("JUST SENT TO server: " + jsonBytes);
        out.flush();
    }

    /**
     * Shows the current state in the GUI
     *
     * @param makeModal - true to make a modal window, false disables modal behavior
     */
    public void show(boolean makeModal) {
        frame.pack();
        frame.setModal(makeModal);
        frame.setVisible(true);
    }

    /**
     * Creates a new game and set the size of the grid
     *
     * @param dimension - the size of the grid will be dimension x dimension
     *                  No changes should be needed here
     */
    public void newGame(int dimension) {
        picPanel.newGame(1);
        outputPanel.appendOutput("Started new game with a " + dimension + "x" + dimension + " board.");
    }

    /**
     * Insert an image into the grid at position (col, row)
     *
     * @param filename - filename relative to the root directory
     * @param row      - the row to insert into
     * @param col      - the column to insert into
     * @return true if successful, false if an invalid coordinate was provided
     * @throws IOException An error occured with your image file
     */
    public boolean insertImage(String filename, int row, int col) throws IOException {
        System.out.println("Image insert");
        String error = "";
        try {
            // insert the image
            if (picPanel.insertImage(filename, row, col)) {
                return true;
            }
            error = "File(\"" + filename + "\") not found.";
        } catch (PicturePanel.InvalidCoordinateException e) {
            // put error in output
            error = e.toString();
        }
        outputPanel.appendOutput(error);
        return false;
    }

    public void insertImage2(JSONObject obj, int row, int col) throws IOException {
        JSONArray imageDataArray = obj.getJSONArray("image");

        byte[] imageData = new byte[imageDataArray.length()];
        for (int i = 0; i < imageDataArray.length(); i++) {
            imageData[i] = (byte) imageDataArray.getInt(i);
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageData);

        String error = "";
        try {
            picPanel.insertImage(byteArrayInputStream, row, col);
            // put status in output
            // outputPanel.appendOutput("Inserting image in position (" + row + ", " + col + ")"); // you can of course remove this

        } catch (PicturePanel.InvalidCoordinateException e) {
            // put error in output
            error = "Invalid coordinates. or image object";
            //error = e.toString();

        }

    }

    int Gkey = 0;
    String Gname = "";
    boolean playing = false;

    /**
     * Submit button handling
     * */
    @Override
    public void submitClicked() {
        JSONObject msg = new JSONObject();

        try {
            // opening a server connection again
            System.out.println("submit clicked ");

            // Pulls the input box text
            String input = (String) outputPanel.getInputText();
            outputPanel.setInputText("");
            // TODO evaluate the input from above and create a request for client.

            if (input.isEmpty()) {
                return;
            } else if (input.equalsIgnoreCase("leader")) {
                open();
                msg.put("type", "leader");
                msg.put("key", Gkey);
                msg.put("answer", "");
            } else if (input.equalsIgnoreCase("play")) {
                if ((!playing) && (!Gname.isEmpty())) {
                    open();
                    msg.put("type", "play");
                    msg.put("key", Gkey);
                    msg.put("answer", "");
                } else {
                    outputPanel.appendOutput("A game is already in process or you did not put your name");
                    return;

                }
            } else if (input.equalsIgnoreCase("berlin")) {
                if (playing) {
                    open();
                    msg.put("type", "play");
                    msg.put("answer", "berlin");
                    msg.put("key", Gkey);
                } else {
                    outputPanel.appendOutput("Type 'play' to start a game...");
                    return;
                }

            } else if (input.equalsIgnoreCase("ireland")) {
                if (playing) {
                    open();
                    msg.put("type", "play");
                    msg.put("answer", "ireland");
                    msg.put("key", Gkey);
                } else {
                    outputPanel.appendOutput("Type 'play' to start a game...");
                    return;
                }
            } else if (input.equalsIgnoreCase("paris")) {
                if (playing) {
                    open();
                    msg.put("type", "play");
                    msg.put("answer", "paris");
                    msg.put("key", Gkey);
                } else {
                    outputPanel.appendOutput("Type 'play' to start a game...");
                    return;
                }
            } else if (input.equalsIgnoreCase("phoenix")) {
                if (playing) {
                    open();
                    msg.put("type", "play");
                    msg.put("answer", "phoenix");
                    msg.put("key", Gkey);
                } else {
                    outputPanel.appendOutput("Type 'play' to start a game...");
                    return;
                }
            } else if (input.equalsIgnoreCase("rome")) {
                if (playing) {
                    open();
                    msg.put("type", "play");
                    msg.put("answer", "rome");
                    msg.put("key", Gkey);
                } else {
                    outputPanel.appendOutput("Type 'play' to start a game...");
                    return;
                }
            } else if (input.equalsIgnoreCase("SanFrancisco")) {
                if (playing) {
                    open();
                    msg.put("type", "play");
                    msg.put("answer", "SanFrancisco");
                    msg.put("key", Gkey);
                } else {
                    outputPanel.appendOutput("Type 'play' to start a game...");
                    return;
                }
            } else if (input.equalsIgnoreCase("switzerland")) {
                if (playing) {
                    open();
                    msg.put("type", "play");
                    msg.put("answer", "switzerland");
                    msg.put("key", Gkey);
                } else {
                    outputPanel.appendOutput("Type 'play' to start a game...");
                    return;
                }
            } else if (input.equalsIgnoreCase("more")) {
                if (playing) {
                    open();
                    msg.put("type", "play");
                    msg.put("answer", "more");
                    msg.put("key", Gkey);
                } else {
                    outputPanel.appendOutput("Type 'play' to start a game...");
                    return;
                }
            } else if (input.equalsIgnoreCase("next")) {
                if (playing) {
                    open();
                    msg.put("type", "play");
                    msg.put("answer", "next");
                    msg.put("key", Gkey);
                } else {
                    outputPanel.appendOutput("Type 'play' to start a game...");
                    return;
                }
            } else if (input.equalsIgnoreCase("quit")) {
                if (playing) {
                    open();
                    msg.put("type", "quit");
                    msg.put("answer", "");
                    msg.put("key", 0);
                } else {
                    outputPanel.appendOutput("There are no game happenning. Type 'play' to start a game...");
                    return;
                }
            } else if (!playing && Gname.isEmpty()) { // anything that comes in is a name.
                open();
                msg.put("type", "name");
                msg.put("key", Gkey);
                msg.put("name", input);
                msg.put("answer", "");
            } else {   //////This is for wrong answers
                if (playing) {
                    open();
                    msg.put("type", "play");
                    msg.put("answer", input);
                    msg.put("key", Gkey);
                } else {
                    outputPanel.appendOutput("Please follow the prompt below...");
                    System.out.println("LET ME SEE THE NAME" + Gname);
                    return;
                }
            }

            // send request to server
            String jsonmsg = msg.toString() + "\n";
            Writing(out, jsonmsg.getBytes());// make jsonObject a string then get the bytes and send to the server
            try {
                JSONObject ans = Read(inputStream); // read
                System.out.println("Below is the response from server");
                System.out.println(ans);

                if (ans.getString("type").equals("play")) {

                    insertImage2(ans, 0, 0);
                    outputPanel.setPoints(ans.getInt("points"));
                    outputPanel.appendOutput(ans.getString("value"));
                    Gkey = ans.getInt("key");
                    playing = ans.getBoolean("playing");
                    Gname = ans.getString("name");
                } else if (ans.getString("type").equals("leader")) {

                    outputPanel.setPoints(ans.getInt("points"));
                    outputPanel.appendOutput(ans.getString("value")); // putting the message in the outputpanel
                    Gname = ans.getString("name");
                } else {
                    insertImage2(ans, 0, 0);
                    Gname = ans.getString("name"); // save the name
                    outputPanel.appendOutput(ans.getString("value"));
                    outputPanel.setPoints(ans.getInt("points"));
                    Gkey = ans.getInt("key");
                    playing = ans.getBoolean("playing");

                }

            } catch (Exception e) {

                e.printStackTrace();
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            close(); // Close the socket and streams after reading the response
        }
    }

    /**
     * Key listener for the input text box
     * <p>
     * Change the behavior to whatever you need
     */
    @Override
    public void inputUpdated(String input) {


        if (input.isEmpty()) {
            input = outputPanel.getInputText();

        }
    }

    public void open() throws IOException {
        sock = new Socket(host, port); // connect to host and socket

        // get output channel
        // create an object output writer (Java only)
        try {
            out = sock.getOutputStream();
            inputStream = sock.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            close(); // Close the socket if ObjectOutputStream creation fails
            throw e; // Rethrow the exception to handle it at the calling code
        }

    }

    public void close() {
        try {
            if (out != null) out.close();
            if (inputStream != null) inputStream.close();
            if (sock != null) sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // create the frame


        try {
            String host = "localhost";
            int port = 8888;


            ClientGui gui = new ClientGui(host, port);
            gui.show(true);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
