
package no.hiof.danielch.eater.model.places;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OpeningHours {

    @SerializedName("open_now")
    @Expose
    private Boolean openNow;
    @SerializedName("weekday_text")
    @Expose
    private List<String> weekdayText = null;

    public Boolean getOpenNow() {
        return openNow;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }

    public List<String> getWeekdayText() {
        return weekdayText;
    }

    public void setWeekdayText(List<String> weekdayText) {
        this.weekdayText = weekdayText;
    }

}

//Weekday text exsample
/*           "Monday: Closed",
             "Tuesday: 12:00 – 10:00 pm",
             "Wednesday: 12:00 – 10:00 pm",
             "Thursday: 12:00 – 10:00 pm",
             "Friday: 12:00 – 11:00 pm",
             "Saturday: 12:00 – 11:00 pm",
              "Sunday: 1:00 – 10:00 pm"*/