import java.util.List; 

public class ExcelRow { 

private List<ExcelCell> excelRow; 

public ExcelRow(final List<ExcelCell> excelRow2) { 
	super(); 
	this.excelRow=excelRow2; 
} 

public List<ExcelCell> getExcelRow() { 
	return excelRow; 
} 

public void setExcelRow(List<ExcelCell> excelRow) { 
	this.excelRow = excelRow; 
} 
} 