package at.campus02;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity

public class Geschenk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //das ist das AUtoincrement
    private int geschenksId;
    private String geschenksEmpfaenger;
    private String geschenksIdee;

    public int getGeschenksId() {
        return geschenksId;
    }

    public void setGeschenksId(int geschenksId) {
        this.geschenksId = geschenksId;
    }

    public String getGeschenksEmpfaenger() {
        return geschenksEmpfaenger;
    }

    public void setGeschenksEmpfaenger(String name) {
        this.geschenksEmpfaenger = name;
    }

    public String getGeschenksIdee() {
        return geschenksIdee;
    }

    public void setGeschenksIdee(String geschenk) {
        this.geschenksIdee = geschenk;
    }

    @Override
    public String toString() {
        return "Geschenksverwaltung{" +
                "geschenksId=" + geschenksId +
                ", name='" + geschenksEmpfaenger + '\'' +
                ", geschenk='" + geschenksIdee + '\'' +
                '}';
    }
}
