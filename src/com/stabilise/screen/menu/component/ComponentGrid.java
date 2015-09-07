//package com.stabilise.screen.menu.component;
//
///**
// * A ComponentGrid manages a two-dimensional array of MenuItems such that said
// * items may be navigated using the keyboard.
// * 
// * <p>A MenuItem's coordinates are treated as its location in the backing 2D
// * array {@code grid} as such: {@code grid[row][col]}, or, in terms of
// * Cartesian coordinates, {@code grid[y][x]}.
// * 
// * <p>For the purpose of the {@code scroll} family of methods such as {@link
// * #scrollUp()}, the (0,0) is the top-left, and the y-axis extends positively
// * downwards.
// */
//public class ComponentGrid {
//    
//    /** Whether or not the grid wraps around unto itself. */
//    public boolean wrap = false;
//    
//    /** The two-dimensional array in which to store the components. */
//    private MenuItem[][] grid;
//    
//    /** The column the currently 'selected' component is in*/
//    private int col;    // x
//    /** The row the currently 'selected' component is in */
//    private int row;    // y
//    
//    
//    /**
//     * Creates a new ComponentGrid instance.
//     * 
//     * @param grid The grid of MenuItems to be manipulated.
//     * 
//     * @throws NullPointerException if {@code grid} or any of its top-level
//     * elements (rows) are {@code null}.
//     * @throws IllegalArgumentException if {@code grid} or any of its top-level
//     * elements (rows) are empty, or if any row contains only {@code null}
//     * elements.
//     */
//    public ComponentGrid(MenuItem[][] grid) {
//        this(grid, 0, 0);
//    }
//    
//    /**
//     * Creates a new ComponentGrid instance.
//     * 
//     * @param grid The grid of MenuItems to be manipulated.
//     * @param col The column of the initially selected item - effectively the 
//     * x-coordinate.
//     * @param row The row of the initially selected item - effectively the
//     * y-coordinate.
//     * 
//     * @throws NullPointerException if {@code grid} or any of its top-level
//     * elements (rows) are {@code null}.
//     * @throws IllegalArgumentException if {@code grid} or any of its top-level
//     * elements (rows) are empty, or if any row contains only {@code null}
//     * elements.
//     */
//    public ComponentGrid(MenuItem[][] grid, int col, int row) {
//        if(grid == null)
//            throw new NullPointerException("grid is null");
//        if(grid.length == 0)
//            throw new IllegalArgumentException("grid is empty");
//        for(int i = 0; i < grid.length; i++) {
//            if(grid[i] == null)
//                throw new NullPointerException("null grid row (grid[" + i + "])");
//            if(grid[i].length == 0)
//                throw new IllegalArgumentException("Empty grid row (grid[" + i + "])");
//            
//            // Confirm the row isn't completely empty
//            boolean hasElement = false;
//            for(int j = 0; j < grid[i].length; j++) {
//                if(grid[i][j] != null) {
//                    hasElement = true;
//                    break;
//                }
//            }
//            if(!hasElement)
//                throw new IllegalArgumentException("Empty grid row (all elements of grid[" + i + "] are null)");
//        }
//        
//        this.grid = grid;
//        
//        this.col = col - 1; // The -1 counteracts the +1 of scroll()
//        this.row = row;
//        
//        // This is to make sure an invalid initial location hasn't been given.
//        // If one was, scroll() will find the first valid one.
//        scroll();
//    }
//    
//    /**
//     * Sets the currently-selected item as the item at the given coordinates.
//     * If the given coordinates are illegal or there exists no enabled item at
//     * those coordinates, it will not be set as the selected component.
//     * 
//     * @param col The column of the item to select (i.e. its x-coordinate).
//     * @param row The row of the item to select (i.e. its y-coordinate).
//     * 
//     * @return {@code true} if there is an item at the defined coordinates.
//     */
//    public boolean setSelectedComponent(int col, int row) {
//        if(row < 0 || row >= grid.length)
//            return false;
//        if(col < 0 || col >= grid[row].length)
//            return false;
//        if(grid[row][col] == null || !grid[row][col].enabled)
//            return false;
//        
//        this.row = row;
//        this.col = col;
//        
//        return true;
//    }
//    
//    /**
//     * Sets the currently-selected item as the defined item, provided the grid
//     * contains the item.
//     * 
//     * @param item The item to set as the currently-selected one.
//     * 
//     * @return {@code true} if the grid contains the defined item - that is,
//     * the setting was a success.
//     */
//    public boolean setSelectedComponent(MenuItem item) {
//        if(grid[row][col] == item)
//            return true;
//        
//        for(int r = 0; r < grid.length; r++) {
//            for(int c = 0; c < grid[r].length; c++) {
//                if(grid[r][c] == item) {
//                    row = r;
//                    col = c;
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//    
//    /**
//     * Gets the currently-selected component.
//     * 
//     * @return The currently-selected component.
//     */
//    public MenuItem getSelectedComponent() {
//        return grid[row][col];
//    }
//    
//    /**
//     * Scrolls to the component above the currently-selected one.
//     * 
//     * @return The newly-selected component.
//     */
//    public MenuItem scrollUp() {
//        if(wrap || (row > 0 && grid[row-1][col] != null)) {
//            while(true) {
//                row = (row - 1 + grid.length) % grid.length;
//                if(grid[row][col] != null && grid[row][col].enabled)
//                    break;
//            }
//        }
//        return grid[row][col];
//    }
//    
//    /**
//     * Scrolls to the component below the currently-selected one.
//     * 
//     * @return The newly-selected component.
//     */
//    public MenuItem scrollDown() {
//        if(wrap || (row < grid.length - 1 && grid[row+1][col] != null)) {
//            while(true) {
//                row = (row + 1) % grid.length;
//                if(grid[row][col] != null && grid[row][col].enabled) 
//                    break;
//            }
//        }
//        return grid[row][col];
//    }
//    
//    /**
//     * Scrolls to the component to the left of the currently-selected one.
//     * 
//     * @return The newly-selected component.
//     */
//    public MenuItem scrollLeft() {
//        if(wrap || (col > 0 && grid[row][col-1] != null)) {
//            while(true) {
//                col = (col - 1 + grid[row].length) % grid[row].length;
//                if(grid[row][col] != null && grid[row][col].enabled)
//                    break;
//            }
//        }
//        return grid[row][col];
//    }
//    
//    /**
//     * Scrolls to the component to the right of the currently-selected one.
//     * 
//     * @return The newly-selected component.
//     */
//    public MenuItem scrollRight() {
//        if(wrap || (col < grid[row].length - 1 && grid[row][col+1] != null)) {
//            while(true) {
//                col = (col + 1) % grid[row].length;
//                if(grid[row][col] != null && grid[row][col].enabled)
//                    break;
//            }
//        }
//        return grid[row][col];
//    }
//    
//    /**
//     * Scrolls to the next component in a left-to-right, top-to-bottom fashion.
//     * 
//     * @return The newly-selected component.
//     */
//    public MenuItem scroll() {
//        while(true) {
//            col++;
//            if(col >= grid[row].length) {
//                row++;
//                if(row >= grid.length)
//                    row = 0;
//                col = 0;
//            }
//            if(grid[row][col] != null && grid[row][col].enabled)
//                break;
//        }
//        return grid[row][col];
//    }
//    
//}
