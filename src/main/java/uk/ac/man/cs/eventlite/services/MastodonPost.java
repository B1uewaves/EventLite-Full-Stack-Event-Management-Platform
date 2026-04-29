package uk.ac.man.cs.eventlite.services;

import java.time.ZonedDateTime;

public class MastodonPost {

    private String time;
    private String date;
    private String content;
    private String url;
	private ZonedDateTime rawDate;

    // getters + setters

    public String getTime() { 
    	return time; 
    	}
    public void setTime(String time) { 
    	this.time = time; 
    	}

    public String getDate() { 
    	return date; 
    	}
    public void setDate(String date) { 
    	this.date = date; 
    	}

    public String getContent() { 
    	return content; 
    	}
    public void setContent(String content) { 
    	this.content = content; 
    	}

    public String getUrl() { 
    	return url; 
    	}
    public void setUrl(String url) { 
    	this.url = url; 
    	}

	public ZonedDateTime getRawDate() { 
		return rawDate; // instead of using the string date we send over the ZonedDateTime object -jj
	}

	public void setRawDate(ZonedDateTime rawDate) { 
		this.rawDate = rawDate; 
	}
}
