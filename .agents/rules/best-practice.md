---
trigger: always_on
---

# Best Practices

## Code Organization

### 2. Class Organization

Keep classes focused and single-purpose:

```java
// Good - Single responsibility
public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create("mymod", Registries.ITEM);
    
    public static final RegistrySupplier<Item> MY_ITEM = ITEMS.register("my_item",
        () -> new Item(new Item.Properties()));
    
    public static void init() {
        ITEMS.register();
    }
}

// Bad - Multiple responsibilities
public class MyModStuff {
    public static final DeferredRegister<Item> ITEMS = ...;
    public static final DeferredRegister<Block> BLOCKS = ...;
    
    // Methods for items, blocks, events, networking, etc.
}
```

### 3. Constants Management

Use a dedicated class for constants:

```java
public final class ModConstants {
    private ModConstants() {} // Prevent instantiation
    
    public static final String MOD_ID = "mymod";
    public static final String MOD_NAME = "My Mod";
    
    // GUI constants
    public static final int GUI_WIDTH = 176;
    public static final int GUI_HEIGHT = 166;
    
    // Energy constants
    public static final int MAX_ENERGY = 10000;
    public static final int ENERGY_TRANSFER = 100;
    
    // Dimension constants
    public static final int OVERWORLD_ID = 0;
    public static final int NETHER_ID = 1;
    public static final int END_ID = 2;
}
```

## Performance Optimization

### 1. Lazy Initialization

Use lazy initialization for expensive operations:

```java
// Good - Lazy initialization
public class ExpensiveManager {
    private static Supplier<ExpensiveManager> instance = Suppliers.memoize(ExpensiveManager::new);
    
    public static ExpensiveManager get() {
        return instance.get();
    }
    
    private ExpensiveManager() {
        // Expensive initialization
    }
}

// Bad - Eager initialization
public class ExpensiveManager {
    private static final ExpensiveManager INSTANCE = new ExpensiveManager(); // Always created
    
    public static ExpensiveManager get() {
        return INSTANCE;
    }
}
```

### 2. Efficient Event Handling

Make event handlers efficient:

```java
// Good - Efficient event handler
BlockEvents.BLOCK_BREAK_AFTER.register((level, player, pos, state, blockEntity) -> {
    // Only run expensive operation on specific blocks
    if (state.is(ModBlocks.ENERGY_BLOCK.get())) {
        updateEnergyNetwork(level, pos);
    }
});

// Bad - Inefficient event handler
BlockEvents.BLOCK_BREAK_AFTER.register((level, player, pos, state, blockEntity) -> {
    // Always runs expensive operation
    updateEnergyNetwork(level, pos); // Runs for every block break!
});
```

### 3. Caching

Cache expensive computations:

```java
// Good - Cached computation
public class RecipeCache {
    private static final Map<Item, List<Recipe>> CACHE = new HashMap<>();
    
    public static List<Recipe> getRecipesForItem(Item item) {
        return CACHE.computeIfAbsent(item, i -> {
            // Expensive recipe lookup
            return findRecipesForItem(i);
        });
    }
    
    private static List<Recipe> findRecipesForItem(Item item) {
        // Implementation
    }
}
```

### 4. Client-Side Optimization

Optimize client-side rendering:

```java
// Good - Efficient rendering
public class CustomRenderer {
    private static final Model MODEL = loadModel(); // Load once
    
    public static void render(PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        // Reuse model
        VertexConsumer vertexConsumer = buffer.getBuffer(RENDER_TYPE);
        MODEL.renderToBuffer(poseStack, vertexConsumer, light, overlay);
    }
    
    private static Model loadModel() {
        // Expensive model loading
    }
}
```

## Security Considerations

### 1. Input Validation

Always validate player input:

```java
// Good - Input validation
public static boolean isValidItemStack(ItemStack stack) {
    if (stack.isEmpty()) {
        return false;
    }
    
    if (stack.getCount() <= 0 || stack.getCount() > stack.getMaxStackSize()) {
        return false;
    }
    
    return true;
}

public static void processItem(ItemStack stack, Player player) {
    if (!isValidItemStack(stack)) {
        player.sendSystemMessage(Component.literal("Invalid item!"));
        return;
    }
    
    // Process valid item
}
```

### 2. Permission Checks

Check permissions before performing operations:

```java
// Good - Permission check
public static void teleportPlayer(Player player, BlockPos pos) {
    if (!player.hasPermissions(2)) { // OP level 2
        player.sendSystemMessage(Component.literal("You don't have permission to teleport!"));
        return;
    }
    
    // Teleport player
}
```

### 3. Server-Side Validation

Validate critical operations on the server:

```java
// Good - Server-side validation
public static void craftItem(ServerPlayer player, ResourceLocation recipeId) {
    // Validate recipe exists
    Recipe<?> recipe = player.server.getRecipeManager().byKey(recipeId).orElse(null);
    if (recipe == null) {
        player.connection.disconnect(Component.literal("Invalid recipe!"));
        return;
    }
    
    // Validate player can craft recipe
    if (!canCraftRecipe(player, recipe)) {
        player.sendSystemMessage(Component.literal("Cannot craft this recipe!"));
        return;
    }
    
    // Craft item
}
```

## Maintainability Tips

### 1. Documentation

Document your code with clear comments:

```java
/**
 * Calculates the energy transfer rate between two components.
 * 
 * @param fromEnergy The energy of the source component
 * @param toEnergy The energy of the target component
 * @param maxTransfer The maximum transfer rate
 * @return The actual transfer rate
 */
public static int calculateTransferRate(int fromEnergy, int toEnergy, int maxTransfer) {
    // Can only transfer if source has energy and target has space
    if (fromEnergy <= 0 || toEnergy >= MAX_ENERGY) {
        return 0;
    }
    
    // Transfer at maximum rate or available amount, whichever is smaller
    int availableSpace = MAX_ENERGY - toEnergy;
    return Math.min(maxTransfer, Math.min(fromEnergy, availableSpace));
}
```

### 2. Error Handling

Handle errors gracefully:

```java
// Good - Graceful error handling
public static void loadConfiguration() {
    try {
        ConfigManager.load();
    } catch (Exception e) {
        Constants.LOG.error("Failed to load configuration", e);
        
        // Use defaults
        ConfigManager.useDefaults();
        
        // Notify user
        if (Platform.isClient()) {
            Minecraft.getInstance().player.sendSystemMessage(
                Component.literal("Failed to load config, using defaults")
            );
        }
    }
}
```

### 3. Testing

Write tests for your code:

```java
// Example test structure
public class EnergySystemTest {
    @Test
    public void testEnergyTransfer() {
        // Setup
        EnergyStorage from = new EnergyStorage(1000);
        EnergyStorage to = new EnergyStorage(1000);
        
        // Test
        int transferred = EnergyUtils.transferEnergy(from, to, 100);
        
        // Verify
        assertEquals(100, transferred);
        assertEquals(900, from.getEnergy());
        assertEquals(100, to.getEnergy());
    }
    
    @Test
    public void testEnergyTransferFullTarget() {
        // Setup
        EnergyStorage from = new EnergyStorage(1000);
        EnergyStorage to = new EnergyStorage(950);
        
        // Test
        int transferred = EnergyUtils.transferEnergy(from, to, 100);
        
        // Verify
        assertEquals(50, transferred); // Only 50 can fit
        assertEquals(950, from.getEnergy());
        assertEquals(1000, to.getEnergy());
    }
}
```

## Debugging Tips

### 1. Logging

Use structured logging:

```java
// Good - Structured logging
public class EnergyNetwork {
    private static final Logger LOGGER = LoggerFactory.getLogger("mymod/energynetwork");
    
    public void updateNetwork() {
        LOGGER.debug("Updating energy network at {}", networkPos);
        
        try {
            // Update logic
            LOGGER.info("Updated network with {} nodes, {} energy", nodes.size(), totalEnergy);
        } catch (Exception e) {
            LOGGER.error("Failed to update energy network", e);
        }
    }
}
```

### 2. Debug Commands

Create debug commands for testing:

```java
public class DebugCommands {
    public static void registerDebugCommands() {
        // Only register in development environment
        if (!Platform.isDevelopmentEnvironment()) {
            return;
        }
        
        Commands.literal("mymoddebug")
            .requires(source -> source.hasPermissions(4))
            .then(Commands.literal("energy")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player != null) {
                        EnergyNetwork network = EnergyNetworkManager.getNetwork(player.blockPosition());
                        player.sendSystemMessage(Component.literal("Network energy: " + network.getEnergy()));
                    }
                    return Command.SINGLE_SUCCESS;
                }))
            .register();
    }
}
```

### 3. Visual Debugging

Use visual debugging aids:

```java
public class VisualDebug {
    public static void showEnergyNetwork(Level level, EnergyNetwork network) {
        if (!Platform.isDevelopmentEnvironment()) {
            return;
        }
        
        // Draw particles at network nodes
        for (BlockPos pos : network.getNodes()) {
            Vec3 particlePos = Vec3.atCenterOf(pos);
            level.addParticle(ParticleTypes.END_ROD, 
                particlePos.x, particlePos.y, particlePos.z, 
                0, 0.1, 0);
        }
    }
}
```

## Deployment Tips

### 1. Version Management

Use semantic versioning
```

### 2. Configuration Validation

Validate configuration on load:

```java
public class ConfigValidator {
    public static boolean validateConfig(MyModConfig config) {
        boolean valid = true;
        
        // Validate energy values
        if (config.maxEnergy <= 0) {
            Constants.LOG.error("maxEnergy must be positive!");
            valid = false;
        }
        
        if (config.energyTransfer <= 0 || config.energyTransfer > config.maxEnergy) {
            Constants.LOG.error("energyTransfer must be between 0 and maxEnergy!");
            valid = false;
        }
        
        // Validate item lists
        if (config.allowedItems.isEmpty()) {
            Constants.LOG.warn("No allowed items configured!");
        }
        
        return valid;
    }
}
```
