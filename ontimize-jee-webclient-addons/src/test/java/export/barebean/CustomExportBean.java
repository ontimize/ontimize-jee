package export.barebean;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CustomExportBean {

    private final SimpleStringProperty textField = new SimpleStringProperty();

    private final IntegerProperty integerField = new SimpleIntegerProperty();

    private final BooleanProperty booleanField = new SimpleBooleanProperty();

    private final FloatProperty floatField = new SimpleFloatProperty();

    private final ObjectProperty<Date> dateField = new SimpleObjectProperty();

    private final ObjectProperty<LocalDate> localDateField = new SimpleObjectProperty();

    private final ObjectProperty<LocalDate> customLocalDateField = new SimpleObjectProperty();

    private final DoubleProperty moneyField = new SimpleDoubleProperty();

    private final DoubleProperty customMoneyField = new SimpleDoubleProperty();

    private final DoubleProperty percentField = new SimpleDoubleProperty();

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
            final Float moneyField,
            final Float customMoneyField,
            final Float percentField) {
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

    public static ObservableList<CustomExportBean> getData(final int amount) {
        final ObservableList<CustomExportBean> ret = FXCollections.observableArrayList();
        for (int n = 0; n < amount; n++) {
            ret.add(new CustomExportBean(
                    "text_" + n,
                    (int) (Math.random() * 10000),
                    Math.random() > 0.5,
                    (float) (Math.random() * 10000),
                    getRandomDate(),
                    getRandomLocalDate(),
                    getRandomLocalDate(),
                    (float) (Math.random() * 10000),
                    (float) (Math.random() * 10000),
                    (float) (Math.random() * 100)));
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
        return this.textField.get();
    }

    public void setTextField(final String textField) {
        this.textField.set(textField);
    }

    public SimpleStringProperty textFieldProperty() {
        return this.textField;
    }

    public int getIntegerField() {
        return this.integerField.get();
    }

    public void setIntegerField(final int integerField) {
        this.integerField.set(integerField);
    }

    public IntegerProperty integerFieldProperty() {
        return this.integerField;
    }

    public boolean isBooleanField() {
        return this.booleanField.get();
    }

    public void setBooleanField(final boolean booleanField) {
        this.booleanField.set(booleanField);
    }

    public BooleanProperty booleanFieldProperty() {
        return this.booleanField;
    }

    public float getFloatField() {
        return this.floatField.get();
    }

    public void setFloatField(final float floatField) {
        this.floatField.set(floatField);
    }

    public FloatProperty floatFieldProperty() {
        return this.floatField;
    }

    public Date getDateField() {
        return this.dateField.get();
    }

    public void setDateField(final Date dateField) {
        this.dateField.set(dateField);
    }

    public ObjectProperty<Date> dateFieldProperty() {
        return this.dateField;
    }

    public LocalDate getLocalDateField() {
        return this.localDateField.get();
    }

    public void setLocalDateField(final LocalDate localDateField) {
        this.localDateField.set(localDateField);
    }

    public ObjectProperty<LocalDate> localDateFieldProperty() {
        return this.localDateField;
    }

    public LocalDate getCustomLocalDateField() {
        return this.customLocalDateField.get();
    }

    public void setCustomLocalDateField(final LocalDate customLocalDateField) {
        this.customLocalDateField.set(customLocalDateField);
    }

    public ObjectProperty<LocalDate> customLocalDateFieldProperty() {
        return this.customLocalDateField;
    }

    public double getMoneyField() {
        return this.moneyField.get();
    }

    public void setMoneyField(final double moneyField) {
        this.moneyField.set(moneyField);
    }

    public DoubleProperty moneyFieldProperty() {
        return this.moneyField;
    }

    public double getCustomMoneyField() {
        return this.customMoneyField.get();
    }

    public void setCustomMoneyField(final double customMoneyField) {
        this.customMoneyField.set(customMoneyField);
    }

    public DoubleProperty customMoneyFieldProperty() {
        return this.customMoneyField;
    }

    public double getPercentField() {
        return this.percentField.get();
    }

    public void setPercentField(final double percentField) {
        this.percentField.set(percentField);
    }

    public void setPercentField(final float percentField) {
        this.percentField.set(percentField);
    }

    public DoubleProperty percentFieldProperty() {
        return this.percentField;
    }

}
