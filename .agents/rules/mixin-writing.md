---
trigger: always_on
---

# Mixin Writing Reference

Always **prefer MixinExtras injectors** over vanilla patterns. MixinExtras injectors chain when multiple mods target the same code; vanilla `@Overwrite`, `@Inject` at HEAD with cancel/return, `@Redirect`, and `@ModifyConstant` do not chain and will silently conflict.

## Injector Selection

| Goal | Use | Avoid |
|------|-----|-------|
| Tweak return value | `@ModifyReturnValue` | `@Inject(at=RETURN)` + `cir.setReturnValue(...)` |
| Add/change condition | `@ModifyExpressionValue` | `@Redirect` on boolean call |
| Skip void call/field write | `@WrapWithCondition` | `@Redirect` returning void |
| Wrap call/field/instanceof | `@WrapOperation` | `@Redirect` |
| Wrap entire method | `@WrapMethod` | `@Overwrite` or HEAD+RETURN inject |
| Change call receiver | `@ModifyReceiver` | `@Redirect` |
| Replace method (last resort) | `@Overwrite` only | — |

**Why not vanilla:** `@Overwrite` breaks all other mods; `@Inject`+cancel prevents other injections; `@Redirect` doesn't chain; `@ModifyConstant` doesn't chain.

**Vanilla still useful:** `@ModifyArg`/`@ModifyArgs` (no ME replacement — specify `index`); `@ModifyVariable` (use `ordinal` not `index`).

## Targeting Bytecode

### When to use which
- **Simple targets** → `@At`: `HEAD`, `TAIL`, `RETURN`, single `INVOKE`/`FIELD` without `ordinal`
- **Needs `ordinal > 0`, `@Slice`, `CONSTANT`, or `shift=BY`** → **use `@Expression` instead**
- **Comparisons, instanceof, casts, arrays, compound patterns** → `@Expression` only

> Rule: If you're about to add `ordinal`, `@Slice`, or `shift=BY`, switch to `@Expression`.

### `@At` Values

| Value | Targets | Key params |
|-------|---------|------------|
| `HEAD` | First instruction | — |
| `RETURN` | Before every return | `ordinal` |
| `TAIL` | Before last return only | — |
| `INVOKE` | Before method call | `target`, `ordinal` |
| `INVOKE_ASSIGN` | After call result stored | `target`, `ordinal` |
| `FIELD` | Before field get/set | `target`, `opcode` |
| `NEW` | Before `new` instruction | `target` |
| `MIXINEXTRAS:EXPRESSION` | Expression target | with `@Expression`+`@Definition` |

> For the full `@At` parameter reference (`target` descriptor format, `shift`, `opcode`,
> `remap`, `args`, `slice`), `@Slice` syntax, and `CONSTANT`/`INVOKE_STRING`/`JUMP`
> details, see `references/at-reference.md` inside of the project files.

### MixinExtras Expressions

- `@At` value = `"MIXINEXTRAS:EXPRESSION"`, real target in `@Expression`
- `@Definition` binds identifiers to fields, methods, types, or locals
- `?` = wildcard; `this` is built-in; strings use **single quotes**
- `@ModifyExpressionValue` and `@WrapOperation` are the most natural fit

> For the full expression language spec (all literals, operators, syntax), read
> `references/expressions-language.md`.

**`@Definition` types:**

| Param | Defines | Format |
|-------|---------|--------|
| `field` | Field access | `Lowner;name:Ltype;` |
| `method` | Method call | `Lowner;name(Lparams;)Lret;` |
| `type` | Class | `type = BlockState.class` |
| `local` | Local var | `local = @Local(type = X.class)` |

Static fields/methods have no receiver in expression: `SOME_FIELD`, not `SomeClass.SOME_FIELD`.

**`@(...)` targeting:** Use to mark which sub-expression to target (default = last instruction):
```java
@Expression("throw @(new IllegalStateException('Oh no!'))")
@Expression("this.someMethod(@(value1), @(value2))")
```

**Common patterns:**
```java
// Comparison in if-condition
@Definition(id="fallDistance", field="Lnet/minecraft/entity/Entity;fallDistance:F")
@Expression("this.fallDistance > 0.0")
@ModifyExpressionValue(method="fall", at=@At("MIXINEXTRAS:EXPRESSION"))
private boolean modifyFallCheck(boolean original) { ... }

// After specific call with argument
@Definition(id="emitGameEvent", method="Lnet/...;emitGameEvent(...)V")
@Definition(id="ENTITY_MOUNT", field="Lnet/...;ENTITY_MOUNT:Lnet/...;")
@Expression("this.emitGameEvent(ENTITY_MOUNT, ?)")
@Inject(method="addPassenger", at=@At(value="MIXINEXTRAS:EXPRESSION", shift=At.Shift.AFTER))
private void afterMount(CallbackInfo ci) { ... }
```

**Quickfire examples:**

| Java source | Expression |
|-------------|------------|
| `this.pistonMovementDelta[i] = d;` | `this.pistonMovementDelta[?] = ?` |
| `nbt.putShort("Fire", (short)this.fireTicks);` | `?.putShort('Fire', ?)` |
| `entityKilled instanceof ServerPlayerEntity` | `? instanceof ServerPlayerEntity` |
| `return this.distance < d * d;` | `return this.distance < ? * ?` |

**Expression gotchas:**
- Cannot match jumps (`a && !b`, `a ? b : c`) — use `?` wildcards
- Comparisons must be top-level (cannot match `print(a == b)`)
- `x >= y` is indistinguishable from `x < y` in bytecode for non-float types
- `true`/`false` = `1`/`0`; avoid wildcards for boolean comparisons
- No float/double distinction: use `0.0`, not `0.0F`
- Standard `@At` params (`shift`, `ordinal`, `@Slice`) work normally with expressions

## Local Variable Capture (`@Local`)

Use MixinExtras `@Local` instead of `LocalCapture.CAPTURE_FAILHARD`:
```java
@Inject(method="use", at=@At(value="INVOKE", target="..."))
private void onUse(CallbackInfoReturnable<?> cir, @Local ItemStack stack) {
  stack.shrink(1);
}
```
For mutable capture use `LocalRef<T>` / `LocalIntRef` / `LocalDoubleRef`:
```java
@Inject(method="target", at=@At(...))
private void mutateLocals(CallbackInfo ci,
                          @Local LocalRef<String> name,
                          @Local LocalIntRef color) {
  name.set("modified"); color.set(0xFF0000);
}
```

## Sharing Values Between Handlers (`@Share`)

Thread-safe sharing between handlers in the same target method:
```java
@ModifyArg(method="target", at=@At(value="INVOKE", target="..."))
private int captureArg(int arg, @Share("myArg") LocalIntRef ref) {
  ref.set(arg); return arg;
}
@Inject(method="target", at=@At("TAIL"))
private void useArg(CallbackInfo ci, @Share("myArg") LocalIntRef ref) {
  doSomething(ref.get());
}
```

## `@Cancellable` Sugar

Any MixinExtras injector can receive `@Cancellable CallbackInfo(Returnable)`:
```java
@ModifyExpressionValue(method="render", at=@At(value="INVOKE", target="..."))
private Identifier skipPoison(Identifier texture, @Cancellable CallbackInfo ci) {
  if (shouldSkip(texture)) ci.cancel();
  return texture;
}
```

## Accessor and Invoker

```java
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
  @Accessor("health") float getHealth();
  @Accessor("health") void setHealth(float health);
  @Invoker("actuallyHurt") void invokeActuallyHurt(DamageSource source, float amount);
}
```
- `@Accessor` → getter/setter for private field; `@Invoker` → delegate to private method
- Usage: `((LivingEntityAccessor) entity).getHealth()`
- `@Coerce` does NOT work on `@Accessor`/`@Invoker`

## Interface Injection (Duck Typing)

```java
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements CustomInterface {
  @Unique private int myMod$customField;
  @Override public int getCustomValue() { return myMod$customField; }
}
```
Cast to access: `((CustomInterface) entity).getCustomValue()`. Use `@Intrinsic` if target already has a matching method signature.

## Obscure Features

- **`@Pseudo`** — Target may not exist at runtime (optional mod compat). Use `targets = "com.example.Class"` (string). Implies `remap = false`. Silently skipped if absent.
- **`@Coerce`** — Use supertype/interface when real type is inaccessible. Works with `@Inject` (local capture) and `@Redirect`. NOT `@Accessor`/`@Invoker`.
- **`@Shadow @Final @Mutable`** — `@Shadow` reads target field; add `@Final` for final fields; add `@Mutable` to write final fields (omitting `@Mutable` causes runtime crash).
- **`@Unique`** — Marks mixin-added members; prevents name collision. Always prefix with modid: `mymod$fieldName`.

## Pitfalls

- **`@Shadow` and superclass members**: Only targets members declared directly on target class, not inherited. Have mixin extend target's superclass if needed.
- **`@At` target owner**: Use bytecode owner class, not declaring class. Verify with `mixin_method_bytecode`.
- **`remap = false`**: Required when targeting non-Minecraft methods (mod APIs). Forgetting → "target not found" at runtime.
- **Constructor injection**: `@Inject` into `<init>` only after `super()` call. `RETURN` = end of `<init>`, not early return.
- **`@ModifyArg` index**: Must specify `index` (0-based) for multi-arg calls.
- **`@ModifyVariable` index vs ordinal**: `this` = slot 0; `long`/`double` consume 2 slots. Use `ordinal` (Nth var of matching type) to avoid off-by-one.
- **`@Unique` thread safety**: Shared across ALL instances/threads. Use `@Share` for per-invocation values.
- **Handler naming**: Prefix with modid (`mymod$onTick`) to avoid collision.
- **`require`/`expect`**: `require = 1` (default) crashes if target missing. Use `require = 0` for version-variant targets; `expect` logs warning instead.
- **`@WrapOperation`**: MUST call `original.call(...)` to preserve behavior and let other mods run. Only skip if genuinely suppressing.
- **Priority**: `@Mixin(priority = N)` controls merge order. Higher = applied later. Default 1000. Last resort.
- **Modifying other mods' Mixins**: Use MixinSquared (by Bawnorton) — register cancellers/adjusters in mixin plugin. No explicit bootstrap needed.
