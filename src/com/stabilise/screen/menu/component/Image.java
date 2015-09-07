//package com.stabilise.screen.menu.component;
//
//import com.stabilise.screen.menu.Menu;
//
///**
// * The image class represents a graphical menu object.
// */
//public class Image extends MenuItem {
//    
//    /** The image's sprite. */
//    private Sprite sprite;
//    
//    /** The sprite's row and col, if the image is using a spritesheet. */
//    private int row, col;
//    
//    
//    /**
//     * Creates an image.
//     * 
//     * @param menu The menu containing this Image.
//     * @param x The x co-ordinate of the Image.
//     * @param y The y co-ordinate of the Image.
//     * @param sprite The image's sprite.
//     */
//    public Image(Menu menu, int x, int y, Sprite sprite) {
//        super(menu, x, y);
//        
//        this.sprite = sprite;
//        sprite.x = x;
//        sprite.y = y;
//        sprite.setTextureMarker(this);
//        
//        enabled = false;
//    }
//    
//    /**
//     * Creates an image.
//     * 
//     * @param menu The menu containing this Image.
//     * @param x The x-coordinate of the Image.
//     * @param y The y-coordinate of the Image.
//     * @param sprite The image's spritesheet containing the image's sprite.
//     * @param col The x position of the sprite within the spritesheet.
//     * @param row The y position of the sprite within the spritesheet.
//     */
//    public Image(Menu menu, int x, int y, SpriteSheet sprite, int col, int row) {
//        super(menu, x, y);
//        
//        this.sprite = sprite;
//        sprite.setTextureMarker(this);
//        
//        this.col = col;
//        this.row = row;
//        
//        enabled = false;
//    }
//    
//    @Override
//    public void update() {
//        super.update();
//        
//        //sprite.x = x;
//        //sprite.y = y;
//        
//        if(sprite instanceof Animation)
//            ((Animation)sprite).update();
//    }
//    
//    @Override
//    public void render() {
//        //if(sprite instanceof SpriteSheet && !(sprite instanceof Animation))
//        //    ((SpriteSheet)sprite).drawSprite(col, row, x, y);
//        //else
//        //    sprite.draw();
//        if(sprite instanceof SpriteSheet && !(sprite instanceof Animation))
//            ((SpriteSheet)sprite).drawSprite(col, row, x, y);
//        else {
//            sprite.x = x;
//            sprite.y = y;
//            sprite.draw();
//        }
//    }
//    
//    @Override
//    public boolean isMouseInBounds(int mouseX, int mouseY) {
//        return false;
//    }
//    
//    @Override
//    public void focus() {
//        throw new RuntimeException("Can't focus on an image!");
//    }
//    
//    @Override
//    public void unfocus() {
//        throw new RuntimeException("Can't focus on an image!");
//    }
//    
//    @Override
//    public boolean canBeFocusedFromHover() {
//        return false;
//    }
//    
//    @Override
//    public boolean canBeFocusedFromClick() {
//        return false;
//    }
//    
//    @Override
//    public void destroy() {
//        if(sprite != null)
//            sprite.destroy();
//    }
//    
//    /**
//     * Returns the image's sprite.
//     * 
//     * @return The image's sprite.
//     */
//    public Sprite getSprite() {
//        return sprite;
//    }
//    
//}
