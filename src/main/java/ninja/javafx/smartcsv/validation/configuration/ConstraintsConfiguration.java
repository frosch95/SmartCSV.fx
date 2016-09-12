package ninja.javafx.smartcsv.validation.configuration;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by PC on 04.09.2016.
 */
public class ConstraintsConfiguration {
    private Boolean required;
    private Boolean unique;
    private Integer minLength;
    private Integer maxLength;

    private String pattern;

    @SerializedName("enum")
    private List<String> enumeration;

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getUnique() {
        return unique;
    }

    public void setUnique(Boolean unique) {
        this.unique = unique;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public List<String> getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

}
