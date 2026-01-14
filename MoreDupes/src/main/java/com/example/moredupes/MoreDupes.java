// MoreDupes.java - Place in src/main/java/com/mistasoup/moredupes/
package com.mistasoup.moredupes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ShulkerBox;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class MoreDupes extends JavaPlugin implements Listener {
    
    private final Map<Location, Long> pistonDupeLastTime = new HashMap<>();
    private final Map<UUID, Long> cactusDupeLastTime = new HashMap<>();
    private final Map<UUID, Long> portalDupeLastTime = new HashMap<>();
    private final Set<UUID> playerDroppedItems = new HashSet<>();
    private final Set<UUID> dupedItems = new HashSet<>();
    private final Map<UUID, Long> frameInteractions = new HashMap<>();
    private final Random random = new Random();
    
    private boolean pistonDupeEnabled;
    private boolean pistonUseAdjacentBlockProtection;
    private long pistonCooldownTicks;
    private boolean pistonVerboseLogging;
    private boolean pistonRequirePlayerNearby;
    private double pistonPlayerDistance;
    private double pistonDupeProbability;
    
    private boolean cactusDupeEnabled;
    private long cactusCooldownTicks;
    private boolean cactusVerboseLogging;
    private double cactusDupeProbability;
    
    private boolean frameDupeNormalEnabled;
    private boolean frameDupeGlowEnabled;
    private boolean frameDupeVerboseLogging;
    
    private boolean portalDupeEnabled;
    private long portalCooldownTicks;
    private boolean portalVerboseLogging;
    private double portalDupeProbability;
    
    private boolean minecartDupeEnabled;
    private double minecartDupeProbability;
    private boolean minecartVerboseLogging;
    
    private boolean dropperDupeEnabled;
    private boolean dispenserDupeEnabled;
    private double dropperDupeProbability;
    private double dispenserDupeProbability;
    private boolean dropperDispenserRequirePlayerNearby;
    private double dropperDispenserPlayerDistance;
    private boolean dropperDispenserVerboseLogging;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("MoreDupes enabled!");
        getLogger().info("Piston dupe: " + pistonDupeEnabled);
        getLogger().info("Cactus dupe: " + cactusDupeEnabled);
        getLogger().info("Frame dupe (normal): " + frameDupeNormalEnabled);
        getLogger().info("Frame dupe (glow): " + frameDupeGlowEnabled);
        getLogger().info("Portal dupe: " + portalDupeEnabled);
        getLogger().info("Minecart dupe: " + minecartDupeEnabled);
        getLogger().info("Dropper dupe: " + dropperDupeEnabled);
        getLogger().info("Dispenser dupe: " + dispenserDupeEnabled);
    }
    
    @Override
    public void onDisable() {
        pistonDupeLastTime.clear();
        cactusDupeLastTime.clear();
        portalDupeLastTime.clear();
        playerDroppedItems.clear();
        dupedItems.clear();
        frameInteractions.clear();
        getLogger().info("MoreDupes disabled!");
    }
    
    private void loadConfig() {
        FileConfiguration config = getConfig();
        
        pistonDupeEnabled = config.getBoolean("piston-dupe.enabled", true);
        pistonUseAdjacentBlockProtection = config.getBoolean("piston-dupe.adjacent-block-protection", false);
        double pistonCooldownSeconds = config.getDouble("piston-dupe.cooldown-seconds", 0.0);
        pistonCooldownTicks = (long) (pistonCooldownSeconds * 20);
        pistonVerboseLogging = config.getBoolean("piston-dupe.verbose-logging", true);
        pistonRequirePlayerNearby = config.getBoolean("piston-dupe.require-player-nearby", false);
        pistonPlayerDistance = config.getDouble("piston-dupe.player-distance", 4.0);
        pistonDupeProbability = config.getDouble("piston-dupe.dupe-probability", 100.0) / 100.0;
        
        cactusDupeEnabled = config.getBoolean("cactus-dupe.enabled", true);
        double cactusCooldownSeconds = config.getDouble("cactus-dupe.cooldown-seconds", 0.0);
        cactusCooldownTicks = (long) (cactusCooldownSeconds * 20);
        cactusVerboseLogging = config.getBoolean("cactus-dupe.verbose-logging", true);
        cactusDupeProbability = config.getDouble("cactus-dupe.dupe-probability", 100.0) / 100.0;
        
        frameDupeNormalEnabled = config.getBoolean("frame-dupe.normal-frames.enabled", true);
        frameDupeGlowEnabled = config.getBoolean("frame-dupe.glow-frames.enabled", true);
        frameDupeVerboseLogging = config.getBoolean("frame-dupe.verbose-logging", true);
        
        portalDupeEnabled = config.getBoolean("portal-dupe.enabled", true);
        double portalCooldownSeconds = config.getDouble("portal-dupe.cooldown-seconds", 0.0);
        portalCooldownTicks = (long) (portalCooldownSeconds * 20);
        portalVerboseLogging = config.getBoolean("portal-dupe.verbose-logging", true);
        portalDupeProbability = config.getDouble("portal-dupe.dupe-probability", 100.0) / 100.0;
        
        minecartDupeEnabled = config.getBoolean("minecart-dupe.enabled", true);
        minecartDupeProbability = config.getDouble("minecart-dupe.dupe-probability", 100.0) / 100.0;
        minecartVerboseLogging = config.getBoolean("minecart-dupe.verbose-logging", true);
        
        dropperDupeEnabled = config.getBoolean("dropper-dispenser-dupe.dropper.enabled", true);
        dispenserDupeEnabled = config.getBoolean("dropper-dispenser-dupe.dispenser.enabled", true);
        dropperDupeProbability = config.getDouble("dropper-dispenser-dupe.dropper.dupe-probability", 100.0) / 100.0;
        dispenserDupeProbability = config.getDouble("dropper-dispenser-dupe.dispenser.dupe-probability", 100.0) / 100.0;
        dropperDispenserRequirePlayerNearby = config.getBoolean("dropper-dispenser-dupe.require-player-nearby", false);
        dropperDispenserPlayerDistance = config.getDouble("dropper-dispenser-dupe.player-distance", 4.0);
        dropperDispenserVerboseLogging = config.getBoolean("dropper-dispenser-dupe.verbose-logging", true);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("moredupesreload")) {
            if (!sender.hasPermission("moredupes.reload") && !sender.isOp()) {
                sender.sendMessage("§cYou don't have permission to use this command!");
                return true;
            }
            
            pistonDupeLastTime.clear();
            cactusDupeLastTime.clear();
            portalDupeLastTime.clear();
            playerDroppedItems.clear();
            dupedItems.clear();
            frameInteractions.clear();
            
            reloadConfig();
            loadConfig();
            
            sender.sendMessage("§aMoreDupes config reloaded!");
            sender.sendMessage("§7Piston dupe: " + pistonDupeEnabled);
            sender.sendMessage("§7Cactus dupe: " + cactusDupeEnabled);
            sender.sendMessage("§7Frame dupe (normal): " + frameDupeNormalEnabled);
            sender.sendMessage("§7Frame dupe (glow): " + frameDupeGlowEnabled);
            sender.sendMessage("§7Portal dupe: " + portalDupeEnabled);
            sender.sendMessage("§7Minecart dupe: " + minecartDupeEnabled);
            sender.sendMessage("§7Dropper dupe: " + dropperDupeEnabled);
            sender.sendMessage("§7Dispenser dupe: " + dispenserDupeEnabled);
            
            getLogger().info("Config reloaded by " + sender.getName());
            return true;
        }
        return false;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!pistonDupeEnabled) return;
        
        Block piston = event.getBlock();
        if (piston.getType() != Material.PISTON) return;
        
        BlockFace direction = event.getDirection();
        
        for (Block pushedBlock : event.getBlocks()) {
            if (isShulkerBox(pushedBlock.getType())) {
                boolean shouldProtect = false;
                
                if (pistonUseAdjacentBlockProtection) {
                    Block blockOpposite = pushedBlock.getRelative(direction);
                    
                    if (blockOpposite.getType().isSolid() && blockOpposite.getType() != Material.AIR) {
                        shouldProtect = true;
                        
                        if (pistonRequirePlayerNearby) {
                            boolean playerNearby = false;
                            Location shulkerLoc = pushedBlock.getLocation().add(0.5, 0.5, 0.5);
                            
                            for (Player player : pushedBlock.getWorld().getPlayers()) {
                                double distance = player.getLocation().distance(shulkerLoc);
                                if (distance <= pistonPlayerDistance) {
                                    playerNearby = true;
                                    if (pistonVerboseLogging) {
                                        getLogger().info("Player " + player.getName() + " is " + String.format("%.2f", distance) + " blocks away");
                                    }
                                    break;
                                }
                            }
                            
                            if (!playerNearby) {
                                shouldProtect = false;
                                if (pistonVerboseLogging) {
                                    getLogger().info("No player within " + pistonPlayerDistance + " blocks - shulker will break");
                                }
                            }
                        }
                        
                        if (pistonVerboseLogging && shouldProtect) {
                            getLogger().info("Solid block detected and conditions met - protecting shulker");
                        }
                    }
                }
                
                if (shouldProtect) {
                    Location shulkerLoc = pushedBlock.getLocation();
                    long currentTime = piston.getWorld().getGameTime();
                    
                    if (pistonDupeLastTime.containsKey(shulkerLoc)) {
                        long timeSinceLastDupe = currentTime - pistonDupeLastTime.get(shulkerLoc);
                        
                        if (timeSinceLastDupe < pistonCooldownTicks) {
                            event.setCancelled(true);
                            if (pistonVerboseLogging) {
                                getLogger().info("Shulker on cooldown - blocking dupe");
                            }
                            break;
                        }
                    }
                    
                    pistonDupeLastTime.put(shulkerLoc, currentTime);
                    event.setCancelled(true);
                } else {
                    if (pistonVerboseLogging) {
                        getLogger().info("No protection - allowing shulker to break");
                    }
                }
                
                if (random.nextDouble() > pistonDupeProbability) {
                    if (pistonVerboseLogging) {
                        getLogger().info("Dupe probability check failed - no dupe");
                    }
                    break;
                }
                
                ShulkerBox shulkerBox = (ShulkerBox) pushedBlock.getState();
                ItemStack shulkerItem = new ItemStack(pushedBlock.getType());
                BlockStateMeta meta = (BlockStateMeta) shulkerItem.getItemMeta();
                
                if (meta != null) {
                    ShulkerBox shulkerState = (ShulkerBox) meta.getBlockState();
                    
                    for (int i = 0; i < shulkerBox.getInventory().getSize(); i++) {
                        ItemStack item = shulkerBox.getInventory().getItem(i);
                        if (item != null) {
                            shulkerState.getInventory().setItem(i, item.clone());
                        }
                    }
                    
                    meta.setBlockState(shulkerState);
                    shulkerItem.setItemMeta(meta);
                }
                
                Location dropLocation = pushedBlock.getLocation().add(0.5, 0.5, 0.5);
                pushedBlock.getWorld().dropItemNaturally(dropLocation, shulkerItem);
                
                if (pistonVerboseLogging) {
                    getLogger().info("Duped shulker box at " + pushedBlock.getLocation());
                }
                
                break;
            }
        }
    }
    
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item droppedItem = event.getItemDrop();
        UUID itemId = droppedItem.getUniqueId();
        
        playerDroppedItems.add(itemId);
        
        // Use Folia's entity scheduler instead of global scheduler
        droppedItem.getScheduler().runDelayed(this, (task) -> {
            playerDroppedItems.remove(itemId);
        }, null, 200L);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCactusDamage(EntityDamageByBlockEvent event) {
        if (!cactusDupeEnabled) return;
        
        if (event.getDamager() == null || event.getDamager().getType() != Material.CACTUS) return;
        if (!(event.getEntity() instanceof Item)) return;
        
        Item item = (Item) event.getEntity();
        ItemStack itemStack = item.getItemStack();
        
        if (!isShulkerBox(itemStack.getType())) return;
        
        event.setCancelled(true);
        
        UUID itemId = item.getUniqueId();
        
        if (!playerDroppedItems.contains(itemId)) {
            if (cactusVerboseLogging) {
                getLogger().info("Shulker not player-dropped - no dupe (protected from breaking)");
            }
            return;
        }
        
        if (dupedItems.contains(itemId)) {
            if (cactusVerboseLogging) {
                getLogger().info("Shulker already duped - no more dupes (protected from breaking)");
            }
            return;
        }
        
        long currentTime = item.getWorld().getGameTime();
        
        if (cactusDupeLastTime.containsKey(itemId)) {
            long timeSinceLastDupe = currentTime - cactusDupeLastTime.get(itemId);
            
            if (timeSinceLastDupe < cactusCooldownTicks) {
                if (cactusVerboseLogging) {
                    getLogger().info("Shulker on cooldown - no dupe (protected from breaking)");
                }
                return;
            }
        }
        
        if (random.nextDouble() > cactusDupeProbability) {
            if (cactusVerboseLogging) {
                getLogger().info("Dupe probability check failed - no dupe");
            }
            dupedItems.add(itemId);
            playerDroppedItems.remove(itemId);
            return;
        }
        
        cactusDupeLastTime.put(itemId, currentTime);
        dupedItems.add(itemId);
        playerDroppedItems.remove(itemId);
        
        ItemStack duplicate = itemStack.clone();
        Item dupedItem = item.getWorld().dropItemNaturally(item.getLocation(), duplicate);
        
        dupedItems.add(dupedItem.getUniqueId());
        
        if (cactusVerboseLogging) {
            getLogger().info("Duped shulker via cactus at " + item.getLocation());
        }
    }
    
    // Track when players interact with frames
    @EventHandler(priority = EventPriority.MONITOR)
    public void onFrameInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) return;
        
        ItemFrame frame = (ItemFrame) event.getRightClicked();
        UUID frameId = frame.getUniqueId();
        
        // Record this interaction
        frameInteractions.put(frameId, System.currentTimeMillis());
        
        if (frameDupeVerboseLogging) {
            getLogger().info("Player " + event.getPlayer().getName() + " interacted with frame " + frameId);
        }
        
        // Check for dupe after a tiny delay to let the item be removed
        frame.getScheduler().runDelayed(this, (task) -> {
            handleFrameDupe(frame, event.getPlayer());
        }, null, 1L);
    }   
 
    private void handleFrameDupe(ItemFrame frame, Player player) {
        // Check if frame still exists
        if (!frame.isValid() || frame.isDead()) {
            if (frameDupeVerboseLogging) {
                getLogger().info("Frame no longer valid");
            }
            return;
        }
        
        ItemStack item = frame.getItem();
        
        if (frameDupeVerboseLogging) {
            getLogger().info("Checking frame - Item in frame: " + (item != null && item.getType() != Material.AIR ? item.getType() : "EMPTY"));
        }
        
        // If frame is now empty, player removed the item
        if (item == null || item.getType() == Material.AIR) {
            UUID frameId = frame.getUniqueId();
            
            // Check if this was a recent interaction
            if (!frameInteractions.containsKey(frameId)) {
                if (frameDupeVerboseLogging) {
                    getLogger().info("No recent interaction found for this frame");
                }
                return;
            }
            
            long timeSinceInteraction = System.currentTimeMillis() - frameInteractions.get(frameId);
            
            // Only dupe if interaction was very recent (within 1 second)
            if (timeSinceInteraction > 1000) {
                frameInteractions.remove(frameId);
                if (frameDupeVerboseLogging) {
                    getLogger().info("Interaction too old: " + timeSinceInteraction + "ms");
                }
                return;
            }
            
            frameInteractions.remove(frameId);
            
            boolean isGlowFrame = frame.getType() == EntityType.GLOW_ITEM_FRAME;
            
            if (isGlowFrame && !frameDupeGlowEnabled) {
                if (frameDupeVerboseLogging) {
                    getLogger().info("Glow frame dupe disabled");
                }
                return;
            }
            if (!isGlowFrame && !frameDupeNormalEnabled) {
                if (frameDupeVerboseLogging) {
                    getLogger().info("Normal frame dupe disabled");
                }
                return;
            }
            
            // Try to find the item that was just dropped near the frame
            Location frameLoc = frame.getLocation();
            
            if (frameDupeVerboseLogging) {
                getLogger().info("Searching for dropped items near frame at " + frameLoc);
            }
            
            int itemsFound = 0;
            for (Entity entity : frame.getWorld().getNearbyEntities(frameLoc, 3, 3, 3)) {
                if (!(entity instanceof Item)) continue;
                
                itemsFound++;
                Item droppedItem = (Item) entity;
                ItemStack droppedStack = droppedItem.getItemStack();
                
                if (frameDupeVerboseLogging) {
                    getLogger().info("Found item: " + droppedStack.getType() + " (ticks lived: " + droppedItem.getTicksLived() + ")");
                }
                
                // Check if this item was dropped very recently (within last 10 ticks)
                if (droppedItem.getTicksLived() > 10) {
                    if (frameDupeVerboseLogging) {
                        getLogger().info("Item too old, skipping");
                    }
                    continue;
                }
                
                // Always dupe - no probability check
                
                // Create duplicate
                ItemStack duplicate = droppedStack.clone();
                frame.getWorld().dropItemNaturally(frameLoc, duplicate);
                
                if (frameDupeVerboseLogging) {
                    String frameType = isGlowFrame ? "glow" : "normal";
                    getLogger().info("SUCCESS! Duped item from " + frameType + " frame: " + droppedStack.getType() + " at " + frameLoc);
                }
                
                return;
            }
            
            if (frameDupeVerboseLogging) {
                getLogger().info("Found " + itemsFound + " items total, but none were recent enough");
            }
        } else {
            if (frameDupeVerboseLogging) {
                getLogger().info("Frame still has item, not checking for dupe");
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalEnter(EntityPortalEnterEvent event) {
        if (!portalDupeEnabled) return;
        if (!(event.getEntity() instanceof Item)) return;
        
        Item item = (Item) event.getEntity();
        ItemStack itemStack = item.getItemStack();
        
        // NOW WORKS WITH ANY ITEM!
        UUID itemId = item.getUniqueId();
        
        if (!playerDroppedItems.contains(itemId)) {
            if (portalVerboseLogging) {
                getLogger().info("Item not player-dropped - no dupe");
            }
            return;
        }
        
        if (dupedItems.contains(itemId)) {
            if (portalVerboseLogging) {
                getLogger().info("Item already duped - no more dupes");
            }
            return;
        }
        
        long currentTime = item.getWorld().getGameTime();
        
        if (portalDupeLastTime.containsKey(itemId)) {
            long timeSinceLastDupe = currentTime - portalDupeLastTime.get(itemId);
            
            if (timeSinceLastDupe < portalCooldownTicks) {
                if (portalVerboseLogging) {
                    getLogger().info("Item on cooldown - no dupe");
                }
                return;
            }
        }
        
        if (random.nextDouble() > portalDupeProbability) {
            if (portalVerboseLogging) {
                getLogger().info("Dupe probability check failed - no dupe");
            }
            dupedItems.add(itemId);
            playerDroppedItems.remove(itemId);
            return;
        }
        
        portalDupeLastTime.put(itemId, currentTime);
        dupedItems.add(itemId);
        playerDroppedItems.remove(itemId);
        
        ItemStack duplicate = itemStack.clone();
        Item dupedItem = item.getWorld().dropItemNaturally(item.getLocation(), duplicate);
        
        dupedItems.add(dupedItem.getUniqueId());
        
        if (portalVerboseLogging) {
            getLogger().info("Duped item via portal: " + itemStack.getType() + " at " + item.getLocation());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMinecartDestroy(VehicleDestroyEvent event) {
        if (!minecartDupeEnabled) return;
        
        // Check if it's a storage minecart (chest/hopper minecart)
        if (!(event.getVehicle() instanceof StorageMinecart)) return;
        
        StorageMinecart minecart = (StorageMinecart) event.getVehicle();
        
        if (minecartVerboseLogging) {
            getLogger().info("Storage minecart destroyed at " + minecart.getLocation());
        }
        
        // Check probability
        if (random.nextDouble() >= minecartDupeProbability) {
            if (minecartVerboseLogging) {
                getLogger().info("Minecart dupe probability check failed - no dupe");
            }
            return;
        }
        
        // Duplicate all items in the minecart
        Location dropLoc = minecart.getLocation();
        int itemsDropped = 0;
        
        for (ItemStack item : minecart.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                ItemStack duplicate = item.clone();
                minecart.getWorld().dropItemNaturally(dropLoc, duplicate);
                itemsDropped++;
            }
        }
        
        if (minecartVerboseLogging) {
            getLogger().info("Duplicated " + itemsDropped + " item stacks from minecart");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        Material blockType = block.getType();
        
        // Check if it's a dropper or dispenser
        boolean isDropper = blockType == Material.DROPPER;
        boolean isDispenser = blockType == Material.DISPENSER;
        
        if (!isDropper && !isDispenser) return;
        
        // Check if this type is enabled
        if (isDropper && !dropperDupeEnabled) return;
        if (isDispenser && !dispenserDupeEnabled) return;
        
        // Check if player nearby is required
        if (dropperDispenserRequirePlayerNearby) {
            boolean playerNearby = false;
            Location blockLoc = block.getLocation().add(0.5, 0.5, 0.5);
            
            for (Player player : block.getWorld().getPlayers()) {
                double distance = player.getLocation().distance(blockLoc);
                if (distance <= dropperDispenserPlayerDistance) {
                    playerNearby = true;
                    if (dropperDispenserVerboseLogging) {
                        getLogger().info("Player " + player.getName() + " is " + String.format("%.2f", distance) + " blocks away from " + blockType);
                    }
                    break;
                }
            }
            
            if (!playerNearby) {
                if (dropperDispenserVerboseLogging) {
                    getLogger().info("No player within " + dropperDispenserPlayerDistance + " blocks - no dupe");
                }
                return;
            }
        }
        
        // Check probability
        double probability = isDropper ? dropperDupeProbability : dispenserDupeProbability;
        
        if (random.nextDouble() >= probability) {
            if (dropperDispenserVerboseLogging) {
                getLogger().info(blockType + " dupe probability check failed - no dupe");
            }
            return;
        }
        
        // Duplicate the dispensed item
        ItemStack item = event.getItem();
        if (item != null && item.getType() != Material.AIR) {
            ItemStack duplicate = item.clone();
            
            // Get the facing direction of the dropper/dispenser
            org.bukkit.block.data.Directional directional = (org.bukkit.block.data.Directional) block.getBlockData();
            BlockFace facing = directional.getFacing();
            
            // Calculate the drop location at the front of the block
            Location dropLoc = block.getLocation().add(0.5, 0.5, 0.5);
            dropLoc.add(facing.getDirection().multiply(0.7)); // Slightly in front
            
            block.getWorld().dropItemNaturally(dropLoc, duplicate);
            
            if (dropperDispenserVerboseLogging) {
                getLogger().info("Duplicated " + item.getType() + " from " + blockType + " at " + block.getLocation() + " facing " + facing);
            }
        }
    }
    
    private boolean isShulkerBox(Material material) {
        return material == Material.SHULKER_BOX ||
               material == Material.WHITE_SHULKER_BOX ||
               material == Material.ORANGE_SHULKER_BOX ||
               material == Material.MAGENTA_SHULKER_BOX ||
               material == Material.LIGHT_BLUE_SHULKER_BOX ||
               material == Material.YELLOW_SHULKER_BOX ||
               material == Material.LIME_SHULKER_BOX ||
               material == Material.PINK_SHULKER_BOX ||
               material == Material.GRAY_SHULKER_BOX ||
               material == Material.LIGHT_GRAY_SHULKER_BOX ||
               material == Material.CYAN_SHULKER_BOX ||
               material == Material.PURPLE_SHULKER_BOX ||
               material == Material.BLUE_SHULKER_BOX ||
               material == Material.BROWN_SHULKER_BOX ||
               material == Material.GREEN_SHULKER_BOX ||
               material == Material.RED_SHULKER_BOX ||
               material == Material.BLACK_SHULKER_BOX;
    }
}