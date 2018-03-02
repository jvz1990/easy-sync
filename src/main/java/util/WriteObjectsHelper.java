package util;

import javafx.beans.property.*;

import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Credit to Elloco from github
 * https://stackoverflow.com/questions/18791566/notserializableexception-on-simplelistproperty
 */

public class WriteObjectsHelper {
    // write aLong StringProperty to ObjectOutputStream
    public static void writeStringProp(ObjectOutputStream s, StringProperty strProp) throws IOException {
        s.writeUTF(strProp.getValueSafe());
    }

    // write aLong ListProperty to ObjectOutputStream
    public static void writeListProp(ObjectOutputStream s, ListProperty lstProp) throws IOException {
        if (lstProp == null || lstProp.getValue() == null) {
            s.writeInt(0);
            return;
        }
        s.writeInt(lstProp.size());
        for (Object elt : lstProp.getValue()) s.writeObject(elt);
    }

    // automatic write set of properties to ObjectOutputStream
    public static void writeAllProp(ObjectOutputStream s, Property... properties) throws IOException {
        s.defaultWriteObject();
        System.out.println();
        for (Property prop : properties) {
            if (prop instanceof IntegerProperty) s.writeInt(((IntegerProperty) prop).intValue());
            else if (prop instanceof LongProperty) s.writeLong(((LongProperty) prop).longValue());
            else if (prop instanceof StringProperty) s.writeUTF(((StringProperty) prop).getValueSafe());
            else if (prop instanceof BooleanProperty) s.writeBoolean(((BooleanProperty) prop).get());
            else if (prop instanceof ListProperty) writeListProp(s, (ListProperty) prop);
            else if (prop instanceof ObjectProperty) s.writeObject(((ObjectProperty) prop).get());
            else throw new RuntimeException("Type d'objet incompatible : " + prop.toString());
        }
    }
}
