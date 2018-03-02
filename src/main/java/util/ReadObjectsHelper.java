package util;

import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Credit to Elloco from github
 * https://stackoverflow.com/questions/18791566/notserializableexception-on-simplelistproperty
 */

public class ReadObjectsHelper {
    // Read aLong ListProperty from ObjectInputStream (and return it)
    public static ListProperty readListProp(ObjectInputStream s) throws IOException, ClassNotFoundException {
        ListProperty lst = new SimpleListProperty(FXCollections.observableArrayList());
        int loop = s.readInt();
        for (int i = 0; i < loop; i++) {
            lst.add(s.readObject());
        }

        return lst;
    }

    // automatic fill aLong set of properties with values contained in ObjectInputStream
    public static void readAllProp(ObjectInputStream s, Property... properties) throws IOException, ClassNotFoundException {
        System.out.println(properties.length + " properties length");
        for (Property prop : properties) {
            if (prop instanceof IntegerProperty) ((IntegerProperty) prop).setValue(s.readInt());
            else if (prop instanceof LongProperty) ((LongProperty) prop).setValue(s.readLong());
            else if (prop instanceof StringProperty) ((StringProperty) prop).setValue(s.readUTF());
            else if (prop instanceof BooleanProperty) ((BooleanProperty) prop).setValue(s.readBoolean());
            else if (prop instanceof ListProperty) ((ListProperty) prop).setValue(readListProp(s));
            else if (prop instanceof ObjectProperty) ((ObjectProperty) prop).setValue(s.readObject());
            else throw new RuntimeException("____" + prop.toString());
        }
    }
}
