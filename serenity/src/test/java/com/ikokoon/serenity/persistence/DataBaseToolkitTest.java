package com.ikokoon.serenity.persistence;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import java.io.File;

import org.junit.Test;

import com.ikokoon.serenity.ATest;
import com.ikokoon.serenity.model.Class;
import com.ikokoon.serenity.model.Method;
import com.ikokoon.toolkit.Toolkit;

public class DataBaseToolkitTest extends ATest {

	@Test
	public void copy() {
		File odbDataBaseFile = new File("./src/test/resources/serenity.odb");
		File ramDataBaseFile = new File("./src/test/resources/serenity.ram");
		File jarDataBaseFile = new File("./src/test/resources/jar.odb");
		File tagDataBaseFile = new File("./src/test/resources/tag.odb");
		Toolkit.deleteFile(odbDataBaseFile, 3);
		Toolkit.deleteFile(ramDataBaseFile, 3);
		IDataBase odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), null);
		DataBaseToolkit.clear(odbDataBase);
		odbDataBase.close();

		IDataBase sourceDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, jarDataBaseFile.getAbsolutePath(), null);
		odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), null);
		IDataBase ramDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, ramDataBaseFile.getAbsolutePath(), odbDataBase);

		Class<?, ?> klass = ramDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.Search"));
		assertNull(klass);
		klass = ramDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.tag.SpellingTag"));
		assertNull(klass);

		DataBaseToolkit.copyDataBase(sourceDataBase, ramDataBase);

		sourceDataBase.close();
		// odbDataBase.close();
		ramDataBase.close();

		sourceDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, tagDataBaseFile.getAbsolutePath(), null);
		odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), null);
		ramDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseRam.class, ramDataBaseFile.getAbsolutePath(), odbDataBase);

		klass = odbDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.Search"));
		assertNotNull(klass);
		klass = odbDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.tag.SpellingTag"));
		assertNull(klass);

		DataBaseToolkit.copyDataBase(sourceDataBase, ramDataBase);

		sourceDataBase.close();
		// odbDataBase.close();
		ramDataBase.close();

		odbDataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, odbDataBaseFile.getAbsolutePath(), null);

		klass = odbDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.Search"));
		assertNotNull(klass);
		klass = odbDataBase.find(Class.class, Toolkit.hash("com.ikokoon.search.tag.SpellingTag"));
		assertNotNull(klass);

		odbDataBase.close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void clear() {
		IDataBase dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, "./serenity/dummy/database.odb", null);
		Class<?, ?> klass = new Class<Package, Method>();
		klass.setId(Long.MAX_VALUE);
		dataBase.persist(klass);
		klass = dataBase.find(Class.class, klass.getId());
		assertNotNull(klass);

		DataBaseToolkit.clear(dataBase);
		dataBase.close();

		dataBase = IDataBase.DataBaseManager.getDataBase(DataBaseOdb.class, "./serenity/dummy/database.odb", null);

		klass = dataBase.find(Class.class, klass.getId());
		assertNull(klass);

		dataBase.close();
	}

}
