/* 
 * To change this template, choose Tools | Templates 
 * and open the template in the editor. 
 */ 
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream; 

/*
  	To enable JMX
  	-Dcom.sun.management.jmxremote=true 
	-Dcom.sun.management.jmxremote.port=3614 
	-Dcom.sun.management.jmxremote.authenticate=false 
	-Dcom.sun.management.jmxremote.ssl=false
 */

public class SmbTestConnect { 

	private static final String DOMAIN_SERVER = null; 
	private static String USER_NAME = "GFT\\AOCS";
	private static String PASSWORD = "";
	private static final String INTERNAL_SOURCE_PATH = "smb://NBVAL689/sambaShare/";
	//private static final String INTERNAL_SOURCE_PATH = "smb://dbg.ads.db.com/lon-gtocoo-g/Investment Banking Operations/DBPACE-DEV-DAPFILE/INT/priceSource/InternalSource/"; 
	//private static final String EXTERNAL_SOURCE_PATH = "smb://dbg.ads.db.com/lon-gtocoo-g/Investment Banking Operations/DBPACE-DEV-DAPFILE/INT/priceSource/ExternalSource/"; 
	//private static final String FILE_NAME = "test.xlsx";
	private static final String FILTER_WILDCARD = "2016-03-31*";
	//private static final String[] FILE_HEADER = { "Project","Description","Activity Type","Task","Date","Hours","Person","Status","Appr Date","Remarks"};
	//private static final String[] EXTERNAL_SOURCE_PATH_HEADER = { "cob", "sym", "sym_type", "source", "cleanBid", "cleanAsk", "cleanExtMid", 
	//		"dirtyBid", "dirtyAsk", "dirtyExtMid", "spreadBid", "spreadAsk", "spreadExtMid", "weightedAverageLife", 
	//		"accruedInterest", "bvalScore", "priceProvider", "evalOrContrib", "numContrib", "quotedOrTrader", 
	//"lastTrade" }; 
	private static final String[] INTERNAL_SOURCE_PATH_HEADER = { "source", "cob", "sym", "sym_type", "cleanBid", "cleanAsk", "dirtyBid", 
			"dirtyAsk", "dirtyExtMid", "spreadBid", "spreadAsk", "spreadMid", "weightedAverageLife", "notional", "book", 
			"location", "cleanExtMid" };
	
	private static int totalFiles = 0;
	private static int totalRows = 0;
	private static long startTime = 0; 
	private static long stopTime = 0; 
	
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SmbTestConnect.class); 

	public static void main(String... args) {
		//USER_NAME = (String)args[0];
		//PASSWORD = (String)args[1];

		startTime = System.currentTimeMillis(); 
		
		readFiles(INTERNAL_SOURCE_PATH);
		
		stopTime = System.currentTimeMillis();
		long millis = stopTime - startTime;
		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days); 
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes); 
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
		
		System.out.println (""); 
		System.out.println ("Results:"); 
		System.out.println ("*****************************************************************"); 
		System.out.println (new Timestamp((new java.util.Date()).getTime()) + " - Total files read: " + totalFiles);
		System.out.println (new Timestamp((new java.util.Date()).getTime()) + " - Total rows read : " + totalRows);
		System.out.println (new Timestamp((new java.util.Date()).getTime()) + " - Elapsed time: " + (stopTime - startTime) + " ms - (" + hours + " hrs " + minutes + " min " + seconds + " secs)");
		System.out.println ("*****************************************************************"); 
	} 

	public static void readFiles(final String SOURCE_PATH) { 
		try { 
			NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(DOMAIN_SERVER,USER_NAME,PASSWORD); //This class stores and encrypts NTLM user credentials. 

			SmbFile sFile = new SmbFile(SOURCE_PATH,auth);

			System.out.println ("");
			System.out.println (new Timestamp((new java.util.Date()).getTime()) + " - Listing files from " + SOURCE_PATH + " Filtered by " + FILTER_WILDCARD); 
			SmbFile[] fileArray = sFile.listFiles(FILTER_WILDCARD);

			for (int i=0; i < fileArray.length; i++) {
				totalFiles++;
				System.out.println (new Timestamp((new java.util.Date()).getTime()) + " - File: " + fileArray[i].getName());
				//printFileContent (SOURCE_PATH+fileArray[i].getName(), auth);
				sFile = new SmbFile(SOURCE_PATH+fileArray[i].getName(), auth); //This class represents a resource on an SMB network. 
				readExcel(sFile,INTERNAL_SOURCE_PATH_HEADER);
			}
		} catch (Exception e) { 
			System.out.println ("Exception: " + e.getClass() + " - " + e.getMessage()); 
		} 
	} 


	private static List<ExcelRow> readExcel(final SmbFile sambaFile, final String[] header) { 

		System.out.println(new Timestamp((new java.util.Date()).getTime()) + " -   Reading file: " + sambaFile.getName() + " ...");

		final List<ExcelRow> excelObject = new ArrayList<ExcelRow>();

		SmbFileInputStream file = null;
		XSSFWorkbook workbook = null;
		try {
			file = new SmbFileInputStream(sambaFile);
			workbook = new XSSFWorkbook(file);
			
			if (workbook != null) {

				final XSSFSheet sheet = workbook.getSheetAt(0);
				
				totalRows += sheet.getLastRowNum();
				
				System.out.println (new Timestamp((new java.util.Date()).getTime()) + " -   Number of rows: " + sheet.getLastRowNum());

				for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
					
					//System.out.println ("-> Reading excel row: "+i);

					final Row row = sheet.getRow(i);
					final List<ExcelCell> excelRow = new ArrayList<ExcelCell>();

					final Iterator<Cell> cellIterator = row.cellIterator();
					
					int columnNum=0;
					while (cellIterator.hasNext()) {

						final Cell cell = cellIterator.next();
						final ExcelCell excelCell = new ExcelCell();

						excelCell.setColumnNames(parseColumnName(cell, header));

						switch (cell.getCellType()) { 
						case (Cell.CELL_TYPE_BLANK): 
							excelCell.setValue(Cell.CELL_TYPE_BLANK);
						break; 
						case (Cell.CELL_TYPE_STRING): 
							excelCell.setValue(cell.getStringCellValue()); 
						break; 
						case (Cell.CELL_TYPE_BOOLEAN): 
							excelCell.setValue(cell.getBooleanCellValue()); 
						break; 
						case (Cell.CELL_TYPE_NUMERIC): 
							if (HSSFDateUtil.isCellDateFormatted(cell)) { 
								try { 
									excelCell.setValue(cell.getDateCellValue()); 
								} catch (final IllegalStateException e) {
									System.out.println("Type Mismatch: " + e.getMessage());
								}
							} else {
								try {
									excelCell.setValue(cell.getNumericCellValue());
								} catch (final IllegalStateException e) {
									System.out.println("Type Mismatch: " + e.getMessage());
								}
							}
						break;
						default:
							System.out.println("The cell ["+i+","+columnNum+"] is corrupted or has invalid type (not String, Date or Numeric value)"); 
							break; 

						}

						//System.out.println ("cell["+i+","+columnNum+"]:"+excelCell.getValue());
						excelRow.add(excelCell);
						columnNum++;
					}
					excelObject.add(new ExcelRow(excelRow));
				}
				System.out.println (new Timestamp((new java.util.Date()).getTime()) + " -   Reading file: " + sambaFile.getName() + " done");
			}
			
		} catch (final Exception e) {
			System.out.println(new Timestamp((new java.util.Date()).getTime()) + " -   Exception: The file " + sambaFile.getName() + " is corrupted or invalid Excel file");
		} finally {
			try {
				System.out.println (new Timestamp((new java.util.Date()).getTime()) + " -   Closing file: " + sambaFile.getName() + " ...");
				file.close();
				System.out.println (new Timestamp((new java.util.Date()).getTime()) + " -   Closing file: " + sambaFile.getName() + " done");
			} catch (Exception e) {
				System.out.println(new Timestamp((new java.util.Date()).getTime()) + " -   Exception: Unable to close samba file " + sambaFile.getName());
			}
		}

		return excelObject;

	}

	private static String parseColumnName(final Cell cell, final String[] header) {
		final Integer index = cell.getColumnIndex();
		return header[index];
	}
	
	/*	
	private static void printFileContent (final String filePath, final NtlmPasswordAuthentication authentication) { 
		StringBuilder builder = null; 
		try { 
			SmbFile sFile = new SmbFile(filePath , authentication); //This class represents a resource on an SMB network. 

			builder = new StringBuilder(); 
			builder = readFileContent(sFile, builder); 

			log.info("========================== display all .txt info  here =============="); 
			log.info(builder.toString()); 
			log.info("========================== End  here ================================"); 

		} catch (Exception e) { 
			System.out.println ("Exception: " + e.getClass() + " - " + e.getMessage()); 
		} 
	} 

	private static StringBuilder readFileContent(SmbFile sFile, StringBuilder builder) { 
		BufferedReader reader = null; 
		try { 
			System.out.println ("Reading file: " + sFile.toString()); 
			reader = new BufferedReader(new InputStreamReader(new SmbFileInputStream(sFile))); 
		} catch (SmbException ex) { 
			System.out.println (ex.getClass() + " - " + ex.getMessage()); 
		} catch (MalformedURLException ex) { 
			System.out.println (ex.getClass() + " - " + ex.getMessage()); 
		} catch (UnknownHostException ex) { 
			System.out.println ("Exception: " + ex.getClass() + " - " + ex.getMessage()); 
		} 

		String lineReader = null; 
		try { 
			while ((lineReader = reader.readLine()) != null) { 
				System.out.println (" -> Line: " + lineReader); 
				builder.append(lineReader).append("\n"); 
			} 
		} catch (IOException e) { 
			System.out.println ("Exception: " + e.getClass() + " - " + e.getMessage()); 
		} finally { 
			try { 
				reader.close(); 
			} catch (IOException e) {  
				System.out.println ("Exception: " + e.getClass() + " - " + e.getMessage());
			} 
		} 
		return builder; 
	} 
*/
	
	
}
