# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build and run
./gradlew run

# Build a distributable jar
./gradlew build

# Run with explicit JVM flags (already set in build.gradle)
./gradlew run --args=""
```

There are no tests in this project yet.

## Architecture

This is a falling-sand particle simulation using Java + LWJGL (OpenGL 3.3 core profile, GLFW).

**Simulation dimensions:** `SIM_W=400 × SIM_H=300` pixels, rendered at `SCALE=2` → 800×600 window.

### Data model (`World`)

`World` stores four flat parallel arrays indexed by `y * width + x`:
- `cells[]` — particle type ID (0 = empty)
- `temps[]` — temperature per cell
- `flags[]` — bit-packed universal booleans; add a `FLAG_*` constant + named accessor pair for each new property (e.g. `FLAG_IGNITED` / `isIgnited`)
- `data[]` — element-specific numeric values (Gas lifetime, fuel level, etc.); full 0–255 range

`World.step()` iterates bottom-to-top (y = height-1 → 0) and dispatches to `Element.update()` for each non-empty cell.

### Element / particle type system

Each particle type is an `Element` subclass. The abstract behavior base classes in `particle/defs/` implement movement physics:

| Class | Behavior |
|-------|----------|
| `Static` | Immobile (wood, stone) |
| `Powder` | Falls, piles diagonally (sand, salt) |
| `Liquid` | Falls, spreads horizontally; `viscosity` controls spread width; `liquidInteractions()` hook for reactions |
| `Gas` | Rises upward; lifetime stored in `extra[]` byte |

Concrete types live in `particle/types/` — they subclass a behavior class, pass an ID and color to `super()`, and override reaction hooks as needed.

**Registering a new element:** add an ID constant to `ElementRegistry.ID`, create a class in `particle/types/` that extends the appropriate `defs/` base, then call `ParticleRegistry.register(new MyElement())` in `Main.java`.

IDs are stored in a fixed-size `Element[256]` array; IDs 0–5 are already claimed (EMPTY, SAND, WATER, WOOD, FIRE, STEAM).

### Rendering pipeline

`World.buildPixels()` builds an RGBA `ByteBuffer` (off-heap via `MemoryUtil`) by looking up `ElementRegistry.colorOf(id)` for each cell. `Renderer` uploads this as a `GL_NEAREST`-filtered texture each frame and draws it on a full-screen quad using a trivial pass-through shader (`shaders/render.vert` / `shaders/render.frag`).