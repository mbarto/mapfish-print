package org.mapfish.print;

import java.io.File;
import java.io.IOException;

import org.mapfish.print.config.layout.MapBlock;
import org.mapfish.print.utils.PJsonObject;
import org.pvalsecc.misc.FileUtilities;

import com.lowagie.text.DocumentException;

public class MapBlockTest extends PdfTestCase {

	public MapBlockTest(String name) {
		super(name);		
	}

	public void testMultipleMaps() throws IOException, DocumentException {
		PJsonObject mapsSpec = MapPrinter.parseSpec(FileUtilities.readWholeTextFile(new File(MapBlockTest.class.getClassLoader()
                .getResource("config/multiple-maps.json").getFile())));;
                
        MapBlock mapBlock = new MapBlock();
        mapBlock.setName("main");
        mapBlock.setWidth("100");
        mapBlock.setHeight("100");
        mapBlock.setAbsoluteX("100");
        mapBlock.setAbsoluteY("100");
        mapBlock.render(mapsSpec, null, context);
                System.out.println(mapsSpec);
	}
	
}
