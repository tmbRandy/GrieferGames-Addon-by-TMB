package tmb.randy.tmbgriefergames.v1_12_2.util.chat;

import java.util.HashMap;
import java.util.Map;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.chat.ChatMessageSendEvent;
import tmb.randy.tmbgriefergames.core.Addon;

public class TypeCorrection {

  private final Map<String, String> replacements = new HashMap<>() {
    {
      put("7r", "/r");
      put("(r", "/r");
      put("t/r", "/r");

      put("7ec", "/ec");
      put("(ec", "/ec");
      put("t/ec", "/ec");

      put("7msg", "/msg");
      put("(msg", "/msg");
      put("t/msg", "/msg");

      put("7pay", "/pay");
      put("(pay", "/pay");
      put("t/pay", "/pay");

      put("7bank", "/bank");
      put("(bank", "/bank");
      put("t/bank", "/bank");

      put("7craft", "/craft");
      put("(craft", "/craft");
      put("t/craft", "/craft");

      put("7invsee", "/invsee");
      put("(invsee", "/invsee");
      put("t/invsee", "/invsee");

      put("7booster", "/booster");
      put("(booster", "/booster");
      put("t/booster", "/booster");

      put("7cooldowns", "/cooldowns");
      put("(cooldowns", "/cooldowns");
      put("t/cooldowns", "/cooldowns");

      put("7p h ", "/p h ");
      put("(p h ", "/p h ");
      put("t/p h ", "/p h ");


        put("7tpahere", "/tpahere");
        put("(tpahere", "/tpahere");
        put("t/tpahere", "/tpahere");

      put("/p t ", "/p trust ");

      put("/cb1", "/switch cb1");
      put("/cb2", "/switch cb2");
      put("/cb3", "/switch cb3");
      put("/cb4", "/switch cb4");
      put("/cb5", "/switch cb5");
      put("/cb6", "/switch cb6");
      put("/cb7", "/switch cb7");
      put("/cb8", "/switch cb8");
      put("/cb9", "/switch cb9");
      put("/cb10", "/switch cb10");
      put("/cb11", "/switch cb11");
      put("/cb12", "/switch cb12");
      put("/cb13", "/switch cb13");
      put("/cb14", "/switch cb14");
      put("/cb15", "/switch cb15");
      put("/cb16", "/switch cb16");
      put("/cb17", "/switch cb17");
      put("/cb18", "/switch cb18");
      put("/cb19", "/switch cb19");
      put("/cb20", "/switch cb20");
      put("/cb21", "/switch cb21");
      put("/cb22", "/switch cb22");
      put("/nature", "/switch nature");
      put("/evil", "/switch cbevil");
      put("/extreme", "/switch extreme");
    }
  };

  public void messageSend(ChatMessageSendEvent event) {
    if((!Addon.getSharedInstance().configuration().getChatConfig().getTypeCorrection().get() && !Addon.getSharedInstance().configuration().getChatConfig().getMessageSplit().get()))
      return;

    String message = event.getMessage();

    // Type correction
    if(Addon.getSharedInstance().configuration().getChatConfig().getTypeCorrection().get()) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
    }

    // Message split
    if(message.length() > 100 && (message.toLowerCase().startsWith("/msg ") || message.toLowerCase().startsWith("/r ")) && Addon.getSharedInstance().configuration().getChatConfig().getMessageSplit().get()) {
      String[] messageArray;

      messageArray = message.split("(?<=\\G.{" + 97 + "})");

      for (int i = 1; i < messageArray.length; i++) {
        messageArray[i] = "/r " + messageArray[i];
      }

        for (String s : messageArray) {
            Addon.getSharedInstance().sendMessage(s);
        }

      message = "";
      event.setCancelled(true);
    }

    if(!event.isCancelled()) {
      event.changeMessage(message);
    } else {
      event.changeMessage("");
    }
  }

}
