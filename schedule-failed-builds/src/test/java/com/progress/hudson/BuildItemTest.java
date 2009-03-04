package com.progress.hudson;

import static org.junit.Assert.*;

import org.junit.Test;

public class BuildItemTest {

	@Test
	public void readyForBuild_rebuildImmediatelyOneTime_shouldRebuildOnceAndThenStop() {
		//Configure buildItem to rebuild immediately, one time.
		BuildItem buildItem = new BuildItem(null,0,1);		
		
		//First time around, the buildItem should rebuild
		assertTrue(buildItem.readyForBuild());
		
		//Second time around, it should not rebuild
		assertFalse(buildItem.readyForBuild());		
	}

}
