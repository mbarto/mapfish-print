package org.mapfish.print.servlet;

import static org.junit.Assert.assertEquals;
import static org.mapfish.print.servlet.MapPrinterServlet.TempFile.cleanUpName;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mapfish.print.ThreadResources;
import org.mapfish.print.config.ConfigTest;
import org.mapfish.print.output.PdfOutputFactory;
import org.mapfish.print.servlet.MapPrinterServlet.TempFile;
import org.mapfish.print.utils.PJsonObject;
import org.mockito.Mockito;

public class MapPrinterServletTest {
    protected ThreadResources threadResources;
    protected MapPrinterServlet servlet;
    File tempDir;
    ServletConfig config;
   

    @Before
	public void setUp() throws ServletException, IOException {
        tempDir = File.createTempFile("mapfish", "print");
        tempDir.delete();
        tempDir.mkdirs();
        this.threadResources = new ThreadResources();
        this.threadResources.init();
        servlet = new MapPrinterServlet();
        config = Mockito.mock(ServletConfig.class);
        ServletContext context = Mockito.mock(ServletContext.class);
        Mockito.when(config.getServletContext()).thenReturn(context);
        Mockito.when(config.getInitParameter("tempdir")).thenReturn(tempDir.getAbsolutePath());
        servlet.init(config);
	}
	
    @After
    public void tearDown() {
        tempDir.delete();
    }
    
    @Test
    public void TempFileFormatFileNameTest() {
        Date date = new Date();

        String dateString = cleanUpName(DateFormat.getDateInstance().format(date));
        String dateTimeString = cleanUpName(DateFormat.getDateTimeInstance().format(date));
        String timeString = cleanUpName(DateFormat.getTimeInstance().format(date));
        String customPattern = "yy-MM-dd";
        String customPattern2 = "yy:MM:dd:mm:ss";
        String custom = new SimpleDateFormat(customPattern).format(date);
        String custom2 = new SimpleDateFormat(customPattern2).format(date);

        assertExpectedFormat(date, "|${else}|", "|${else}|", "");
        assertExpectedFormat(date, "|"+dateString+"|", "|${date}|", "");
        assertExpectedFormat(date, "|"+dateTimeString+"|", "|${dateTime}|", "");
        assertExpectedFormat(date, "|"+timeString+"|", "|${time}|", "");
        assertExpectedFormat(date, "|"+custom+"|", "|${"+customPattern+"}|", "");
        assertExpectedFormat(date, "|"+custom2+"|", "|${"+customPattern2+"}|", "");
        assertExpectedFormat(date, "|"+timeString+"|"+dateString, "|${time}|${date}", "");
        assertExpectedFormat(date, "|"+timeString+"|"+dateTimeString, "|${time}|${dateTime}", "");
        assertExpectedFormat(date, "|"+timeString+"|"+dateTimeString+"|"+timeString, "|${time}|${dateTime}|${time}", "");
        assertExpectedFormat(date, "|"+custom+"|"+custom2+"|"+timeString, "|${"+customPattern+"}|${"+customPattern2+"}|${time}", "");
    }

    @Test
    public void addSuffixTest() {
        Date date = new Date();

        assertExpectedFormat(date, "filename.pdf", "filename", "pdf");
        assertExpectedFormat(date, "filename.pdf", "filename", ".pdf");
        assertExpectedFormat(date, "filename.pdf", "filename.pdf", ".pdf");
        assertExpectedFormat(date, "filename.tif.pdf", "filename.tif", ".pdf");
    }
    

    @Test
    public void testInlineDownload() throws Exception {
    	final File file = ConfigTest.getSampleConfigFiles().get("inlineDownload.yaml");
    	Mockito.when(config.getInitParameter("config")).thenReturn(file.getAbsolutePath());
        File outputFile = new File(getClass().getResource("/pdf/sample.pdf").getFile());
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ServletOutputStream sos = Mockito.mock(ServletOutputStream.class);
        Mockito.when(response.getOutputStream()).thenReturn(sos);
        servlet.sendPdfFile(response, new TempFile(outputFile, new PJsonObject(
                new JSONObject(), ""), new PdfOutputFactory().create("pdf")),
                false);
        
        Mockito.verify(response,Mockito.never()).setHeader(Mockito.eq("Content-disposition"), Mockito.anyString());
    }

    private void assertExpectedFormat(Date date, String expected, String fileName, String suffix) {
        assertEquals(expected, MapPrinterServlet.TempFile.formatFileName(suffix, fileName, date));
    }

}
