
package Service;


import Dados.MsgChat;
import Dados.MsgChat.Action;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.awt.windows.ThemeReader;

/**
 * @author Wdenardi
 */
public class ServidorService {

    private ServerSocket serverSocket;
    private Socket socket;
    
    // Toda nova conexao(Usuarios) eh adicionado na lista.
    
    private Map<String, ObjectOutputStream> mapOnlines = new HashMap<String, ObjectOutputStream>();
 
           //Metodo de validacao servidor
    public ServidorService() {
        try {
            
            //porta local que aguarda uma nova conexao
            
            serverSocket = new ServerSocket(5555);
            serverSocket.bind(new InetSocketAddress("192.168.0.1", 0));


            System.out.println("Servidor on!");

            
            // Mantem a porta aberta, aguardando conexao enquanto o servidor estiver online.
            
            while (true) {
                socket = serverSocket.accept();

                new Thread(new ListenerSocket(socket)).start();
            }

        } catch (IOException ex) {
            Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    // Socket padrao java (Logica nao inclusa)
    private class ListenerSocket implements Runnable {

        private ObjectOutputStream output;
        private ObjectInputStream input;

        public ListenerSocket(Socket socket) {
            try {
                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input = new ObjectInputStream (socket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            MsgChat message = null;
            try {
                
                // While recebera todas as msg dos clientes, enquanto existir msg
                // E exibiremos essas msg em uma tela
                while ((message = (MsgChat) input.readObject()) != null) {
                    Action action = message.getAction();

                    
                    // solicitacao de conexao
                    if (action.equals(Action.Conecta)) {
                        boolean isConnect = connecta_servidor(message, output);
                        if (isConnect) {
                            mapOnlines.put(message.getName(), output);
                            verificaUsuariosOnline();
                        }
                        
                        //solicita desconexão 
                    } else if (action.equals(Action.Desconecta)) {
                        desconecta_usuario(message, output);
                        verificaUsuariosOnline();
                        return;
                        
                        // Envia msg
                    } else if (action.equals(Action.Manda_msg)) {
                        envia_msg(message);
                        
                        //Envia msg para todos
                    } else if (action.equals(Action.Manda_msg_todos)) {
                        envia_msg_tt(message);
                    }
                    else if(action.equals(Action.Clientes_online)){
                       
                    }
                }
            } catch (IOException ex) {
                MsgChat cm = new MsgChat();
                cm.setName(message.getName());
                try {
                    desconecta_usuario(cm, output);
                } catch (IOException ex1) {
                    Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex1);
                }
                try {
                    verificaUsuariosOnline();
                } catch (IOException ex1) {
                    Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex1);
                }
                System.out.println(message.getName() + " deixou o chat!");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private boolean connecta_servidor(MsgChat message, ObjectOutputStream output) {
        if (mapOnlines.size() == 0) {
            message.setText("Conectado");
            msg(message, output);
            return true;
        }

        if (mapOnlines.containsKey(message.getName())) {
            message.setText("Desconectado");
            msg(message, output);
            return false;
        } else {
            message.setText("Conectado");
            msg(message, output);
            return true;
        }
    }

    private void desconecta_usuario(MsgChat message, ObjectOutputStream output) throws IOException {
        mapOnlines.remove(message.getName());

        message.setText(" Adeus!");

        message.setAction(Action.Manda_msg);

        envia_msg_tt(message);

        System.out.println("Usuario " + message.getName()+ " saiu ");
    }
    
    private void msg(MsgChat message, ObjectOutputStream output) {
        try {
            output.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void envia_msg(MsgChat message) throws IOException {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            if (kv.getKey().equals(message.getNameReserved())) {
                kv.getValue().writeObject(message);
            }
        }
    }

    private void envia_msg_tt(MsgChat message) throws IOException {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            if (!kv.getKey().equals(message.getName())) {
                message.setAction(Action.Manda_msg);
                kv.getValue().writeObject(message);
            }
        }
    }
    
    private void verificaUsuariosOnline() throws IOException {
        Set<String> setNames = new HashSet<String>();
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            setNames.add(kv.getKey());
        }

        MsgChat message = new MsgChat();
        message.setAction(Action.Clientes_online);
        message.setSetOnlines(setNames);

        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            message.setName(kv.getKey());
            kv.getValue().writeObject(message);
        }
    }
}
