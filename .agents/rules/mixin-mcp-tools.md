---
trigger: always_on
---

# MixinMCP Tools

MixinMCP tools are provided by the IntelliJ MCP server. They index the entire classpath including inside dependency jars (Grep cannot see these), provide type hierarchy, call graph, and reference tools faster than text search, and return clean structured results. Dependencies without published sources are decompiled via Vineflower so every library is searchable.

**ALWAYS prefer these tools over Grep, Read, or jar extraction.**

## Invoking Tools

Call directly by name once the IntelliJ MCP server is connected:
mixin_find_class(className="net.minecraft.world.level.Level", includeMembers=true)
Read tool descriptions for all parameters before calling.

## Tool Selection

| Goal | Tool |
|------|------|
| Look up class by FQCN | `mixin_find_class` (if SourceKind is "Classes JAR (binary)", use `mixin_get_dep_source` for better source) |
| Read one method body | `mixin_find_class(className, methodName=...)` — avoids dumping huge files like `Block`/`BlockBehaviour`; pair with `fieldName` for a single field |
| Search names across classpath | `mixin_search_symbols` |
| Grep dependency sources by regex | `mixin_search_in_deps` → `mixin_get_dep_source` with returned `url`; pass `contextLines` for short bodies to skip follow-up read |
| Read a known dependency file | `mixin_get_dep_source(path="io/redspace/.../Utils.java")` |
| Inheritance chain | `mixin_type_hierarchy` |
| All implementors | `mixin_find_impls` |
| All usages of class/method/field | `mixin_find_references` (supports `memberName` for both methods and fields) |
| Cross-mod mixin conflicts | `mixin_find_targeting_mixins` — finds all @Mixin classes + injection points |
| Call graph | `mixin_call_hierarchy` |
| Method origin in hierarchy | `mixin_super_methods` |
| All overrides of a method | `mixin_find_overrides` |
| Synthetic/lambda method names | `mixin_class_bytecode(filter="synthetic")` |
| Exact @At(target) for INVOKE | `mixin_method_bytecode` — read owner from INVOKE* instructions |
| Convert mapping namespaces | `mixin_mappings_lookup` — mojmap/yarn/intermediary/srg/obf for class/method/field |
| Diagnose missing source roots | `mixin_list_source_roots` |

## Examples
// Look up a class
mixin_find_class(className="net.minecraft.world.level.Level", includeMembers=true)

// Search deps, then read result
mixin_search_in_deps(regexPattern="destroyBlock", fileMask="Level")
mixin_get_dep_source(url="<url from result>", lineNumber=42, linesBefore=10, linesAfter=20)

// Capture short body inline (skip follow-up read)
mixin_search_in_deps(regexPattern="isPathfindable\(", pathPrefix="net/minecraft/", contextLines=8)

// Read one method from huge class
mixin_find_class(className="net.minecraft.world.level.block.state.BlockBehaviour", methodName="isPathfindable")

// Narrow to MC-only sources
mixin_search_in_deps(regexPattern="addEffect\(", pathPrefix="net/minecraft/", timeout=25000)

// Read by known path
mixin_get_dep_source(path="io/redspace/ironsspellbooks/player/ServerPlayerEvents.java", lineNumber=360, linesBefore=20, linesAfter=20)

// Field references
mixin_find_references(className="net.minecraft.world.entity.LivingEntity", memberName="DATA_HEALTH_ID")

## Common Pitfalls

### mixin_find_class
- Huge classes (`Block`, `BlockBehaviour`, `LivingEntity`): use `methodName=` or `fieldName=` to read only what you need; `includeSource=true` dumps 50KB+ for some classes.
- Inherited members show an `(inherited from X)` tag — use `mixin_super_methods` or call `mixin_find_class` on `X` for canonical declarations.
- Overloads are listed in order with line range headers — disambiguate by parameter list, not index.

### mixin_search_in_deps
- `regexPattern` is **Java regex** — escape metacharacters: `addEffect\\(` not `addEffect(`.
- `fileMask`: case-insensitive substring match in the path inside jar; no wildcards → substring; with wildcards → glob. Does NOT match jar names. Short substrings (e.g. `apotheosis`) can match compat packages in other mods — use `pathPrefix` for precision.
- `pathPrefix`: restricts to files whose path starts with prefix (e.g. `net/minecraft/` or `io/redspace/ironsspellbooks/`). Forward slashes.
- `contextLines` (default 0, max 200): lines around each match. Use 3–10 for short method bodies to capture inline and skip the follow-up read; keep 0 for wide searches.
- `roots`: `all` (default), `library` (published -sources.jar), `decompiled` (MixinMCP cache). Cache files skipped if same path matched in library sources.
- Broad searches can time out — increase `timeout` (e.g. 20000–30000) when no `fileMask`.

### Vanilla / Forge / NeoForge Sources Empty

When `mixin_search_in_deps` returns nothing for `net/minecraft/`, `net/minecraftforge/`, or `net/neoforged/`:
1. Run `mixin_list_source_roots` — read the MDG merged-jar source auto-attach section
2. Fix any auto-attach warnings first — all other fallbacks fight the wrong fire
3. Sources truly missing? Run toolchain recovery: Fabric Loom `genSources`, NeoForge MDG `downloadAssets`, any loader `genDependencySources --force`, then `mixin_sync_project`
4. Last resort: `mixin_find_class(includeSource=true)` and `mixin_method_bytecode` work via PSI/classfile without source roots

### mixin_get_dep_source
- `url`: copy exact `url:` string from search results (strip `[rootKind: ...]`).
- `path`: package path with `/` separators and `.java` extension. NOT a filesystem path.
- If path not found → fall back to `mixin_search_in_deps` and use the returned `url`.

### mixin_find_references / mixin_call_hierarchy / mixin_super_methods / mixin_find_overrides
- `memberName` supports both methods and fields. Disambiguate overloads with `parameterTypes=["MobEffectInstance"]` or `methodDescriptor="(Lnet/...;)Z"`. Parameterless: `parameterTypes=[]`.
- `mixin_find_references` returns both runtime call sites AND string references in mixin annotations.
- `mixin_call_hierarchy` covers direct calls, constructors (`[ctor]`), method refs, and lambda synthetics (`[lambda]`). Output is `owner#name(descriptor)` in JVM format, ready for `@At(target="...")`. Recurses up to `maxDepth` (default 3, max 10); cycles marked `[cycle]`.
- `mixin_find_overrides`: downward counterpart to `mixin_super_methods`. `[abstract]` tags mark abstract overrides. Non-overridable methods (static/private/final/constructors) return an explanation.
- `mixin_super_methods`: walks full chain to every root declaration, not just direct super. `[root declaration]` entries are usually the best mixin target for affecting all overriders.

### mixin_search_symbols
- Searches **short names only**, not FQCNs. Pass `LivingEntity`, not the full package path.
- Required param: `query` (single name substring). Search one name at a time.

### mixin_class_bytecode
- Decompiled source does NOT show synthetic method names. To target a lambda, MUST use `filter="synthetic"`.

### mixin_method_bytecode
- Each INVOKE* shows the **real owner class**, not the declaring class from source. Always use this owner in `@At(target="...")`.

### mixin_find_targeting_mixins
- Increase `maxResults` (default 50) for heavily-targeted classes like `LivingEntity` or `Player`.

### mixin_type_hierarchy
- Increase `maxResults` for heavily-inherited classes like `LivingEntity` or `Block`.
- Use `direction="supers"` to skip subtype listing when only needing the upward chain.
- **Direct interfaces** = own `implements`/`extends`; **inherited interfaces** = transitive closure tagged `(from X)` or `(via X)`. Check both before assuming a class doesn't implement something.

### mixin_mappings_lookup
- Input symbol must be in the `from` namespace — can't mix-and-match names across namespaces.
- Method/field inputs include owner class: `net/minecraft/src/C_12_.m_8793_`. Accepts `.` or `/` separators.
- Method descriptor optional — if omitted, all overloads on the class are returned.
- MC version auto-detected from `gradle.properties`. Pass `mcVersion` explicitly if auto-detect fails.
- First call per MC version downloads mappings to `~/.cache/mixinmcp/mappings/`; subsequent calls are memory-cached.
- `obf` namespace available for debugging. SRG published by Forge `mcp_config`/NeoForge `neoform`. ProGuard (Mojang) mappings don't carry field descriptors.

## Mixin Workflow

1. **Type hierarchy first** — run `mixin_type_hierarchy` before designing the @Mixin; the target's parent often defines the real injection site.
2. **Bytecode for synthetics** — `mixin_class_bytecode(filter="synthetic")` is the only way to get the `lambda$X$N` symbol for `method="..."`.
3. **Bytecode owner for `@At(target)`** — the INVOKE owner can differ from the declaring class in source; use `mixin_method_bytecode` and copy that owner.
4. **Method origin** — `mixin_super_methods` walks up to original declaration; `mixin_find_overrides` walks down to every concrete implementation.
5. **After writing a mixin** — validate for errors before committing.
6. **After dependency changes** — run `./gradlew genDependencySources` then `mixin_sync_project`.

For injector selection, `@At` format, and MixinExtras `@Expression` syntax, switch to the **mixin-writing** skill once the target is nailed down.

## Troubleshooting

**If `mixin_*` tools are not available:**
1. Ensure IntelliJ is running with the project open
2. Check: Settings → Plugins → verify "MCP Server" and "MixinMCP" are both enabled
3. Verify IntelliJ MCP server is configured in `~/.claude/settings.json` or project `.claude/settings.json` under `mcpServers`
4. **Restart Claude Code** if IntelliJ was started after the Claude Code session began
