package sandbox.elements.gasses;

import sandbox.elements.ElementRegistry;

public class Smoke extends Gas {
    public Smoke() {
        super(ElementRegistry.ID.SMOKE, "Smoke", new int[]{100, 100, 100}, 500);
    }
    
}
