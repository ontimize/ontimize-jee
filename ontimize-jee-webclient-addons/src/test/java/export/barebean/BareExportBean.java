package export.barebean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;


interface Tree {

    List<BareExportBean> getChildren();

}

public class BareExportBean implements Tree {

    private  Integer id;

    private  String firstName;

    private  String lastName;

    private  String gender;

    private  Integer age;

    private  Boolean single;

    private  Date birthDate;

    private  LocalDate localBirthDate;

    private  Integer percentage;

    private  Double doublePercentage;

    private  Double money;

    private  String street;

    private  Integer zipCode;

    private  String city;

    private  String country;

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

    public static List<BareExportBean> getBareExportBeanList() {
        return getBareExportBeanList(20);
    }

    public static List<BareExportBean> getBareExportBeanList(final int size) {
        final List<BareExportBean> ret = new ArrayList<>();
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

    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getGender() {
        return gender;
    }

    public Integer getAge() {
        return age;
    }

    public Boolean isSingle() {
        return single;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public LocalDate getLocalBirthDate() {
        return localBirthDate;
    }

    public Integer getPercentage() {
        return percentage;
    }

    public Double getDoublePercentage() {
        return doublePercentage;
    }

    public Double getMoney() {
        return money;
    }

    public String getStreet() {
        return street;
    }

    public Integer getZipCode() {
        return zipCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setSingle(Boolean single) {
        this.single = single;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public void setLocalBirthDate(LocalDate localBirthDate) {
        this.localBirthDate = localBirthDate;
    }

    public void setPercentage(Integer percentage) {
        this.percentage = percentage;
    }

    public void setDoublePercentage(Double doublePercentage) {
        this.doublePercentage = doublePercentage;
    }

    public void setMoney(Double money) {
        this.money = money;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
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
