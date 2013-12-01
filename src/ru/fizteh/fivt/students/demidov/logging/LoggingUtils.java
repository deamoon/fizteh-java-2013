package ru.fizteh.fivt.students.demidov.logging;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class LoggingUtils {
    static final Set<Object> addedArguments = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
    
    static public void writeLog(JSONObject jsonObject, Object object, Method method, Object[] arguments, Writer writer) {
        JSONArray jsonArray = new JSONArray();        
        jsonObject.put("timestamp", System.currentTimeMillis());
        jsonObject.put("class", object.getClass().getName());
        
        jsonObject.put("method", method.getName());
        if (arguments != null) {
            logIterable(jsonArray, Arrays.asList(arguments));
        }
        
        jsonObject.put("arguments", jsonArray);
    }

    static public void logReturnValue(JSONObject jsonObject, Object value) {
        JSONArray jsonArray = new JSONArray();
        
        if (value == null) {
            jsonObject.put("returnValue", JSONObject.NULL);
            return;
        }
            
        if (value.getClass().isArray()) {
            logIterable(jsonArray, Arrays.asList((Object[]) value));
        } else if (value instanceof Iterable) {
            logIterable(jsonArray, (Iterable)value);
        } else {
            jsonObject.put("returnValue", value);
            return;
        }
        
        jsonObject.put("returnValue", jsonArray);
    }

    static public void logIterable(JSONArray jsonArray, Iterable arguments) {
        addedArguments.add(arguments);
        
        for (Object currentArgument: arguments) {
            if (currentArgument != null) {
                if (!(addedArguments.contains(currentArgument))) {
                    JSONArray currentArray = new JSONArray();
                    logIterable(currentArray, (Iterable)currentArgument);
                    jsonArray.put(currentArray);
                } else {
                    jsonArray.put("cyclic");
                }
            } else if (currentArgument.getClass().isArray()) {
                jsonArray.put(currentArgument.toString());
            } else if (currentArgument instanceof Iterable) {
                jsonArray.put(currentArgument);
            } else {
                jsonArray.put(currentArgument);
            }
        }
    }
}
