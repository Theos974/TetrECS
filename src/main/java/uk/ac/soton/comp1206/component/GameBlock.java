package uk.ac.soton.comp1206.component;

import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);
    private boolean hovered = false;
    private boolean centre = false;


    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
        Color.TRANSPARENT,
        Color.DEEPPINK,
        Color.RED,
        Color.ORANGE,
        Color.YELLOW,
        Color.YELLOWGREEN,
        Color.LIME,
        Color.GREEN,
        Color.DARKGREEN,
        Color.DARKTURQUOISE,
        Color.DEEPSKYBLUE,
        Color.AQUA,
        Color.AQUAMARINE,
        Color.BLUE,
        Color.MEDIUMPURPLE,
        Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);


    /**
     * Create a new single Game Block
     *
     * @param gameBoard the board this block belongs to
     * @param x         the column the block exists in
     * @param y         the row the block exists in
     * @param width     the width of the canvas to render
     * @param height    the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);


    }

    /**
     * When the value of this block is updated,
     *
     * @param observable what was updated
     * @param oldValue   the old value
     * @param newValue   the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue,
                             Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */

    public void paint() {


        if (value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);

            if (centre) { //if centre is true it draws the center indicator on current PieceBoard
                drawCenterIndicator();
                logger.info("center indicator drawn");
            }
        }

        // If hover is true and you are not hovering over the displayed piece, then paint hover effect
        if (this.hovered && !isPartOfPieceBoard()) {
            paintHoverEffect();
        }

    }

    /**
     *paints the block currently being hovered on
     */
    private void paintHoverEffect() {


        var gc = getGraphicsContext2D();
        // If the value is 0 (empty block), use a more noticeable color for hover effect
        if (value.get() == 0) {
            gc.setFill(Color.rgb(200, 200, 200, 0.5)); // Light gray with semi-transparency
        } else {
            gc.setFill(
                Color.rgb(255, 255, 255, 0.3)); // White with semi-transparency for colored blocks
        }

        // Draws the hover effect
        gc.fillRect(1, 1, width - 2, height - 2);

        // Outlines for the hover effect to make it stand out
        gc.setStroke(Color.rgb(255, 255, 255, 0.7));
        gc.setLineWidth(2);
        gc.strokeRect(1, 1, width - 2, height - 2);

    }

    /**
     * when hovering over a block, "hovered" set as focus
     */
    protected void setHovered(boolean focus) {
        this.hovered = focus;
        paint();
    }



    /**
     * //method for when the mouse exits the block
     */
    public void removeHover() {
        hovered = false;
        paint(); // repaint to reflect the change in hover state
    }

    /**
     * checks whether the current block is part of PieceBoard
     * @return true or false
     */
    private boolean isPartOfPieceBoard() {
        return this.gameBoard instanceof PieceBoard;
    }


    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0, 0, width, height);

        // Draws the outline of the tile
        gc.setStroke(Color.DARKGRAY); // A dark outline
        gc.setLineWidth(1); // Thin line
        gc.strokeRect(0, 0, width, height);

        // Draws the top and left edges to look like a light source is coming from the top left
        gc.setStroke(Color.LIGHTGRAY.brighter()); // Lighter edge to simulate a raised tile
        gc.strokeLine(0, 0, width, 0); // Top edge
        gc.strokeLine(0, 0, 0, height); // Left edge

        // Draws the bottom and right edges to look indented
        gc.setStroke(Color.DARKGRAY.darker()); // Darker edge to simulate shadow
        gc.strokeLine(width, 0, width, height); // Right edge
        gc.strokeLine(0, height, width, height); // Bottom edge
    }

    /**
     * Paint this canvas with the given colour
     *
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        // Clears the canvas
        gc.clearRect(0, 0, width, height);

        // Fills the background to give depth, dark colour to simulate shadow
        gc.setFill(((Color) colour).darker().darker());
        gc.fillRect(0, 0, width, height);

        // Creates a 3D effect by layering gradients
        RadialGradient gradient = new RadialGradient(
            0, 0,
            width / 2, height / 2,
            Math.max(width, height) / 2,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, ((Color) colour).brighter().brighter()),
            new Stop(1, (Color) colour)
        );

        // Apply gradient to simulate a curved surface
        gc.setFill(gradient);
        gc.fillRect(3, 3, width - 6, height - 6);

        // Creates highlights for a glossy effect
        LinearGradient highlightGradient = new LinearGradient(
            0, 0, width, height,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.WHITE),
            new Stop(1, ((Color) colour).brighter())
        );
        gc.setFill(highlightGradient);

        // Draws the border for a crisp look
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0, 0, width, height);
    }


    /**
     * responsible for drawing the circle indicator
     */
    private void drawCenterIndicator() {
        var gc = getGraphicsContext2D();
        double centerX = width / 2;
        double centerY = height / 2;
        double radius =
            Math.min(width, height) / 4; // radius is a quarter of the smallest dimension

        gc.setFill(Color.rgb(255, 255, 211, 0.7)); // Choose color for the center indicator
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    /**
     * sets centre true for centre indicator
     */
    public void isCenterBlock() {
        centre = true;
        paint();
    }

    /**
     * Get the column of this block
     *
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     *
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     *
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Fades out the blocks cleared ( visual representation )
     */
    public void fadeOut() {
        final long startNanoTime = System.nanoTime();
        final double fadeDurationSeconds = 1.0; // 1 second fade duration
        final double nanoToSeconds = 1e-9;
        // Retrieve the original color of the block
        Color originalColor = COLOURS[value.get()];

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                double elapsedTime = (currentNanoTime - startNanoTime) * nanoToSeconds;

                // Calculate opacity based on elapsed time
                double opacity = 1 - (elapsedTime / fadeDurationSeconds);

                if (opacity <= 0) {
                    stop();
                   paintEmpty();
                }else {
                    var gc = getGraphicsContext2D();
                    // Interpolate the color to gradually become transparent
                    Color fadedColor = originalColor.interpolate(Color.TRANSPARENT, 1 - opacity);

                    // Set the fill to the faded color and fill the rectangle
                    gc.setFill(fadedColor);
                    gc.fillRect(0, 0, width, height);

                }
            }

        };
        timer.start();

    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     *
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}
