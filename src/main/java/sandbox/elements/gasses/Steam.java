package sandbox.elements.gasses;

import sandbox.elements.ElementRegistry;

public class Steam extends Gas {
    public Steam() {
        super(ElementRegistry.ID.STEAM, "Steam", new int[]{200, 200, 215}, 400);
    }
}
