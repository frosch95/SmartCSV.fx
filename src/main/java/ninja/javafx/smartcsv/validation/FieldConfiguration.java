package ninja.javafx.smartcsv.validation;

import com.google.gson.annotations.SerializedName;

/**
 * @author abi
 */
public class FieldConfiguration {

    public enum Type {
        @SerializedName("string") STRING,
        @SerializedName("integer") INTEGER,
        @SerializedName("number") NUMBER,
        @SerializedName("date") DATE,
        @SerializedName("datetime") DATETIME,
        @SerializedName("time") TIME
// TODO: currently not supported
//        @SerializedName("object") OBJECT,
//        @SerializedName("array") ARRAY,
//        @SerializedName("duration") DURATION,
//        @SerializedName("geopoint") GEOPOINT,
//        @SerializedName("geojson") GEOJSON
    }

    private String name;
    private String title;
    private Type type;
    private String description;
    private String format;
    private Object missingValue;
    private ConstraintsConfiguration constraints;
    private String groovy;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Object getMissingValue() {
        return missingValue;
    }

    public void setMissingValue(Object missingValue) {
        this.missingValue = missingValue;
    }

    public ConstraintsConfiguration getConstraints() {
        return constraints;
    }

    public void setConstraints(ConstraintsConfiguration constraints) {
        this.constraints = constraints;
    }

    public String getGroovy() {
        return groovy;
    }

    public void setGroovy(String groovy) {
        this.groovy = groovy;
    }

}
