package io.github.lizhangqu.plugin

class SampleConfig {
    final String name
    String stringConfig
    boolean booleanConfig
    File fileConfig

    SampleConfig(String name) {
        this.name = name
    }

    void stringConfig(String stringConfig) {
        this.stringConfig = stringConfig
    }

    void booleanConfig(boolean booleanConfig) {
        this.booleanConfig = booleanConfig
    }

    void fileConfig(File fileConfig) {
        this.fileConfig = fileConfig
    }

    @Override
    String toString() {
        return "SampleConfig{" +
                "name='" + name + '\'' +
                ", stringConfig='" + stringConfig + '\'' +
                ", booleanConfig=" + booleanConfig +
                ", fileConfig=" + fileConfig +
                '}';
    }
}
