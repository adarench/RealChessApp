package passoff.server;

import com.google.gson.GsonBuilder;

public class TestFactory {
    // This test factory will be implemented in future phases
    
    public static Long getMessageTime() {
        return 3000L;
    }

    public static GsonBuilder getGsonBuilder() {
        return new GsonBuilder();
    }
}