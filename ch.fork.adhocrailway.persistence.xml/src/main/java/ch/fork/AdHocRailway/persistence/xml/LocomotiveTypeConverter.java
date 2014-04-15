package ch.fork.AdHocRailway.persistence.xml;

import ch.fork.AdHocRailway.model.locomotives.LocomotiveType;
import com.thoughtworks.xstream.converters.SingleValueConverter;

public class LocomotiveTypeConverter implements SingleValueConverter {

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") final Class type) {
        return type.equals(LocomotiveType.class);
    }

    @Override
    public Object fromString(final String type) {
        return LocomotiveType.fromString(type.toLowerCase());
    }

    @Override
    public String toString(final Object arg0) {
        return ((LocomotiveType) arg0).getId();
    }

}
