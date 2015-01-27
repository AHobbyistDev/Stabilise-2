package com.stabilise.opengl;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/**
 * A TextureSheet allows for a Texture to be easily treated as a grid of
 * constituent TextureRegions, or "cells".
 */
public class TextureSheet implements Disposable {
	
	/** The base texture from which the cells of this TextureSheet are taken. */
	public final Texture texture;
	/** The number of columns and rows in this TextureSheet. */
	public final int cols, rows;
	
	/** The dimensions of each cell, in pixels. */
	protected final int cellWidth, cellHeight;
	
	
	/**
	 * Creates a new TextureSheet.
	 * 
	 * @param texture The texture upon which to construct this sheet.
	 * @param cols The number of columns in this sheet.
	 * @param rows The number of rows in this sheet.
	 * 
	 * @throws NullPointerException if {@code texture} is {@code null}.
	 * @throws IllegalArgumentException if either {@code cols} or {@code rows}
	 * are {@code null}.
	 */
	public TextureSheet(Texture texture, int cols, int rows) {
		if(texture == null)
			throw new NullPointerException();
		if(cols < 1 || rows < 1)
			throw new IllegalArgumentException();
		
		this.texture = texture;
		this.cols = cols;
		this.rows = rows;
		
		cellWidth = texture.getWidth() / cols;
		cellHeight = texture.getHeight() / rows;
	}
	
	/**
	 * Gets the i<sup><font size=-1>th</font></sup> cell of this TextureSheet;
	 * equivalent to {@link #setRegion(int, int)
	 * setRegion(i % rows, i / cols)}.
	 * 
	 * <p>{@code i} should remain within the range {@code 0 <= i < cols*rows}.
	 */
	public TextureRegion getRegion(int i) {
		return getRegion(i % rows, i/cols);
	}
	
	/**
	 * Gets the cell at (x,y) of this TextureSheet.
	 * 
	 * <p>{@code x} and {@code y} should remain within the ranges {@code 0 <= x
	 * < }{@link #cols}; {@code 0 <= y < }{@link #rows}.
	 */
	public TextureRegion getRegion(int x, int y) {
		return new TextureRegion(
				texture,
				cellWidth * x,
				cellHeight * y,
				cellWidth,
				cellHeight
		);
	}
	
	@Override
	public void dispose() {
		texture.dispose();
	}
	
	//--------------------==========--------------------
	//------------=====Static Functions=====------------
	//--------------------==========--------------------
	
	/**
	 * Creates and returns a TextureSheet for which the {@link #getRegion(int)}
	 * method is very optimised (note this comes at a memory usage cost as the
	 * TextureRegion for each cell is created and stored immediately). Usage of
	 * this static factory is most suitable for when that method is what will
	 * be used most often on the returned TextureSheet.
	 * 
	 * @param texture The texture upon which to construct this sheet.
	 * @param cols The number of columns in this sheet.
	 * @param rows The number of rows in this sheet.
	 * 
	 * @throws NullPointerException if {@code texture} is {@code null}.
	 * @throws IllegalArgumentException if either {@code cols} or {@code rows}
	 * are {@code null}.
	 */
	public static TextureSheet sequentiallyOptimised(Texture texture, int cols, int rows) {
		return new TextureSheetSequential(texture, cols, rows);
	}
	
	/**
	 * Creates and returns a TextureSheet for which the {@link
	 * #getRegion(int, int)} method is very optimised (note this comes at a
	 * memory usage cost as the TextureRegion for each cell is created and
	 * stored immediately). Usage of this static factory is most suitable for
	 * when that method is what will be used most often on the returned
	 * TextureSheet.
	 * 
	 * @param texture The texture upon which to construct this sheet.
	 * @param cols The number of columns in this sheet.
	 * @param rows The number of rows in this sheet.
	 * 
	 * @throws NullPointerException if {@code texture} is {@code null}.
	 * @throws IllegalArgumentException if either {@code cols} or {@code rows}
	 * are {@code null}.
	 */
	public static TextureSheet twoDimensionallyOptimised(Texture texture, int cols, int rows) {
		return new TextureSheetMultidimensional(texture, cols, rows);
	}
	
	//--------------------==========--------------------
	//-------------=====Nested Classes=====-------------
	//--------------------==========--------------------
	
	private static class TextureSheetSequential extends TextureSheet {
		
		private final TextureRegion[] regions;
		
		private TextureSheetSequential(Texture texture, int cols, int rows) {
			super(texture, cols, rows);
			regions = new TextureRegion[cols*rows];
			for(int i = 0; i < regions.length; i++)
				regions[i] = new TextureRegion(
						texture,
						cellWidth * (i % rows),
						cellHeight * (i / cols),
						cellWidth,
						cellHeight
				);
		}
		
		@Override
		public TextureRegion getRegion(int i) {
			return regions[i];
		}
		
		@Override
		public TextureRegion getRegion(int x, int y) {
			return getRegion(x * cols + y);
		}
		
	}
	
	private static class TextureSheetMultidimensional extends TextureSheet {
		
		private final TextureRegion[][] regions; // indexed as [y][x]
		
		private TextureSheetMultidimensional(Texture texture, int cols, int rows) {
			super(texture, cols, rows);
			regions = new TextureRegion[rows][cols];
			for(int y = 0; y < rows; y++)
				for(int x = 0; x < cols; x++)
					regions[y][x] = new TextureRegion(
							texture,
							cellWidth * x,
							cellHeight * y,
							cellWidth,
							cellHeight
					);
		}
		
		@Override
		public TextureRegion getRegion(int x, int y) {
			return regions[x][y];
		}
		
	}
	
}
