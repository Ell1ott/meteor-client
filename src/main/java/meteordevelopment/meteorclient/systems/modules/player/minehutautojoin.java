/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.player;


import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

import net.minecraft.client.network.ServerInfo;


public class minehutautojoin extends Module {
    String ipText;


    public minehutautojoin() {
        super(Categories.Player, "minehut auto join", "automaticly starts your minehut server up when you are trying to join");
        // MeteorClient.EVENT_BUS.subscribe(new StaticListener());
    }

    @Override
    public void onActivate() {
        swap();
        
    }

    @Override
    public void onDeactivate() {
         swap();
    }

    public void swap() {
        
        ServerInfo server = mc.getCurrentServerEntry();

        if (server == null) {
            // info("Couldn't obtain any server information.");
            return;
        }

        //String ipv4 = "";
        // try {
        //     ipv4 = InetAddress.getByName(server.address).getHostAddress();
        // } catch (UnknownHostException ignored) {}
        // if (ipv4.isEmpty()) {
        // }
        // else{
        //     ipText = ipv4;
        // }
        
        ipText = server.address;
        // mc.player.sendChatMessage(ipText);

        if (ipText.contains("minehut")){
            String url = ipText;
            String str = url.substring(0 , url.indexOf("minehut") - 1);
            
            mc.player.sendChatMessage("/join " + str);
        }
        
        
    }


    }
