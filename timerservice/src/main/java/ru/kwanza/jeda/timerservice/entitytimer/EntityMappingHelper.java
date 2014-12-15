package ru.kwanza.jeda.timerservice.entitytimer;

import java.lang.reflect.AnnotatedElement;

/**
 * @author Michael Yeskov
 */
public class EntityMappingHelper {

    public static String getPropertyName(AnnotatedElement annotatedElement) {
        if (annotatedElement instanceof java.lang.reflect.Field) {
            return getFieldPropertyName((java.lang.reflect.Field) annotatedElement);
        } else if (annotatedElement instanceof java.lang.reflect.Method) {
            return getMethodPropertyName((java.lang.reflect.Method) annotatedElement);
        } else {
            throw new RuntimeException("Unknown AnnotatedElement: " + annotatedElement);
        }
    }

    private static String getFieldPropertyName(java.lang.reflect.Field field) {
        return field.getName();
    }

    private static String getMethodPropertyName(java.lang.reflect.Method method) {
        final String methodName = method.getName();
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return uncapitalize(methodName.substring(3, methodName.length()));
        } else {
            throw new RuntimeException("Incorrect getter name: " + methodName);
        }
    }

    private static String uncapitalize(String str) {
        return changeFirstCharacterCase(str, false);
    }

    private static String changeFirstCharacterCase(String stringValue, boolean capitalize) {
        final StringBuilder stringBuilder = new StringBuilder(stringValue.length());
        if (capitalize) {
            stringBuilder.append(Character.toUpperCase(stringValue.charAt(0)));
        } else {
            stringBuilder.append(Character.toLowerCase(stringValue.charAt(0)));
        }
        stringBuilder.append(stringValue.substring(1));
        return stringBuilder.toString();
    }
}
