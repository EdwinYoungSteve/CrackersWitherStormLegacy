# Operations Log

## 2026-08-05 - CRL reuse audit

- Reused CRL-provided LWJGLX, Mixin, and MixinExtras integration instead of local compatibility dependencies.
- Replaced reflection-based Minecraft field/method access with CRL Mixin accessors and shadows.
- Replaced duplicate super beacon ingredient matching with Forge `Ingredient` and `CraftingHelper`.
- Centralized modern-to-1.12 recipe item fallbacks and preserved modern durability-insensitive ingredient matching.
- Replaced manual upstream archive stream closing and copying with Java 25 resource APIs.
- Fixed `BufferedImage` cleanup on unchanged textures and failed PNG conversion.
- The first Mixin-enabled compile observed an incomplete generated `srg2mcp.jar`. The mapping file completed generation, the orphaned task-owned Gradle daemon was identified by PID, parent, creation time, and command line, then stopped. The next compile completed successfully without deleting source or cache files.
- Verification: `compileJava`, focused recipe tests, full `test` (11 passed), `jar`, and `remapJar` succeeded with Java 25, `--no-daemon`, and `--max-workers=1`.
- Final artifact contains the Mixin config and refmap and contains no upstream ARR JAR, texture, or sound entries.
- No Minecraft client or server task was launched. No task-owned Java or Gradle process remained after verification.

## 2026-08-05 - CRL LWJGL 3 follow-up review

- Corrected the earlier LWJGLX conclusion: CRL's `LWJGLTransformer` is registered as a core transformer and only intercepts `org.lwjgl.*` class loads, then merges the corresponding `org.lwjglx.*` implementation into that legacy namespace.
- `GuiSuperBeacon` directly imports `org.lwjglx.input.Mouse`; both compiled classes and the remapped release JAR retain that reference. This bypasses the `org.lwjgl.input.Mouse` compatibility class used by Minecraft and can read a separate, unadvanced event queue. The source should use `org.lwjgl.input.Mouse` and let CRL bridge it to LWJGL 3.
- The two `org.lwjgl.opengl.GL11` imports are appropriate: they only supply primitive constants while rendering remains on Minecraft's `GlStateManager`, `BufferBuilder`, and `Tessellator` paths.
- CRL 0.6.6-alpha already supplies LWJGL 3.4.1 modules (`glfw`, `jemalloc`, `openal`, `opengl`, `stb`, `tinyfd`), lwjglxx 1.1.21, and JOML 1.10.9. The release JAR does not bundle any `org.lwjgl`, `org.joml`, or `com.cleanroommc` classes.
- JOML, STB, direct GLFW, and experimental Kirino rendering would not simplify the current fixed-function/TESR code. In particular, direct GLFW input would bypass Minecraft's event lifecycle, and STB image resizing would add native-buffer ownership to a one-time resource conversion path.
- Small non-LWJGL cleanup candidates found without changing behavior: replace the private `LegacyWitherStormPartLogic.clamp` with Java 25 `Math.clamp`; remove the unused `atlasTexture` parameter and unused RGB locals in `RenderSuperBeacon`; remove the redundant Gradle `java` plugin when `java-library` is applied; consider records/switch expressions only for immutable private DTOs and mapping code.
- Review was read-only apart from this operations log. No build or game task was run.

## 2026-08-05 - CRL LWJGL namespace correction

- Cleanroom's official porting guide explicitly states that mods must not call `org.lwjglx` classes because they are runtime implementation classes.
- Followed the official Cleanroom mod template by enabling its compile-only `com.cleanroommc:lwjglx:1.0.0` compatibility artifact, then changed the super beacon GUI back to the `org.lwjgl.input.Mouse` namespace used by Minecraft. The artifact is compile-only and is not bundled.

## 2026-08-05 - CRL Mixin bootstrap and development startup repair

- Removed the duplicate `crl.dev.mixin` system property and `MixinConfigs` manifest entry. The development classpath could not expose the split resource output early enough for `crl.dev.mixin`, causing `MixinInitialisationError` before mod loading.
- Added `WitherStormMixinLoader`, which uses the existing CRL contracts by implementing both Forge `IFMLLoadingPlugin` and CleanMix `ILateMixinLoader`. The existing coremod build switches now provide `fml.coreMods.load` in development and `FMLCorePlugin` plus `FMLCorePluginContainsFMLMod` in the release manifest.
- Updated the Mixin configuration compatibility level from `JAVA_8` to `JAVA_25`, matching the CRL source and class-file target.
- Added loading-contract tests and connected the test source set to the main CRL/Minecraft classpath so those contracts can be verified without launching the game.
- A user-launched IntelliJ client run passed the former Mixin initialization failure, loaded `WitherStormLegacyMixinLoader`, initialized LWJGL 3.4.1, and created an OpenGL 4.6 window. Its log then exposed two independent blockers: unexpanded Blossom metadata and Future MC's missing `forgelin` dependency.
- Moved `mcmod.info` and `pack.mcmeta` into `src/main/resource-templates`, converted placeholders to Blossom's `{{ ... }}` syntax, and added a JSON metadata regression test.
- Added Cleanroom's `Forgelin-Continuous` as a non-transitive development runtime mod. It provides the compatibility `forgelin` mod ID required by Future MC without bundling Forgelin or Kotlin into this mod.
- Verification: `compileJava`, focused Mixin loader tests, full `test` (14 passed), `jar`, and `remapJar` succeeded with Java 25, `--no-daemon`, and `--max-workers=1`. The release JAR contains the loader, Mixin config, generated refmap, valid metadata, and coremod manifest entries; it contains no Forgelin, Kotlin, LWJGL, JOML, or upstream ARR JAR content.
- Codex did not launch `runClient`, Minecraft, or a game window and did not terminate the user-launched client or any IntelliJ-owned process.

## 2026-08-05 - Mixin target timing correction

- The user-provided client log confirmed that the external ARR archive was valid; the remaining crash was `Minecraft` being cast to `MinecraftAccessor` before the accessor mixin had been applied.
- The mixin configuration targets vanilla classes (`Minecraft` and `EntityLivingBase`). `ILateMixinLoader` queues configs during `Loader.loadMods`, after those classes are defined, so it cannot transform them.
- Changed `WitherStormMixinLoader` to use CRL's `IEarlyMixinLoader` alongside `IFMLLoadingPlugin`, which queues the configuration from `CoreModManager.injectCoreModTweaks` before vanilla class loading.
- Verification: `compileJava` passed with Java 25, `--no-daemon`, and `--max-workers=1`; no client task was launched.

## 2026-08-05 - Tainted blocks, foods, and sickness cure parity

- Replaced the tainted dust placeholder based on `BlockRedstoneWire` with a dedicated non-powered wire block. It only connects to itself, preserves the upstream `none`/`side`/`up` state values and slope behavior, and retains rotation, mirroring, and purple tinting without exposing a redstone power property.
- Ported the complete upstream food values and independent potion-effect chances for withered flesh and withered spider eyes.
- Changed golden apple stew to 1.12 soup semantics so it is non-stackable and returns a bowl, while preserving regeneration, absorption, rarity, nutrition, saturation, and sickened-entity curing behavior.
- Added an NBT-backed 1,200-tick player cure state that pauses wither sickness progression while treatment is active.
- Replaced six generic tainted statue blocks with a dedicated directional, metadata-safe, rotatable, mirrored, non-colliding implementation that copies the appropriate slime- or bone-block material behavior.
- Added focused tests for dust connectivity/state serialization, foods, cure state, and statue metadata/geometry.

## 2026-08-05 - Wither potions and brewing parity

- Confirmed from the upstream bytecode that the three potion types are `wither` (Wither I, 900 ticks), `long_wither` (Wither I, 1,800 ticks), and `strong_wither` (Wither II, 432 ticks), then registered matching 1.12 `PotionType` instances.
- Reused the official 1.12 `PotionHelper.addMix` API for five type conversions. Because potion-type conversions preserve the registered potion container, these provide all 15 upstream drinkable, splash, and lingering brewing combinations without duplicating container-specific recipes.
- Ported poison, long poison, and strong poison conversions through the withered spider eye, plus redstone and glowstone upgrades from the base wither potion.
- Extended the external language conversion so upstream potion, splash-potion, and lingering-potion names are exposed under the display keys expected by 1.12. No upstream language asset was copied into the port JAR.
- Added tests for registry names, effect durations/amplifiers, all 15 real brewing outputs, container preservation, stacked-input rejection, unrelated potion/ingredient rejection, and legacy language keys.
- Verification: `compileJava`, focused potion/brewing/language tests, full `test` (41 passed), `jar`, and the finalized `remapJar` all succeeded with Java 25, `--no-daemon`, and `--max-workers=1`. No client or server task was launched.

## 2026-08-05 - Registered block strength and light parity

- Confirmed Cleanroom-FG3's 1.12.2 `Block#setResistance` conversion (`parameter * 3`, then explosion resistance divides by 5) from the decompiled runtime source. Added one shared modern-to-legacy conversion helper and corrected all migrated block constructors that were using modern resistance values as legacy parameters.
- Audited the upstream block registry and corrected hardness, explosion resistance, sound/material mapping, harvest level, and light values for flesh blocks, tainted stone/cobblestone/sandstone families, glass, planks, leaves, doors, trapdoors, buttons, pressure plates, slabs, stairs, walls, Formidibomb, firework bundle, Withered Phlegm, both super beacons, the tainted torch, and the tainted dust lamp.
- Preserved the upstream stair-specific resistance even though 1.12 `BlockStairs` delegates explosion resistance to its model block.
- Added `LegacyRegisteredBlockPropertiesTest` covering 53 total tests across the full suite, including modern strength/resistance and light-level assertions for the registered block families.
- Verification: focused block-property tests, full `test` (53 passed), `jar`, and `remapJar` succeeded with Java 25, `--no-daemon`, and `--max-workers=1`. No client, server, or game task was launched.
