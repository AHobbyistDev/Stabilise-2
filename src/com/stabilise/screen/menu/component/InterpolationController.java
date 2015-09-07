//package com.stabilise.screen.menu.component;
//
//import com.stabilise.util.maths.Interpolation;
//
///**
// * An InterpolationController manages and controls the interpolation of some
// * property or properties of a MenuItem.
// * 
// * <p>Tint and scale interpolation has yet to be implemented.
// */
//public class InterpolationController extends ItemController {
//    
//    /** The current positional interpolation. */
//    private PositionInterp positional = null;
//    
//    
//    /**
//     * Creates a new interpolation controller.
//     * 
//     * @param item The MenuItem to control.
//     */
//    public InterpolationController(MenuItem item) {
//        super(item);
//    }
//    
//    @Override
//    public void update() {
//        if(positional != null) {
//            if(positional.update())
//                positional = null;
//        }
//    }
//    
//    /**
//     * Interpolates the MenuItem from its current position to the given
//     * position. If the item is already being interpolated, the current
//     * interpolation will halt and be replaced by this one.
//     * 
//     * @param position The position to interpolate to.
//     * @param interpFunc The interpolation function to use.
//     * @param ticks The total number of ticks over which the interpolation is
//     * to elapse.
//     * 
//     * @throws NullPointerException if either {@code position} or {@code
//     * interpFunc} are {@code null}.
//     * @throws IllegalArgumentException if {@code ticks < 1}.
//     */
//    public void toPosition(Position position, Interpolation interpFunc, int ticks) {
//        if(position == null)
//            throw new NullPointerException("position is null!");
//        if(interpFunc == null)
//            throw new NullPointerException("interpFunc is null!");
//        if(ticks < 1)
//            throw new IllegalArgumentException("ticks must be > 0!");
//        
//        Position from = positional != null ? positional.getCurrentPosition() :
//            (item.getPosition() != null ? item.getPosition() : new Position(0f,0f,item.x,item.y));
//        
//        positional = new PositionInterp(from, position, interpFunc, ticks);
//    }
//    
//    /*
//    public void queueToPosition(Position position, Interpolation interpFunc, Interpolation.Type type, int ticks) {
//        // TODO
//    }
//    */
//    
//    //--------------------==========--------------------
//    //-------------=====Nested Classes=====-------------
//    //--------------------==========--------------------
//    
//    /**
//     * An interpolation.
//     */
//    private static abstract class Interp {
//        
//        /** The interpolation function to use. */
//        protected final Interpolation func;
//        /** The total number of ticks over which the interpolation is to 
//         * elapse. This is a float as to prevent the need for continuous
//         * conversions. */
//        protected final float maxTicks;
//        /** The number of elapsed ticks. */
//        protected int ticks;
//        
//        
//        /**
//         * Creates a new Interp.
//         * 
//         * @param func The interpolation function to use.
//         * @param ticks The total number of ticks over which the interpolation
//         * is to elapse.
//         */
//        protected Interp(Interpolation func, int ticks) {
//            this.func = func;
//            this.maxTicks = (float)ticks;
//            this.ticks = -1;
//        }
//        
//        /**
//         * Updates the interp.
//         * 
//         * @return {@code true} if the interp has completed; {@code false}
//         * otherwise.
//         */
//        protected final boolean update() {
//            ticks++;
//            
//            if(ticks == maxTicks) {
//                complete();
//                return true;
//            }
//            
//            interpolate();
//            return false;
//        }
//        
//        /**
//         * Performs the interpolative step.
//         */
//        protected abstract void interpolate();
//        
//        /**
//         * Completes the interpolation.
//         */
//        protected abstract void complete();
//    }
//    
//    /**
//     * A positional interpolation.
//     */
//    private class PositionInterp extends Interp {
//        
//        /** The position to move from. */
//        private final Position from;
//        /** The position to move to. */
//        private final Position to;
//        
//        
//        /**
//         * Creates a new positional interp.
//         * 
//         * @param from The position to interpolate from.
//         * @param to The position to interpolate to.
//         * @param func The interpolation function to use.
//         * @param ticks The total number of ticks over which the interpolation
//         * is to elapse.
//         */
//        private PositionInterp(Position from, Position to, Interpolation func, int ticks) {
//            super(func, ticks);
//            
//            this.from = from;
//            this.to = to;
//            
//            // Temporarily remove the item's position 
//            item.setPosition(null);
//        }
//        
//        @Override
//        protected void interpolate() {
//            float t = func.transform(ticks/maxTicks);
//            item.x = (int)Interpolation.lerp(from.getX(), to.getX(), t);
//            item.y = (int)Interpolation.lerp(from.getY(), to.getY(), t);
//        }
//        
//        /**
//         * Gets the current position defined by this interp.
//         * 
//         * @return The current position.
//         */
//        private Position getCurrentPosition() {
//            return new Position(from, to, func.transform(ticks/maxTicks));
//        }
//        
//        @Override
//        protected void complete() {
//            item.setPosition(to);
//        }
//        
//    }
//    
//}
