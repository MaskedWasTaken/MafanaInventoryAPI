package me.TahaCheji.discordCommand;


import me.TahaCheji.Inv;
import me.TahaCheji.InventoryDataHandler;
import me.TahaCheji.objects.DatabaseInventoryData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class InventoryCommand extends ListenerAdapter implements Listener {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        if (e.getAuthor().isBot()) {
            return;
        }
        if (e.getMessage().getContentRaw().contains("!Inv")) {
            String[] args = e.getMessage().getContentRaw().split(" ");
            if (args.length == 1) {
                e.getChannel().sendMessage("Error: !Inventory [Player Name]").queue();
                return;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if (!player.hasPlayedBefore()) {
                e.getChannel().sendMessage("Error: That player does not exist").queue();
                return;
            }
            DatabaseInventoryData data = Inv.getInstance().getInvMysqlInterface().getData(player);
            try {
                List<String> items = new ArrayList<>();
                for (ItemStack itemStack : new InventoryDataHandler(Inv.getInstance()).decodeItems(data.getRawInventory())) {
                    if (itemStack == null) {
                        continue;
                    }
                   items.add(itemStack.getItemMeta().getDisplayName());
                    sendEmbed(e.getChannel(), player.getName() + " Inventory", items.toString(), "This is all logged in a data base");
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if (e.getMessage().getContentRaw().contains("!ban")) {
            String[] args = e.getMessage().getContentRaw().split(" ");
            if (args.length == 1 || args.length == 2 || args.length == 3) {
                e.getChannel().sendMessage("Error: !Ban [Amount of hours] [Player Name] [Reason]").queue();
                return;
            }
            Player player = Bukkit.getPlayer(args[1]);
            int hours = 60 * Integer.getInteger(args[2]);
            Date date = new Date(System.currentTimeMillis()+ 60L * hours * 1000);
            if (player == null) {
                e.getChannel().sendMessage("Error: That player does not exist").queue();
                return;
            }
            e.getChannel().sendMessage("You have banned " + player.getName()).queue();
            player.banPlayer(args[3], date, "From Discord", true);
        }
        if (e.getMessage().getContentRaw().contains("!unBan")) {
            String[] args = e.getMessage().getContentRaw().split(" ");
            if (args.length == 1) {
                e.getChannel().sendMessage("Error: !unBan [Player Name]").queue();
                return;
            }
            OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
            if(!player.isBanned()) {
                e.getChannel().sendMessage("Error: That player is not banned").queue();
                return;
            }
            Bukkit.getBannedPlayers().remove(player);
            e.getChannel().sendMessage("You have unBaned " + player.getName()).queue();

        }
        if(e.getMessage().getContentRaw().contains("!kick")) {
            String[] args = e.getMessage().getContentRaw().split(" ");
            if (args.length == 1 || args.length == 2) {
                e.getChannel().sendMessage("Error: !kick [Player Name] [Reason]").queue();
                return;
            }
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) {
                e.getChannel().sendMessage("Error: That player does not exist").queue();
                return;
            }
            e.getChannel().sendMessage("You have kicked " + player.getName()).queue();
            player.kickPlayer(args[2]);

        }
    }

    @EventHandler
    public void chatEvent(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        TextChannel textChannel = Inv.getInstance().builder.getTextChannelsByName("server-chat", true).get(0);
        textChannel.sendMessage("**" + e.getPlayer().getName() + ":** " + message).queue();
    }

    @EventHandler
    public void joinEvent(PlayerJoinEvent e) {
        TextChannel textChannel = Inv.getInstance().builder.getTextChannelsByName("server-chat", true).get(0);
        textChannel.sendMessage("**" + e.getPlayer().getName() + ":** " + "has joined the server").queue();
    }

    @EventHandler
    public void leaveEvent(PlayerQuitEvent e) {
        TextChannel textChannel = Inv.getInstance().builder.getTextChannelsByName("server-chat", true).get(0);
        textChannel.sendMessage("**" + e.getPlayer().getName() + ":** " + "has left the server").queue();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage()) return;
        String message = event.getMessage().getContentRaw();
        User user = event.getAuthor();
        Bukkit.broadcastMessage(ChatColor.GOLD + user.getName() + "#" + user.getDiscriminator() + ": §e" + message);
    }

    public void sendEmbed(MessageChannel channel, String title, String description, String footer) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(title);
        embed.setDescription(description);
        embed.setFooter(footer);
        channel.sendMessage(embed.build()).queue();
    }

}
