package cl.dualdraw;

import android.content.Context;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Puente entre la pantalla y la conexión propia (LinkService + Net). Mantiene la forma de hablar que DUAL Draw
 * tenía dentro de ChronOS: hub.send(tipo, clave, valor…), hub.connected(), oyentes Link.Listener.
 */
final class Hub implements Net.Listener {

    private static Hub inst;
    private final List<Link.Listener> ls = new CopyOnWriteArrayList<>();
    private boolean on;

    static synchronized Hub get(Context c) {
        if (inst == null) { inst = new Hub(); LinkService.attach(inst); }
        return inst;
    }

    void add(Link.Listener l) { if (!ls.contains(l)) ls.add(l); LinkService.attach(this); }
    void remove(Link.Listener l) { ls.remove(l); }

    boolean connected() { return LinkService.isConnected(); }

    void send(JSONObject m) { LinkService.send(m); }

    void send(String t, Object... kv) {
        try {
            JSONObject m = new JSONObject().put("t", t);
            for (int i = 0; i + 1 < kv.length; i += 2) m.put(String.valueOf(kv[i]), kv[i + 1]);
            send(m);
        } catch (Exception ignored) { }
    }

    @Override public void onMessage(JSONObject m) { for (Link.Listener l : ls) l.onMessage(m); }

    @Override public void onState(boolean connected, String info) {
        if (connected == on && !connected) return;
        on = connected;
        for (Link.Listener l : ls) l.onState(connected ? Link.CONNECTED : Link.WAITING, info);
    }
}
