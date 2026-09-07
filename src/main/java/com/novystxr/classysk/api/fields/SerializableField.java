package com.novystxr.classysk.api.fields;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import com.novystxr.classysk.api.Modifier;
import com.novystxr.classysk.api.fields.SkriptField.FieldSignature;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.converter.Converters;

import java.io.StreamCorruptedException;

public class SerializableField implements YggdrasilExtendedSerializable {

    public Object[] value;
    public boolean isPlural;

    public SerializableField(Object[] value, boolean isPlural) {
        this.value = value;
        this.isPlural = isPlural;
    }

    public SerializableField() {}

    @Override
    public Fields serialize() {
        Fields fields = new Fields();

        fields.putObject("value", value);
        fields.putPrimitive("isPlural", isPlural);

        return fields;
    }

    @Override
    public void deserialize(@NotNull Fields fields) throws StreamCorruptedException {
        value = fields.getObject("value", Object[].class);
        isPlural = fields.getPrimitive("isPlural", boolean.class);
    }

    public boolean canBeSaved() {
        Object[] newValue = new Object[value.length];
        for (int i = 0; i < value.length; i++) {
            if (value[i] == null) {
                continue;
            }
            ClassInfo<?> classInfo = Classes.getSuperClassInfo(value[i].getClass());
            Class<?> serializeAs = classInfo.getSerializeAs();
            if (serializeAs != null) {
                classInfo = Classes.getExactClassInfo(serializeAs);
                if (classInfo == null) return false;
                newValue[i] = Converters.convert(value[i], serializeAs);
            } else {
                newValue[i] = value[i];
            }
            if (classInfo.getSerializer() == null) {
                return false;
            }
        }
        value = newValue;
        return true;
    }

    public FieldSignature mergeSignature(FieldSignature signature) {
        Modifier[] modifiers = Modifier.without(signature.modifiers(), Modifier.STATIC);
        return new FieldSignature(signature.name(), Object.class, null, modifiers, isPlural);
    }
}
