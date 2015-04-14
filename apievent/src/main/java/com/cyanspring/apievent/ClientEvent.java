package com.cyanspring.apievent;

import java.io.Serializable;

/**
 * Description....
 * <ul>
 * <li> Description
 * </ul>
 * <p/>
 * Description....
 * <p/>
 * Description....
 * <p/>
 * Description....
 *
 * @author elviswu
 * @version %I%, %G%
 * @since 1.0
 */
public class ClientEvent implements Serializable {
    private String key;
    private String receiver;
    public ClientEvent(String key, String receiver){
        this.key = key;
        this.receiver = receiver;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getKey() {
        return key;
    }
}
