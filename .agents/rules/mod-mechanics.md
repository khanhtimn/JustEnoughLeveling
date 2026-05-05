---
trigger: model_decision
description: Apply before writing any Mixin or event handler in a Minecraft mod. Enforces a three-tier priority: check mod library APIs first, then Fabric/NeoForge platform events, only falling back to Mixin when no event covers the use case.
---

## Event Handling Strategy

When implementing behavior hooks, always follow this priority order.
Each tier is more fragile and mod-incompatible than the last — only
escalate when the tier above cannot accomplish the goal.

### Tier 1: Framework / Mod Library APIs (Preferred)
Check if the mod framework or a library already on the classpath
exposes a purpose-built event or callback for what you need.

Examples:
- SpellsAPI, IronsSpellbooks, Geckolib, Patchouli, etc. providing
  their own event buses or registration hooks
- Any mod explicitly designed to be extended (APIs marked `@ApiStatus.OverrideOnly`,
  `@EventBusSubscriber`, or documented extension points)

**Use this if available.** These are stable, maintained, and designed
for cross-mod compatibility. Breaking changes are versioned.

### Tier 2: Platform-Specific Event Registration (Fallback)
If no mod library API covers the use case, use the loader's native
event system.

**Fabric:**
- `ServerLifecycleEvents`, `ServerEntityEvents`, `ServerPlayerEvents`,
  `ServerWorldEvents`, `AttackEntityCallback`, `UseBlockCallback`,
  `UseItemCallback`, `EntityElytraEvents`, etc. via Fabric API
- Register in your mod initializer:
  `SomeEvent.EVENT.register((args) -> { ... });`
- Prefer `EventFactory.createArrayBacked` if defining your own event

**NeoForge / Forge:**
- Subscribe via `@SubscribeEvent` on a class registered with
  `MinecraftForge.EVENT_BUS.register(this)` or
  `NeoForge.EVENT_BUS.register(this)`
- Prefer `IEventBus` injection via mod constructor for mod-lifecycle events
- Use `@Mod.EventBusSubscriber(bus = Bus.FORGE)` for game events,
  `Bus.MOD` for mod lifecycle events

**Why prefer over Mixin:**
Platform events are designed to be stacked — multiple mods can
subscribe to the same event without conflict. They are also remapped,
debuggable, and explicitly supported across loader versions.

### Tier 3: Mixin (Last Resort)
Only reach for Mixin when Tier 1 and Tier 2 cannot accomplish the goal
(e.g., the behavior is deep in vanilla logic with no event coverage,
or you need to modify a return value mid-method).

**Risks to communicate before writing a Mixin:**
- Mixins are **bytecode-level** — they break silently if the target
  method is renamed, refactored, or inlined across MC versions
- Multiple mods targeting the same injection point can conflict,
  especially with `@Inject` at `HEAD` + cancel or `@Redirect`
- Each Mixin added increases maintenance burden on every MC update

**Mixin discipline when unavoidable:**
- Prefer MixinExtras injectors (`@ModifyReturnValue`, `@WrapOperation`,
  `@ModifyExpressionValue`, `@WrapWithCondition`) — they chain safely
- Avoid `@Overwrite` and `@Redirect` entirely unless no other option
- Use `@Expression` over `ordinal`-based `@At` targeting for resilience
- Always prefix handler methods with your modid: `mymod$onTick`
- Set `require = 0` for targets that may not exist in all versions

### Decision Checklist

Before writing any hook, answer in order:
1. Does a mod library API (Tier 1) expose this event or callback? → Use it
2. Does Fabric API / NeoForge event system (Tier 2) cover this? → Use it
3. Is Mixin truly the only option? → Document why, then write the
   most chainable, narrowest Mixin possible

Never use Mixin to solve a problem that a platform event already handles.
