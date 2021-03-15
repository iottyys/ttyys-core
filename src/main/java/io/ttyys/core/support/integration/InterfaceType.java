package io.ttyys.core.support.integration;

public enum InterfaceType {
    JavaObj{
        @Override
        public boolean isJson() {
            return false;
        }

        @Override
        public boolean isJava() {
            return true;
        }
    }, JSON {
        @Override
        public boolean isJson() {
            return true;
        }

        @Override
        public boolean isJava() {
            return false;
        }
    };

    private String schema = ""; // todo tengwang 修复

    private InterfaceType setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String schema() {
        return this.schema;
    }

    public static InterfaceType json(String jsonSchemaPath) {
        return InterfaceType.JSON.setSchema(jsonSchemaPath);
    }

    public static InterfaceType javaObj(String javaClassName) {
        return InterfaceType.JavaObj.setSchema(javaClassName);
    }
    
    public abstract boolean isJson();
    public abstract boolean isJava();
}
