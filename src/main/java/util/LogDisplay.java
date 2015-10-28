package util;

import java.util.List;

//import objects.Party;
//import objects.Party.PartyMember;

import org.apache.commons.collections.iterators.ReverseListIterator;

import exodus.Constants.StatusType;
import exodus.Constants.TransportContext;
import exodus.Exodus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import exodus.Party;
import exodus.Party.PartyMember;

public class LogDisplay {

    final List<String> logs = new FixedSizeArrayList<>(20);
    final BitmapFont font;

    static final int LOG_AREA_WIDTH = 256;
    static final int LOG_AREA_TOP = 384;

    static final int LOG_X = 736;
    
    public LogDisplay(BitmapFont font) {
        this.font = font;
    }

    public void append(String s) {
        synchronized (logs) {
            if (logs.isEmpty()) {
                logs.add("");
            }
            String l = logs.get(logs.size() - 1);
            l = l + s;
            logs.remove(logs.size() - 1);
            logs.add(l);
        }
    }

    public void logDeleteLastChar() {
        synchronized (logs) {
            if (logs.isEmpty()) {
                return;
            }
            String l = logs.get(logs.size() - 1);
            l = l.substring(0, l.length() - 1);
            logs.remove(logs.size() - 1);
            logs.add(l);
        }
    }

    public void add(String s) {
        synchronized (logs) {
            logs.add(s);
        }
    }

    public void render(Batch batch, Party party) {

        float y = Exodus.SCREEN_HEIGHT - 48;
        for (int i = 0; i < party.getMembers().size(); i++) {
            PartyMember pm = party.getMember(i);

            String s = (i + 1) + " - " + pm.getPlayer().name;
            String d = pm.getPlayer().health + "" + pm.getPlayer().status.getId();

            font.setColor(i == party.getActivePlayer() ? new Color(.35f, .93f, 0.91f, 1) : Color.WHITE);
            if (pm.getPlayer().status == StatusType.POISONED) {
                font.setColor(Color.GREEN);
            }
            if (pm.getPlayer().status == StatusType.COLD) {
                font.setColor(Color.CYAN);
            }
            if (pm.getPlayer().status == StatusType.DEAD) {
                font.setColor(Color.GRAY);
            }

            font.draw(batch, s, LOG_X + 8, y);
            font.draw(batch, d, LOG_X + 8 + 110, y);

            y = y - 24;

        }

        font.setColor(Color.WHITE);
        y = 32;

        synchronized (logs) {
            ReverseListIterator iter = new ReverseListIterator(logs);
            while (iter.hasNext()) {
                String next = (String) iter.next();
                GlyphLayout layout = new GlyphLayout(font, next, Color.WHITE, LOG_AREA_WIDTH - 8, Align.left, true);
                y += layout.height + 10;
                if (y > LOG_AREA_TOP) {
                    break;
                }
                font.draw(batch, layout, LOG_X + 8, y);
            }
        }
    }
}
