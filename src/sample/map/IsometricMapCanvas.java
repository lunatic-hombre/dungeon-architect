package sample.map;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class IsometricMapCanvas implements MapCanvas {

    private static final double XSCALE = 2, YSCALE = 5, ROOM_HEIGHT = 3;
    private static final Color STROKE_COLOR = Color.BLACK;

    private final Canvas canvas;
    private final GraphicsContext graphics;
    private double gridSize;

    public IsometricMapCanvas(double width, double height, double gridSize) {
        this(new Canvas(width, height), gridSize);
    }

    public IsometricMapCanvas(Canvas canvas, double gridSize) {
        this.canvas = canvas;
        this.graphics = canvas.getGraphicsContext2D();
        this.gridSize = gridSize;
    }

    @Override
    public void drawRoom(RoomLocation location, Room room) {
        final double midX = canvas.getWidth()/2d,
                     midY = canvas.getHeight()/2d,
                     depth = room.getDepth()*gridSize,
                     length = room.getLength()*gridSize;
        // TODO room location

        double[] xpoints = new double[] {
                midX - length/XSCALE + depth/XSCALE,
                midX - length/XSCALE - depth/XSCALE,
                midX + length/XSCALE - depth/XSCALE,
                midX + length/XSCALE + depth/XSCALE,
        };
        double[] ypoints = new double[] {
                midY + length/YSCALE + depth/YSCALE,
                midY + length/YSCALE - depth/YSCALE,
                midY - length/YSCALE - depth/YSCALE,
                midY - length/YSCALE + depth/YSCALE,
        };

        graphics.setStroke(STROKE_COLOR);
        graphics.setFill(Color.rgb(220,220,220));
        //graphics.fillPolygon(xpoints, ypoints, 4);
        graphics.strokePolygon(xpoints, ypoints, 4);

        // vertical lines
        graphics.strokeLine(midX - length/XSCALE - depth/XSCALE, midY + length/YSCALE - depth/YSCALE,
                midX - length/XSCALE - depth/XSCALE, midY + length/YSCALE - depth/YSCALE - ROOM_HEIGHT*gridSize);
        graphics.strokeLine(midX + length/XSCALE - depth/XSCALE, midY - length/YSCALE - depth/YSCALE,
                midX + length/XSCALE - depth/XSCALE, midY - length/YSCALE - depth/YSCALE - ROOM_HEIGHT*gridSize);
        graphics.strokeLine(midX + length/XSCALE + depth/XSCALE, midY - length/YSCALE + depth/YSCALE,
                midX + length/XSCALE + depth/XSCALE, midY - length/YSCALE + depth/YSCALE - ROOM_HEIGHT*gridSize);

        // connect vertical lines
        graphics.strokeLine(midX - length/XSCALE - depth/XSCALE, midY + length/YSCALE - depth/YSCALE - ROOM_HEIGHT*gridSize,
                midX + length/XSCALE - depth/XSCALE, midY - length/YSCALE - depth/YSCALE - ROOM_HEIGHT*gridSize);
        graphics.strokeLine(midX + length/XSCALE - depth/XSCALE, midY - length/YSCALE - depth/YSCALE - ROOM_HEIGHT*gridSize,
                midX + length/XSCALE + depth/XSCALE, midY - length/YSCALE + depth/YSCALE - ROOM_HEIGHT*gridSize);

        graphics.setLineWidth(1);

        // north-south grid
        for (double l=gridSize; l < length; l+=gridSize) {
            graphics.strokeLine(
                    midX - (length-2*l)/XSCALE + depth/XSCALE, midY + (length-2*l)/YSCALE + depth/YSCALE,
                    midX - (length-2*l)/XSCALE - depth/XSCALE, midY + (length-2*l)/YSCALE - depth/YSCALE
            );
        }

        // west-east grid
        for (double d=gridSize; d < depth; d+=gridSize) {
            graphics.strokeLine(
                    midX - length/XSCALE - (depth-2*d)/XSCALE, midY + length/YSCALE - (depth-2*d)/YSCALE,
                    midX + length/XSCALE - (depth-2*d)/XSCALE, midY - length/YSCALE - (depth-2*d)/YSCALE
            );
        }

    }

    public Canvas getCanvas() {
        return canvas;
    }

}
