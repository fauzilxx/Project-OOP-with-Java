package projectpbo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RoomBooking {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty patient_name = new SimpleStringProperty();
    private final StringProperty illness = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty room_name = new SimpleStringProperty(); // formatted "dd MMMM yyyy HH:mm"
    private final StringProperty room_type = new SimpleStringProperty();
    private final StringProperty check_in = new SimpleStringProperty();
    private final StringProperty check_out = new SimpleStringProperty();
    private final StringProperty days  = new SimpleStringProperty();
    private final StringProperty cost = new SimpleStringProperty();

    public RoomBooking (int id, String patient_name, String illness,String address, String room_name, String room_type, String check_in, String check_out, String days, String cost) {
        this.id.set(id);
        this.patient_name.set(patient_name);
        this.illness.set(illness);
        this.address.set(address);
        this.room_name.set(room_name);
        this.room_type.set(room_type);
        this.check_in.set(check_in);
        this.check_out.set(check_out);
        this.days.set(days);
        this.cost.set(cost);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public String getpatient_name() { return patient_name.get(); }
    public StringProperty get_patientProperty() { return patient_name; }
    public String getillness() { return illness.get(); }
    public StringProperty illnessProperty() { return illness; }
    public String getroom_name() { return room_name.get(); }
    public StringProperty room_nameProperty () { return room_name; }
    public String getroom_type() { return room_type.get(); }
    public StringProperty room_typeProperty() { return room_type; }
    public String getcheck_in() { return check_in.get(); }
    public StringProperty check_inProperty() { return check_in; }
    public String getcheck_out() { return check_out.get(); }
    public StringProperty check_outProperty() { return check_out; }
    public String getdays() { return days.get(); }
    public StringProperty daysProperty() { return days; }
    public String getcost() { return cost.get(); }
    public StringProperty costProperty() { return cost; }

    public boolean matches(String q) {
        if (q == null || q.isBlank()) return true;
        String all = (getId()+" "+getpatient_name()+" "+getillness()+" "+getroom_name()+" "+getroom_type()+ " " +getcheck_in() +" " +getcheck_out()+ " " + getdays()+ " "+ getcost()).toLowerCase();
        return all.contains(q.toLowerCase());
    }

    
}
