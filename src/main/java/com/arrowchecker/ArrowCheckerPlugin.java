package com.arrowchecker;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
        name = "Arrow Checker"
)
public class ArrowCheckerPlugin extends Plugin
{
    @Inject
    private Notifier notifier;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ArrowCheckerConfig config;

    @Inject
    private ItemManager itemManager;

    @Override
    protected void startUp() throws Exception
    {
        clientThread.invokeLater(() ->
        {
            final ItemContainer container = client.getItemContainer(InventoryID.EQUIPMENT);

            if (container != null)
            {
                checkInventory(container.getItems());
            }
        });
    }

    @Override
    protected void shutDown() throws Exception
    {

    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getItemContainer() != client.getItemContainer(InventoryID.EQUIPMENT))
        {
            return;
        }

        checkInventory(event.getItemContainer().getItems());
    }

    private void checkInventory(final Item[] items)
    {
        // Check for weapon slot items. This overrides the ammo slot,
        // as the player will use the thrown weapon (eg. chinchompas, knives, darts)
        if (items.length > EquipmentInventorySlot.WEAPON.getSlotIdx())
        {
            final Item weapon = items[EquipmentInventorySlot.WEAPON.getSlotIdx()];
            final ItemComposition weaponComp = itemManager.getItemComposition(weapon.getId());

            // If item is a longbow or shortbow
            if (weaponComp.getId() >= 837 && weaponComp.getId() <= 861)
            {
                final Item ammo = items[EquipmentInventorySlot.AMMO.getSlotIdx()];
                final ItemComposition ammoComp = itemManager.getItemComposition(ammo.getId());

                // If there are no arrows equipped
                if (ammo.getId() == -1)
                {
                    notifier.notify("You have no arrows");
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You have no arrows", null);
                    return;
                }
                int[][] ranks = {
                        {839, 841, 882, 883, 884, 885},
                        {843, 845, 886, 887},
                        {847, 849, 888, 889},
                        {851, 853, 890, 891},
                        {855, 857, 859, 861, 892, 893}
                };

                int bowRank = -1;
                int ammoRank = -1;
                int foundRank = 0;

                for (int i = 0; i < ranks.length && foundRank < 2; i++)
                {
                    for (int id : ranks[i])
                    {
                        if (foundRank == 2)
                        {
                            break;
                        }

                        if (weaponComp.getId() == id)
                        {
                            bowRank = i;
                            foundRank++;
                        }

                        if (ammoComp.getId() == id)
                        {
                            ammoRank = i;
                            foundRank++;
                        }
                    }
                }

                if (bowRank != ammoRank)
                {
                    notifier.notify("Your arrows are incompatible with your bow");
                    client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Your arrows are incompatible with your bow", null);
                }
            }
        }
    }

    @Provides
    ArrowCheckerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(ArrowCheckerConfig.class);
    }
}
