package sandbox.elements.solids;

import sandbox.core.World;
import sandbox.elements.ElementRegistry;

public class Diamond extends MovableSolid {
    public Diamond() {
        super(ElementRegistry.ID.DIAMOND, "Diamond", new int[]{40, 110, 240}, 0.3f, 0.95f);
    }
    
    @Override
    public void onSpawn(World world, int x, int y) {
        // Create a sparkly diamond with bright spots
        world.setShade(x, y, (byte)((Math.random() * 50) + 200));
        world.setData(x, y, (byte)(Math.random() * 255)); // store twinkle phase
        world.setFalling(x, y, true);
    }

    @Override
    public void update(World world, int x, int y) {
        // Call parent update for physics
        super.update(world, x, y);
        
        // Add dynamic twinkling effect
        byte phase = world.getData(x, y);
        phase = (byte)((phase + 1) % 256);
        world.setData(x, y, phase);
        
        // Vary shade based on twinkle phase
        int baseShade = 200;
        int twinkle = (int)(Math.sin(phase / 40.0) * 30) + 35; // oscillate between 5 and 65
        world.setShade(x, y, (byte)(baseShade + twinkle));
        
        // Occasionally create bright sparkle flashes
        if (Math.random() < 0.02) { // 2% chance per frame
            world.setShade(x, y, (byte)255); // full brightness flash
        }
    }

    @Override
    public void onNeighbor(World world, int x, int y, int nx, int ny) {
        // Make neighboring diamonds sparkle more when next to each other
        if (!world.isEmpty(nx, ny) && world.get(nx, ny) == ElementRegistry.ID.DIAMOND) {
            // Brighten slightly when touching other diamonds
            byte currentShade = world.getShade(x, y);
            if (currentShade < 240) {
                world.setShade(x, y, (byte)(currentShade + 10));
            }
        }
    }

}
