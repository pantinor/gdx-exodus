package util;

import java.util.List;

//import objects.Party;
//import objects.Party.PartyMember;
import org.apache.commons.collections.iterators.ReverseListIterator;

import exodus.Constants.StatusType;
import exodus.Exodus;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Align;
import exodus.Party;
import exodus.Party.PartyMember;
import objects.SaveGame.CharacterRecord;

public class LogDisplay {

    final List<String> logs = new FixedSizeArrayList<>(20);

    static final int LOG_AREA_WIDTH = 256;
    static final int LOG_AREA_TOP = 396;
    static final int LOG_X = 740;

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

        float y = Exodus.SCREEN_HEIGHT - 32 - 75;
        for (int i = 0; i < party.getMembers().size(); i++) {
            PartyMember pm = party.getMember(i);

            Exodus.smallFont.setColor(i == party.getActivePlayer() ? Color.YELLOW : Color.WHITE);
            if (pm.getPlayer().status == StatusType.POISONED) {
                Exodus.smallFont.setColor(Color.GREEN);
            }
            if (pm.getPlayer().status == StatusType.ASH) {
                Exodus.smallFont.setColor(Color.FIREBRICK);
            }
            if (pm.getPlayer().status == StatusType.DEAD) {
                Exodus.smallFont.setColor(Color.DARK_GRAY);
            }
            if (pm.getPlayer().health > 0 && pm.getPlayer().health < 3) {
                Exodus.smallFont.setColor(Color.RED);
            }
            
            batch.draw(Exodus.faceTiles[pm.getPlayer().portaitIndex], LOG_X + 3, y + 5);
            
            CharacterRecord r = pm.getPlayer();

            String d = r.name.toUpperCase() + "  " + r.sex.toString() + " " + r.race.toString() + " " + r.profession.toString();
            Exodus.smallFont.draw(batch, d, LOG_X + 64, y + 65);
            
            d = String.format("HLTH: %d %s LVL: %d EXP: %d", pm.getPlayer().health, pm.getPlayer().status.getId(),pm.getPlayer().getLevel(), pm.getPlayer().exp);
            Exodus.smallFont.draw(batch, d, LOG_X + 64, y + 45);
            
            d = String.format("GOLD: %d FOOD: %d MANA: %d/%d", pm.getPlayer().gold, pm.getPlayer().food, pm.getPlayer().mana, pm.getPlayer().getMaxMana());
            Exodus.smallFont.draw(batch, d, LOG_X + 64, y + 25);
            
            y -= 77;

        }

        Exodus.smallFont.setColor(Color.WHITE);
        y = 44;

        synchronized (logs) {
            ReverseListIterator iter = new ReverseListIterator(logs);
            while (iter.hasNext()) {
                String next = (String) iter.next();
                GlyphLayout layout = new GlyphLayout(Exodus.font, next, Color.WHITE, LOG_AREA_WIDTH - 8, Align.left, true);
                y += layout.height + 10;
                if (y > LOG_AREA_TOP) {
                    break;
                }
                Exodus.font.draw(batch, layout, LOG_X + 8, y);
            }
        }
    }
}
