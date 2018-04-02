package com.stabilise.world.loader;

import com.stabilise.util.annotation.ThreadSafeMethod;
import com.stabilise.util.io.data.DataCompound;
import com.stabilise.world.Region;

/**
 * Does some region loading/saving. Is an interface so that, like world
 * generators, I can chuck on more if there's some additional stuff I'd like
 * to lump on when saving, without interfering with the bulk of the data.
 * 
 * Implementors should be careful not to have name collisions when saving
 * things or bad things will probably happen if different such conflicting
 * IRegionLoaders are operating.
 */
public interface IRegionLoader {
	
	/**
	 * Loads the region data into {@code r} from the given DataCompound.
	 * 
	 * @param r The region to load into.
	 * @param c The compound to read from.
	 * @param generated Whether the region has been generated. (This might
	 * affect what implementors attempt to read from the compound.) This is
	 * consistent between loads/saves -- i.e., if a region is saved with
	 * {@code generated} set to either true/false, it will have the same value
	 * when 
	 */
	@ThreadSafeMethod
	void load(Region r, DataCompound c, boolean generated);
	
	/**
	 * Saves the region data from {@code r} into the given DataCompound.
	 * 
	 * @param r The region to save.
	 * @param c The compound to write to.
	 * @param generated Whether the region has been generated. (This might
	 * affect what implementors attempt to write to the compound).
	 */
	@ThreadSafeMethod
	void save(Region r, DataCompound c, boolean generated);
	
}
