import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private MyServer myServer;
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String name;
    public String getName(){
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) throws IOException {
        this.myServer = myServer;
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
        this.name = "";
        new Thread(()->{
            try {
                authentication();
                readMessages();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

                ).start();
    }
    public void authentication()throws  IOException{
        while (true){
            String str = in.readUTF();
            if(str.startsWith("/auth")){
                String [] parts = str.split("\\s");
                String nick = myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                // /auth login1 pass1
                if(nick!=null){
                    if(!myServer.isNickBusy(nick)){
                        sendMsg("/authok" + nick);
                    }
                }
            }
        }
    }
    public void readMessages() throws  IOException{
        while(true){
            String str = in.readUTF();
            System.out.println("от" + name + ": " + str);
            if(str.equals("/end")){
                return;
            }
            myServer.broadcastMsg(name + ": " + str);
        }
    }
    public void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public void closeConnection(){
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + "вышел");
        try{
            out.close();
            in.close();
            socket.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
