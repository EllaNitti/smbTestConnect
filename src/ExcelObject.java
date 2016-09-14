import java.util.List; 

public class ExcelObject { 

private List<ExcelRow> excelObject; 

public ExcelObject(final List<ExcelRow> excelObject2) { 
		super(); 
		this.excelObject=excelObject2; 
} 

public List<ExcelRow> getExcelObject() { 
		return excelObject; 
} 

public void setExcelObject(List<ExcelRow> excelObject) { 
		this.excelObject = excelObject; 
} 
} 
