/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Dados;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author WIlliam Denardi
 */
public class MsgChat implements Serializable {
    
    private String name;
    private String text;
    private String nameReserved;
    private Set<String> setOnlines = new HashSet<String>();
    private Action action;

    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNameReserved() {
        return nameReserved;
    }

    public void setNameReserved(String nameReserved) {
        this.nameReserved = nameReserved;
    }

    public Set<String> getSetOnlines() {
        return setOnlines;
    }

    public void setSetOnlines(Set<String> setOnlines) {
        this.setOnlines = setOnlines;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

  
    public enum Action {
        Conecta, Desconecta, Manda_msg, Manda_msg_todos, Clientes_online
    }
}
