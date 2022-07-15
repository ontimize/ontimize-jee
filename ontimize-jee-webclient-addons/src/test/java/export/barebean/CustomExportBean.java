package export.barebean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class CustomExportBean {

    private String textField;

    private Integer integerField;

    private Boolean booleanField;

    private Float floatField;

    private Date dateField;

    private LocalDate localDateField;

    private LocalDate customLocalDateField;

    private Double moneyField;

    private Double customMoneyField;

    private Double percentField;

    public CustomExportBean() {
    }

    public CustomExportBean(
            final String textField,
            final Integer integerField,
            final Boolean booleanField,
            final Float floatField,
            final Date dateField,
            final LocalDate localDateField,
            final LocalDate customLocalDateField,
            final Double moneyField,
            final Double customMoneyField,
            final Double percentField) {
        this.setTextField(textField);
        this.setIntegerField(integerField);
        this.setBooleanField(booleanField);
        this.setFloatField(floatField);
        this.setDateField(dateField);
        this.setLocalDateField(localDateField);
        this.setCustomLocalDateField(customLocalDateField);
        this.setMoneyField(moneyField);
        this.setCustomMoneyField(customMoneyField);
        this.setPercentField(percentField);
    }

    public static CustomExportBean cloneBean(final CustomExportBean original) {
        final CustomExportBean p = new CustomExportBean();
        p.setTextField(original.getTextField());
        p.setIntegerField(original.getIntegerField());
        p.setBooleanField(original.isBooleanField());
        p.setFloatField(original.getFloatField());
        p.setDateField(original.getDateField());
        p.setLocalDateField(original.getLocalDateField());
        p.setCustomLocalDateField(original.getCustomLocalDateField());
        p.setMoneyField(original.getMoneyField());
        p.setCustomMoneyField(original.getCustomMoneyField());
        p.setPercentField(original.getPercentField());
        return p;
    }

    public static List<CustomExportBean> getData(final int amount) {
        final List<CustomExportBean> ret = new ArrayList<>();
        for (int n = 0; n < amount; n++) {
            ret.add(new CustomExportBean(
                    "text_" + n,
                    (int) (Math.random() * 10000),
                    Math.random() > 0.5,
                    (float) (Math.random() * 10000),
                    getRandomDate(),
                    getRandomLocalDate(),
                    getRandomLocalDate(),
                    (double) (Math.random() * 10000),
                    (double) (Math.random() * 10000),
                    (double) (Math.random() * 100)));
        }
        return ret;
    }

    private static Date getRandomDate() {
        final int year = 1900;
        final int dayOfYear = 123;
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
        return calendar.getTime();
    }

    private static LocalDate getRandomLocalDate() {
        final long randomDay = (int) LocalDate.of(1900, 1, 1).toEpochDay()
                + new Random().nextInt((int) LocalDate.of(2015, 1, 1).toEpochDay()
                        - (int) LocalDate.of(1900, 1, 1).toEpochDay());
        return LocalDate.ofEpochDay(randomDay);
    }

    @Override
    public String toString() {
        return "CustomExportBean{" +
                "textField=" + this.textField +
                ", integerField=" + this.integerField +
                ", booleanField=" + this.booleanField +
                ", floatField=" + this.floatField +
                ", dateField=" + this.dateField +
                ", localDateField=" + this.localDateField +
                ", customLocalDateField=" + this.customLocalDateField +
                ", moneyField=" + this.moneyField +
                ", customMoneyField=" + this.customMoneyField +
                ", percentField=" + this.percentField +
                '}';
    }

    public String getTextField() {
        return textField;
    }

    public void setTextField(String textField) {
        this.textField = textField;
    }

    public Integer getIntegerField() {
        return integerField;
    }

    public void setIntegerField(Integer integerField) {
        this.integerField = integerField;
    }

    public Boolean isBooleanField() {
        return booleanField;
    }

    public void setBooleanField(Boolean booleanField) {
        this.booleanField = booleanField;
    }

    public Float getFloatField() {
        return floatField;
    }

    public void setFloatField(Float floatField) {
        this.floatField = floatField;
    }

    public Date getDateField() {
        return dateField;
    }

    public void setDateField(Date dateField) {
        this.dateField = dateField;
    }

    public LocalDate getLocalDateField() {
        return localDateField;
    }

    public void setLocalDateField(LocalDate localDateField) {
        this.localDateField = localDateField;
    }

    public LocalDate getCustomLocalDateField() {
        return customLocalDateField;
    }

    public void setCustomLocalDateField(LocalDate customLocalDateField) {
        this.customLocalDateField = customLocalDateField;
    }

    public Double getMoneyField() {
        return moneyField;
    }

    public void setMoneyField(Double moneyField) {
        this.moneyField = moneyField;
    }

    public Double getCustomMoneyField() {
        return customMoneyField;
    }

    public void setCustomMoneyField(Double customMoneyField) {
        this.customMoneyField = customMoneyField;
    }

    public Double getPercentField() {
        return percentField;
    }

    public void setPercentField(Double percentField) {
        this.percentField = percentField;
    }
}
