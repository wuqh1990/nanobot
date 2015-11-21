package it.paspiz85.nanobot.game;

import java.util.Arrays;

/**
 * Troops info.
 *
 * @author paspiz85
 *
 */
public final class TroopsInfo {

    private final int[] troopsCount;

    public TroopsInfo(final int[] troopsCount) {
        this.troopsCount = troopsCount;
    }

    public int[] getTroopsCount() {
        return troopsCount;
    }

    public int getTroopsCountSum() {
        return Arrays.stream(troopsCount).sum();
    }
}
