package com.ericlam.mc.placeholder.addon;

import com.dragonite.mc.dnmc.core.config.yaml.Configuration;
import com.dragonite.mc.dnmc.core.config.yaml.Resource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Resource(locate = "placeholders.yml")
public class PapiAddonConfig extends Configuration {


    public Map<String, Placeholder> placeholders;

    public static class Placeholder {
        public String table;
        public String uuid;
        private List<String> columns;

        public List<Stats> getColumns() {
            return columns.stream().filter(str -> str.split(":").length == 3).map(str -> {
                String[] stats = str.split(":");
                return new Stats(stats[0], stats[1], stats[2]);
            }).collect(Collectors.toList());
        }

    }

    public static class Stats {
        public String placeholder;
        public String column;
        public String def;

        private Stats(String placeholder, String column, String def) {
            this.placeholder = placeholder;
            this.column = column;
            this.def = def;
        }
    }
}
