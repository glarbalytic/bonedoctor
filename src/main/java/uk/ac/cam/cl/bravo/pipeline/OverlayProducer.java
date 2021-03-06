package uk.ac.cam.cl.bravo.pipeline;

import io.reactivex.Observable;
import javafx.util.Pair;

import java.awt.image.BufferedImage;

public interface OverlayProducer {

    /**
     * Will request the overlay - starts the computation. Returns immediately.
     * This function has idempotent.
     *
     * @return Observable that, when subscribed, will emit the result as soon as it is computed. If the result has
     * already been computed, it is emitted immediately after subscribing. Emits always only one result.
     * Result is a rated pair (transformed, overlaid)
     */
    Observable<Rated<Pair<BufferedImage, BufferedImage>>> requestOverlay();

}
