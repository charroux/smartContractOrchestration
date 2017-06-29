package orcha.lang.configuration

import java.text.SimpleDateFormat

class OrchaSession {
	
	long timestamp = Calendar.getInstance().getTimeInMillis()
	Date date = new Date(timestamp)
	
	String getDateByFormat(String format){
		SimpleDateFormat formatter = new SimpleDateFormat(format)
		return formatter.format(date)
	}

}
