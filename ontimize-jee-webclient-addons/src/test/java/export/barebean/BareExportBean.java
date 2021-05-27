package export.barebean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;


interface Tree {

    List<BareExportBean> getChildren();

}

public class BareExportBean implements Tree {

    private final IntegerProperty id = new SimpleIntegerProperty();

    private final StringProperty firstName = new SimpleStringProperty();

    private final StringProperty lastName = new SimpleStringProperty();

    private final StringProperty gender = new SimpleStringProperty();

    private final IntegerProperty age = new SimpleIntegerProperty();

    private final BooleanProperty single = new SimpleBooleanProperty();

    private final ObjectProperty<Date> birthDate = new SimpleObjectProperty();

    private final ObjectProperty<LocalDate> localBirthDate = new SimpleObjectProperty();

    private final IntegerProperty percentage = new SimpleIntegerProperty();

    private final DoubleProperty doublePercentage = new SimpleDoubleProperty();

    private final DoubleProperty money = new SimpleDoubleProperty();

    private final StringProperty street = new SimpleStringProperty();

    private final IntegerProperty zipCode = new SimpleIntegerProperty();

    private final StringProperty city = new SimpleStringProperty();

    private final StringProperty country = new SimpleStringProperty();

    /*
     * For trees
     */
    List<BareExportBean> children = new ArrayList<>();

    public BareExportBean() {
        // no-op
    }

    public BareExportBean(
            final Integer id,
            final String firstName,
            final String lastName,
            final String gender,
            final Integer age,
            final String street,
            final int zipCode,
            final String city,
            final String country,
            final Boolean single,
            final double money,
            final int percentage,
            final double doublePercentage,
            final Date birthDate,
            final LocalDate localBirthDate) {
        super();
        this.setId(id);
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setGender(gender);
        this.setAge(age);
        this.setStreet(street);
        this.setZipCode(zipCode);
        this.setCity(city);
        this.setCountry(country);
        this.setSingle(single);
        this.setMoney(money);
        this.setPercentage(percentage);
        this.setDoublePercentage(doublePercentage);
        this.setBirthDate(birthDate);
        this.setLocalBirthDate(localBirthDate);
    }

    public static ObservableList<BareExportBean> getBareExportBeanList() {
        return getBareExportBeanList(20);
    }

    public static ObservableList<BareExportBean> getBareExportBeanList(final int size) {
        final ObservableList<BareExportBean> ret = FXCollections.observableArrayList();
        for (int n = 0; n < size; n++) {
            final BareExportBean bareExportBean = new BareExportBean(
                    n,
                    "Name_" + n,
                    "LastName_" + n,
                    Math.random() > .5 ? "M" : "F",
                    (int) (Math.random() * 100),
                    "Adress_" + n,
                    (int) (Math.random() * 10000),
                    "City " + (int) (Math.random() * 1000),
                    "Country " + (int) (Math.random() * 1000),
                    Math.random() > .5,
                    Math.random() * 10000,
                    (int) (Math.random() * 100),
                    Math.random() * 100,
                    new Date(-946771200000L + (Math.abs((int) (Math.random() * 100000)) % (70L * 365 * 24 * 60 * 60
                            * 1000))),
                    LocalDate.ofEpochDay((long) ((int) LocalDate.of(1900, 1, 1).toEpochDay() + new Random().nextInt(
                            (int) LocalDate.of(2015, 1, 1).toEpochDay() - (int) LocalDate.of(1900, 1, 1)
                                .toEpochDay()))));
            // final ObservableList<BareExportBean> children = getBareExportBeanList();
            // bareExportBean.getChildren().addAll(children);
            ret.add(bareExportBean);
        }
        return ret;
    }

    public static BareExportBean cloneBareExportBean(final BareExportBean original) {
        final BareExportBean p = new BareExportBean();
        p.setAge(original.getAge());
        p.setCity(original.getCity());
        p.setCountry(original.getCountry());
        p.setFirstName(original.getFirstName());
        p.setGender(original.getGender());
        p.setId(original.getId());
        p.setLastName(original.getLastName());
        p.setSingle(original.isSingle());
        p.setStreet(original.getStreet());
        p.setZipCode(original.getZipCode());
        p.setMoney(original.getMoney());
        p.setPercentage(original.getPercentage());
        final List<BareExportBean> children = original.getChildren();
        if ((children != null) && (!children.isEmpty())) {
            for (final BareExportBean child : children) {
                p.addChild(cloneBareExportBean(child));
            }
        }
        return p;
    }

    public static TreeItem<BareExportBean> getBeanTree() {
        final ObservableList<BareExportBean> data = getBareExportBeanList();
        final TreeItem<BareExportBean> rootNode = new TreeItem<>();
        buildTree(rootNode, data);
        return rootNode;
    }

    public static TreeItem<BareExportBean> getHugeBeanTree() {
        return getHugeBeanTree(1000);
    }

    public static TreeItem<BareExportBean> getHugeBeanTree(final int size) {
        final ObservableList<BareExportBean> list = FXCollections.observableArrayList();
        final ObservableList<BareExportBean> personList = getBareExportBeanList();
        for (int i = 0; i < size; i++) {
            final int index = (int) Math.floor((Math.random() * 6));
            final BareExportBean person = personList.get(index);
            list.add(cloneBareExportBean(person));
        }
        final TreeItem<BareExportBean> rootNode = new TreeItem<>();
        buildTree(rootNode, list);
        return rootNode;
    }

    public static TreeItem<BareExportBean> cloneTreeBean(final TreeItem<BareExportBean> original) {
        final BareExportBean p = cloneBareExportBean(original.getValue());
        final TreeItem<BareExportBean> root = new TreeItem<>();
        final ObservableList<TreeItem<BareExportBean>> children = original.getChildren();
        if ((children != null) && (!children.isEmpty())) {
            for (final TreeItem<BareExportBean> child : children) {
                root.getChildren().add(cloneTreeBean(child));
            }
        }
        return root;
    }

    public static void buildTree(final TreeItem<BareExportBean> rootNode, final List<BareExportBean> bareExportBeans) {
        bareExportBeans.forEach(bareExportBean -> {
            final TreeItem<BareExportBean> childNode = new TreeItem<>(bareExportBean);
            childNode.setExpanded(true);
            rootNode.getChildren().add(childNode);
            if (!bareExportBean.getChildren().isEmpty()) {
                buildTree(childNode, bareExportBean.getChildren());
            }
        });
    }

    @Override
    public List<BareExportBean> getChildren() {
        return this.children;
    }

    public void setChildren(final List<BareExportBean> children) {
        this.children = children;
    }

    public void addChild(final BareExportBean child) {
        this.children.add(child);
    }

    /*
     * Properties
     */
    public IntegerProperty idProperty() {
        return this.id;
    }

    public int getId() {
        return this.idProperty().get();
    }

    public void setId(final int id) {
        this.idProperty().set(id);
    }

    public StringProperty firstNameProperty() {
        return this.firstName;
    }

    public String getFirstName() {
        return this.firstNameProperty().get();
    }

    public void setFirstName(final String firstName) {
        this.firstNameProperty().set(firstName);
    }

    public StringProperty lastNameProperty() {
        return this.lastName;
    }

    public String getLastName() {
        return this.lastNameProperty().get();
    }

    public void setLastName(final String lastName) {
        this.lastNameProperty().set(lastName);
    }

    public StringProperty streetProperty() {
        return this.street;
    }

    public String getStreet() {
        return this.streetProperty().get();
    }

    public void setStreet(final String street) {
        this.streetProperty().set(street);
    }

    public IntegerProperty zipCodeProperty() {
        return this.zipCode;
    }

    public int getZipCode() {
        return this.zipCodeProperty().get();
    }

    public void setZipCode(final int zipCode) {
        this.zipCodeProperty().set(zipCode);
    }

    public StringProperty cityProperty() {
        return this.city;
    }

    public String getCity() {
        return this.cityProperty().get();
    }

    public void setCity(final String city) {
        this.cityProperty().set(city);
    }

    public StringProperty countryProperty() {
        return this.country;
    }

    public String getCountry() {
        return this.countryProperty().get();
    }

    public void setCountry(final String country) {
        this.countryProperty().set(country);
    }

    public IntegerProperty ageProperty() {
        return this.age;
    }

    public int getAge() {
        return this.ageProperty().get();
    }

    public void setAge(final int age) {
        this.ageProperty().set(age);
    }

    public DoubleProperty moneyProperty() {
        return this.money;
    }

    public double getMoney() {
        return this.moneyProperty().get();
    }

    public void setMoney(final double money) {
        this.moneyProperty().set(money);
    }

    public Date getBirthDate() {
        return this.birthDate.get();
    }

    public void setBirthDate(final Date birthDate) {
        this.birthDate.set(birthDate);
    }

    public ObjectProperty<Date> birthDateProperty() {
        return this.birthDate;
    }

    public LocalDate getLocalBirthDate() {
        return this.localBirthDate.get();
    }

    public void setLocalBirthDate(final LocalDate localBirthDate) {
        this.localBirthDate.set(localBirthDate);
    }

    public ObjectProperty<LocalDate> localBirthDateProperty() {
        return this.localBirthDate;
    }

    public IntegerProperty percentageProperty() {
        return this.percentage;
    }

    public int getPercentage() {
        return this.percentageProperty().get();
    }

    public void setPercentage(final int percentage) {
        this.percentageProperty().set(percentage);
    }

    public DoubleProperty doublePercentageProperty() {
        return this.doublePercentage;
    }

    public double getDoublePercentage() {
        return this.doublePercentage.get();
    }

    public void setDoublePercentage(final double doublePercentage) {
        this.doublePercentageProperty().set(doublePercentage);
    }

    public BooleanProperty singleProperty() {
        return this.single;
    }

    public boolean isSingle() {
        return this.singleProperty().get();
    }

    public void setSingle(final boolean single) {
        this.singleProperty().set(single);
    }

    public StringProperty genderProperty() {
        return this.gender;
    }

    public String getGender() {
        return this.genderProperty().get();
    }

    public void setGender(final String gender) {
        this.genderProperty().set(gender);
    }

    @Override
    public String toString() {
        // return super.toString();
        return "Id: " + this.getId() + "\n"
                + "Name: " + this.getFirstName() + "\n"
                + "Surname: " + this.getLastName() + "\n"
                + "Gender: " + Gender.getByCode(this.getGender()) + "\n"
                + "Age: " + this.getAge() + "\n"
                + "City: " + this.getCity() + "\n"
                + "Country: " + this.getCountry() + "\n"
                + "Single?: " + (this.isSingle() ? "yes" : "no");
    }

    public enum Gender {

        FEMALE("F", "Female"), MALE("M", "Male");

        private final String code;

        private final String text;

        Gender(final String code, final String text) {
            this.code = code;
            this.text = text;
        }

        public static Gender getByCode(final String genderCode) {
            for (final Gender g : Gender.values()) {
                if (g.code.equals(genderCode)) {
                    return g;
                }
            }
            return null;
        }

        public String getCode() {
            return this.code;
        }

        public String getText() {
            return this.text;
        }

        @Override
        public String toString() {
            return this.text;
        }

    }

}
