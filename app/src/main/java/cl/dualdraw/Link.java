package cl.dualdraw;

import org.json.JSONObject;

/** Estados y oyentes de la conexión (los mismos nombres que en ChronOS, de donde vino DUAL Draw). */
final class Link {
    private Link() { }
    static final int WAITING = 1, CONNECTED = 2;
    interface Listener {
        void onMessage(JSONObject m);
        void onState(int st, String info);
    }
}
