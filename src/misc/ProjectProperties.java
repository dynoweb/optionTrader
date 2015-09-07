package misc;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


public class ProjectProperties {

	// ProjectProperties.dateFormat.format(longOptionOpen.getExpiration())
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, YYYY ");
	public static SimpleDateFormat shortDateFormat = new SimpleDateFormat("MM-dd-YY ");
	public static SimpleDateFormat abriviatedDateFormat = new SimpleDateFormat("MM YY ");
	
	// ProjectProperties.df.format(54.680054);
	public static DecimalFormat df = new DecimalFormat("#.00");
	

}
